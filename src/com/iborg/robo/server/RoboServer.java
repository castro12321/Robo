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

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Robot;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.iborg.hsocket.IServerSocket;
import com.iborg.robo.RoboProtocol;
import com.iborg.robo.TcpServerSocketFactory;
import com.iborg.util.ConfigFile;

/**
 * 
 * @author <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */
public class RoboServer
{
	
	private static PrintWriter logger = null;
	
	public static void log(String msg)
	{
		System.out.println("[S] " + msg);
		try
		{
    		if(logger == null)
    			logger = new PrintWriter(new BufferedWriter(new FileWriter("RoboServerLog", true)));
    		if(logger != null)
    		{
        		logger.println("[S] " + msg);
    		    logger.close();
    		}
		} catch (IOException e) {
			System.out.println("[S] ERROR: Cannot write to the log file");
		}
	}
	
	
	public static void main(String args[]) throws AWTException
	{
		log("Starting Robo server");
		try
		{
			log("Loading config 'Robo.cfg'");
			ConfigFile.process("Robo.cfg");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		// create server socket
		IServerSocket serverSocket = null;
		String connectionType = System.getProperty(RoboProtocol.paramConnectionType);
		log("Preparing server socket with connectionType=" + connectionType);
		if("tcp".equalsIgnoreCase(connectionType))
			serverSocket = TcpServerSocketFactory.createServerSocket();
		else
			System.err.println("Unknown connection type: " + connectionType);
		
		if(serverSocket != null)
		{
			log("Server socket ready. Starting listener");
			// create a robot to feed in GUI events
			Frame frame = new Frame("RoboServer"); // this seems to be neccessary for java.awt.Robot
			frame.setVisible(true);
			frame.dispose();
			Robot robot = new Robot();
			// listen for connections
			new RoboServerListener(robot, serverSocket).run();
		}
		else
			log("ERROR: Cannot create serverSocket!");
	}
}
