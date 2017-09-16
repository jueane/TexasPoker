package com.lingzerg.poker.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Recharge extends BaseEntity {
	
	
	@ManyToOne(targetEntity = Member.class)
	@JoinColumn(name = "memberId", insertable = false, updatable = false, nullable = false)
	private Member member; //会员
	
	private int memberId;
	
	private int count;
	// 1 充值, 2 游戏内, 3 活动, 4 苹果机, 5 发牌女郎, 6 表情, 7 评论奖励, 8 首日登陆奖励, 9 邀请好友
	private int sourceType;
	
	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}
	
	

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

}
