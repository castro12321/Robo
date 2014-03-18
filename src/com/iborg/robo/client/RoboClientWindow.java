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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

import com.iborg.hsocket.ISocket;


public class RoboClientWindow
{
	protected final JFrame window = new JFrame();
	protected final float ratioX, ratioY;
	protected Rectangle oldSize;
	
	
	public RoboClientWindow(final RoboClient roboclient, final ISocket socket)
	{
		// Create main window
		window.setLayout(new BorderLayout());
		window.add(BorderLayout.CENTER, roboclient.screenCanvas);
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		final int sizeX = 800, sizeY = 600;
		final int locationX = (screenDimension.width / 2 - sizeX / 2), locationY = (screenDimension.height / 2 - sizeY / 2);
		window.setSize(sizeX, sizeY);
		window.setLocation(locationX, locationY);
		window.setVisible(true);
		window.addWindowListener(new WindowAdapter()
		{	@Override public void windowClosing(WindowEvent e)
			{ RoboClientWindow.this.windowClosing(roboclient, socket); }
		});
		window.addComponentListener(new ComponentAdapter()
		{	@Override public void componentMoved(ComponentEvent e)
			{ windowMoved(); }
			@Override public void componentResized(ComponentEvent e)
			{ windowResized(e.getComponent()); }
		});
		
		// Calculate main window size
		oldSize = window.getBounds();
		ratioX = (float) oldSize.width  / (float) oldSize.height;
		ratioY = (float) oldSize.height / (float) oldSize.width;
		RoboClient.log("Original size: " + oldSize.width + " " + oldSize.height + " ratio: " + ratioX + " " + ratioY);
	}
	
	
	public void close()
	{
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
	}
	
	
	protected void windowClosing(RoboClient robo, ISocket socket)
	{
		try
		{
			socket.close();
		}
		catch(IOException ioe)
		{}
		window.dispose();
		robo.stop();
	}
	
	
	protected void windowMoved()
	{
	}
	
	
	protected void windowResized(Component resized)
	{
	}
	
	
	protected void adjustSizeToAspectRatio(JFrame frame)
	{
		Rectangle windowSize = window.getBounds();
		if(windowSize.width != oldSize.width)
			windowSize.height = (int) ((float) windowSize.width  * ratioY);
		else if(windowSize.height != oldSize.height)
			windowSize.width  = (int) ((float) windowSize.height * ratioX);
		frame.setBounds(windowSize);
		oldSize = windowSize;
	}
}
