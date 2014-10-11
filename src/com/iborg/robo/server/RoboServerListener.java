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

/*
 * RoboServerListener.java
 *
 * Created on November 11, 2002, 12:46 PM
 */

package com.iborg.robo.server;

import java.awt.Robot;

import com.iborg.hsocket.IServerSocket;
import com.iborg.hsocket.ISocket;

/**
 * Waits for new client connections.
 * When new client connects, the previously connected client is killed
 * so that only one client can be connected at a time
 */
public class RoboServerListener
{
    Robot robot;
    IServerSocket serverSocket;
    RoboServerProcessor client = null;
	
	public RoboServerListener(Robot robot, IServerSocket serverSocket)
	{
		this.robot = robot;
		this.serverSocket = serverSocket;
	}


	public void run()
	{
		try
		{
			while(true)
			{
				ISocket s = serverSocket.accept();
				RoboServer.log("Got connection. Processing");
				
				if(client != null)
				{
					client.kill();
					RoboServer.log("Killed old connection.");
				}
				
				client = new RoboServerProcessor(s, robot);
				client.start();
				RoboServer.log("Started new client");
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}
}

