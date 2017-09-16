package poker.entity;

/*
 * Description:无
 * Author:je
 * Date:2015年1月26日
 */
public class KnockoutProfitInfo extends BaseEntity {
	private int memberId;
	private int rommLevel;
	private int entryFee;
	private int reward;

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getRommLevel() {
		return rommLevel;
	}

	public void setRommLevel(int rommLevel) {
		this.rommLevel = rommLevel;
	}

	public int getEntryFee() {
		return entryFee;
	}

	public void setEntryFee(int entryFee) {
		this.entryFee = entryFee;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

}
