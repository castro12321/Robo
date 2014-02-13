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
 * HSocketInputStream.java
 *
 * Created on March 25, 2002, 1:02 PM
 */

package com.iborg.hsocket;

import java.io.*;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */
public class HSocketInputStream extends InputStream {
    
    HSocket socket;
    byte [] data;
    InputStream dataInputStream;
    
    /** Creates new HSocketInputStream */
    public HSocketInputStream(HSocket socket) {
        this.socket = socket;
    }
    
    private void refresh() throws IOException {
        data = socket.read();
        dataInputStream = new ByteArrayInputStream(data);
    }
    
    public int available() throws IOException {
        if(dataInputStream == null) {
            refresh();
        }
        int available = 0;
        if(dataInputStream != null) {
            available = dataInputStream.available();
        }
        return available;
    }
    
    public int read() throws java.io.IOException {
        if(dataInputStream == null) {
            refresh();
        }
        if(dataInputStream != null) {
            int b =  dataInputStream.read();
            if(b == -1) {
                dataInputStream = null;
                return read();
            } else {
                return b;
            }
        }
        return -1;
    }
    
    public int read(byte[] b, int off, int len) throws java.io.IOException {
	if(len == 0)
		return 0;

        if(dataInputStream == null) {
            refresh();
        }
        if(dataInputStream != null) {
            int r = dataInputStream.read(b, off, len);
            if(r > 0) {
                return r;
            } else {
                dataInputStream = null;
                refresh();
                while(true) {
                    r = dataInputStream.read(b, off, len);
                    if(r > 0) {
                        return r;
			  }

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            }
        }
        return -1;
    }
    
    public boolean markSupported() {
        return false;
    }
}
