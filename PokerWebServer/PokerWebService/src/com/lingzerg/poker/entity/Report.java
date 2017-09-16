package com.lingzerg.poker.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Report extends BaseEntity {
	
	@ManyToOne(targetEntity = Member.class)
	@JoinColumn(name = "memberId", insertable = false, updatable = false, nullable = false)
	private Member member; //会员
	
	private int memberId;
	private String content;
	
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
	public int getMemberId() {
		return memberId;
	}
	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
