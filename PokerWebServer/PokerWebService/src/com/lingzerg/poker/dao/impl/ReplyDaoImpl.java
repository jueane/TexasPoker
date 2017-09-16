package com.lingzerg.poker.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.lingzerg.poker.dao.ReplyDao;
import com.lingzerg.poker.entity.Reply;

@Repository
public class ReplyDaoImpl extends BaseDaoImpl<Reply> implements ReplyDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<Reply> getReplyListByMessageId(int messageId) {
		Session session=sessionFactory.getCurrentSession();
		Criteria crt=session.createCriteria(Reply.class);
		crt.add(Restrictions.eq("deleted", false));
		return crt.add(Restrictions.eq("messageId", messageId)).list();
	}

}
