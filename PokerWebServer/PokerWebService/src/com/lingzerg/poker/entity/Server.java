package com.lingzerg.poker.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Server extends BaseEntity {
	private int type;
	@Column(length = 64)
	private String ip;
	private int port;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
