package poker.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Description:无
 * Author:赵俊杰
 * Date:2015年2月12日
 */
public class LockTest {

	Lock lock=new ReentrantLock();
	int m=1;
	
	public class A{
		int s;
		public A(){
			s=m++;
		}
		public void run() {
			System.out.println("s:"+s);
		}
		
	}
	
	int a=1;
	public void test(){
		for(int i=0;i<20;i++){
			
			
			new Thread(new Runnable() {
				@Override
				public void run() {
//					System.out.println("wait "+b);
					lock.lock();
					int b=a++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("got "+b);
					lock.unlock();

				}
			}).start();

		}
	}
	
	public static void main(String args[]){
		new LockTest().test();
	}
	
	
	
	
	
	
}
