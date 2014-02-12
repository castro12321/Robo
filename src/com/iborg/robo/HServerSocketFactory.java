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
 * HServerSocketFactory.java
 *
 * Created on November 11, 2002, 12:42 PM
 */
package com.iborg.robo;

import com.iborg.hsocket.*;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */
 
public class HServerSocketFactory {
    public static IServerSocket createServerSocket() {
        String connectionURL = "http://localhost/share/";
        String acceptToken = "bg";
        String param = System.getProperty(RoboProtocol.paramAcceptToken);
        if(param != null) {
            acceptToken = param;
        }
        
        param = System.getProperty(RoboProtocol.paramConnectionURL);
        if(param != null) {
            connectionURL = param;
        }
        // this ignores host names for self-generated certificates
        String setDefaultHostnameVerifier = System.getProperty(RoboProtocol.paramSetDefaultHostnameVerifier);
        if(setDefaultHostnameVerifier != null) {
		HServerSocketFactoryHttpsHandling.setDefaultHostnameVerifier();
        }
        System.out.println("HServerSocketFactory " + connectionURL + ":" + acceptToken );
        IServerSocket serverSocket = new HServerSocket(connectionURL, acceptToken);
        return serverSocket;
    }
}

