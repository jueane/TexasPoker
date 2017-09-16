package entity;

import java.util.Date;

public class MemberInfo extends BaseEntity {
	private String username;
	private String password;
	private String email;
	private String phone;
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
	private String friendIdList;
	private String missionList;
	private boolean verified;
	private String token;
	private int portrait;
	private Date lastLoginDate;

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

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
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

	public byte[] getMaxCards() {
		return maxCards;
	}

	public void setMaxCards(byte[] maxCards) {
		this.maxCards = maxCards;
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

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

}
