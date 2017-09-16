package com.lingzerg.gamecenter.main;

import com.lingzerg.gamecenter.main.gate.Gate;
import com.lingzerg.gamecenter.main.gate.impl.GateImpl;
import com.lingzerg.gamecenter.util.JLog;

public class Launch {
	public static void main(String[] args) {
		JLog.infoln("Launch GameCenter");
		Gate gate = new GateImpl();
		gate.startup();
		JLog.infoln("GameCenter close.");
	}
}
