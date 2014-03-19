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

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
    private Graphics2D offScreenGraphics;
    
    private RoboClient roboClient;
    
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
        
        // Important line below! Allows to use TAB key and other functional keys
        setFocusTraversalKeysEnabled(false);
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                try {
                    roboClient.processor.keyPressed(e.getKeyCode());
                    e.consume();
                } catch (Exception ex) {
                }
            }
            public void keyReleased(KeyEvent e) {
                try {
                    roboClient.processor.keyReleased(e.getKeyCode());
                    e.consume();
                } catch (Exception ex) {
                }
            }
        });
        
        
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                roboClient.processor.adjustScale();
                offScreenImage = createImage(getSize().width, getSize().height);
                offScreenGraphics = (Graphics2D) offScreenImage.getGraphics();
                roboClient.processor.adjustScale();
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
    
    @Override
    public void paint(Graphics g)
    {
    	paint((Graphics2D)g);
    }
    public void paint(Graphics2D g) {
        if(image != null) {
            Dimension d = this.getSize();
            
            offScreenGraphics.setComposite(AlphaComposite.Src);
            offScreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            offScreenGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
            //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            
            RoboClientProcessor target = roboClient.processor;
            offScreenGraphics.drawImage(image, 0, 0, d.width, d.height, 0, 0, target.width, target.height, null);
            g.drawImage(offScreenImage, 0, 0, null);
        }
    }
    
    public void createScreenImage(int width, int height) {
        image = createImage(width, height);
        imageGraphics = image.getGraphics();
    }
}

