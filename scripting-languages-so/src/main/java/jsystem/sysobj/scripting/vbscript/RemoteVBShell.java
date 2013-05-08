/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.vbscript;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import jsystem.sysobj.scripting.tcl.ShellCommand;

public interface RemoteVBShell extends Remote {
	
	/**
	 * Lanuch Tcl shell.
	 */
	public abstract int launch() throws RemoteException, IOException;
	
	public abstract void executeCommand(int id, ShellCommand command) throws RemoteException;
	
	public abstract void exit(int id) throws RemoteException;
	
	/**
	 * @return Returns the timeout.
	 */
	public abstract long getTimeout(int id) throws RemoteException;
	
	/**
	 * @param timeout
	 *        The timeout to set.
	 */
	public abstract void setTimeout(int id, long timeout) throws RemoteException;
	
}