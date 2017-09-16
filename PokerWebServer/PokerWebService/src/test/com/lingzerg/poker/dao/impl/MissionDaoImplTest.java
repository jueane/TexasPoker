package test.com.lingzerg.poker.dao.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lingzerg.poker.dao.impl.MissionDaoImpl;

public class MissionDaoImplTest {

	@Test
	public void testGetByIdInt() {
		MissionDaoImpl mdi = new MissionDaoImpl();
		mdi.getById(0);
	}

	@Test
	public void testGetListIntInt() {
		fail("Not yet implemented");
	}
}
