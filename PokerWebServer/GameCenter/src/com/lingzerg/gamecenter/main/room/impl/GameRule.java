package com.lingzerg.gamecenter.main.room.impl;

import java.util.ArrayList;
import java.util.List;

import com.lingzerg.gamecenter.main.player.Player;
import com.lingzerg.gamecenter.main.player.PlayerStatus;
import com.lingzerg.gamecenter.util.JLog;

public class GameRule {

	class PlayersCards {
		int id;
		Player player;
		String title;
		byte[] cards;
		int[] values;// 牌值
		int[] colors;// 花色
		int type;// 牌型
		int value;// FFFFFFFF，第7个F表示牌型。前5个F表示值
		byte[] maxCards = new byte[5];// 最大牌组合
		boolean won;
	}

	private List<PlayersCards> playersCardsList = new ArrayList<>();

	public int getPlayerCount() {
		int count = 0;
		if (playersCardsList != null) {
			count = playersCardsList.size();
		}
		return count;
	}

	public static void main(String[] argStrings) {
		GameRule gameRule = new GameRule();
		System.out.println("begin!");
		byte[] cards = new byte[] { 0x4e, 0x2e, 0x48, 0x28, 0x26, 0x44, 0x04 };
		CardRule.showCards(cards);
		System.out.println("排序");
		gameRule.add(0, null, cards);
		CardRule.showCards(gameRule.playersCardsList.get(0).cards);
		System.out.println(gameRule.playersCardsList.get(0).title);
		CardRule.showCards(gameRule.playersCardsList.get(0).maxCards);

		gameRule.compare();
	}

	public List<PlayersCards> compare() {
		int len = playersCardsList.size();
		if (len <= 0) {
			JLog.infoln("PlayerCards is less than 1.");
			return playersCardsList;
		}
		for (int i = 0; i < len - 1; i++) {
			for (int j = i; j < len; j++) {
				if (playersCardsList.get(i).value < playersCardsList.get(j).value && playersCardsList.get(j).player.getStatus() != PlayerStatus.FOLD) {
					PlayersCards playersCardsTmp = new PlayersCards();
					playersCardsTmp = playersCardsList.get(i);
					playersCardsList.set(i, playersCardsList.get(j));
					playersCardsList.set(j, playersCardsTmp);
				}
			}
		}
		// 胜利玩家列表
		int maxValue = playersCardsList.get(0).value;
		for (int i = 0; i < len; i++) {
			if (maxValue == playersCardsList.get(i).value && playersCardsList.get(i).player.getStatus() != PlayerStatus.FOLD) {
				playersCardsList.get(i).won = true;
			}
		}
		return playersCardsList;
	}

	public int add(int id, Player player, byte[] cards) {
		if (cards.length < 5) {
			JLog.infoln("cards length is less than 5.");
			return 0;
		}
		int[] values = new int[cards.length];
		// 取牌值
		for (int i = 0; i < values.length; i++) {
			values[i] = getValue(cards[i]);
		}
		// 排序……牌值序
		for (int i = 0; i < cards.length - 1; i++) {
			for (int j = i + 1; j < cards.length; j++) {
				if (values[i] < values[j]) {
					// 排序——值
					int tmpValue = values[i];
					values[i] = values[j];
					values[j] = tmpValue;
					// 排序——牌
					byte tmpByte = cards[i];
					cards[i] = cards[j];
					cards[j] = tmpByte;
				}
			}
		}
		// 加入list
		PlayersCards playersCards = new PlayersCards();
		playersCards.id = id;
		playersCards.player = player;
		playersCards.cards = cards;
		playersCards.values = values;
		playersCardsList.add(playersCards);
		// 计算牌型
		if (royalFlush(playersCards)) {
		} else if (straightFlush(playersCards)) {
		} else if (fourOfKind(playersCards)) {
		} else if (fullHouse(playersCards)) {
		} else if (flush(playersCards)) {
		} else if (straight(playersCards)) {
		} else if (threeOfKind(playersCards)) {
		} else if (twoPairs(playersCards)) {
		} else if (onePair(playersCards)) {
		} else {
			highCard(playersCards);
		}
		return 1;
	}

