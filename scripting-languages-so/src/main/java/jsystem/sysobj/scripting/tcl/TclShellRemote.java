/*
 * Created on Mar 17, 2005
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.tcl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.framework.analyzer.AnalyzerParameter;
import systemobject.terminal.Prompt;

import com.aqua.sysobj.conn.CliCommand;

/**
 * @author guy.arieli
 */
public class TclShellRemote extends TclShellImpl {
	
	private static final String ENTER = "\r\n";
	
	TclCliConnection conn;
	
	private OutputStream in;
	
	private StringBuffer buffer;
	
	String ip = null;
	
	/**
	 * Class constructor
	 * 
	 * @param ip
	 *        IP of remote Tcl server.
	 */
	public TclShellRemote(String ip) {
		this.ip = ip;
	}
	
	/**
	 * Lanuch Tcl shell.
	 */
	@Override
	public void launch() throws Exception {
		conn = new TclCliConnection();
		conn.setHost(ip);
		conn.connect();
		
		TclCliCommand cmd = new TclCliCommand("\u0003");
		conn.command(cmd);
		
		TclCliCommand cmd2 = new TclCliCommand("");
		cmd2.setAddEnter(false);
		cmd2.setTimeout(1000);
		conn.command(cmd2);
		conn.command(cmd2);
		
		TclCliCommand cmd3 = new TclCliCommand("set PP \"" + getPrompt() + "\"");
		conn.command(cmd3);
		
		source("scripts/JSystem.tcl");
		
		conn.setScrallEndTimeout(0);
	}
	
	/**
	 * Return command output
	 * 
	 * @return Command output
	 */
	@Override
	public String getResults() {
		return buffer.toString();
	}
	
	/**
	 * Execute raw Tcl command.
	 * 
	 * @param command
	 *        The command to execute
	 * @return flag indicating weather command succedded or failed
	 * @throws Exception
	 */
	@Override
	public synchronized boolean command(String command) throws Exception {
		
		TclCliCommand clicmd = new TclCliCommand(command);
		clicmd.setSilent(true);
		Prompt p = new Prompt();
		p.setCommandEnd(true);
		p.setPrompt(getPrompt());
		clicmd.setTimeout(getTimeout());
		clicmd.setPrompts(new Prompt[] { p });
		conn.command(clicmd);
		this.buffer.append(clicmd.getResult());
		
		return true;
		
	}
	
	@Override
	public synchronized void executeCommand(ShellCommand command) {
		
		String cmd = null;
		String toSend;
		try {
			this.buffer = new StringBuffer();
			// command("set errorCode NONE; set errorInfo {}");
			cmd = command.getCommand() + " " + command.getParmetersAsString();
			String cmd1 = "puts \"\\nstdout: <$stdOut>\";"
					+ "puts \"return value: <$returnValue>\";"
					+ "puts \"errorCode: <$errorCode>\";"
					+ "puts \"errorInfo: <$errorInfo>\"; puts $PP";
			toSend = "set errorCode NONE; set errorInfo {}; set stdOut [catch { " + cmd + " } returnValue]; " + cmd1;
			command(toSend);
		} catch (Exception e) {
			command.setFail(true);
			command.setErrorString(e.getMessage());
			return;
		}
		
		String scriptOutput = getResults();
		if (scriptOutput.length() <= toSend.length()) {
			command.setFail(true);
			command.setErrorString("Script out shorter then command, command = " + toSend + ", out = " + scriptOutput);
			return;
		}
		Pattern p = Pattern.compile("\\<([^\\>]*)\\>", Pattern.DOTALL);
		Matcher m = p.matcher(scriptOutput.substring(toSend.length(), scriptOutput.length()));
		
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars Stdout from: " + scriptOutput);
			return;
		}
		command.setStdOut(m.group(1));
		
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars return value from: " + scriptOutput);
			return;
		}
		command.setReturnValue(m.group(1));
		
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars error code from: " + scriptOutput);
			return;
		}
		command.setErrorCode(m.group(1));
		
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars error string from: " + scriptOutput);
			return;
		}
		command.setErrorString(m.group(1));
		
		if (!command.getStdOut().equalsIgnoreCase("0")) {
			command.setFail(true);
			command.setErrorString(command.getReturnValue());
		}
		
	}
	
	@Override
	public void exit() {
		try {
			if (in != null) {
				in.write(("exit" + ENTER).getBytes());
				in.flush();
			}
		} catch (IOException ignore) {

		}
	}
	
	@Override
	public void close() {
		super.close();
	}
	
	public void source(String fileName) throws IOException {
		
		ShellCommand cmd = new ShellCommand("source", fileName.replace('\\', '/'));
		executeCommand(cmd);
		if (cmd.isFail()) {
			throw new IOException(cmd.getErrorString().split("[\\n\\r]")[0]);
		}
	}
	
	@Override
	public void handleCliCommand(String title, CliCommand command) throws Exception {
		
		conn.command(command);
		
		String name = getXPath().replaceAll("/sut/", "") + ": ";
		
		setTestAgainstObject(command.getResult());
		if (command.isFailed() && (!command.isIgnoreErrors()) && (!forceIgnoreAnyErrors)) {
			report.report(name + title + ", " + command.getFailCause(), command.getResult(), false);
			Exception e = command.getThrown();
			if (e != null) {
				throw e;
			}
			throw new Exception("Cli command failed");
		}
		
		if (!command.isSilent())
			report.report(name + title, command.getResult(), true);
		
		if (command.isIgnoreErrors() || (forceIgnoreAnyErrors)) {
			;
		} else {
			AnalyzerParameter[] analyzers = command.getAnalyzers();
			if (analyzers != null) {
				for (int i = 0; i < analyzers.length; i++) {
					analyze(analyzers[i], true);
				}
			}
		}
		forceIgnoreAnyErrors = false;
	}
	
	@Override
	public void source(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void source(InputStream stream, Properties p) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void source(InputStream stream, HashMap<String[], String> entryMap) throws IOException {

	}
	
}
