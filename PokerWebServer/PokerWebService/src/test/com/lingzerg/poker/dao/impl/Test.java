package test.com.lingzerg.poker.dao.impl;


public class Test {
	public static void main(String[] args){
		int pArray[] = {25,50,70,80,90,95,100};
		int reward[] = {300,500,800,1000,2000,5000,9999};
		
		int currP = (int) (Math.random()*100);
		System.out.println(currP);
		int level = 0;
		while (currP > pArray[level]) {
			level++;
		}
		System.out.println(reward[level]);
	}
}
