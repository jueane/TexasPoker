package poker.util;

import static org.junit.Assert.*;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Description:无
 * Author:赵俊杰
 * Date:2015年2月12日
 */
public class MapTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<>();
//		for (int i = 100; i > 0; i--) {
//			if(i>20&&i<80){
//				continue;
//			}
//			map.put(i+10000, "" + i);
//		}
		
		Iterator<Integer> itMap=map.keySet().iterator();
		map.put(100, null);
		String a=map.get(100);
		System.out.println("a:"+a);
		
		while(itMap.hasNext()){
			
			System.out.println(itMap.next());
			
		}
		
		
		System.out.println(map.lastKey());
		
		

	}

}
