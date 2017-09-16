package com.lingzerg.gamecenter.util;

import java.util.List;

public class OrderHelper {
	public static void orderDesc(List<Integer> intList) {
		for (int i = 0; i < intList.size() - 1; i++) {
			for (int j = i; j < intList.size(); j++) {
				if (intList.get(i) < intList.get(j)) {
					int tmpInt = intList.get(i);
					intList.set(i, intList.get(j));
					intList.set(j, tmpInt);
				}
			}
		}
	}
	

	public static void orderAsc(List<Integer> intList) {
		for (int i = 0; i < intList.size() - 1; i++) {
			for (int j = i; j < intList.size(); j++) {
				if (intList.get(i) > intList.get(j)) {
					int tmpInt = intList.get(i);
					intList.set(i, intList.get(j));
					intList.set(j, tmpInt);
				}
			}
		}
	}
	

}
