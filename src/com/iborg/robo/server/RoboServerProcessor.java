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
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.iborg.hsocket.ISocket;
import com.iborg.robo.RoboProtocol;

public class RoboServerProcessor extends Thread
{
	private InputStream is;
	private OutputStream os;
	private final Robot robot;
	private final RoboServerGraphicsProcessor graphicsProcessor;
    
	RoboServerProcessor(ISocket s, Robot robot)
	{
		this.robot = robot;
		try
		{
			os = s.getOutputStream();
			is = s.getInputStream();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
		
		graphicsProcessor = new RoboServerGraphicsProcessor(os, robot);
    }
    
    public void kill()
    {
    	interrupt();
    }
    
    public synchronized void cleanup()
    {
    	RoboServer.log("Thread: Socket cleanup.");
		try
    	{
    		os.write(RoboProtocol.CONNECTION_CLOSED);
    		os.flush();
    		is.close();
    		os.close();
    	}
    	catch(IOException e) {}
    }
    
    // main loop
	public void run()
	{
        RoboServerLoginProcessor loginProcessor = new RoboServerLoginProcessor(is, os, this);
        boolean loggedIn = loginProcessor.run();
       
        RoboServer.log("Did client login? " + loggedIn);
        if(loggedIn)
        	while(handleNextCommand())
        		;
        cleanup();
    }
    
    private boolean handleNextCommand()
    {
		try
		{
			int command = is.read();
			
			if(interrupted())
				return false;
			
			switch(command)
			{
                case RoboProtocol.SCREEN_REQUEST:        screenRequest();   break;
                case RoboProtocol.MOUSE_MOVED:           mouseMoved();      break;
                case RoboProtocol.MOUSE_PRESSED:         mousePressed();    break;
                case RoboProtocol.MOUSE_RELEASED:        mouseReleased();   break;
                case RoboProtocol.KEY_PRESSED:           keyPressed();      break;
                case RoboProtocol.KEY_RELEASED:          keyReleased();     break;
                case RoboProtocol.SCREEN_PARAM_REQUEST:  sendScreenParam(); break;
                case RoboProtocol.SCREEN_SET_COM_PARAMS: setComParams();    break;
                case -1:
                	RoboServer.log("Got -1 message.");
                    try { Thread.sleep(1000); } catch (Exception e) {}
                    break;
                default:
                    System.err.println("unknown command " + command);
                    return true;
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
			return false;
		}
		
		return true;
    }
    
    private synchronized void screenRequest()
    {
    	graphicsProcessor.screen();
    }
    
    private synchronized void sendScreenParam() {
        try {
            os.write(RoboProtocol.SCREEN_PARAM_RESPONSE);
            DataOutputStream dos = new DataOutputStream(os);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenDimension = toolkit.getScreenSize();
            // TODO: (Connection speed optimizations) if lowered, will the outbound transfer lower too?
            dos.writeInt(screenDimension.width);
            dos.writeInt(screenDimension.height);
            os.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
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
        graphicsProcessor.maxSend = dis.readInt();
        graphicsProcessor.maxScreenUpdateChunk = dis.readInt();
        RoboServer.log("Setting comm params: " + graphicsProcessor.maxSend + "; " + graphicsProcessor.maxScreenUpdateChunk);
    }
}