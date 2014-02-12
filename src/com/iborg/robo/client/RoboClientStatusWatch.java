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

package com.iborg.robo.client;

import java.util.*;
import com.iborg.robo.*;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class RoboClientStatusWatch extends Thread {
    
    public RoboClient roboClient;
    
    long firstTime = -1L;
    long startTime;
    int requestCounter;
    long lastTime;
    
    public RoboClientStatusWatch(RoboClient roboClient)
    {
        this.roboClient = roboClient;
    }
 
    public void run()
    {
        while(true) {
            long delta = System.currentTimeMillis() - startTime;
            roboClient.currentTime.setText(formatTime(delta));
            try {
                Thread.currentThread().sleep(200);
            } catch (Exception e) {
            }
        }
    }
    
    private String formatTime(long time)
    {
        String string = "" + (time / 1000) + "." + (time % 1000);
        return string;
    }
    
    public void startCounts()
    {
        startTime = System.currentTimeMillis();
        if(firstTime == -1) {
            firstTime = startTime;
        }
    }
    
    public void completeCount()
    {
        requestCounter ++;
        
        long delta = System.currentTimeMillis() - startTime;
        lastTime = delta;
        roboClient.lastTime.setText(formatTime(delta));

        delta = System.currentTimeMillis() - firstTime;
        roboClient.averageTime.setText(formatTime(delta / requestCounter));
    }
}

    
