/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.perl;

import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CliConnection;

/**
 * represent a perl shell. this shell will be executed on a remote remote machine. The shell constractor receive the
 * shell cli connection.
 * <p>
 * The launch method is used to launch the perl using the launch command: perl -de 42.
 * <p>
 * To execute shell command you can use 2 methods: executePerlCommand and executeCommand.
 * <p>
 * 
 * @author guy.arieli
 */
public class PerlShell {
	protected CliConnection connection;
	protected String perlDir;
	protected String perlLaunchCommand = "perl -de 42";

	public PerlShell(CliConnection connection, String perlDir) {
		this.connection = connection;
		this.perlDir = perlDir;
	}

	public void launch() throws Exception {
		if (!connection.isConnected()) {
			connection.connect();
		}
		CliCommand cmd = new CliCommand();
		cmd.setCommands(new String[] { "cd " + perlDir });
		connection.command(cmd);
		cmd = new CliCommand();
		cmd.setCommands(new String[] { perlLaunchCommand });
		connection.command(cmd);

	}

	/**
	 * Execute perl command.
	 * 
	 * @param cmd
	 * @throws Exception
	 */
	public void executePerlCommand(PerlCommand cmd) throws Exception {
		String result = executeCommand(cmd.toString());
		cmd.setResult(result);
		int retStart = result.lastIndexOf("RES_START**");
		int retEnd = result.lastIndexOf("**RES_END");
		int evalStart = result.lastIndexOf("EVL_START**");
		int evalEnd = result.lastIndexOf("**EVL_END");
		if (retStart >= 0 && retEnd >= 0) {
			cmd.setReturnValue(result.substring(retStart + "RES_START**".length(), retEnd));
		}
		if (evalStart >= 0 && evalEnd >= 0) {
			cmd.setErrorString((result.substring(evalStart + "EVL_START**".length(), evalEnd)));
		}
	}

	public void executeBasic(String command) {
		CliCommand cliCommand = new CliCommand();
		cliCommand.setCommands(new String[] { command });
		connection.command(cliCommand);
	}

	public String executeCommand(String command) throws Exception {
		CliCommand cliCommand = new CliCommand();
		cliCommand.setCommands(new String[] { "eval { $res = " + command + " } ;",
				"print \"RES_START**$res**RES_END\"", "print \"EVL_START**$@**EVL_END\"" });
		connection.command(cliCommand);

		if (cliCommand.isFailed()) {
			Exception e = cliCommand.getThrown();
			if (e != null) {
				throw e;
			}
			throw new Exception("Cli command failed");
		}

		return cliCommand.getResult();
	}
}
