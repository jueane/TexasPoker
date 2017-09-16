package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class Reply extends BaseEntity {
	private int messageId;
	private boolean response;
	private String content;

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}

}
