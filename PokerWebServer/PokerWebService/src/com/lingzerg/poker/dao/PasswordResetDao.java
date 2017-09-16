package com.lingzerg.poker.dao;

import com.lingzerg.poker.entity.PasswordReset;

public interface PasswordResetDao extends BaseDao<PasswordReset>{
	
	PasswordReset getLastByMemberId(int memberId);
	
}
