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

package com.iborg.robo;
/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class RoboProtocol {
	public static final int VERSION = 4;
    public static final int PORT = 5000;
    
    public static final int SCREEN_REQUEST = 1;
    public static final int SCREEN_PARAM_REQUEST = 2;
    public static final int SCREEN_PARAM_RESPONSE = 3;
    public static final int SCREEN_ADJUSTMENT_END = 4;
    public static final int SCREEN_RESPONSE_PART = 5;
    public static final int SCREEN_NOP = 6;
    public static final int SCREEN_COLOR_MODEL = 7;
    public static final int SCREEN_SET_COM_PARAMS = 8;

    public static final int REQUEST_LOGIN = 21;
    public static final int LOGIN_SUCCESSFUL = 22;
    public static final int LOGIN_FAILED = 24;
    public static final int LOGIN = 25;
    public static final int LOGIN_MESSAGE_DIGEST = 26;

    public static final int MOUSE_MOVED = 40;
    public static final int MOUSE_PRESSED = 41;
    public static final int MOUSE_RELEASED = 42;
    public static final int KEY_PRESSED = 43;
    public static final int KEY_RELEASED = 44;
    
    public static final int CONNECTION_CLOSED = 60;

	public static final String paramPassword = "robo.password";
	public static final String paramConnectionType = "robo.connectionType";
	public static final String paramHost = "robo.host";
	public static final String paramPort = "robo.port";


}
