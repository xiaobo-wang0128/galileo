package org.armada.galileo.rainbow_gate.transfer.domain;

import java.io.Serializable;


public class SftpAuth implements Serializable {

	private String sftpHost;

	private int sftpPort;

	private String sftpUsername;

	private String sftpPassword;

	public SftpAuth() {
	}

	public SftpAuth(String sftpHost, int sftpPort, String sftpUsername, String sftpPassword) {
		this.sftpHost = sftpHost;
		this.sftpPort = sftpPort;
		this.sftpUsername = sftpUsername;
		this.sftpPassword = sftpPassword;
	}

	public String getConnectionString() {
		return "[host=" + sftpHost + ", port=" + sftpPort + ", uname=" + sftpUsername + ", password=" + sftpPassword + "]";
	}

	public String getConnectionHidden() {
		return "[host=" + sftpHost + ", port=" + sftpPort + ", uname=***, password=*** ]";
	}

	
	public String getSftpHost() {
		return sftpHost;
	}

	public void setSftpHost(String sftpHost) {
		this.sftpHost = sftpHost;
	}

	public int getSftpPort() {
		return sftpPort;
	}

	public void setSftpPort(int sftpPort) {
		this.sftpPort = sftpPort;
	}

	public String getSftpUsername() {
		return sftpUsername;
	}

	public void setSftpUsername(String sftpUsername) {
		this.sftpUsername = sftpUsername;
	}

	public String getSftpPassword() {
		return sftpPassword;
	}

	public void setSftpPassword(String sftpPassword) {
		this.sftpPassword = sftpPassword;
	}

}
