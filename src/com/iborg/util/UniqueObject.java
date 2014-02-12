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

package com.iborg.util;
/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */

public class UniqueObject {
    
    static String   oldUniqueObjectBase = null;
    static int      oldUniqueObjectOffset = -1;
    private final static String     separator = "\\";
    
    public static Object createUniqueObject() {
        return createUniqueString();
    }
    
    public static String createUniqueString() {
        String uniqueObjectBase = "" + (new java.util.Date()).getTime();
        
        synchronized(separator) {
            if(!(uniqueObjectBase.equals(oldUniqueObjectBase))) {
                oldUniqueObjectBase = uniqueObjectBase;
                oldUniqueObjectOffset = -1;
            }
            oldUniqueObjectOffset++;
        }
        
        return(uniqueObjectBase + ":" + oldUniqueObjectOffset);
    }
}
