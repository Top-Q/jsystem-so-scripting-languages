package tests.perl;

import java.util.ArrayList;

import systemobject.terminal.BufferInputStream;
import systemobject.terminal.Cli;
import systemobject.terminal.Prompt;
import systemobject.terminal.RS232;
import systemobject.terminal.SSH;
import systemobject.terminal.Telnet;
import systemobject.terminal.VT100FilterInputStream;

import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.Position;

public class WindowsCliConnection extends CliConnectionImpl {
	
	@Override
	public Position[] getPositions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Prompt[] getPrompts() {
		ArrayList<Prompt> promps = new ArrayList<Prompt>();
		Prompt p = new Prompt();
		p.setPrompt("^.*>");
		p.setCommandEnd(true);
		p.setRegularExpression(true);
		p.setDontWaitForScrollEnd(true);
		promps.add(p);
		
		p = new Prompt();
		p.setPrompt("login:");
		p.setStringToSend(user);
		promps.add(p);
		
		p = new Prompt();
		p.setPrompt("password:");
		p.setStringToSend(password);
		promps.add(p);
		
		return promps.toArray(new Prompt[0]);
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
		boolean isRs232 = false;
		if (host.toLowerCase().startsWith("com") || protocol.toLowerCase().equals("rs232")) { // syntax for com conneciton found
			isRs232 = true;
			String[] params = host.split("\\;");
			if (params.length < 5) {
				throw new Exception("Unable to extract parameters from host: " + host);
			}
			terminal = new RS232(params[0], Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]),
					Integer.parseInt(params[4]));
		} else if (protocol.toLowerCase().equals("ssh")) {
			terminal = new SSH(host, user, password);
		} else {
			terminal = new Telnet(host, port, useTelnetInputStream);
			if (dump) {
				((Telnet) terminal).setVtType(null);
			}
		}
		cli = new Cli(terminal);
		cli.setGraceful(graceful);
		if (useBuffer) {
			buffer = new BufferInputStream();
			terminal.addFilter(buffer);
			buffer.startThread();
		}
		Prompt[] prompts = getPrompts();
		for (int i = 0; i < prompts.length; i++) {
			cli.addPrompt(prompts[i]);
		}
		terminal.addFilter(new VT100FilterInputStream());
		if (isRs232) {
			cli.command("");
		} else {
			cli.login(60000, delayedTyping);
		}
		connected = true;
	}
	
}
