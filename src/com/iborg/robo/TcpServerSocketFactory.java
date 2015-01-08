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
 * TcpServerSocketFactory.java
 *
 * Created on November 11, 2002, 12:43 PM
 */

package com.iborg.robo;
import java.io.*;

import com.iborg.hsocket.*;
import com.iborg.robo.server.RoboServer;
/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */
public class TcpServerSocketFactory {
    public static IServerSocket createServerSocket() {
        int port = RoboProtocol.PORT;
        
        String param = System.getProperty(RoboProtocol.paramPort);
        if(param != null) {
            try {
                port = Integer.parseInt(param);
            } catch (Exception e) {
            }
        }
        RoboServer.log("TcpServerSocketFactory " + port);
        
        IServerSocket serverSocket = null;
        try {
            serverSocket = new TcpServerSocket(port);
        } catch (IOException ioe) {
            RoboServer.log(ioe);
        }
        return serverSocket;
    }
}

