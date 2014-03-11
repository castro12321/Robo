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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

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
    
    // Connection
    public  String pass;
    private ISocket socket;
    
    public RoboClient()
    {
    }
    
    public void createRemoteScreenPanel(Container c) {
        c.setLayout(new BorderLayout());
        screenCanvas = new RoboClientScreenCanvas(this);
        c.add(BorderLayout.CENTER, screenCanvas);
    }
    
    public void createUserInterface() {
        final Frame f = new  Frame();
        
        createRemoteScreenPanel(f);
        // position frame
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenDimension = toolkit.getScreenSize();
        f.setSize(screenDimension.width/2, screenDimension.height/2);
        f.setLocation(screenDimension.width/4, screenDimension.height/4);
        f.setVisible(true);
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                }
                f.dispose();
                stop();
            }
        });
        
        f.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                screenCanvas.repaint();
            }
        });
        
    }
    
    public void startCapture() {
        processor = new RoboClientProcessor(socket, this);
        createUserInterface();
        (processor).start();
    }
    
    public void startLogin() {
        (new RoboClientLoginProcessor(socket, this)).start();
    }
    
    
    public void init()
    {
    	log("init1");
    	String host = getParameter(RoboProtocol.paramHost);
    	log("init2 " + host);
    	int port = Integer.parseInt(getParameter(RoboProtocol.paramPort));
    	log("init3 " + port);
    	pass = getParameter(RoboProtocol.paramPassword);
    	log("init4 " + pass);
        try {
        	socket = new TcpSocket(host, port);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public void start() {
        startLogin();
        //startCapture();
    }
    
    public void stop()
    {
    }
    
    
    private final static boolean debug = true;
    public static void log(String msg)
    {
    	if(!debug)
    		return;
    	System.out.println(msg);
    }
    
}

