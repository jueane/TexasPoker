package poker.main.room;

public class Phase {
	public static final int WAITING = 0;// 未开始
	public static final int INITING = 1;// 初始化中
	public static final int BETTING = 2;// 下注中
	public static final int DATA_PERSISTENT = 3;// 结算中
	public static final int READY_FOR_NEXT = 4;// 准备下一局
}
