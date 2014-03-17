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
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class RoboServerListener extends Thread {
    Robot robot;
    IServerSocket serverSocket;
    
    RoboServerProcessor client = null;
    
    public RoboServerListener(Robot robot, IServerSocket serverSocket) {
        this.robot = robot;
        this.serverSocket = serverSocket;
        start();
    }
    
    public void run() {
        try {
            while(true) {
                ISocket s = serverSocket.accept();
                
                if(client != null)
                	client.kill();
                	
                client = new RoboServerProcessor(s, robot);
                client.start();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}

