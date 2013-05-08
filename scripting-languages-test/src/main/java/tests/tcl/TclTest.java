/*
 * Created on 08/05/2005
 */
package tests.tcl;

import java.io.File;
import java.io.IOException;

import jsystem.sysobj.scripting.tcl.ShellCommand;
import jsystem.sysobj.scripting.tcl.TclShell;
import jsystem.sysobj.scripting.tcl.TclShellLocal;
import junit.framework.SystemTestCase;

public class TclTest extends SystemTestCase {
	
	TclShell shell;
	
	@Override
	public void setUp() throws Exception {
		String shellPath = sut().getValue("/sut/tcl/shellPath/text()");
		shell = new TclShellLocal(new File(shellPath));
		shell.launch();
	}
	
	public void testRawShellCommad() throws Exception {
		shell.command("puts \"Hello World\"");
	}
	
	public void testPrintFlagsCommad() throws IOException {
		shell.setPrintCommand(false);
		shell.setPrintReturn(false);
		ShellCommand cmd = new ShellCommand("puts", "Hello World 1");
		shell.executeCommand(cmd);
	}
	
	public void testShellCommand() throws IOException {
		
		ShellCommand cmd = new ShellCommand();
		cmd.setCommand("puts");
		cmd.setParameters(new String[] { "Hello World 1" });
		shell.executeCommand(cmd);
		assertTrue("ShellCommand constructor with no parameters failed", cmd.getStdOut().indexOf("Hello World 1") >= 0);
		report.report("ShellCommand constructor with no parameters successfully completed");
		
		cmd = new ShellCommand("puts", "Hello World 2");
		shell.executeCommand(cmd);
		assertTrue("ShellCommand constructor with parameters failed", cmd.getStdOut().indexOf("Hello World 2") >= 0);
		report.report("ShellCommand constructor with parameters successfully completed");
		
	}
	
	@Override
	public void tearDown() {
		shell.exit();
	}
	
	public void testSetTitle() throws Exception {
		shell.setWindowTitle("hello world");
		Thread.sleep(1000 * 10);
	}
	
}
