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
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import com.iborg.hsocket.HSocket;
import com.iborg.hsocket.ISocket;
import com.iborg.hsocket.TcpSocket;
import com.iborg.robo.RoboProtocol;
import com.iborg.util.ConfigFile;
/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */



public class RoboClient extends Applet {
	private static final long serialVersionUID = 1L;
    
    Scrollbar hscrollBar;
    Scrollbar vscrollBar;
    RoboClientScreenCanvas screenCanvas;
    RoboClientProcessor processor;
    RoboClientStatusWatch statusWatcher;
    Label lastTime;
    Label currentTime;
    Label averageTime;
    String host;
    int port;
    ISocket socket;
    static boolean standalone = false;
    
    public RoboClient()
    {
    }
    
    public void createRemoteScreenPanel(Container c) {
        c.setLayout(new BorderLayout());
        vscrollBar = new Scrollbar();
        c.add(BorderLayout.EAST, vscrollBar);
        hscrollBar = new Scrollbar(Scrollbar.HORIZONTAL);
        Panel panel1 = new Panel();
        panel1.setLayout(new BorderLayout());
        panel1.add(BorderLayout.CENTER, hscrollBar);
        
        Panel panel = new Panel();
        averageTime = new Label("avg:");
        panel.add(averageTime);
        lastTime = new Label("last:");
        panel.add(lastTime);
        currentTime = new Label("cur:");
        panel.add(currentTime);
        panel1.add(BorderLayout.EAST, panel);
        c.add(BorderLayout.SOUTH, panel1);
        screenCanvas = new RoboClientScreenCanvas(this);
        c.add(BorderLayout.CENTER, screenCanvas);
        
        
        hscrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                screenCanvas.repaint();
            }
        });
        
        vscrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                screenCanvas.repaint();
            }
        });
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
        statusWatcher = new RoboClientStatusWatch(this);
        statusWatcher.start();
        (processor).start();
    }
    
    public void startLogin() {
        (new RoboClientLoginProcessor(socket, this)).start();
    }
    
    
    public void init() {
    	
    	log("init1");
        String connection = getParameter(RoboProtocol.paramConnectionType);
        log("init2 connection: " + connection);
        
        if(connection == null || connection.equalsIgnoreCase("web") ) {
        	log("ini3-0");
            host = getCodeBase().toString();
            log("init3-1 host: " + host);
            
            String key = getParameter(RoboProtocol.paramAcceptToken);
            log("init3-2 key: " + key);
            if(key == null)
                key = "bg";
            try {
                socket = new HSocket(host, key);
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
        	log("ini4-0");
            String s = getParameter(RoboProtocol.paramHost);
            if(s != null)
                host = s;
            else
                host = getCodeBase().getHost();
            log("init4-1 host: " + host);
            
            int port = RoboProtocol.PORT;
            s = getParameter(RoboProtocol.paramPort);
            log("init4-2 port: " + port);
            if(s != null) {
                try {
                    port = Integer.parseInt(s);
                } catch (Exception e) {
                }
            }
            log("init4-3 port: " + port);
            try {
                socket = new TcpSocket(host, port);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
    
    public void start() {
        startLogin();
        //startCapture();
    }
    
    public void stop() {
        if(standalone)
            System.exit(0);
    }
    
    public static void main(String args[]) throws AWTException {
        
        standalone = true;

        String fileName = "Robo.cfg";
        if(args.length > 0) {
            fileName = args[0];
        }
        try {
            ConfigFile.process(fileName);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        
        RoboClient roboClient = new RoboClient();
        roboClient.startLogin();
    }
    
    
    private final static boolean debug = true;
    public static void log(String msg)
    {
    	if(!debug)
    		return;
    	System.out.println(msg);
    }
    
}

