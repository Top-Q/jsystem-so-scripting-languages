/*
 * Created on Jan 23, 2006
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.tcl;

import systemobject.terminal.BufferInputStream;
import systemobject.terminal.Cli;
import systemobject.terminal.Prompt;
import systemobject.terminal.Telnet;

import com.aqua.sysobj.conn.CliConnectionImpl;

/**
 * Handle connectivity to the 'AG2000' devices.
 * Monitor and act by selected prompts
 * 
 * @author ohad.crystal@aquasw.com
 */

public class TclCliConnection extends CliConnectionImpl {
	
	@Override
	public Prompt[] getPrompts() {
		Prompt[] p = new Prompt[1];
		
		p[0] = new Prompt();
		p[0].setPrompt("Ixia>");
		p[0].setCommandEnd(true);
		
		return p;
	}
	
	@Override
	public void connect() throws Exception {
		if (host == null) {
			throw new Exception("Default connection ip/comm is not configured");
		}
		report.report("Init cli, host: " + host);
		if (dummy) {
			return;
		}
		// Terminal t;
		terminal = new Telnet(host, port, useTelnetInputStream);
		// terminal.setScrallEndTimeout(100);
		cli = new Cli(terminal);
		if (useBuffer) {
			buffer = new BufferInputStream();
			terminal.addFilter(buffer);
			buffer.startThread();
		}
		Prompt[] prompts = getPrompts();
		for (int i = 0; i < prompts.length; i++) {
			cli.addPrompt(prompts[i]);
		}
		connected = true;
	}
	
	@Override
	public com.aqua.sysobj.conn.Position[] getPositions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setScrallEndTimeout(long time) {
		terminal.setScrollEndTimeout(time);
	}
	
}
