package com.lingzerg.poker.service;

import java.util.List;

import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.ui.webservice.viewmodel.FriendVm;
import com.lingzerg.poker.ui.webservice.viewmodel.LoginVm;
import com.lingzerg.poker.ui.webservice.viewmodel.MessageDetailVm;
import com.lingzerg.poker.ui.webservice.viewmodel.MessageVm;
import com.lingzerg.poker.ui.webservice.viewmodel.ResultVm;
import com.lingzerg.poker.util.Pager;

public interface MemberService {

	ResultVm register(Member member, String inviterUsername);

	LoginVm login(String username, String password);

	LoginVm loginByToken(String token);

	LoginVm loginByQQ(String openId, String qqToken, String qqNickname, boolean male);

	ResultVm logout(String token);

	ResultVm getBackPasswordRequest(String username);

	ResultVm resetPassword(String username, String newPassword, String validationCode);

	ResultVm phoneUnbindRequest(String token);

	ResultVm phoneUnbind(String token, String validationCode);

	ResultVm phoneBindRequest(String token, String phone);

	ResultVm phoneBind(String token, String validationCode);

	ResultVm modifyMemberNickname(String token, String nickname);

	ResultVm modifyMemberMale(String token, boolean male);

	ResultVm modifyMemberPortrait(String token, int portrait);

	ResultVm modifyMemberPortraitBorder(String token, int portraitBorder);

	ResultVm modifyMemberSign(String token, String sign);

	List<FriendVm> getFriendList(String token);

	ResultVm addFriend(String token, int memberId);

	ResultVm addFriendByUsername(String token, String username);

	ResultVm deleteFriend(String token, int memberId);

	ResultVm sendMessage(String token, int memberId, String messageContent);

	List<MessageVm> getMessageReceivedList(String token, int pageIndex, int pageSize);

	List<MessageVm> getMessageSentList(String token, int pageIndex, int pageSize);

	MessageDetailVm getMessageDetail(String token, int messageId);

	public Member insert(Member obj);

	public Member update(Member obj);

	public int delete(int id);

	public Member getById(int id);

	Pager getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc, String word);
}
