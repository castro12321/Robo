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

package com.iborg.robo.client;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;

import com.iborg.hsocket.ISocket;
import com.iborg.robo.RoboProtocol;
/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */

public class RoboClientProcessor extends Thread {
    InputStream is;
    OutputStream os;
    RoboClient roboClient;
    int width, height;
    
    int pixelSize = 32;
    int redMask = 0xFF0000;
    int greenMask = 0x00FF00;
    int blueMask = 0x0000FF;
    int alphaMask = 0xFF000000;
    
    
    RoboClientProcessor(ISocket s, RoboClient roboClient) {
        try {
            os= s.getOutputStream();
            is = s.getInputStream();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        this.roboClient = roboClient;
    }
    
    public void run() {
        
        requestScreenParam();
        
        while(true) {
            try {
                int command = is.read();
                switch(command) {
                    case RoboProtocol.SCREEN_ADJUSTMENT_END:
                        screenAdjustmentEnd(true);
                        break;
                    case RoboProtocol.SCREEN_RESPONSE_PART:
                        screenAdjustmentEnd(false);
                        break;
                    case RoboProtocol.SCREEN_NOP:
                        screenNop();
                        break;
                    case RoboProtocol.SCREEN_PARAM_RESPONSE:
                        screenParam();
                        break;
                    case RoboProtocol.SCREEN_COLOR_MODEL:
                        screenColorModel();
                        break;
                    default:
                        //System.out.println("unknown command " + command);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
                break;
            }
        }
    }
    
    public synchronized void requestScreenParam() {
        
        try {
            os.write(RoboProtocol.SCREEN_PARAM_REQUEST );
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    public synchronized void requestScreen() {
        
        try {
            os.write(RoboProtocol.SCREEN_REQUEST);
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    public synchronized void mouseMoved(int x, int y) throws Exception {
        os.write(RoboProtocol.MOUSE_MOVED);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(x);
        dos.writeInt(y);
        
        dos.flush();
    }
    
    public synchronized void mouseDragged(int x, int y) throws Exception {
        os.write(RoboProtocol.MOUSE_MOVED);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(x);
        dos.writeInt(y);
        dos.flush();
    }
    
    public synchronized void mousePressed(int modifiers) throws Exception {
        os.write(RoboProtocol.MOUSE_PRESSED);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(modifiers);
        dos.flush();
    }
    
    public synchronized void mouseReleased(int modifiers) throws Exception {
        os.write(RoboProtocol.MOUSE_RELEASED);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(modifiers);
        dos.flush();
    }
    
    public synchronized void keyPressed(int key) throws Exception {
        os.write(RoboProtocol.KEY_PRESSED);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(key);
        dos.flush();
    }
    
    public synchronized void keyReleased(int key) throws Exception {
        os.write(RoboProtocol.KEY_RELEASED);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(key);
        dos.flush();
    }
    
    private synchronized void screenParam() {
        try {
            DataInputStream dis = new DataInputStream(is);
            width = dis.readInt();
            height = dis.readInt();
            roboClient.screenCanvas.createScreenImage(width, height);
            adjustScale();
            requestScreen();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    private synchronized void screenColorModel() {
        try {
            DataInputStream dis = new DataInputStream(is);
            redMask = dis.readInt();
            greenMask = dis.readInt();
            blueMask = dis.readInt();
            alphaMask = dis.readInt();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    private void drawScreen() {
        roboClient.screenCanvas.repaint();
    }
    
    private void screenNop() {
        requestScreen();
    }
    
    private void screenAdjustmentEnd(boolean draw) {
        try {
            DataInputStream dis = new DataInputStream(is);
            int stripsRecieved = dis.readInt();
            int length = dis.readInt();
            byte [] buffer = new byte[length];
            dis.readFully(buffer);
            
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
            InflaterInputStream zis = new InflaterInputStream(bis);
            DataInputStream ois = new DataInputStream(zis);
            
            Toolkit tk = Toolkit.getDefaultToolkit();
            ColorModel cm = new DirectColorModel(pixelSize, redMask, greenMask, blueMask, alphaMask);
            for(int r=0; r < stripsRecieved; r++) {
                
                int x = ois.readInt();
                int y = ois.readInt();
                int width = ois.readInt();
                int height = ois.readInt();
                int newPixels [] = readInts(ois, width * height);
                
                Image image = tk.createImage(new MemoryImageSource(width, height, cm, newPixels, 0, width));
                roboClient.screenCanvas.imageGraphics.drawImage(image, x, y, null);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        if(draw)
            drawScreen();
    }
    
    private int [] readInts(DataInputStream dis, int length) throws IOException {
        
        int [] ints = new int[length];
        byte [] b = new byte[length * 4];
        
        dis.readFully(b);
        int off = 0;
        for(int i=0; i< length; i++) {
            ints[i] = ((b[off + 3] & 0xFF) << 0) +
            ((b[off + 2] & 0xFF) << 8) +
            ((b[off + 1] & 0xFF) << 16) +
            ((b[off + 0] & 0xFF) << 24);
            
            off += 4;
        }
        return ints;
        
    }
    
    public void adjustScale() {
    	// TODO: adjust scale
    }
}


