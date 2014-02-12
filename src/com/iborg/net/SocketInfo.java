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

package com.iborg.net;

import java.util.*;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class SocketInfo
{
    public String ip;
    public long created;
    
    // read stats
    public long lastRead;
    public long reads;
    public long bytesRead;
    
    // write stats
    public long lastWrite;
    public long writes;
    public long bytesWritten;
    
    
    
    public SocketInfo()
    {
        created = (new Date()).getTime();
        lastRead = created;
        lastWrite = created;
    }
    
    public void updateReadStats(long bytesRead)
    {
        lastRead = (new Date()).getTime();
        reads++;
        this.bytesRead += bytesRead;
    }

    public void updateWriteStats(long bytesWritten)
    {
        lastWrite = (new Date()).getTime();
        writes++;
        this.bytesWritten += bytesWritten;
    }

}
