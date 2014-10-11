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
public class RoboClient extends Applet {
	private static final long serialVersionUID = 1L;
    
    RoboClientScreenCanvas screenCanvas;
    RoboClientProcessor processor;
    RoboClientWindow window;
    
    // Connection
    String pass;
    private ISocket socket;
    
    public RoboClient()
    {
    }
    
    public void createUserInterface() {
    	screenCanvas = new RoboClientScreenCanvas(this);
        window = new RoboClientWindowScalable(this, socket);
    }
    
    public void startCapture() {
        processor = new RoboClientProcessor(socket, this);
        createUserInterface();
        (processor).start();
    }
    
    public void startLogin() {
        (new RoboClientLoginProcessor(socket, this)).run();
    }
    
    @Override
    public void init()
    {
    	log("Starting client...");
    	String host = getParameter(RoboProtocol.paramHost);
    	log("host=" + host);
    	int port = Integer.parseInt(getParameter(RoboProtocol.paramPort));
    	log("port=" + port);
    	pass = getParameter(RoboProtocol.paramPassword);
    	log("pass=" + pass);
        try {
        	socket = new TcpSocket(host, port);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    @Override
    public void start() {
        startLogin();
        //startCapture();
    }
    
    @Override
    public void stop()
    {
    }
    
    
    private final static boolean debug = true;
    public static void log(String msg)
    {
    	if(!debug)
    		return;
    	System.out.println("[C] " + msg);
    }
    
}

