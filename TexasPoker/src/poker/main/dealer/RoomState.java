package poker.main.dealer;

//对外暴露2种操作：锁定状态，查看状态，改变状态。
public class RoomState {

	private int phase;

	boolean isLocked;

	public int getPhase() {
		return phase;
	}

	public void waitting() {
		phase = Phase.WAITING;
	}

	public synchronized boolean initing() {
		if (isLocked) {
			phase = Phase.INITING;
			isLocked = false;
			return true;
		}
		return false;
	}

	public synchronized boolean betting() {
		if (isLocked) {
			phase = Phase.BETTING;
			isLocked = false;
			return true;
		}
		return false;
	}

	public synchronized boolean dataPersistent() {
		if (isLocked) {
			phase = Phase.DATA_PERSISTENT;
			isLocked = false;
			return true;
		}
		return false;
	}

	public synchronized boolean readyForNext() {
		if (isLocked) {
			phase = Phase.READY_FOR_NEXT;
			isLocked = false;
			return true;
		}
		return false;
	}

}
