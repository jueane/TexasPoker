package poker.data;

import java.util.List;

import org.junit.Test;

import poker.data.RoomKnockoutData;
import poker.entity.RoomKnockoutInfo;

public class RoomKnockoutDataTest extends RoomKnockoutData {

	@Test
	public void test() {
		List<RoomKnockoutInfo> roomList = getRoomTypeList();

		for (int i = 0; i < roomList.size(); i++) {
			System.out.print("id:" + roomList.get(i).getId() + ",");
			System.out.print("entryFee:" + roomList.get(i).getEntryFee() + ",");
			System.out.print("smallBlind:" + roomList.get(i).getSmallBlind() + ",");
			System.out.print("bigBlind:" + roomList.get(i).getBigBlind() + ",");
			System.out.print("initbankroll:" + roomList.get(i).getInitBankroll() + ",");
			for (int j = 0; j < roomList.get(i).getReward().length; j++) {
				System.out.print(roomList.get(i).getReward()[j]+",");
			}
			System.out.println();

		}

	}
}
