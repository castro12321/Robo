/*
 * CommunicationManager.java
 *
 * Created on March 18, 2002, 5:31 PM
 */

package com.iborg.net;
import java.util.*;
import java.io.*;

/**
 *
 * @author  Umax Customer
 * @version
 */
public class CommunicationManager {
    
    public static long OPEN_WAIT=60000L;
    
    //static HashMap accepted = new HashMap();
    private static Map users = new HashMap();
    private static Map currentSockets = new HashMap();
    private static Map comBuffer = new HashMap();
    
    private static Map socketInfo = new HashMap();
    
    private static Thread cleanupThread;
    static class Cleanup implements Runnable {
        public void run() {
            Set toClose = new TreeSet();
            while(true) {
                toClose.clear();
                Map map = CommunicationManager.getCurrentSockets();
                long timeStamp = (new Date()).getTime();
                Set keys = map.keySet();
                Iterator iterator = keys.iterator();
                while( iterator.hasNext()) {
                    String socketName = (String)iterator.next();
                    SocketInfo socketInfo = (SocketInfo) CommunicationManager.getSocketInfo().get(socketName);
                    if(socketInfo.lastRead == 0)
                        socketInfo.lastRead = timeStamp;
                    if(socketInfo.lastWrite == 0)
                        socketInfo.lastWrite = timeStamp;
                    if( timeStamp - socketInfo.lastRead > 180000L &&
                    timeStamp - socketInfo.lastWrite > 180000L) {
                        toClose.add(socketName);
                    }
                }
                iterator = toClose.iterator();
                while( iterator.hasNext()) {
                    String socketName = (String)iterator.next();
                    close(socketName);
                }
                
                try {
                    Thread.currentThread().sleep(60000L);
                } catch (Exception e) {
                }
            }
        }
    }
    
    static {
        Cleanup cleanup = new Cleanup();
        cleanupThread = new Thread(cleanup);
        cleanupThread.start();
    }
    
    public static Map getUsers() {
        return users;
    }
    
    public static Map getCurrentSockets() {
        return currentSockets;
    }
    
    public static Map getSocketInfo() {
        return socketInfo;
    }
    
    public static Set getUserShares(String userId) {
        Set shares = null;
        UserRecord userRecord = (UserRecord) users.get(userId);
        
        if(userRecord != null) {
            shares = userRecord.advertised;
        }
        
        return shares;
    }
    
    public static String accept(String userId, String key) {
        String socket = null;
        UserRecord userRecord = (UserRecord) users.get(userId);
        
        if(userRecord == null) {
            userRecord = new UserRecord();
            users.put(userId, userRecord);
        }
        
        userRecord.advertised.add(key);
        
        HashMap accepted = userRecord.accepted;
        
        Object o = accepted.get(key);
        if(o != null) {
            List sockets = (List) o;
            if(sockets.size() > 0) {
                socket = (String)sockets.get(0);
                synchronized(socket) {
                    sockets.remove(0);
                    socket.notify();
                }
            }
            synchronized(accepted) {
                if(sockets.size() > 0) {
                    accepted.remove(key);
                }
            }
        }
        return socket;
    }
    
    public static String open(String userId, String key) {
        String clientSocket = null;
        UserRecord userRecord = (UserRecord) users.get(userId);
        
        if(userRecord != null) {
            HashMap accepted = userRecord.accepted;
            
            synchronized(accepted) {
                List sockets = (List) accepted.get(key);
                if(sockets == null) {
                    sockets = new ArrayList();
                }
                String socket = com.iborg.util.UniqueObject.createUniqueString();
                String serverSocket = socket + "S";
                clientSocket = socket + "C";
                
                sockets.add(serverSocket);
                
                accepted.put(key, sockets);
                
                currentSockets.put(serverSocket, clientSocket);
                currentSockets.put(clientSocket, serverSocket);
                
                socketInfo.put(serverSocket, new SocketInfo());
                socketInfo.put(clientSocket, new SocketInfo());
                
                synchronized(serverSocket) {
                    try {
                        serverSocket.wait(OPEN_WAIT);
                    } catch (InterruptedException ie) {
                    }
                    if(sockets.contains(serverSocket)) {
                        sockets.remove(serverSocket);
                        currentSockets.remove(serverSocket);
                        currentSockets.remove(clientSocket);
                        return null;
                    }
                }
                comBuffer.put(serverSocket, new ArrayList());
                comBuffer.put(clientSocket, new ArrayList());
            }
        }
        return clientSocket;
    }
    
    public static void close(String key) {
        comBuffer.remove(key);
        currentSockets.remove(key);
        socketInfo.remove(key);
    }
    
    public static int write(String socket, byte [] buffer, int length) throws IOException {
        int len = 0;
        
        Object o = currentSockets.get(socket);
        if(o != null) {
            List messages = (List)comBuffer.get(o);
            if(messages == null) {
                throw new IOException("Socket is being closed");
            }
            byte [] data = new byte[length];
            for(int i = 0; i < length; i++) {
                data[i] = buffer[i];
            }
            messages.add(data);
            len = length;
        } else {
            throw new IOException("Socket is closed");
        }
        
        o = socketInfo.get(socket);
        if(o != null) {
            SocketInfo socketInfo = (SocketInfo) o;
            socketInfo.updateWriteStats(len);
        }
        
        return len;
    }
    
    public static byte [] read(String socket) throws IOException {
        byte [] data = null;
        Object o = currentSockets.get(socket);
        if(o != null) {
            List messages = (List)comBuffer.get(socket);
            if(messages.size() > 0) {
                data = (byte [])messages.get(0);
                messages.remove(0);
            } else {
                if(currentSockets.get(o) == null) {
                    throw new IOException("Socket is being closed");
                }
            }
        } else {
            throw new IOException("Socket is closed");
        }
        o = socketInfo.get(socket);
        if(o != null) {
            SocketInfo socketInfo = (SocketInfo) o;
            int len = 0;
            if(data != null)
                len = data.length;
            socketInfo.updateReadStats(len);
        }
        return data;
    }
    
}

