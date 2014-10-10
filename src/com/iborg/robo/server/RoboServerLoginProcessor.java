package com.iborg.robo.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Date;

import com.iborg.robo.RoboProtocol;

/**
 * Manages the logging process:
 * - Ask user for password
 * - Receive password
 * - Check if valid
 * - Response to the user
 */
public class RoboServerLoginProcessor
{
	private long loginMask; // challenge for messageDigest login
	private final InputStream  is;
	private final OutputStream os;
	private final RoboServerProcessor parentProcessor;
	
	
	public RoboServerLoginProcessor(InputStream is, OutputStream os, RoboServerProcessor parentProcessor)
	{
		this.is = is;
		this.os = os;
		this.parentProcessor = parentProcessor;
	}
	
	/**
	 * Runs the process described in the main comment:
	 * - Ask user for password
	 * - Receive password
	 * - Check if valid
	 * - Response to the user
	 * @return Whether user logged in successfuly or not
	 */
	public boolean run()
	{
		RoboServer.log("Running login process");
		boolean loggedIn = false;
		int loginCounter = 3;
		
		try
		{
			while(!loggedIn)
			{
				sendLoginRequest();
				int command = is.read();
				
				if(parentProcessor.isInterrupted())
					return false;
				
				if(command == RoboProtocol.LOGIN || command == RoboProtocol.LOGIN_MESSAGE_DIGEST)
					loggedIn = processLogin(command);
				else
					break;
				
				loginCounter--;
				if(loginCounter <= 0)
				{
					sendLoginFailed();
					break;
				}
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
		
		return loggedIn;
	}
	
	
	private synchronized void sendLoginRequest()
	{
		try
		{
			RoboServer.log("Sending login request");
			os.write(RoboProtocol.REQUEST_LOGIN);
			loginMask = (new Date()).getTime();
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeLong(loginMask);
			dos.flush();
			os.flush();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
	
	
	private synchronized void sendLoginFailed()
	{
		try
		{
			RoboServer.log("Login failed");
			os.write(RoboProtocol.LOGIN_FAILED);
			os.flush();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
	
	
	private synchronized void sendLoginSuccessful()
	{
		try
		{
			RoboServer.log("Login successful");
			os.write(RoboProtocol.LOGIN_SUCCESSFUL);
			os.flush();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
	
	
	private boolean verifyPassword(int command, byte[] msg)
	{
		String password = System.getProperty(RoboProtocol.paramPassword);
		RoboServer.log("Password needed=" + password);
		
		if(command == RoboProtocol.LOGIN_MESSAGE_DIGEST)
		{
			RoboServer.log("Processing ciphered password; received ciphered pass=" + msg);
			
			try
			{
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
				String secret = password + loginMask;
				byte[] buffer = secret.getBytes();
				messageDigest.update(buffer);
				byte[] digest = messageDigest.digest();
				if(digest.length == msg.length)
				{
					for(int i = 0; i < digest.length; i++)
					{
						if(digest[i] != msg[i])
							return false;
					}
					return true;
				}
				return false;
			}
			catch(Exception e)
			{
				e.printStackTrace(System.err);
				return false;
			}
		}
		else
		{
			RoboServer.log("Not ciphered password processing; pass=" + msg);
			return ((new String(msg)).equals(password));
		}
	}
	
	
	private boolean processLogin(int command)
	{
		try
		{
			DataInputStream dis = new DataInputStream(is);
			int length = dis.readInt();
			byte[] buffer = new byte[length];
			int counter = 0;
			while(counter < length)
			{
				int r = is.read(buffer, counter, length - counter);
				if(r == -1)
				{
					break;
				}
				counter += r;
			}
			
			if(verifyPassword(command, buffer))
			{
				
				sendLoginSuccessful();
				
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
		}
		return false;
	}
}