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

import java.io.*;
import java.util.*;

/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */

public class ConfigFile {
    
    public static void process(String fileName) throws IOException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Properties properties = System.getProperties();
        try {
            while(true) {
                String line = bufferedReader.readLine();
                if(line == null) {
                    break;
                }
                line = line.trim();
                if(line.length() == 0) {
                    continue;
                }
                if(!line.startsWith("#")) {
                    int index = line.indexOf('=');
                    if(index != -1) {
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();
                        properties.put(key, value);
                    } else {
                        properties.put(line, line);
                    }
                }
            }
        } catch (IOException e) {
        }
        bufferedReader.close();
        fileReader.close();
    }
}