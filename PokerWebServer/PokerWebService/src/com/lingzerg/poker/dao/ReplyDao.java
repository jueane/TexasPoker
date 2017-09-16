package com.lingzerg.poker.dao;

import java.util.List;

import com.lingzerg.poker.entity.Reply;

public interface ReplyDao extends BaseDao<Reply>{
	
	List<Reply> getReplyListByMessageId(int messageId);

}
