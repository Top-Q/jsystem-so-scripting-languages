/*
 * Created on Mar 17, 2005
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.vbscript;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.sysobj.scripting.tcl.ShellCommand;
import jsystem.utils.FileUtils;

/**
 * @author guy.arieli
 */
public class VbShell {
	
	private static final String ENTER = "\n";
	
	// private String shellName;
	private File dir;
	
	//
	private Process p;
	
	private InputStream out;
	
	private InputStream err;
	
	private OutputStream in;
	
	private String prompt = "vbscript# ";
	
	private long timeout = 60000;
	
	private StringBuffer buffer;
	
	private String logFile = null;
	
	private boolean printCommand = true;
	
	private boolean printReturn = true;
	
	private String TEST_SCRIPT = "do while true"
			+ ENTER
			+ "   set ln = Nothing"
			+ ENTER
			+ "	ln = wscript.stdin.readline"
			+ ENTER
			+ "	if lcase(trim(ln)) = \"exit\" then exit do"
			+ ENTER
			+ "	on error resume next"
			+ ENTER
			+ "	err.clear"
			+ ENTER
			+ "   wscript.echo \"stdout: <\""
			+ ENTER
			+ "   'wscript.echo \"line: \" & ln"
			+ ENTER
			+ "   'if ln = Nothing then exit do"
			+ ENTER
			+ "	ExecuteGlobal ln"
			+ ENTER
			+ "   wscript.echo \">\" & vbCrLf"
			+ ENTER
			+ "   wscript.echo \"errorCode: <\""
			+ ENTER
			+ "   wscript.echo err.number"
			+ ENTER
			+ "   wscript.echo \">\" & vbCrLf"
			+ ENTER
			+ "   wscript.echo \"errorInfo: <\""
			+ ENTER
			+ "   wscript.echo err.description"
			+ ENTER
			+ "   wscript.echo \">\" & vbCrLf"
			+ ENTER
			+ "	wscript.stdout.write(\""
			+ prompt
			+ "\")"
			+ ENTER
			+ "loop"
			+ ENTER;
	
	public VbShell(File dir) {
		this.dir = dir;
	}
	
	/**
	 * Lanuch Tcl shell.
	 */
	public void launch() throws IOException {
		launch("main.vbs");
	}
	
	public void launch(String tmpFileName) throws IOException {
		File file = new File(dir, tmpFileName);
		extractToFile(TEST_SCRIPT, file);
		p = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/C", "cscript.exe", "main.vbs" }, null, dir);
		in = p.getOutputStream();
		err = p.getErrorStream();
		out = p.getInputStream();
	}
	
	/**
	 * Return command output
	 * 
	 * @return Command output
	 */
	public String getResults() {
		return buffer.toString();
	}
	
	/**
	 * Execute raw VB command.
	 * 
	 * @param command
	 *        The command to execute
	 * @return flag indicating weather command succedded or failed
	 * @throws IOException
	 */
	public synchronized boolean command(String command) throws IOException {
		
		buffer = new StringBuffer();
		
		// Write command to standard input
		in.write((command + ENTER).getBytes());
		in.flush();
		
		// Get command execution time
		long startTime = System.currentTimeMillis();
		int times = 0;
		while (true) {
			if (times == 50) {
				System.out.println("in loop : " + (System.currentTimeMillis() - startTime) + "\r\ntimeout =" + timeout);
				times = 0;
			} else {
				times++;
			}
			// if (errFound){
			// if (System.currentTimeMillis() - startTime > errTimeout){
			// throw new IOException("Timeout on vbscript std err");
			// }
			// } else {
			if (System.currentTimeMillis() - startTime > timeout) {
				throw new IOException("Timeout on vbscript");
			}
			// }
			
			int avail = err.available();
			while (avail > 0) {
				char c = (char) err.read();
				System.err.print(c);
				buffer.append(c);
				if (promptFound()) {
					return false;
				}
				avail--;
			}
			
			avail = out.available();
			while (avail > 0) {
				char c = (char) out.read();
				if (isPrintReturn()) {
					System.out.print(c);
				}
				buffer.append(c);
				if (promptFound()) {
					return true;
				}
				avail--;
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			
		}
		
	}
	
	public synchronized void executeCommand(ShellCommand command) {
		
		if (isPrintCommand()) {
			System.out.print("\n" + command.getCommand());
		}
		if (logFile != null) {
			try {
				FileUtils.append(logFile, "\n" + command.getCommand());
			} catch (Exception e) {
				command.setFail(true);
				command.setErrorString("Unable to write to log file: " + logFile);
				return;
			}
		}
		
		try {
			command(command.getCommand());
		} catch (IOException e) {
			command.setFail(true);
			command.setErrorString(e.getMessage());
			return;
		}
		
		String scriptOutput = getResults();
		Pattern p = Pattern.compile("\\<([^\\>]*)\\>", Pattern.MULTILINE);
		Matcher m = p.matcher(scriptOutput);
		
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars Stdout from: " + scriptOutput);
			return;
		}
		command.setStdOut(m.group(1).trim());
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars Stdout from: " + scriptOutput);
			return;
		}
		command.setErrorCode(m.group(1).trim());
		if (!m.find()) {
			command.setFail(true);
			command.setErrorString("Unable to pars Stdout from: " + scriptOutput);
			return;
		}
		command.setErrorString(m.group(1).trim());
		
