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

/*
 * HSocket.java
 *
 * Created on March 18, 2002, 9:29 PM
 */

package com.iborg.hsocket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.iborg.util.UniqueObject;

/**
 *
 * @author  <a href="mailto:sanych@comcast.net">Boris Galinsky</a>.
 * @version
 */
public class HSocket implements ISocket {
    String key;
    String base;
    String openSpec;
    String writeSpec;
    String readSpec;
    String closeSpec;
    boolean opened;
    boolean noOutput = false;
    
    /** Creates new HSocket */
    public HSocket(String base, String key) throws IOException {
        this(base, key, false);
    }
    
    public HSocket(String base, String key, boolean opened) throws IOException {
        this.key = key;
        this.base = base;
        prepareSpecs();
        if (opened == true)
            this.opened = opened;
        else
            open();
    }
    
    private void prepareSpecs() {
        try {
            openSpec = new URL(base + "open?" + key).toString();
            writeSpec = new URL(base + "write?" + key).toString();
            readSpec = new URL(base + "read?" + key).toString();
            closeSpec = new URL(base + "close?" + key).toString();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    private void open() throws IOException {
        try {
            String openWithFill = openSpec + ":" + UniqueObject.createUniqueObject();
            URL url = new URL(openWithFill);
            URLConnection connection = url.openConnection();
            InputStream urlStream = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(urlStream);
            BufferedReader br = new BufferedReader(isr);
            String in;
            in = br.readLine();
            //            System.out.println("in is ->" + in + "<-");
            if (in != null) {
                key = in;
                opened = true;
                prepareSpecs();
            } else {
                if (!opened)
                    throw new IOException("Failed to open");
            }
            urlStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IOException("Failed to open");
        }
    }
    
    public int write(byte[] data, int offset, int length) throws IOException {
        try {
            URL url = new URL(writeSpec);
            int totalWritten = 0;
            
            while(totalWritten != length) {
                int lenToWrite;
                //int magicNumber = 1024 * 16;
                int magicNumber = 3800;
                if(length - totalWritten > magicNumber)
                    lenToWrite = magicNumber;
                else
                    lenToWrite = length - totalWritten;
                
                int written = 0;
                try {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("content-type", "application/binary");
                    connection.setRequestProperty("content-length", String.valueOf(lenToWrite));
                    OutputStream out = connection.getOutputStream();
                    out.write(data, offset + totalWritten, lenToWrite);
                    out.flush();
                    InputStream urlStream = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(urlStream);
                    BufferedReader br = new BufferedReader(isr);
                    String in;
                    in = br.readLine();
                    urlStream.close();
                    if (in != null) {
                        try {
                            written = Integer.parseInt(in);
                        } catch (NumberFormatException nfe) {
                            throw new IOException(in);
                        }
                    } else {
                        //throw new IOException("Failed to read on write");
                    }
                } catch (java.io.FileNotFoundException fnfe) {
                    System.err.println("file not found " + fnfe);
                } catch (java.net.UnknownHostException uhe) {
                    System.err.println("file not found " + uhe);
                } catch (IOException ioe) {
                    throw ioe;
                } catch (Exception e) {
                    System.err.println("Failed to write " + e);
                    e.printStackTrace(System.err);
                }
                totalWritten += written;
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
            return totalWritten;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IOException("Failed to write");
        }
    }
    
    public byte[] read() throws IOException {
        try {
            while (opened) {
                String readWithFill = readSpec + UniqueObject.createUniqueObject();
                URL url = new URL(readWithFill );
                URLConnection connection = url.openConnection();
                InputStream urlStream = connection.getInputStream();
                int available = connection.getContentLength(); //urlStream.available();
                
                if (available > 0) {
                    byte[] data = new byte[available];
                    DataInputStream dis = new DataInputStream(urlStream);
                    dis.readFully(data);
                    dis.close();
                    urlStream.close();
                    return data;
                }
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                }
            }
            throw new IOException("Socket is closed");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IOException("Failed to read - socket is closed");
        }
    }
    
    public void shutdownOutput() throws IOException {
        noOutput = true;
    }
    
    public void close() throws IOException {
        shutdownOutput();
        opened = false;
        try {
            URL url = new URL(closeSpec);
            URLConnection connection = url.openConnection();
            InputStream urlStream = connection.getInputStream();
            int available = urlStream.available();
            if (available > 0) {
                byte[] data = new byte[available];
                urlStream.read(data);
                urlStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IOException("Failed to close");
        }
    }
    
    public InputStream getInputStream() {
        return new HSocketInputStream(this);
    }
    
    public OutputStream getOutputStream() {
        return new HSocketOutputStream(this);
    }
    
}