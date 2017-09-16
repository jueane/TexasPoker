package com.lingzerg.poker.service;

public interface ToyService {
	public int playOneArmBandit(String token, String chip);
	public int playWheel(String token);
	public double getTotal();
}
