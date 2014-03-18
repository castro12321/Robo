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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

import com.iborg.hsocket.ISocket;

public class RoboClientWindowBackup
{
	private JFrame resizeBorder = new JFrame();
	private JFrame window       = new JFrame();
	private Character resizing        = null;
	
	public RoboClientWindowBackup(final RoboClient roboclient, final ISocket socket)
	{
		resizeBorder.getContentPane().setBackground(Color.WHITE);
		resizeBorder.setVisible(false);
		
		window.setLayout(new BorderLayout());
		window.add(BorderLayout.CENTER, roboclient.screenCanvas);
		
		// Create frame
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		final int sizeX = 800, sizeY = 600;
		final int locationX = (screenDimension.width / 2 - sizeX / 2), locationY = (screenDimension.height / 2 - sizeY / 2);
		window.setSize(sizeX, sizeY);
		window.setLocation(locationX, locationY);
		window.setVisible(true);
		
		// Global mouse listener
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final AWTEventListener listener = new AWTEventListener()
		{
			public void eventDispatched(AWTEvent event)
			{
				if(event.getID() == MouseEvent.MOUSE_MOVED)
				{
					window.setBounds(resizeBorder.getBounds());
					window.setVisible(true);
					resizeBorder.setVisible(false);
					resizing = null;
				}
			}
		};
		toolkit.addAWTEventListener(listener, AWTEvent.MOUSE_MOTION_EVENT_MASK);
		
		
		// Window listener
		window.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				try
				{
					socket.close();
				}
				catch(IOException ioe)
				{}
				toolkit.removeAWTEventListener(listener);
				window.dispose();
				window = null;
				resizeBorder.dispose();
				resizeBorder = null;
				roboclient.stop();
			}
		});
		
		// Component Moved/Resized listener
		window.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentMoved(ComponentEvent e)
			{
				resizeBorder.setBounds(window.getBounds());
				// roboclient.screenCanvas.repaint();
			}
			
			int         oldWidth  = window.getWidth();
			int         oldHeight = window.getHeight();
			final float ratioX    = (float) oldWidth / (float) oldHeight;
			final float ratioY    = (float) oldHeight / (float) oldWidth;
			
			
			@Override
			public void componentResized(ComponentEvent e)
			{
				window.setVisible(false);
				resizeBorder.setVisible(true);
				
				Component component = e.getComponent();
				Rectangle newSize = component.getBounds();
				RoboClient.log("bound: " + newSize.x + " " + newSize.y + " " + newSize.width + " " + newSize.height);
				
				if(resizing == null)
					if(newSize.width != oldWidth)
						resizing = 'x';
					else
						resizing = 'y';
					
				if(resizing == 'x')
				{
					oldWidth = newSize.width;
					newSize.height = (int) ((float) newSize.width * ratioY);
				}
				else
				{
					newSize.width = (int) ((float) newSize.height * ratioX);
					oldHeight = newSize.height;
				}
				
				resizeBorder.setBounds(newSize);
			}
		});
	}
	
	
	public void close()
	{
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
	}
}
