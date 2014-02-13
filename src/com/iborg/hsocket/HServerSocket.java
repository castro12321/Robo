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
 * HServerSocket.java
 *
 * Created on March 18, 2002, 9:27 PM
 */

package com.iborg.hsocket;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class HServerSocket implements IServerSocket {
    String key;
    String base;
    String acceptSpec;
    /** Creates new HServerSocket */
    public HServerSocket(String base, String key) {
        //this(host, 80, key);
        this.key = key;
        this.base = base;
		try {
        acceptSpec = new URL(base + "accept?" + key).toString();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
    }
    
    public ISocket accept() {
        while (true) {
            try {
                URL url = new URL(acceptSpec);
                URLConnection connection = url.openConnection();
                InputStream urlStream = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(urlStream);
                BufferedReader br = new BufferedReader(isr);
                String in;
                in = br.readLine();
                urlStream.close();
//                System.out.println("in is ->" + in + "<-");
                if(in != null) {
                    return new HSocket(base, in, true);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
            }
            
        }
    }
}