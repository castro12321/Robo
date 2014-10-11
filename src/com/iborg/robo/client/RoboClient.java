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

import java.applet.Applet;

import com.iborg.hsocket.ISocket;
import com.iborg.hsocket.TcpSocket;
import com.iborg.robo.RoboProtocol;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */
public class RoboClient extends Applet
{
	private static final long serialVersionUID = 1L;
    
	RoboClientScreenCanvas screenCanvas;
    RoboClientProcessor processor;
    RoboClientWindow window;
    
    private ISocket socket;
    
    public RoboClient()
    {
    }
    
    public void startCapture() {
        processor = new RoboClientProcessor(socket, this);
        screenCanvas = new RoboClientScreenCanvas(this);
        window = new RoboClientWindowScalable(this, socket);
        processor.run();
    }
    
	
	@Override
	public void start()
	{
		log("Starting client...");
    	String host = getParameter(RoboProtocol.paramHost);
    	String port = getParameter(RoboProtocol.paramPort);
    	String pass = getParameter(RoboProtocol.paramPassword);
    	log("host=" + host);
    	log("port=" + port);
    	log("pass=" + pass);
		
		try
		{
			socket = new TcpSocket(host, Integer.parseInt(port));
			new RoboClientLoginProcessor(socket, this, pass).run();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
    @Override
    public void init()
    {
    }
    
    @Override
    public void stop()
    {
    }
    
    public static void log(String msg)
    {
    	System.out.println("[C] " + msg);
    }
    
}

