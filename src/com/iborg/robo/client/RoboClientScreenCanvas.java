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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

/** 
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version 
 */

public class RoboClientScreenCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	
    private Image image;
    public Graphics imageGraphics;
    
    private Image offScreenImage;
    private Graphics offScreenGraphics;
    
    RoboClient roboClient;
    public int offsetX;
    public int offsetY;
    
    public RoboClientScreenCanvas() {
        super();
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                try {
                    roboClient.processor.mouseMoved(e.getX(), e.getY());
                } catch (Exception ex) {
                }
            }
            
            public void mouseDragged(MouseEvent e) {
                try {
                    roboClient.processor.mouseDragged(e.getX(), e.getY());
                } catch (Exception ex) {
                }
            }
            
        });
        
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }
            public void mousePressed(MouseEvent e) {
                try {
                    roboClient.processor.mousePressed(e.getModifiers());
                } catch (Exception ex) {
                }
            }
            public void mouseReleased(MouseEvent e) {
                try {
                    roboClient.processor.mouseReleased(e.getModifiers());
                } catch (Exception ex) {
                }
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
            
        });
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                try {
                    roboClient.processor.keyPressed(e.getKeyCode());
                } catch (Exception ex) {
                }
            }
            public void keyReleased(KeyEvent e) {
                try {
                    roboClient.processor.keyReleased(e.getKeyCode());
                } catch (Exception ex) {
                }
            }
        });
        
        
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                roboClient.processor.adjustScrollbars();
                offScreenImage = createImage(getSize().width, getSize().height);
                offScreenGraphics = offScreenImage.getGraphics();
                roboClient.processor.adjustScrollbars();
            }
        });
        
        
        
        
    }
    public RoboClientScreenCanvas(Image image) {
        this();
        this.image = image;
    }
    
    public RoboClientScreenCanvas(RoboClient roboClient) {
        this();
        this.roboClient = roboClient;
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    public void paint(Graphics g) {
        if(image != null) {
            Dimension d = this.getSize();
            if(roboClient.vscrollBar.getMaximum() > roboClient.vscrollBar.getVisible())
                offsetY = (image.getHeight(null) - d.height) * roboClient.vscrollBar.getValue() / (roboClient.vscrollBar.getMaximum() - roboClient.vscrollBar.getVisible());
            else
                offsetY = 0;
            if(roboClient.hscrollBar.getMaximum() > roboClient.hscrollBar.getVisible())
                offsetX = (image.getWidth(null) - d.width) * roboClient.hscrollBar.getValue() / (roboClient.hscrollBar.getMaximum() - roboClient.hscrollBar.getVisible());
            else
                offsetX = 0;
            offScreenGraphics.drawImage(image, 0, 0, d.width, d.height, offsetX, offsetY, offsetX + d.width, offsetY + d.height, null);
            g.drawImage(offScreenImage, 0, 0, null);
        }
    }
    
    public void createScreenImage(int width, int height) {
        image = createImage(width, height);
        imageGraphics = image.getGraphics();
    }
}

