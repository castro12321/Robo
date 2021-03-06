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
 * TcpServerSocket.java
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

public class TcpServerSocket implements IServerSocket
{
	ServerSocket serverSocket;
	
	public TcpServerSocket(int port) throws IOException
	{
		serverSocket = new ServerSocket(port);
	}
	
	public ISocket accept() throws IOException
	{
		Socket socket = serverSocket.accept();
		return new TcpSocket(socket);
	}
}
