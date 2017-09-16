package com.lingzerg.poker.dao;

import java.util.List;

import com.lingzerg.poker.entity.Message;

public interface MessageDao extends BaseDao<Message> {

	List<Message> getByReceiveId(int receiveId,int pageIndex,int pageSize,String orderBy, boolean asc);
	
	List<Message> getBySendId(int sendId,int pageIndex,int pageSize,String orderBy, boolean asc);
	
}
