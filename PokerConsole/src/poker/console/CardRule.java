package poker.console;

import java.util.Random;

public class CardRule {
	// 高位4位1,2,3,4对应“黑红梅方"，低位4位对应纸牌的大小，从2到E。为方便比较大小，E代表牌中的"A"。
	protected byte[] cardList;
	protected int[] positions;
	protected int remain = 0; // 未发牌数量

	public CardRule() {
		init();
	}

	protected void init() {
		cardList = new byte[] { 0x12, 0x22, 0x32, 0x42, 0x13, 0x23, 0x33, 0x43, 0x14, 0x24, 0x34, 0x44, 0x15, 0x25, 0x35, 0x45, 0x16, 0x26, 0x36, 0x46, 0x17, 0x27, 0x37, 0x47, 0x18, 0x28, 0x38, 0x48,
				0x19, 0x29, 0x39, 0x49, 0x1a, 0x2a, 0x3a, 0x4a, 0x1b, 0x2b, 0x3b, 0x4b, 0x1c, 0x2c, 0x3c, 0x4c, 0x1d, 0x2d, 0x3d, 0x4d, 0x1e, 0x2e, 0x3e, 0x4e };
		positions = new int[cardList.length];
	}

	public CardRule(byte[] cards) {
		if (cards == null) {
			System.err.println("Cards is null.");
		} else {
			cardList = cards;
		}
		positions = new int[cardList.length];
	}

	public static void main(String[] args) {
		byte[] cardstttt = new byte[] { 0x3a, 0x1a };
		showCards(cardstttt);
		new CardRule().shuffle();
	}

	public void shuffle() {
		Random random = new Random();
		remain = 0;
		int cardCount = cardList.length;
		while (remain < cardCount) {
			int tmpInt = Math.abs(random.nextInt()) % cardCount;
			boolean repeat = false;
			for (int i = 0; i < remain; i++) {
				if (positions[i] == tmpInt) {
					repeat = true;
					break;
				}
			}
			if (!repeat) {
				positions[remain++] = tmpInt;
			}
		}
	}

	public byte getCard() {
		if (remain <= 0) {
			shuffle();
		}
		return cardList[positions[--remain]];
	}

	public byte[] getCards(int count) {
		byte[] cards = new byte[count];
		for (int i = 0; i < count; i++) {
			cards[i] = getCard();
		}
		return cards;
	}

	private static void showCard(byte card) {
		int color = (card >> 4) & 0xf;
		if (color == 1) {
			System.out.print("黑");
		} else if (color == 2) {
			System.out.print("红");
		} else if (color == 3) {
			System.out.print("梅");
		} else if (color == 4) {
			System.out.print("方");
		}
		int a = card & 0xf;
		if (a == 14) {
			System.out.print("A");
		} else {
			System.out.print("" + a);
		}
	}

	public static void showCards(byte[] cards) {
		for (int i = 0; i < cards.length; i++) {
			showCard(cards[i]);
			if (i < cards.length - 1) {
				System.out.print("，");
			}
		}
		System.out.print("\r\n");
	}
}
