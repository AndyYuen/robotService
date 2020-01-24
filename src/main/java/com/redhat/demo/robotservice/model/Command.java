package com.redhat.demo.robotservice.model;

public class Command {
	private String robotName;
	private String CmdString;
	
	public String getRobotName() {
		return robotName;
	}
	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}

	public String getCmdString() {
		return CmdString;
	}
	public void setCmdString(String cmdString) {
		CmdString = cmdString;
	}

}
