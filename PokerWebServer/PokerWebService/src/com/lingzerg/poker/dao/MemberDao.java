package com.lingzerg.poker.dao;

import java.util.List;

import com.lingzerg.poker.entity.Member;

public interface MemberDao extends BaseDao<Member> {
	Member getByUsername(String username);

	Member getByEmail(String email);

	Member getByPhone(String phone);

	Member getByToken(String token);

	Member getByOpenId(String openId);

	List<Member> getListByMemberIdList(List<Integer> idList);

	@Override
	List<Member> getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc);

	List<Integer> challengeIdList();

}
