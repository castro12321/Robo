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
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.iborg.hsocket.ISocket;
import com.iborg.robo.RoboProtocol;
/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */

public class RoboClientLoginProcessor
{
	private final RoboClient roboClient;
	private final String password;
	private InputStream is;
    private OutputStream os;
    private int maxPixels = -1;
    private int maxUpdateChunk = -1;
    
    
	RoboClientLoginProcessor(ISocket s, RoboClient roboClient, String password)
	{
		this.password = password;
		this.roboClient = roboClient;
		try
		{
			os = s.getOutputStream();
			is = s.getInputStream();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
    
    public void run() {
        
        boolean loop = true;
        while(loop) {
            try {
                int command = is.read();
                switch(command) {
                    case RoboProtocol.REQUEST_LOGIN:
                    	RoboClient.log("Got login request");
                        login();
                        break;
                    case RoboProtocol.LOGIN_SUCCESSFUL:
                    	RoboClient.log("Got login successful");
                        loginSuccesful();
                        loop = false;
                        break;
                    case RoboProtocol.LOGIN_FAILED:
                    	RoboClient.log("Got login failed");
                        loginFailed();
                        loop = false;
                        break;
                    default:
                        //System.out.println("unknown command " + command);
                        break;
                }
            } catch (Exception e) {
                System.err.println(e);
                break;
            }
        }
    }
    
    
    public void login() {
        DataInputStream dis = new DataInputStream(is);
        long loginMask = 0;
        try {
            loginMask = dis.readLong();
        } catch (Exception e) {
        }
        
        RoboClient.log("Got password: " + password);
        
        try {
            try {
                //java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("SHA-1");
                
                Class<?> classMessageDigest = Class.forName("java.security.MessageDigest");
                Class<?> [] getMethodParams = new Class[1];
                getMethodParams[0] = String.class;
                java.lang.reflect.Method method = classMessageDigest.getMethod("getInstance", getMethodParams);
                Object [] methodParam = new Object[1];
                methodParam[0] = "SHA-1";
                Object messageDigest = method.invoke(null, methodParam);
                
                String msg = password + loginMask;
                byte [] buffer = msg.getBytes();
                
                // messageDigest.update(buffer);
                getMethodParams[0] = buffer.getClass();
                method = classMessageDigest.getMethod("update", getMethodParams);
                methodParam[0] = buffer;
                method.invoke(messageDigest, methodParam);
                
                //byte[] digest = messageDigest.digest();
                getMethodParams = null;
                method = classMessageDigest.getMethod("digest", getMethodParams);
                byte [] digest = (byte []) method.invoke(messageDigest, (Object)null);
                sendLogin(RoboProtocol.LOGIN_MESSAGE_DIGEST, digest);
                return;
            } catch (Exception e) {
                System.err.println(e);
            }
            sendLogin(RoboProtocol.LOGIN, password.getBytes());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            sendLogin(RoboProtocol.LOGIN, password.getBytes());
        }
        
    }
    
    private static void centerFrame(Window w) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenDimension = toolkit.getScreenSize();
        w.setLocation((screenDimension.width - w.getSize().width)/2, (screenDimension.height - w.getSize().height)/2);
    }
    
    public void advancedSetupDialog() {
        // create dialog
        final Dialog dialog = new Dialog(new Frame(), "Advanced communication parameters", true);
        final TextField maxPixelsField = new TextField(10);
        final TextField maxUpdateChunkField = new TextField(10);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        
        maxPixelsField.setText(Integer.toString(maxPixels));
        maxUpdateChunkField.setText(Integer.toString(maxUpdateChunk));
        
        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    maxPixels = Integer.parseInt(maxPixelsField.getText());
                } catch (Exception e1) {
                }
                try {
                    maxUpdateChunk = Integer.parseInt(maxUpdateChunkField.getText());
                } catch (Exception e1) {
                }
                dialog.dispose();
            }
        };
        
        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        };
        
        okButton.addActionListener(okListener);
        maxPixelsField.addActionListener(okListener);
        maxUpdateChunkField.addActionListener(okListener);
        cancelButton.addActionListener(cancelListener);
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });
        
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        
        Panel panel = new Panel();
        panel.setLayout(gridBag);
        
        Label label = new Label("Maximum Pixels in a Chunk");
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gridBag.setConstraints(label,gbc);
        panel.add(label);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(maxPixelsField,gbc);
        panel.add(maxPixelsField);
        
        label = new Label("Maximum Transmission Size");
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gridBag.setConstraints(label,gbc);
        panel.add(label);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gridBag.setConstraints(maxUpdateChunkField,gbc);
        panel.add(maxUpdateChunkField);
        
        dialog.add(BorderLayout.CENTER, panel);
        
        panel = new Panel();
        panel.setLayout(new FlowLayout());
        panel.add(okButton);
        panel.add(cancelButton);
        dialog.add(BorderLayout.SOUTH, panel);
        dialog.pack();
        
        centerFrame(dialog);
        dialog.setVisible(true);
        
        System.out.println("action " + maxUpdateChunk + " " + maxPixels);
    }
    
    private synchronized void sendLogin(int command, byte [] buffer) {
        try {
            os.write(command);
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeInt(buffer.length);
            dos.write(buffer);
            os.flush();
        } catch (Exception e) {
        }
    }
    
    private void loginSuccesful() {
        if(maxPixels != -1 || maxUpdateChunk != -1) {
            try {
                os.write(RoboProtocol.SCREEN_SET_COMMUNICATION_PARAMETERS);
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeInt(maxPixels);
                dos.writeInt(maxUpdateChunk);
                os.flush();
            } catch (Exception e) {
            }
        }
        roboClient.startCapture();
    }
    
    private void loginFailed() {
        // create dialog
        final Dialog dialog = new Dialog(new Frame(), "Login failed", true);
        Label explanation = new Label("You failed to login. Application is terminating...");
        Button button = new Button("OK");
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        };
        
        button.addActionListener(actionListener);
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });
        
        Panel panel = new Panel();
        panel.setLayout(new FlowLayout());
        panel.add(explanation);
        panel.add(button);
        dialog.add(BorderLayout.CENTER, panel);
        dialog.pack();
        
        // position dialog
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenDimension = toolkit.getScreenSize();
        dialog.setLocation((screenDimension.width - dialog.getSize().width)/2, (screenDimension.height - dialog.getSize().height)/2);
        
        dialog.setVisible(true);
        
        roboClient.stop();
    }
}
