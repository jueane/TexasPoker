package com.lingzerg.poker.ui.webservice.common;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class Initialization implements ApplicationListener<ApplicationEvent> {

	private boolean started = false;

	// @Autowired
	// private ServerDao serverDao;

	@Override
	public void onApplicationEvent(ApplicationEvent arg0) {
		if (started == false) {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("App Initialize");

			setRoomList();
			setServerList();

			System.out.println("--------------------------------------------------------------------------------");
			started = true;
		}
	}

	private void setRoomList() {

	}

	private void setServerList() {

	}

}
