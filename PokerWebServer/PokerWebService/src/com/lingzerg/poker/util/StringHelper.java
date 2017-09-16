package com.lingzerg.poker.util;

import java.util.ArrayList;
import java.util.List;

public class StringHelper {

	public static List<Integer> stringToListInt(String source) {
		if (source == null) {
			return null;
		}
		String[] strList = source.split(",");
		List<Integer> intList = new ArrayList<>();
		for (int i = 0; i < strList.length; i++) {
			try {
				intList.add(Integer.parseInt(strList[i]));
			} catch (Exception e) {
			}
		}
		return intList;
	}

	public static String listIntToString(List<Integer> listInt) {
		StringBuilder sb = new StringBuilder();
		if (listInt == null || listInt.size() <= 0) {
			return null;
		}
		for (int i = 0; i < listInt.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(listInt.get(i));
		}
		return sb.toString();
	}
	
	public static double[] stringArrayToDoubleArray(String[] args) {
		double[] doublearry = new double[args.length];
		for (int i = 0; i < args.length; i++) {
			doublearry[i] = Double.valueOf(args[i].trim());
		}
		return doublearry;
	}

	public static int[] stringArrayToIntArray(String[] args) {
		int[] intarry = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			intarry[i] = Integer.valueOf(args[i].trim());
		}
		return intarry;
	}
	
	
	
}
