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
	static
	{
		try
		{
			logger = new PrintWriter(new BufferedWriter(new FileWriter("RoboServerLog", true)));
		}
		catch(IOException e)
		{
			log("[S] ERROR: Cannot open/create RoboServerLog file");
		}
	}
	
	public static void log(Exception e)
	{
		e.printStackTrace();
		if(logger != null)
		{
			logger.println("[S] ERROR EXCEPTION:");
			e.printStackTrace(logger);
		}
	}
	
	public static void log(String msg)
	{
		System.out.println("[S] " + msg);
		if(logger != null)
		{
    		logger.println("[S] " + msg);
		    logger.flush();
		}
	}
	
	
	public static void main(String args_unused[]) throws AWTException
	{
		log("Starting Robo server v" + RoboProtocol.VERSION);
		log("TrueColor quality: " + RoboServerProcessor.trueColorQuality);
		try
		{
			log("Loading config 'Robo.cfg'");
			ConfigFile.process("Robo.cfg");
		}
		catch(IOException ioe)
		{
			log(ioe);
		}
		
		// create a robot to feed in GUI events
		Frame frame = new Frame("RoboServer"); // this seems to be neccessary for java.awt.Robot
		frame.setVisible(true);
		frame.dispose();
		Robot robot = new Robot();
		
		// create server socket
		log("Preparing TCP server socket");
		IServerSocket serverSocket = TcpServerSocketFactory.createServerSocket();
		
		if(serverSocket != null)
		{
			log("Server socket ready. Starting listener");
			new RoboServerListener(robot, serverSocket).run();
		}
		else
			log("ERROR: Cannot create serverSocket!");
	}
}
