package poker.data;

import java.util.List;

import org.junit.Test;

import poker.data.RoomData;
import poker.entity.RoomInfo;
import poker.main.room.RoomType;

public class RoomDataTest extends RoomData {

	@Test
	public void test() {
		// 检测是房间信息是否能正常载入
		List<RoomInfo> roomTypeList = getRoomTypeList(RoomType.OMAHA);

		for (int i = 0; i < roomTypeList.size(); i++) {
			System.out.println("Id:" + roomTypeList.get(i).getId() + "，smallBlind:" + roomTypeList.get(i).getSmallBlind() + ",bigBlind:" + roomTypeList.get(i).getBigBlind() + ",average:" + roomTypeList.get(i).getAverageTake() + ",minTkae"
					+ roomTypeList.get(i).getMinTake() + ",maxTake:" + roomTypeList.get(i).getMaxTake() + ",type:" + roomTypeList.get(i).getType()

			);
		}
	}

}
