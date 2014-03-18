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

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.iborg.hsocket.ISocket;

public class RoboClientWindowScalable extends RoboClientWindow implements ActionListener
{
	private Timer recalculateTimer  = new Timer(350, this);
	
	
	public RoboClientWindowScalable(final RoboClient roboclient, final ISocket socket)
    {
		super(roboclient, socket);
		recalculateTimer.setRepeats(false);
    }
	
	
	@Override
	protected void windowResized(Component resized)
	{
		Rectangle newSize = resized.getBounds();
		RoboClient.log("new: " + newSize.x + " " + newSize.y + " " + newSize.width + " " + newSize.height);
		
		if(recalculateTimer.isRunning())
			recalculateTimer.restart();
		else
			recalculateTimer.start();
		
		super.windowResized(resized);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		resizeFinished();
	}
	private void resizeFinished()
	{
		RoboClient.log("Resize done");
		adjustSizeToAspectRatio(window);
	}
}
