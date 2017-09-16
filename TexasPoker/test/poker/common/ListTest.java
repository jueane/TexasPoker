package poker.common;

import java.util.ArrayList;
import java.util.List;

public class ListTest {

	public static void main(String[] args) {
		List<Integer> intList = new ArrayList<>();
		intList.add(5);
		intList.add(8);

		intList.remove(Integer.valueOf(5));
		System.out.println(intList.size());
		

	}

}
