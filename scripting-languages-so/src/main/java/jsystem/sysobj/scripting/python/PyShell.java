package jsystem.sysobj.scripting.python;

import jsystem.framework.system.SystemObjectImpl;
import systemobject.terminal.Prompt;

import com.aqua.stations.StationDefaultImpl;
import com.aqua.stations.StationsFactory;
import com.aqua.sysobj.conn.CliApplication;
import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CliConnection;

public class PyShell extends SystemObjectImpl {
	
	private StationDefaultImpl station;
	
	private CliApplication cliApplication;
	
	private CliConnection cliConnection;
	
	private String worstation;
	
	private String os;
	
	private String user;
	
	private String password;
	
	private StringBuffer logFileS = null;
	
	private Prompt[] pyPrompts = new Prompt[1];
	
	@Override
	public void init() throws Exception {
		
		super.init();
		
		pyPrompts[0] = new Prompt();
		pyPrompts[0].setPrompt(">>> ");
		pyPrompts[0].setCommandEnd(true);
		
		station = StationsFactory.createStation(getWorstation(), getOs(), getOs().equals("linux") ? "ssh" : "telnet", getUser(),
				getPassword(), null);
		station.init();
		cliApplication = station.getCliSession(false);
		cliConnection = cliApplication.getConnectivityManager().getCli();
		cliConnection.setPrompts(pyPrompts);
		
		logFileS = new StringBuffer();
		
	}
	
	@Override
	public void close() {
		station.close();
		super.close();
	}
	
	public String handleCliCommand(String command) throws Exception {
		return handleCliCommand(command, null);
	}
	
	public String handleCliCommand(String command, String prompt) throws Exception {
		logFileS.append(command + "<br>");
		CliCommand cmd = new CliCommand(command);
		cmd.addErrors("Error");
		if (prompt != null) {
			cmd.setPromptString(prompt);
		}
		cliConnection.handleCliCommand(command, cmd);
		return cmd.getResult();
	}
	
	public String handleCliCommandSend(String command) throws Exception {
		logFileS.append(command + "<br>");
		cliConnection.sendString(command + "\r", false);
		report.report(command, command, true);
		return "";
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getWorstation() {
		return worstation;
	}
	
	public void setWorstation(String worstation) {
		this.worstation = worstation;
	}
	
	public String getOs() {
		return os;
	}
	
	public void setOs(String os) {
		this.os = os;
	}
	
	public StringBuffer getLogFileS() {
		return logFileS;
	}
	
}
