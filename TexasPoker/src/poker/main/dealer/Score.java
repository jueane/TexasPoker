package poker.main.dealer;

import poker.main.player.Player;

public class Score {
	public int id;
	public Player player;
	public byte[] cards;
	public int[] values;// 牌值
	public int[] colors;// 花色
	public int type;// 牌型
	public String title;
	public int value;// FFFFFFFF，第7个F表示牌型。前5个F表示值
	public byte[] maxCards = new byte[5];// 最大牌组合
	public boolean won;
}
