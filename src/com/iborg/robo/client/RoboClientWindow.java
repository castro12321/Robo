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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

import com.iborg.hsocket.ISocket;

class RoboJFrame extends JFrame
{
    private static final long serialVersionUID = 1L;
	private final RoboClientWindow roboWindow;
	
	RoboJFrame(RoboClientWindow roboWindow)
	{
		this.roboWindow = roboWindow;
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		roboWindow.resizeDone();
	}
}

public class RoboClientWindow
{
	private JFrame window      = new RoboJFrame(this);
	protected final float ratioX, ratioY;
	protected Rectangle   oldSize;
	protected boolean     resizing;
	
	
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
			{
				RoboClientWindow.this.windowClosing(roboclient, socket);
			}
		});
		window.addComponentListener(new ComponentAdapter()
		{	@Override public void componentResized(ComponentEvent e)
			{
				windowResized(e.getComponent());
			}
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
		window = null;
		robo.stop();
	}
	
	
	protected void windowResized(Component resized)
	{
		Rectangle newSize = resized.getBounds();
		RoboClient.log("last: " + oldSize.width + " " + oldSize.height + " " + resizing);
		RoboClient.log("bound: " + newSize.x + " " + newSize.y + " " + newSize.width + " " + newSize.height);
		
		if(newSize.width  != oldSize.width
		|| newSize.height != oldSize.height)
			resizing = true;
		resizingWindow.setBound(newSize);
		
		if(resizing == 'x')
		{
			oldWidth = newSize.width;
			newSize.height = (int) ((float) newSize.width * ratioY);
		}
		if(resizing == 'y')
		{
			newSize.width = (int) ((float) newSize.height * ratioX);
			oldHeight = newSize.height;
		}
		
		window.setBounds(newSize);
	}
	
	
	void resizeDone()
	{
		if(resizing) // window has been resized
		{
			// Take additional actions
			// resize main window to fit resizeWindow
			// hide resize window
			resizing = false;
		}
	}
	
	
	void adjustSizeToAspectRatio(JFrame window)
	{
		Rectangle windowSize = window.getBounds();
		final float newRatioX = (float) windowSize.width  / (float) windowSize.height;
		final float newRatioY = (float) windowSize.height / (float) windowSize.width;
		
		if(newRatioX > ratioX)
			;
	}
}
