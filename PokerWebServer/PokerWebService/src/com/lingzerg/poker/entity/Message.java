package com.lingzerg.poker.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Message extends BaseEntity {
	private int sendId;
	private int receiveId;
	private String content;// 客户端140
	private boolean hasRead;

	@ManyToOne(targetEntity = Member.class)
	@JoinColumn(name = "sendId", insertable = false, updatable = false, nullable = false)
	private Member sendMember;

	@ManyToOne(targetEntity = Member.class)
	@JoinColumn(name = "receiveId", insertable = false, updatable = false, nullable = false)
	private Member receiveMember;

	public int getSendId() {
		return sendId;
	}

	public void setSendId(int sendId) {
		this.sendId = sendId;
	}

	public int getReceiveId() {
		return receiveId;
	}

	public void setReceiveId(int receiveId) {
		this.receiveId = receiveId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Member getSendMember() {
		return sendMember;
	}

	public void setSendMember(Member sendMember) {
		this.sendMember = sendMember;
	}

	public Member getReceiveMember() {
		return receiveMember;
	}

	public void setReceiveMember(Member receiveMember) {
		this.receiveMember = receiveMember;
	}

	public boolean isHasRead() {
		return hasRead;
	}

	public void setHasRead(boolean hasRead) {
		this.hasRead = hasRead;
	}

}
