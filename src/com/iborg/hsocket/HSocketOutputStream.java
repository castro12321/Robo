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
 * HSocketOutputStream.java
 *
 * Created on March 25, 2002, 1:35 PM
 */

package com.iborg.hsocket;

import java.io.*;
/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */
public class HSocketOutputStream extends OutputStream {
    HSocket socket;
    ByteArrayOutputStream outputStream;
    
    /** Creates new HSocketOutputStream */
    public HSocketOutputStream(HSocket socket) {
        this.socket = socket;
        outputStream = new ByteArrayOutputStream();
    }
    
    public void write(int param) throws java.io.IOException {
        outputStream.write(param);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }
    
    public void write(byte [] b) throws IOException {
        outputStream.write(b);
    }
    
    public void flush() throws IOException
    {
        byte [] buffer = outputStream.toByteArray();
        socket.write(buffer, 0, buffer.length);
        outputStream.reset();
    }
}
