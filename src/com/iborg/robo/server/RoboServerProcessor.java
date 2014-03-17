/*
 *  Copyright (C) 2002 iBorg Corporation. All Rights Reserved.
 *  Copyright (C) 2002 Boris Galinsky. All Rights Reserved.
 *
 *  This file is part of the share system.
 *
 *  The share system is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation.
 *
 *  See terms of license at gnu.org.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.iborg.robo.server;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.iborg.hsocket.ISocket;
import com.iborg.robo.RoboProtocol;
/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */

public class RoboServerProcessor extends Thread {
	
    InputStream is;
    OutputStream os;
    Robot robot;
    Rectangle screenRect;
    int newScreen = -1;
    int oldScreen = -1;
    int pixels[][];
    int width;
    int height;
    int stripsSent;
    BufferedImage image;
    boolean loggedIn = false;
    int maxSend = 100000;
    int maxScreenUpdateChunk = 500000;
    
    RoboServerProcessor(ISocket s, Robot robot) {
        this.robot = robot;
        try {
            os = s.getOutputStream();
            is = s.getInputStream();
        } catch (Exception e) {
            System.err.println(e);
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenDimension = toolkit.getScreenSize();
        screenRect = new Rectangle(0, 0, screenDimension.width, screenDimension.height);
    }
    
    public void kill()
    {
    	interrupt();
    }
    
    
    // Returns whether handled interrupted() or not
    public synchronized boolean handleInterrupted()
    {
    	if(interrupted())
    	{
    		try
        	{
        		os.write(RoboProtocol.CONNECTION_CLOSED);
        		os.flush();
        	}
        	catch(IOException e) {}
    		return true;
    	}
    	return false;
    }
    
    
    // main loop
    public void run() {
        
        int loginCounter = 3;
        try {
            while(!loggedIn) {
                sendLoginRequest();
                int command = is.read();
                
                if(handleInterrupted())
                	return;
                
                if(command == RoboProtocol.LOGIN || command == RoboProtocol.LOGIN_MESSAGE_DIGEST) {
                    loggedIn = processLogin(command);
                } else {
                    break;
                }
                loginCounter--;
                if(loginCounter <= 0) {
                    sendLoginFailed();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        
        
        if(loggedIn) {
            while(true) {
                try {
                    int command = is.read();
                    
                    if(handleInterrupted())
                    	return;
                    
                    switch(command) {
                        case RoboProtocol.SCREEN_REQUEST:
                            screen();
                            break;
                        case RoboProtocol.MOUSE_MOVED:
                            mouseMoved();
                            break;
                        case RoboProtocol.MOUSE_PRESSED:
                            mousePressed();
                            break;
                        case RoboProtocol.MOUSE_RELEASED:
                            mouseReleased();
                            break;
                        case RoboProtocol.KEY_PRESSED:
                            keyPressed();
                            break;
                        case RoboProtocol.KEY_RELEASED:
                            keyReleased();
                            break;
                        case RoboProtocol.SCREEN_PARAM_REQUEST:
                            sendScreenParam();
                            break;
                        case RoboProtocol.SCREEN_SET_COMMUNICATION_PARAMETERS:
                            setComParams();
                            break;
                        case -1:
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                            }
                            break;
                        default:
                            System.err.println("unknown command " + command);
                            break;
                    }
                } catch (Exception e) {
                    System.err.println(e);
                    break;
                }
            }
        }
    }
    
    
    
    // send functions
    long loginMask;             // challenge for messageDigest login
    
    private synchronized void sendLoginRequest() {
        try {
            os.write(RoboProtocol.REQUEST_LOGIN);
            loginMask = (new Date()).getTime();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeLong(loginMask);
            dos.flush();
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private synchronized void sendLoginFailed() {
        try {
            os.write(RoboProtocol.LOGIN_FAILED);
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private synchronized void sendLoginSuccessful() {
        try {
            os.write(RoboProtocol.LOGIN_SUCCESSFUL);
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private boolean verifyPassword(int command, byte [] msg) {
        String password = System.getProperty(RoboProtocol.paramPassword);
        if(command == RoboProtocol.LOGIN_MESSAGE_DIGEST) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
                
                String secret = password + loginMask;
                
                byte [] buffer = secret.getBytes();
                
                messageDigest.update(buffer);
                
                byte[] digest = messageDigest.digest();
                
                if(digest.length == msg.length) {
                    for(int i=0; i<digest.length; i++) {
                        if(digest[i] != msg[i])
                            return false;
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        } else {
            return((new String(msg)).equals(password));
        }
        return false;
        
    }
    
    private boolean processLogin(int command) {
        try {
            DataInputStream dis = new DataInputStream(is);
            int length = dis.readInt();
            byte [] buffer = new byte[length];
            int counter = 0;
            while(counter < length) {
                int r = is.read(buffer, counter, length - counter);
                if(r == -1) {
                    break;
                }
                counter += r;
            }
            
            if(verifyPassword(command, buffer)) {
                
                sendLoginSuccessful();
                
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    private void sendColorModel(DirectColorModel directColorModel) {
        int redMask = directColorModel.getRedMask();
        int greenMask = directColorModel.getGreenMask();
        int blueMask = directColorModel.getBlueMask();
        int alphaMask = directColorModel.getAlphaMask();
        
        try {
            os.write(RoboProtocol.SCREEN_COLOR_MODEL);
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeInt(redMask);
            dos.writeInt(greenMask);
            dos.writeInt(blueMask);
            dos.writeInt(alphaMask);
            
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private void getPixels() throws IOException {
        try {
            image = robot.createScreenCapture(screenRect);
        } catch (OutOfMemoryError oome) {
            System.err.println("Out of memory while capturing screen");
            Runtime runtime = Runtime.getRuntime();
            System.err.println("Total memory = " + runtime.totalMemory() + " free memory = " + runtime.freeMemory());
        }
        if(image != null) {
            if(newScreen == -1) {
                width = image.getWidth();
                height = image.getHeight();
                pixels = new  int[2][];
                pixels[0] = new int[width * height];
                pixels[1] = new int[width * height];
                
                ColorModel colorModel = image.getColorModel();
                if(colorModel instanceof DirectColorModel) {
                    sendColorModel((DirectColorModel) colorModel);
                }
            }
            
            if(oldScreen == -1) {
                if(newScreen == -1) {
                    newScreen = 0;
                } else {
                    newScreen = 1;
                    oldScreen = 0;
                }
            } else {
                if(newScreen == 0) {
                    newScreen = 1;
                    oldScreen = 0;
                } else {
                    newScreen = 0;
                    oldScreen = 1;
                }
            }
            try {
                PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels[newScreen], 0, width);
                pg.grabPixels();
                if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                    throw new IOException("failed to load image contents");
                }
            }
            catch (InterruptedException e) {
                throw new IOException("image load interrupted");
            }
        }
    }
    
    private synchronized void screen() {
        try {
            getPixels();
            analalyzeSend();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println(e);
        }
    }
    
    ByteArrayOutputStream adjustmentByteArrayOutputStream;
    DeflaterOutputStream adjustmentDeflater;
    DataOutputStream adjustmentArrayOutputStream;
    
    private void resetSendParams() {
        stripsSent = 0;
        adjustmentByteArrayOutputStream = new ByteArrayOutputStream();
        adjustmentDeflater = new DeflaterOutputStream(adjustmentByteArrayOutputStream, new Deflater(Deflater.BEST_COMPRESSION));
        try {
            adjustmentArrayOutputStream = new DataOutputStream(adjustmentDeflater);
        } catch (Exception e) {
        }
    }
    
    private synchronized void endSend(int type) {
        if(stripsSent > 0) {
            try {
                os.write(type);
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeInt(stripsSent);
                
                adjustmentArrayOutputStream.flush();
                
                adjustmentDeflater.finish();
                
                adjustmentByteArrayOutputStream.flush();
                
                byte [] buffer = adjustmentByteArrayOutputStream.toByteArray();
                dos.writeInt(buffer.length);
                os.write(buffer);
                
                os.flush();
                
                //sendCompare();
                
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
    
    private synchronized void sendScreenSegment(int x, int y, int w, int h) {
        
        if(maxSend != -1) {
            if(w * h > maxSend) {
                int totalSent = 0;
                while(totalSent < h) {
                    int h1 = maxSend / w;
                    if(h1 == 0)
                        h1 = 1;
                    if(h1 > h - totalSent)
                        h1 = h - totalSent;
                    sendScreenSegment(x, y + totalSent, w, h1);
                    totalSent += h1;
                }
                return;
            }
        }
        
        stripsSent++;
        
        try {
            adjustmentArrayOutputStream.writeInt(x);
            adjustmentArrayOutputStream.writeInt(y);
            adjustmentArrayOutputStream.writeInt(w);
            adjustmentArrayOutputStream.writeInt(h);
            writeInts(pixels[newScreen], width, height, x, y, w, h);
            adjustmentArrayOutputStream.flush();
            
            
            if(maxScreenUpdateChunk != -1 && adjustmentArrayOutputStream.size() > maxScreenUpdateChunk) {
                endSend(RoboProtocol.SCREEN_RESPONSE_PART);
                resetSendParams();
            }
            
        } catch (Exception e) {
        }
    }
    
    private void writeInts(int [] ints, int width, int height, int x, int y, int w, int h) throws IOException {
        byte [] b = new byte[w * h * 4];
        int off = 0;
        for(int l = 0; l < h; l ++) {
            for(int k = 0; k < w; k ++ ) {
                int val = ints[(y + l) * width + x + k];
                b[off + 3] = (byte) (val >>> 0);
                b[off + 2] = (byte) (val >>> 8);
                b[off + 1] = (byte) (val >>> 16);
                b[off + 0] = (byte) (val >>> 24);
                off += 4;
            }
        }
        adjustmentArrayOutputStream.write(b);
        
    }
    
    private synchronized void sendNop() {
        try {
            os.write(RoboProtocol.SCREEN_NOP);
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private synchronized void sendScreenParam() {
        try {
            os.write(RoboProtocol.SCREEN_PARAM_RESPONSE);
            DataOutputStream dos = new DataOutputStream(os);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenDimension = toolkit.getScreenSize();
            dos.writeInt(screenDimension.width);
            dos.writeInt(screenDimension.height);
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private java.util.List<Rectangle> segments = new ArrayList<Rectangle>();
    
    private void registerScreenSegment(int x, int y, int w, int h) {
        segments.add(new Rectangle(x, y, w, h));
        //sendScreenSegment(x, y, w, h);
    }
    
    private void resetSegments() {
        segments.clear();
    }
    
    private void sendSegments() {
        
        Rectangle rectangle = null;
        while(segments.size() > 0) {
            if(rectangle == null) {
                rectangle = (Rectangle)segments.get(0);
                segments.remove(0);
            }
            Rectangle net = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height + 1);
            boolean added = false;
            int i;
            for(i = 0; i < segments.size(); i++) {
                if(net.intersects((Rectangle)segments.get(i))) {
                    rectangle.add((Rectangle)segments.get(i));
                    segments.remove(i);
                    added = true;
                    break;
                }
            }
            if(!added) {
                sendScreenSegment(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                rectangle = null;
            }
        }
        if(rectangle != null) {
            sendScreenSegment(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
    }
    
    private synchronized void analalyzeSend() {
        resetSendParams();
        if(oldScreen != -1 && newScreen != -1) {
            
            resetSegments();
            for( int y = 0; y < height; y++) {
                int start = -1;
                int end = -1;
                for( int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if(pixels[newScreen][index] != pixels[oldScreen][index]) {
                        if(start == -1) {
                            start = x;
                        } else {
                            end = x;
                        }
                    } else {
                        if(start != -1) {
                            if(end == -1) {
                                end = start;
                            }
                            if(x - end > 50) {
                                registerScreenSegment(start, y, end - start + 1, 1);
                                start = -1;
                                end = -1;
                            }
                        }
                    }
                }
                if(start != -1) {
                    if(end == -1) {
                        end = start;
                    }
                    registerScreenSegment(start, y, end - start + 1, 1);
                }
            }
            sendSegments();
            endSend(RoboProtocol.SCREEN_ADJUSTMENT_END);
            
        } else {
            if(!(oldScreen == -1 && newScreen == -1)) {
                sendScreenSegment(0, 0, width, height);
                endSend(RoboProtocol.SCREEN_ADJUSTMENT_END);
            }
        }
        sendNop();
    }
    
    // mouse control
    private void mouseMoved() throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int x = dis.readInt();
        int y = dis.readInt();
        robot.mouseMove(x, y);
    }
    
    private void mousePressed() throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int button = dis.readInt();
        robot.mousePress(button);
    }
    
    private void mouseReleased() throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int button = dis.readInt();
        robot.mouseRelease(button);
    }
    
    // keyboard control
    private void keyPressed() throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int key = dis.readInt();
        try {
            robot.keyPress(key);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private void keyReleased() throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int key = dis.readInt();
        try {
            robot.keyRelease(key);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private void setComParams() throws IOException {
        DataInputStream dis = new DataInputStream(is);
        maxSend = dis.readInt();
        maxScreenUpdateChunk = dis.readInt();
    }
    
    
}

