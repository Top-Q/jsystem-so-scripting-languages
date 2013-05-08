/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.sysobj.scripting.perl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

public class PerlCommand {
	private String command;
	
	private ArrayList<Object> params = new ArrayList<Object>();
	
	private String result;
	
	private String returnValue;
	
	private String errorString;
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public String getReturnValue() {
		return returnValue;
	}
	
	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	
	public String getErrorString() {
		return errorString;
	}
	
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}
	
	public void addParam(Object param) {
		params.add(param);
	}
	
	public void addParam(Object... params) {
		for (Object o : params) {
			addParam(o);
		}
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
		return command + getParamAsPerlString(params.toArray()) + ";";
	}
	
	public String toFullString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Command: ");
		buf.append(toString());
		buf.append("\n");
		buf.append("Return: ");
		buf.append(getReturnValue());
		buf.append("\n");
		buf.append("Error: ");
		buf.append(getErrorString());
		buf.append("\n");
		return buf.toString();
	}
	
	public static String getParamAsPerlString(Object param) {
		if (param instanceof String) {
			return "\"" + param + "\"";
		} else if (param instanceof Properties) {
			Properties p = (Properties) param;
			ArrayList<Object> list = new ArrayList<Object>();
			Enumeration<Object> keys = p.keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				list.add(key);
				list.add(p.get(key));
			}
			return getParamAsPerlString(list.toArray());
		} else if (param instanceof Object[]) {
			Object[] params = (Object[]) param;
			StringBuffer buf = new StringBuffer();
			buf.append("(");
			for (int i = 0; i < params.length; i++) {
				if (i != 0) {
					buf.append(", ");
				}
				buf.append(getParamAsPerlString(params[i]));
			}
			buf.append(")");
			return buf.toString();
		} else if (param instanceof ArrayList<?>) {
			ArrayList<?> al = (ArrayList<?>) param;
			return getParamAsPerlString(al.toArray());
		} else if (param instanceof Integer) {
			return getParamAsPerlString(param.toString());
		} else if (param instanceof Long) {
			return getParamAsPerlString(param.toString());
		} else {
			return getParamAsPerlString(param.toString());
		}
	}
}