		if (!command.getErrorCode().equalsIgnoreCase("0")) {
			command.setFail(true);
		}
		
	}
	
	public void exit() {
		try {
			if (in != null) {
				in.write(("WScript.Quit" + ENTER).getBytes());
				in.flush();
			}
		} catch (IOException ignore) {

		}
		try {
			in.close();
			out.close();
			err.close();
		} catch (Throwable t) {

		}
		p.destroy();
	}
	
	private boolean promptFound() {
		return buffer.toString().endsWith(prompt);
	}
	
	private boolean isScrallEnd(long timeout) {
		
		int avail = 0;
		try {
			avail = out.available() + err.available();
			if (avail > 4) {
				System.err.println("Prompt skip avail is: " + avail);
				return false;
			}
		} catch (IOException e) {
			return true;
		}
		try {
			Thread.sleep(timeout / 2);
		} catch (InterruptedException e) {}
		int avail2 = 0;
		try {
			avail2 = out.available() + err.available();
			if (avail2 == avail) {
				return true;
			}
		} catch (IOException e) {
			return true;
		}
		try {
			Thread.sleep(timeout / 2);
		} catch (InterruptedException e) {}
		int avail3 = 0;
		try {
			avail3 = out.available() + err.available();
			if (avail3 == avail2 && avail3 < 30) {
				return true;
			}
			System.err.println("Prompt skip avail3 is: " + avail3);
			return false;
		} catch (IOException e) {
			return true;
		}
	}
	
	public void close() {
		if (p != null) {
			exit();
			p.destroy();
		}
	}
	
	public void extractToFile(String script, File fName) throws IOException {
		FileWriter fw = new FileWriter(fName);
		fw.write(script);
		fw.flush();
		fw.close();
	}
	
	/**
	 * @return Returns the timeout.
	 */
	public long getTimeout() {
		return timeout;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.aqua.vbscript.RemoteVBShell#setTimeout(long)
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * @return Returns the logFile.
	 */
	public String getLogFile() {
		return logFile;
	}
	
	/**
	 * @param logFile
	 *        The logFile to set.
	 */
	public void setLogFile(String logFile) {
		this.logFile = logFile;
		try {
			FileUtils.write(logFile, "");
		} catch (Exception e) {}
	}
	
	public boolean isPrintCommand() {
		return printCommand;
	}
	
	public void setPrintCommand(boolean printCommand) {
		this.printCommand = printCommand;
	}
	
	public boolean isPrintReturn() {
		return printReturn;
	}
	
	public void setPrintReturn(boolean printReturn) {
		this.printReturn = printReturn;
	}
	
	public static void main(String[] args) {
		VbShell vb = new VbShell(new File(System.getProperty("user.dir")));
		try {
			vb.launch();
			
			ShellCommand sc = new ShellCommand("Set Word = CreateObject(\"Word.Application\")", (Object[]) null);
			vb.executeCommand(sc);
			System.out.println(sc.toString());
			sc = new ShellCommand("Word.Visible = True");
			vb.executeCommand(sc);
			System.out.println(sc.toString());
			sc = new ShellCommand("Word.Documents.Add");
			vb.executeCommand(sc);
			System.out.println(sc.toString());
			Thread.sleep(2000);
			sc = new ShellCommand("Word.Quit");
			vb.executeCommand(sc);
			System.out.println(sc.toString());
			vb.exit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vb.exit();
	}
}
