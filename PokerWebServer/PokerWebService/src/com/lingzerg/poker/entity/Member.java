package com.lingzerg.poker.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Member extends BaseEntity {

	@Column(length = 32)
	private String username;
	@Column(length = 32)
	private String password;
	@Column(length = 32)
	private String email;
	@Column(length = 32)
	private String phone;
	private String phoneToValidate;
	private String phoneValidationCode;
	@Column(length = 64)
	private String openId;
	private boolean male;
	private Date birthday;
	private String nickname;
	private String sign;
	private int gold;
	private int gem;
	private int maxTotalGold;
	private int maxScore;
	private int winTimes;
	private int loseTimes;
	private byte[] maxCards;
	private int maxCardsValue;
	@Column(length = 2048)
	private String friendIdList;
	@Column(length = 2048)
	private String missionList;
	private boolean verified;
	private String token;
	private int portrait;
	private int portraitBorder;
	private Date lastLoginDate;
	private int status;// 用户状态 0.无。 1 手机号码已验证, 2 封号用户

	public String getPhoneToValidate() {
		return phoneToValidate;
	}

	public void setPhoneToValidate(String phoneToValidate) {
		this.phoneToValidate = phoneToValidate;
	}

	public String getPhoneValidationCode() {
		return phoneValidationCode;
	}

	public void setPhoneValidationCode(String phoneValidationCode) {
		this.phoneValidationCode = phoneValidationCode;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getMaxCardsValue() {
		return maxCardsValue;
	}

	public void setMaxCardsValue(int maxCardsValue) {
		this.maxCardsValue = maxCardsValue;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getGem() {
		return gem;
	}

	public void setGem(int gem) {
		this.gem = gem;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public int getMaxTotalGold() {
		return maxTotalGold;
	}

	public void setMaxTotalGold(int maxTotalGold) {
		this.maxTotalGold = maxTotalGold;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public int getWinTimes() {
		return winTimes;
	}

	public void setWinTimes(int winTimes) {
		this.winTimes = winTimes;
	}

	public int getLoseTimes() {
		return loseTimes;
	}

	public void setLoseTimes(int loseTimes) {
		this.loseTimes = loseTimes;
	}

	public String getFriendIdList() {
		return friendIdList;
	}

	public void setFriendIdList(String friendIdList) {
		this.friendIdList = friendIdList;
	}

	public String getMissionList() {
		return missionList;
	}

	public void setMissionList(String missionList) {
		this.missionList = missionList;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getPortrait() {
		return portrait;
	}

	public void setPortrait(int portrait) {
		this.portrait = portrait;
	}

	public int getPortraitBorder() {
		return portraitBorder;
	}

	public void setPortraitBorder(int portraitBorder) {
		this.portraitBorder = portraitBorder;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public byte[] getMaxCards() {
		return maxCards;
	}

	public void setMaxCards(byte[] maxCards) {
		this.maxCards = maxCards;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

}