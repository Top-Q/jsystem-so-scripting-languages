/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.vbscript;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import jsystem.sysobj.scripting.tcl.ShellCommand;

public class RemoteVbShellImpl extends UnicastRemoteObject implements RemoteVBShell {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1476056632859630162L;
	
	protected RemoteVbShellImpl() throws RemoteException {
		super();
	}
	
	@Override
	public int launch() throws RemoteException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void executeCommand(int id, ShellCommand command) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void exit(int id) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public long getTimeout(int id) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void setTimeout(int id, long timeout) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
}
