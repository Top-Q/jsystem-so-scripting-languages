package tests.perl;

import java.util.Properties;

import jsystem.sysobj.scripting.perl.PerlCommand;
import jsystem.sysobj.scripting.perl.PerlShell;
import junit.framework.SystemTestCase;

import com.aqua.sysobj.conn.CliConnectionImpl;

public class PerlTest extends SystemTestCase {
	CliConnectionImpl conn = null;
	
	PerlShell shell;
	
	@Override
	public void setUp() throws Exception {
		conn = new WindowsCliConnection();
		conn.setHost("127.0.0.1");
		conn.setUser("aqua");
		conn.setPassword("hgk1!tnhr");
		// conn.setDump(true);
		conn.setUseTelnetInputStream(true);
		
		shell = new PerlShell(conn, "c:\\perl\\bin");
		shell.launch();
	}
	
	public void testBasicCommand() throws Exception {
		shell.executeBasic("sub myPrint { print \"$_[0]\";}");
		PerlCommand cmd = new PerlCommand();
		cmd.setCommand("&myPrint");
		cmd.addParam("Hello");
		shell.executePerlCommand(cmd);
		System.out.println("\n" + cmd.toFullString());
	}
	
	public void testParms() {
		Properties p = new Properties();
		p.setProperty("DDD", "1");
		p.setProperty("fff", "2");
		Object[] o = new Object[] { p, "value3" };
		System.out.println(PerlCommand.getParamAsPerlString(o));
	}
}
