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
import java.io.IOException;

import com.iborg.hsocket.IServerSocket;
import com.iborg.robo.RoboProtocol;
import com.iborg.robo.TcpServerSocketFactory;
import com.iborg.util.ConfigFile;
/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class RoboServer {
    
    public static void main(String args[]) throws AWTException {
        String fileName = "Robo.cfg";
        if(args.length > 0) {
            fileName = args[0];
        }
        try {
            ConfigFile.process(fileName);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }

        // this seems to be neccessary for java.awt.Robot
        Frame frame = new Frame("RoboServer");
        frame.setVisible(true);
        frame.dispose();

        // create server socket
        IServerSocket serverSocket = null;
        String connectionType = System.getProperty(RoboProtocol.paramConnectionType);
        if("tcp".equalsIgnoreCase(connectionType)) {
            serverSocket = TcpServerSocketFactory.createServerSocket();
        } else {
            System.err.println("Unknown connection type " + connectionType);
        }
        
        if(serverSocket != null) {
            // create a robot to feed in GUI events
            Robot robot = new Robot();
            //listen for connections
            new RoboServerListener(robot, serverSocket);
        }
    }
}
