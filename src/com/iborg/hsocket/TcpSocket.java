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
 * TcpSocket.java
 *
 * Created on March 25, 2002, 1:35 PM
 */
package com.iborg.hsocket;
/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */
import java.net.*;
import java.io.*;

public class TcpSocket implements ISocket {
    Socket socket;
    
    public TcpSocket(Socket socket) {
        this.socket = socket;
    }
    public TcpSocket(String host, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
    }
    public void close() throws IOException {
        socket.close();
    }
    
    public InputStream getInputStream() throws IOException {
        return(socket.getInputStream());
    }
    
    public OutputStream getOutputStream()  throws IOException {
        return(socket.getOutputStream());
    }
    
}
