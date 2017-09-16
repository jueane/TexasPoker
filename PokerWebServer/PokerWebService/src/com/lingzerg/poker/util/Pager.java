package com.lingzerg.poker.util;

import java.math.BigDecimal;
import java.util.List;

public class Pager {
	public List<Object> list;
	public int pageIndex = 1;
	public int pageSize = 10;
	public int total = 0;
	public int groupSize=7;
	private int pageIndexTotal;
	
	public List<Object> getList() {
		return list;
	}
	public void setList(List<Object> list) {
		this.list = list;
	}
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getGroupSize() {
		return groupSize;
	}
	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}
	public int getPageIndexTotal() {
		this.pageIndexTotal = new BigDecimal((float)total/pageSize).setScale(0, BigDecimal.ROUND_UP).intValue();
		System.out.println("total - "+total);
		System.out.println("pageIndexTotal - "+pageIndexTotal);
		if (pageIndexTotal > (pageIndex + 7)) {
			pageIndexTotal = pageIndex+7;
		}
		return pageIndexTotal;
	}
	public void setPageIndexTotal(int pageIndexTotal) {
		this.pageIndexTotal = pageIndexTotal;
	}
}
