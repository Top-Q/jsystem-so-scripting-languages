/*
 * Created on Jan 23, 2006
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.tcl;

import com.aqua.sysobj.conn.CliCommand;

/**
 * Create an 'AG2000' type command
 * Monitor general CLI errors
 * 
 * @author ohad.crystal@aquasw.com
 */
public class TclCliCommand extends CliCommand {
	
	public TclCliCommand(String command) {
		super();
		setCommands(new String[] { command });
		addErrors("Command not found");
		
	}
	
}