	private int getValue(byte card) {
		int value = (int) (card & 0xf);
		return value;
	}

	private int getColor(byte card) {
		int color = ((int) (card >> 4) & 0xf);
		return color;
	}

	// 10. 皇家同花顺……最高为Ace（一点）的同花顺。(一种）
	private boolean royalFlush(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		List<byte[]> resultList = new ArrayList<>();
		int len = cards.length;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len && j != i; j++) {
				for (int k = 0; k < len && k != i && k != j; k++) {
					for (int l = 0; l < len && l != i && l != j && l != k; l++) {
						for (int m = 0; m < len && m != i && m != j && m != k && m != l; m++) {
							byte[] newCards = new byte[] { cards[i], cards[j], cards[k], cards[l], cards[m] };
							orderDesc(newCards);
							if (getValue(newCards[0]) == 14 && getValue(newCards[1]) == 13 && getValue(newCards[2]) == 12 && getValue(newCards[3]) == 11 && getValue(newCards[4]) == 10) {
								int color = getColor(newCards[0]);
								if (color == getColor(newCards[1]) && color == getColor(newCards[2]) && color == getColor(newCards[3]) && color == getColor(newCards[4])) {
									resultList.add(newCards);
									playersCards.maxCards = newCards;
									playersCards.value = 0x0a000000;
									playersCards.title = "皇家同花顺";
									playersCards.type = 10;
									return true;
								}
							}

						}
					}
				}
			}
		}
		return false;
	}

	// 9. 同花顺……同一花色，顺序的牌。（9种）
	private boolean straightFlush(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		List<byte[]> resultList = new ArrayList<>();
		int len = cards.length;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len && j != i; j++) {
				for (int k = 0; k < len && k != i && k != j; k++) {
					for (int l = 0; l < len && l != i && l != j && l != k; l++) {
						for (int m = 0; m < len && m != i && m != j && m != k && m != l; m++) {
							byte[] newCards = new byte[] { cards[i], cards[j], cards[k], cards[l], cards[m] };
							orderDesc(newCards);
							if (getValue(newCards[0]) == getValue(newCards[1]) + 1 && getValue(newCards[0]) == getValue(newCards[2]) + 2 && getValue(newCards[0]) == getValue(newCards[3]) + 3 && getValue(newCards[0]) == getValue(newCards[4]) + 4) {
								int color = getColor(newCards[0]);
								if (color == getColor(newCards[1]) && color == getColor(newCards[2]) && color == getColor(newCards[3]) && color == getColor(newCards[4])) {
									resultList.add(newCards);
									System.out.println("同花顺");
								} else {
									// System.out.println("顺子");
								}
							} else if (getValue(newCards[0]) == 14 && getValue(newCards[1]) == 5 && getValue(newCards[2]) == 4 && getValue(newCards[3]) == 3 && getValue(newCards[4]) == 2) {
								// 将A放置最后
								byte tmpBt = newCards[0];
								newCards[0] = newCards[1];
								newCards[1] = newCards[2];
								newCards[2] = newCards[3];
								newCards[3] = newCards[4];
								newCards[4] = tmpBt;

								int color = getColor(newCards[0]);
								if (color == getColor(newCards[1]) && color == getColor(newCards[2]) && color == getColor(newCards[3]) && color == getColor(newCards[4])) {
									resultList.add(newCards);
									System.out.println("同花顺……最小");
								} else {
								}
							}

						}
					}
				}
			}
		}
		if (resultList.size() > 0) {
			for (int i = 0; i < resultList.size() - 1; i++) {
				for (int j = i; j < resultList.size(); j++) {
					if (getValue(resultList.get(i)[0]) < getValue(resultList.get(j)[0])) {
						byte[] tmpBytes = resultList.get(i);
						resultList.set(i, resultList.get(j));
						resultList.set(j, tmpBytes);
					}
				}
			}
			playersCards.maxCards = resultList.get(0);
			playersCards.value = 0x09000000 | getValue(playersCards.maxCards[0]);
			playersCards.title = "同花顺";
			playersCards.type = 9;
			return true;
		}

		return false;
	}

	// 8. 四条……“四张”或“炸弹"。有四张同一点数的牌。（13种）
	private boolean fourOfKind(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		int[] values = playersCards.values;
		for (int i = 0; i < values.length - 3; i++) {
			if (values[i] == values[i + 1] && values[i + 1] == values[i + 2] && values[i + 2] == values[i + 3]) {
				// 获取最大单牌
				byte maxSingleCard = 0x12;
				for (int j = 0; j < values.length; j++) {
					if (values[j] != values[i]) {
						maxSingleCard = cards[j];
						break;
					}
				}
				playersCards.maxCards[0] = cards[i];
				playersCards.maxCards[1] = cards[i + 1];
				playersCards.maxCards[2] = cards[i + 2];
				playersCards.maxCards[3] = cards[i + 3];
				playersCards.maxCards[4] = maxSingleCard;
				playersCards.value = 0x08000000 | values[i];
				playersCards.title = "四条";
				playersCards.type = 8;
				return true;
			}
		}
		return false;
	}

	// 7. 满堂红（亦称“葫芦”）：三张同一点数的牌，加一对其他点数的牌。（13种）
	private boolean fullHouse(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		int[] values = playersCards.values;
		boolean isThreeOfKind = false;
		boolean isPair = false;
		int valueOfThree = 0;
		// 是否三条
		for (int i = 0; i < values.length - 2; i++) {
			if (values[i] == values[i + 1] && values[i + 1] == values[i + 2]) {
				valueOfThree = values[i];
				playersCards.maxCards[0] = cards[i];
				playersCards.maxCards[1] = cards[i + 1];
				playersCards.maxCards[2] = cards[i + 2];
				isThreeOfKind = true;
				break;
			}
		}
		// 是否有不等于三条值的一对。
		for (int i = 0; i < values.length - 1; i++) {
			if (values[i] == values[i + 1] && values[i] != valueOfThree) {
				playersCards.maxCards[3] = cards[i];
				playersCards.maxCards[4] = cards[i + 1];
				isPair = true;
				break;
			}
		}
		// 结果
		if (isThreeOfKind && isPair) {
			int retValue = 0x07000000 | valueOfThree;
			playersCards.value = retValue;
			playersCards.title = "满堂红";
			playersCards.type = 7;
			return true;
		}
		return false;
	}

	// 6. 同花……五张同一花色的牌。（13的5次方）
	private boolean flush(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		byte[] cardsNew = new byte[cards.length];
		for (int i = 0; i < cards.length; i++) {
			cardsNew[i] = cards[i];
		}
		// 排序……花色序
		for (int i = 0; i < cardsNew.length - 1; i++) {
			for (int j = i + 1; j < cardsNew.length; j++) {
				if (getColor(cardsNew[i]) < getColor(cardsNew[j])) {
					byte tmpCard = cardsNew[i];
					cardsNew[i] = cardsNew[j];
					cardsNew[j] = tmpCard;
				}
			}
		}
		int color = 0;// 同花的花色
		for (int i = 0; i < cardsNew.length - 4; i++) {
			if (getColor(cardsNew[i]) == getColor(cardsNew[i + 1]) && getColor(cardsNew[i + 1]) == getColor(cardsNew[i + 2]) && getColor(cardsNew[i + 2]) == getColor(cardsNew[i + 3]) && getColor(cardsNew[i + 3]) == getColor(cardsNew[i + 4])) {
				color = getColor(cardsNew[i]);
				break;
			}
		}
		if (color > 0) {
			List<Byte> byteList = new ArrayList<>();
			for (int i = 0; i < cards.length; i++) {
				if (getColor(cards[i]) == color) {
					byteList.add(cards[i]);
				}
			}
			int value = (int) 0x06000000;
			value |= getValue(byteList.get(0)) << 16;
			value |= getValue(byteList.get(1)) << 12;
			value |= getValue(byteList.get(2)) << 8;
			value |= getValue(byteList.get(3)) << 4;
			value |= getValue(byteList.get(4));
			playersCards.value = value;
			for (int i = 0; i < 5; i++) {
				playersCards.maxCards[i] = byteList.get(i);
			}
			playersCards.title = "同花";
			playersCards.type = 6;
			return true;
		}

		return false;
	}

	// 5. 顺子……五张顺连的牌。
	private boolean straight(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		List<byte[]> resultList = new ArrayList<>();
		int len = cards.length;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len && j != i; j++) {
				for (int k = 0; k < len && k != i && k != j; k++) {
					for (int l = 0; l < len && l != i && l != j && l != k; l++) {
						for (int m = 0; m < len && m != i && m != j && m != k && m != l; m++) {
							byte[] newCards = new byte[] { cards[i], cards[j], cards[k], cards[l], cards[m] };
							orderDesc(newCards);
							if (getValue(newCards[0]) == getValue(newCards[1]) + 1 && getValue(newCards[0]) == getValue(newCards[2]) + 2 && getValue(newCards[0]) == getValue(newCards[3]) + 3 && getValue(newCards[0]) == getValue(newCards[4]) + 4) {
								resultList.add(newCards);
							} else if (getValue(newCards[0]) == 14 && getValue(newCards[1]) == 5 && getValue(newCards[2]) == 4 && getValue(newCards[3]) == 3 && getValue(newCards[4]) == 2) {
								// 将A放置最后
								byte tmpBt = newCards[0];
								newCards[0] = newCards[1];
								newCards[1] = newCards[2];
								newCards[2] = newCards[3];
								newCards[3] = newCards[4];
								newCards[4] = tmpBt;
								resultList.add(newCards);
							}

						}
					}
				}
			}
		}
		if (resultList.size() > 0) {
			for (int i = 0; i < resultList.size() - 1; i++) {
				for (int j = i; j < resultList.size(); j++) {
					if (getValue(resultList.get(i)[0]) < getValue(resultList.get(j)[0])) {
						byte[] tmpBytes = resultList.get(i);
						resultList.set(i, resultList.get(j));
						resultList.set(j, tmpBytes);
					}
				}
			}
			playersCards.maxCards = resultList.get(0);
			playersCards.value = 0x05000000 | getValue(playersCards.maxCards[0]);
			playersCards.title = "顺子";
			playersCards.type = 5;
			return true;
		}

		return false;
	}

	// 4. 三条……有三张同一点数的牌。
	private boolean threeOfKind(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		int[] values = playersCards.values;
		boolean isThreeOfKind = false;
		int valueOfThree = 0;
		for (int i = 0; i < values.length - 2; i++) {
			if (values[i] == values[i + 1] && values[i + 1] == values[i + 2]) {
				valueOfThree = values[i];
				playersCards.maxCards[0] = cards[i];
				playersCards.maxCards[1] = cards[i + 1];
				playersCards.maxCards[2] = cards[i + 2];
				playersCards.value = 0x04000000 | values[i];
				playersCards.title = "三条";
				playersCards.type = 4;
				isThreeOfKind = true;
			}
		}
		// 找出两张最大单牌
		List<Byte> twoSingleCards = new ArrayList<>();
		if (isThreeOfKind) {
			for (int i = 0; i < cards.length; i++) {
				if (values[i] != valueOfThree) {
					twoSingleCards.add(cards[i]);
					if (twoSingleCards.size() >= 2) {
						break;
					}
				}
			}
			playersCards.maxCards[3] = twoSingleCards.get(0);
			playersCards.maxCards[4] = twoSingleCards.get(1);
			return true;
		}
		return false;
	}

	// 3. 两对……两张相同点数的牌，加另外两张相同点数的牌。
	private boolean twoPairs(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		int[] values = playersCards.values;
		int pairCount = 0;
		int firstValueOfPair = 0;
		int secondValueOfPair = 0;
		for (int i = 0; i < values.length - 1; i++) {
			if (values[i] == values[i + 1]) {
				if (pairCount == 0) {
					firstValueOfPair = values[i];
					playersCards.maxCards[0] = cards[i];
					playersCards.maxCards[1] = cards[i + 1];
				} else if (pairCount == 1) {
					secondValueOfPair = values[i];
					playersCards.maxCards[2] = cards[i];
					playersCards.maxCards[3] = cards[i + 1];
				}
				pairCount++;
				i++;// 跳过一位
			}
		}
		if (pairCount >= 2) {// 有可能是三对
			// 取出除了两对外最大的单牌
			int singleCardValue = 0;
			for (int i = 0; i < values.length; i++) {
				if (values[i] != firstValueOfPair && values[i] != secondValueOfPair) {
					singleCardValue = values[i];
					playersCards.maxCards[4] = cards[i];
					break;
				}
			}

			int retValue = 0x03000000;
			retValue |= firstValueOfPair << 8;
			retValue |= secondValueOfPair << 4;
			retValue |= singleCardValue;
			playersCards.value = retValue;
			playersCards.title = "两对";
			playersCards.type = 3;
			return true;
		}
		return false;
	}

	// 2.一对……两张相同点数的牌。
	private boolean onePair(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		int[] values = playersCards.values;
		boolean isPair = false;
		int valueOfPair = 0;
		List<Byte> threeSingleCards = new ArrayList<>();
		for (int i = 0; i < values.length - 1; i++) {
			if (values[i] == values[i + 1]) {
				valueOfPair = values[i];
				isPair = true;
				playersCards.maxCards[0] = cards[i];
				playersCards.maxCards[1] = cards[i + 1];
				break;
			}
		}
		if (isPair) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != valueOfPair) {
					threeSingleCards.add(cards[i]);
					if (threeSingleCards.size() >= 3) {
						break;
					}
				}
			}
			playersCards.maxCards[2] = threeSingleCards.get(0);
			playersCards.maxCards[3] = threeSingleCards.get(1);
			playersCards.maxCards[4] = threeSingleCards.get(2);

			int retValue = 0x02000000;
			retValue |= valueOfPair << 12;
			retValue |= getValue(threeSingleCards.get(0)) << 8;
			retValue |= getValue(threeSingleCards.get(1)) << 4;
			retValue |= getValue(threeSingleCards.get(2));
			playersCards.value = retValue;
			playersCards.title = "一对";
			playersCards.type = 2;
			return true;
		}
		return false;
	}

	// 1. 高牌……不符合上面任何一种牌型的牌型，由单牌且不连续不同花的组成，以点数决定大小。
	private boolean highCard(PlayersCards playersCards) {
		byte[] cards = playersCards.cards;
		int[] values = playersCards.values;
		for (int i = 0; i < 5; i++) {
			playersCards.maxCards[i] = cards[i];
		}
		int retValue = 0x01000000;
		retValue |= values[0] << 16;
		retValue |= values[1] << 12;
		retValue |= values[2] << 8;
		retValue |= values[3] << 4;
		retValue |= values[4];
		playersCards.value = retValue;
		playersCards.title = "高牌";
		playersCards.type = 1;
		return true;
	}

	private void orderDesc(byte[] bytes) {
		if (bytes == null || bytes.length < 2) {
			System.out.println("组数长度不足2.");
			return;
		}
		for (int i = 0; i < bytes.length - 1; i++) {
			for (int j = i; j < bytes.length; j++) {
				if (getValue(bytes[i]) < getValue(bytes[j])) {
					byte tmpByte = bytes[i];
					bytes[i] = bytes[j];
					bytes[j] = tmpByte;
				}
			}
		}
	}
}
