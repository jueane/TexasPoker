package com.lingzerg.poker.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.config.Config;
import com.lingzerg.poker.dao.InviteDao;
import com.lingzerg.poker.dao.MemberDao;
import com.lingzerg.poker.dao.MessageDao;
import com.lingzerg.poker.dao.PasswordResetDao;
import com.lingzerg.poker.dao.ReplyDao;
import com.lingzerg.poker.entity.Invite;
import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.entity.Message;
import com.lingzerg.poker.entity.PasswordReset;
import com.lingzerg.poker.entity.Reply;
import com.lingzerg.poker.service.MemberService;
import com.lingzerg.poker.ui.webservice.common.Constant;
import com.lingzerg.poker.ui.webservice.viewmodel.FriendVm;
import com.lingzerg.poker.ui.webservice.viewmodel.LoginVm;
import com.lingzerg.poker.ui.webservice.viewmodel.MemberVm;
import com.lingzerg.poker.ui.webservice.viewmodel.MessageDetailVm;
import com.lingzerg.poker.ui.webservice.viewmodel.MessageVm;
import com.lingzerg.poker.ui.webservice.viewmodel.ResultVm;
import com.lingzerg.poker.ui.webservice.viewmodel.ReplyVm;
import com.lingzerg.poker.util.DateHelper;
import com.lingzerg.poker.util.EMail;
import com.lingzerg.poker.util.Encryption;
import com.lingzerg.poker.util.Pager;
import com.lingzerg.poker.util.PhoneUtil;
import com.lingzerg.poker.util.StringHelper;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDao memberDao;
	@Autowired
	private PasswordResetDao passwordResetDao;
	@Autowired
	private InviteDao inviteDao;
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private ReplyDao replyDao;

	private DateFormat dateFormat = new SimpleDateFormat("s");

	@Override
	public ResultVm register(Member member, String inviterUsername) {
		ResultVm resultVm = new ResultVm();
		if (member.getUsername() != null && member.getPassword() != null) {
			member.setUsername(member.getUsername().trim());
			member.setPassword(member.getPassword().trim());
		}
		if (member.getUsername() == null || member.getUsername().isEmpty() || member.getPassword() == null || member.getPassword().isEmpty()) {
			resultVm.status = 3;
			resultVm.msg = "用户名和密码不能为空";
			return resultVm;
		}

		Member memberRead = memberDao.getByUsername(member.getUsername());
		if (memberRead != null) {
			resultVm.status = 2;
			resultVm.msg = "用户名已存在！";
		} else {
			member.setCreateDate(new Date());
			member.setGold(Config.getInstance().registerInitGold);
			memberDao.insert(member);
			resultVm.status = 1;
			resultVm.msg = "注册成功！";
			// 添加好友邀请记录
			if (inviterUsername != null && !inviterUsername.isEmpty()) {
				try {
					Member friendMember = memberDao.getByUsername(inviterUsername);
					if (friendMember != null) {
						Invite invite = new Invite();
						invite.setMemberId(member.getId());
						invite.setParentId(friendMember.getId());
						invite.setCreateDate(new Date());
						inviteDao.insert(invite);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		return resultVm;
	}

	@Override
	public LoginVm login(String username, String password) {
		LoginVm loginVm = new LoginVm();
		Member member = null;
		if (username != null && password != null) {
			username = username.trim();
			password = password.trim();
		}
		if (username.contains("@")) {
			member = memberDao.getByEmail(username);
		} else if (username.matches("[0-9]+")) {
			member = memberDao.getByPhone(username);
		} else {
			member = memberDao.getByUsername(username);
		}

		if (member != null && member.getPassword().equals(password)) {
			if ((member.getStatus() & 2) == 2) {
				loginVm.status = 3;
				loginVm.msg = "账号已禁用";
				return loginVm;
			}
			// 更新token
			String uniqueString = member.getUsername() + member.getPassword() + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String token = Encryption.md5(uniqueString);
			member.setToken(token);
			// 返回结果
			loginVm.status = 1;
			loginVm.msg = "登录成功";
			loginVm.token = token;
			loginVm.exchangeDisplay = Config.getInstance().hiden;
			loginVm.memberVm = new MemberVm();
			loginVm.memberVm.id = member.getId();
			loginVm.memberVm.gold = member.getGold();
			loginVm.memberVm.gem = member.getGem();
			loginVm.memberVm.phone = member.getPhone();
			loginVm.memberVm.nickname = member.getNickname();
			loginVm.memberVm.winTimes = member.getWinTimes();
			loginVm.memberVm.loseTimes = member.getLoseTimes();
			loginVm.memberVm.maxScore = member.getMaxScore();
			if (member.getMaxCards() != null) {
				loginVm.memberVm.maxCards = new int[member.getMaxCards().length];
				for (int i = 0; i < member.getMaxCards().length; i++) {
					loginVm.memberVm.maxCards[i] = member.getMaxCards()[i];
				}
			}
			loginVm.memberVm.portrait = member.getPortrait();
			loginVm.memberVm.portraitBorder = member.getPortraitBorder();
			loginVm.memberVm.male = member.isMale();
			loginVm.memberVm.sign = member.getSign();
			loginVm.memberVm.isChallenger = (member.getGold() >= Config.getInstance().challengerGoldRequirement) & memberDao.challengeIdList().contains(member.getId());
			int tmpNo = Integer.parseInt(dateFormat.format(new Date()));
			loginVm.memberVm.onlineTotal = Config.getInstance().baseOnlineCount + tmpNo;
		} else {
			loginVm.status = 2;
			loginVm.msg = "用户名或密码错误";
		}
		return loginVm;
	}

	@Override
	public LoginVm loginByToken(String token) {
		LoginVm loginVm = new LoginVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			loginVm.status = 2;
			loginVm.msg = "请登录";
		} else {
			if ((member.getStatus() & 2) == 2) {
				loginVm.status = 3;
				loginVm.msg = "账号已禁用";
				return loginVm;
			}

			loginVm.status = 1;
			loginVm.msg = "登录成功";
			loginVm.exchangeDisplay = Config.getInstance().hiden;
			loginVm.memberVm = new MemberVm();
			loginVm.memberVm.id = member.getId();
			loginVm.memberVm.nickname = member.getNickname();
			loginVm.memberVm.gold = member.getGold();
			loginVm.memberVm.gem = member.getGem();
			loginVm.memberVm.phone = member.getPhone();
			loginVm.memberVm.winTimes = member.getWinTimes();
			loginVm.memberVm.loseTimes = member.getLoseTimes();
			loginVm.memberVm.maxScore = member.getMaxScore();
			if (member.getMaxCards() != null) {
				loginVm.memberVm.maxCards = new int[member.getMaxCards().length];
				for (int i = 0; i < member.getMaxCards().length; i++) {
					loginVm.memberVm.maxCards[i] = member.getMaxCards()[i];
				}
			}
			loginVm.memberVm.portrait = member.getPortrait();
			loginVm.memberVm.portraitBorder = member.getPortraitBorder();
			loginVm.memberVm.male = member.isMale();
			loginVm.memberVm.sign = member.getSign();
			loginVm.memberVm.isChallenger = (member.getGold() >= Config.getInstance().challengerGoldRequirement) & memberDao.challengeIdList().contains(member.getId());
			int tmpNo = Integer.parseInt(dateFormat.format(new Date()));
			loginVm.memberVm.onlineTotal = Config.getInstance().baseOnlineCount + tmpNo;
		}
		return loginVm;
	}

	@Override
	public LoginVm loginByQQ(String openId, String qqToken, String qqNickname, boolean male) {
		LoginVm loginVm = new LoginVm();
		Member member = null;
		member = memberDao.getByOpenId(openId);

		// 下一局false替换成QQtoken验证结果
		if (false) {
			loginVm.status = 4;
			loginVm.msg = "qqToken过期";
			return loginVm;
		}

		// 如果无此用户，则创建.
		if (member == null) {
			member = new Member();
			member.setOpenId(openId);
			member.setCreateDate(new Date());
			member.setGold(Config.getInstance().registerInitGold);
			member.setNickname(qqNickname);
			member.setMale(male);
			memberDao.insert(member);
		} else if ((member.getStatus() & 2) == 2) {
			loginVm.status = 3;
			loginVm.msg = "账号已禁用";
			return loginVm;
		}
		// 更新token
		String uniqueString = member.getOpenId() + member.getPassword() + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String token = Encryption.md5(uniqueString);
		member.setToken(token);
		// 返回结果
		loginVm.status = 1;
		loginVm.msg = "登录成功";
		loginVm.token = token;
		loginVm.exchangeDisplay = Config.getInstance().hiden;
		loginVm.memberVm = new MemberVm();
		loginVm.memberVm.id = member.getId();
		loginVm.memberVm.gold = member.getGold();
		loginVm.memberVm.gem = member.getGem();
		loginVm.memberVm.phone = member.getPhone();
		loginVm.memberVm.nickname = member.getNickname();
		loginVm.memberVm.winTimes = member.getWinTimes();
		loginVm.memberVm.loseTimes = member.getLoseTimes();
		loginVm.memberVm.maxScore = member.getMaxScore();
		if (member.getMaxCards() != null) {
			loginVm.memberVm.maxCards = new int[member.getMaxCards().length];
			for (int i = 0; i < member.getMaxCards().length; i++) {
				loginVm.memberVm.maxCards[i] = member.getMaxCards()[i];
			}
		}
		loginVm.memberVm.portrait = member.getPortrait();
		loginVm.memberVm.portraitBorder = member.getPortraitBorder();
		loginVm.memberVm.male = member.isMale();
		loginVm.memberVm.sign = member.getSign();
		loginVm.memberVm.isChallenger = (member.getGold() >= Config.getInstance().challengerGoldRequirement) & memberDao.challengeIdList().contains(member.getId());
		int tmpNo = Integer.parseInt(dateFormat.format(new Date()));
		loginVm.memberVm.onlineTotal = Config.getInstance().baseOnlineCount + tmpNo;

		return loginVm;
	}

	@Override
	public ResultVm logout(String token) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		member.setToken(null);
		resultVm.status = 1;
		return resultVm;
	}

	@Override
	public ResultVm getBackPasswordRequest(String username) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByUsername(username);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		String validationCode = EMail.generateValidationCode();
		if (member.getEmail() == null) {
			resultVm.status = 4;
			resultVm.msg = "未设置Email";
			return resultVm;
		}
		String notice = EMail.send(member.getEmail(), "poker password reset validation", "密码重置 验证码为：" + validationCode);
		if (!String.valueOf(true).equals(notice)) {
			resultVm.status = 3;
			resultVm.msg = "发送邮件错误";
			return resultVm;
		}
		PasswordReset passwordReset = new PasswordReset();
		passwordReset.setMemberId(member.getId());
		passwordReset.setValidationCode(validationCode);
		passwordReset.setOldPassword(member.getPassword());
		passwordReset.setCreateDate(new Date());
		passwordResetDao.insert(passwordReset);
		resultVm.status = 1;
		resultVm.msg = "申请成功，已发送邮件到玩家邮箱";
		return resultVm;
	}

	@Override
	public ResultVm resetPassword(String username, String newPassword, String validationCode) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByUsername(username);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		PasswordReset lastPasswordReset = passwordResetDao.getLastByMemberId(member.getId());
		// 判断是否超2天
		int differDays = 0;
		try {
			differDays = DateHelper.daysBetween(lastPasswordReset.getCreateDate(), new Date());
		} catch (ParseException e) {
			differDays = 10;
			e.printStackTrace();
			System.err.println("日期转换错误");
		}
		if (differDays > 2) {
			resultVm.status = 3;
			resultVm.msg = "验证码过期";
			return resultVm;
		}
		member.setPassword(newPassword.trim());
		lastPasswordReset.setExpired(true);
		resultVm.status = 1;
		resultVm.msg = "密码重置成功";
		return resultVm;
	}

	@Override
	public ResultVm phoneUnbindRequest(String token) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		if (member.getPhone() == null) {
			resultVm.status = 3;
			resultVm.msg = "未绑定手机";
			return resultVm;
		}
		String validationCode = PhoneUtil.generateValidationCode();
		member.setPhoneValidationCode(validationCode);
		PhoneUtil.send(member.getPhone(), "您的验证码是：" + validationCode + "。请不要把验证码泄露给其他人。");
		resultVm.status = 1;
		resultVm.msg = "验证码已发送";
		return resultVm;
	}

	@Override
	public ResultVm phoneUnbind(String token, String validationCode) {
		ResultVm resultVm = new ResultVm();
		if (validationCode == null) {
			resultVm.status = 4;
			resultVm.msg = "请填写验证码";
			return resultVm;
		}
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		if (member.getPhone() == null) {
			resultVm.status = 3;
			resultVm.msg = "未绑定手机";
			return resultVm;
		}
		if (validationCode.equals(member.getPhoneValidationCode())) {
			member.setPhone(null);
		}
		resultVm.status = 1;
		resultVm.msg = "手机解绑成功";
		return resultVm;
	}

	@Override
	public ResultVm phoneBindRequest(String token, String phone) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		if (member.getPhone() != null) {
			resultVm.status = 3;
			resultVm.msg = "请先解绑手机";
			return resultVm;
		}
		String validationCode = PhoneUtil.generateValidationCode();
		member.setPhoneToValidate(phone);
		member.setPhoneValidationCode(validationCode);
		PhoneUtil.send(phone, "您的验证码是：" + validationCode + "。请不要把验证码泄露给其他人。");
		resultVm.status = 1;
		resultVm.msg = "验证码已发送";
		return resultVm;
	}

	@Override
	public ResultVm phoneBind(String token, String validationCode) {
		ResultVm resultVm = new ResultVm();
		if (validationCode == null) {
			resultVm.status = 4;
			resultVm.msg = "请填写验证码";
			return resultVm;
		}
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		if (member.getPhone() != null) {
			resultVm.status = 3;
			resultVm.msg = "请先解绑手机";
			return resultVm;
		}
		if (validationCode.equals(member.getPhoneValidationCode())) {
			member.setPhone(member.getPhoneToValidate());
		}
		resultVm.status = 1;
		resultVm.msg = "手机绑定成功";
		return resultVm;
	}

	@Override
	public ResultVm modifyMemberNickname(String token, String nickname) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 3;
			resultVm.msg = "请登录";
			return resultVm;
		}
		if (nickname == null || nickname.isEmpty()) {
			resultVm.status = 2;
			resultVm.msg = "昵称不能为空";
			return resultVm;
		}
		member.setNickname(nickname);
		resultVm.status = 1;
		resultVm.msg = "昵称修改成功";
		return resultVm;
	}

	@Override
	public ResultVm modifyMemberMale(String token, boolean male) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "请登录";
			return resultVm;
		}
		member.setMale(male);
		resultVm.status = 1;
		resultVm.msg = "性别修改成功";
		return resultVm;
	}

	@Override
	public ResultVm modifyMemberPortrait(String token, int portrait) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "请登录";
			return resultVm;
		}
		member.setPortrait(portrait);
		resultVm.status = 1;
		resultVm.msg = "头像修改成功";
		return resultVm;
	}

	@Override
	public ResultVm modifyMemberPortraitBorder(String token, int portraitBorder) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "请登录";
			return resultVm;
		}
		member.setPortraitBorder(portraitBorder);
		resultVm.status = 1;
		resultVm.msg = "头像边框修改成功";
		return resultVm;
	}

	@Override
	public ResultVm modifyMemberSign(String token, String sign) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "请登录";
			return resultVm;
		}
		member.setSign(sign);
		resultVm.status = 1;
		resultVm.msg = "签名档修改成功";
		return resultVm;
	}

	@Override
	public List<FriendVm> getFriendList(String token) {
		Member member = memberDao.getByToken(token);
		if (member != null && member.getFriendIdList() != null) {
			List<Member> memberList = memberDao.getListByMemberIdList(StringHelper.stringToListInt(member.getFriendIdList()));
			List<FriendVm> friendList = new ArrayList<>();
			for (int i = 0; i < memberList.size(); i++) {
				FriendVm friendVm = new FriendVm();
				friendVm.id = memberList.get(i).getId();
				friendVm.nickname = memberList.get(i).getNickname();
				friendVm.portrait = memberList.get(i).getPortrait();
				friendVm.portraitBorder = memberList.get(i).getPortraitBorder();
				friendVm.sign = memberList.get(i).getSign();
				friendVm.lastLoginDate = memberList.get(i).getLastLoginDate();
				friendVm.isChallenger = (memberList.get(i).getGold() >= Config.getInstance().challengerGoldRequirement) & memberDao.challengeIdList().contains(memberList.get(i).getId());
				friendVm.gold = memberList.get(i).getGold();
				friendVm.online = (member.getToken() != null && !member.getToken().isEmpty());
				friendList.add(friendVm);
			}
			return friendList;
		}
		return null;
	}

	@Override
	public ResultVm addFriend(String token, int memberId) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "未登录";
			return resultVm;
		}
		if (memberDao.getById(memberId) == null) {
			resultVm.status = 3;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		// 获取好友id列表的List<Integer>形式
		List<Integer> memberIdList = null;
		String friendIdListString = member.getFriendIdList();
		if (friendIdListString != null && !friendIdListString.isEmpty()) {
			memberIdList = StringHelper.stringToListInt(friendIdListString);
		} else {
			memberIdList = new ArrayList<>();
		}
		// 添加新好友Id
		if (memberIdList.contains(memberId)) {
			resultVm.status = 4;
			resultVm.msg = "好友已存在";
			return resultVm;
		} else {
			memberIdList.add(memberId);
			// 重新组合成String
			try {
				String finalFriendIdListString = StringHelper.listIntToString(memberIdList);
				// 确保不会发生意外将好友id列表清空
				if (finalFriendIdListString != null && !finalFriendIdListString.isEmpty()) {
					member.setFriendIdList(finalFriendIdListString);
				}
			} catch (Exception e) {
				member.setFriendIdList(friendIdListString);
				resultVm.status = 5;
				resultVm.msg = "系统错误";
				return resultVm;
			}
		}
		resultVm.status = 1;
		resultVm.msg = "添加好友成功";
		return resultVm;
	}

	@Override
	public ResultVm addFriendByUsername(String token, String username) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "未登录";
			return resultVm;
		}
		// 获取好友id
		Member friendMember = memberDao.getByUsername(username);
		if (friendMember == null) {
			resultVm.status = 3;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		int memberId = friendMember.getId();
		// 获取好友id列表的List<Integer>形式
		List<Integer> memberIdList = null;
		String friendIdListString = member.getFriendIdList();
		if (friendIdListString != null && !friendIdListString.isEmpty()) {
			memberIdList = StringHelper.stringToListInt(friendIdListString);
		} else {
			memberIdList = new ArrayList<>();
		}
		// 添加新好友Id
		if (memberIdList.contains(memberId)) {
			resultVm.status = 4;
			resultVm.msg = "好友已存在";
			return resultVm;
		} else {
			memberIdList.add(memberId);
			// 重新组合成String
			try {
				String finalFriendIdListString = StringHelper.listIntToString(memberIdList);
				// 确保不会发生意外将好友id列表清空
				if (finalFriendIdListString != null && !finalFriendIdListString.isEmpty()) {
					member.setFriendIdList(finalFriendIdListString);
				}
			} catch (Exception e) {
				member.setFriendIdList(friendIdListString);
				resultVm.status = 5;
				resultVm.msg = "系统错误";
				return resultVm;
			}
		}
		resultVm.status = 1;
		resultVm.msg = "添加好友成功";
		return resultVm;
	}

	@Override
	public ResultVm deleteFriend(String token, int memberId) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "未登录";
			return resultVm;
		}
		// 获取好友id列表的List<Integer>形式
		List<Integer> memberIdList = null;
		String friendIdListString = member.getFriendIdList();
		if (friendIdListString != null && !friendIdListString.isEmpty()) {
			memberIdList = StringHelper.stringToListInt(friendIdListString);
		} else {
			memberIdList = new ArrayList<>();
		}
		if (memberDao.getById(memberId) == null) {
			resultVm.status = 3;
			resultVm.msg = "玩家不存在";
			return resultVm;
		}
		// 删除好友Id
		if (memberIdList.contains(memberId)) {
			memberIdList.remove(memberIdList.indexOf(memberId));
		} else {
			resultVm.status = 4;
			resultVm.msg = "好友不存在";
			return resultVm;
		}
		// 重新组合成String
		try {
			String finalFriendIdListString = StringHelper.listIntToString(memberIdList);
			// 确保不会发生意外将好友id列表清空
			if (friendIdListString != null && !finalFriendIdListString.isEmpty()) {
				member.setFriendIdList(finalFriendIdListString);
			}
		} catch (Exception e) {
			member.setFriendIdList(friendIdListString);
			resultVm.status = 5;
			resultVm.msg = "系统错误";
			return resultVm;
		}
		resultVm.status = 1;
		resultVm.msg = "删除好友成功";
		return resultVm;
	}

	@Override
	public ResultVm sendMessage(String token, int memberId, String messageContent) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		if (member == null) {
			resultVm.status = 2;
			resultVm.msg = "未登录";
			return resultVm;
		}
		// 限制消息长度
		if (messageContent.length() > Constant.messageLength) {
			messageContent = messageContent.substring(0, Constant.messageLength);
		}
		Message message = new Message();
		message.setSendId(member.getId());
		message.setReceiveId(memberId);
		message.setContent(messageContent);
		message.setCreateDate(new Date());
		messageDao.insert(message);
		resultVm.status = 1;
		resultVm.msg = "消息发送成功";
		return resultVm;
	}

	@Override
	public List<MessageVm> getMessageReceivedList(String token, int pageIndex, int pageSize) {
		Member member = memberDao.getByToken(token);
		if (member == null) {
			return null;
		}
		List<Message> messageList = messageDao.getByReceiveId(member.getId(), pageIndex, pageSize, "id", false);
		List<MessageVm> messageVmList = null;
		if (messageList != null && messageList.size() > 0) {
			messageVmList = new ArrayList<>();
			for (int i = 0; i < messageList.size(); i++) {
				MessageVm messageVm = new MessageVm();
				messageVm.id = messageList.get(i).getId();
				messageVm.senderId = messageList.get(i).getSendId();
				// ----------------------------待优化，关联外键循环访问数据库
				messageVm.senderUsername = messageList.get(i).getSendMember().getUsername();
				messageVm.content = messageList.get(i).getContent();
				messageVm.read = messageList.get(i).isHasRead();
				messageVmList.add(messageVm);
			}
		}
		return messageVmList;
	}

	@Override
	public List<MessageVm> getMessageSentList(String token, int pageIndex, int pageSize) {
		Member member = memberDao.getByToken(token);
		if (member == null) {
			return null;
		}
		List<Message> messageList = messageDao.getBySendId(member.getId(), pageIndex, pageSize, "id", false);
		List<MessageVm> messageVmList = null;
		if (messageList != null && messageList.size() > 0) {
			messageVmList = new ArrayList<>();
			for (int i = 0; i < messageList.size(); i++) {
				MessageVm messageVm = new MessageVm();
				messageVm.id = messageList.get(i).getId();
				messageVm.receiverId = messageList.get(i).getReceiveId();
				// ----------------------------待优化，关联外键循环访问数据库
				messageVm.senderUsername = messageList.get(i).getReceiveMember().getUsername();
				messageVm.senderNickname = messageList.get(i).getReceiveMember().getNickname();
				messageVm.content = messageList.get(i).getContent();
				messageVm.read = messageList.get(i).isHasRead();
				messageVmList.add(messageVm);
			}
		}
		return messageVmList;
	}

	@Override
	public MessageDetailVm getMessageDetail(String token, int messageId) {
		Member member = memberDao.getByToken(token);
		if (member == null) {
			return null;
		}
		Message message = messageDao.getById(messageId);
		// 消息为空或既不是发信人也不是收信人
		if (message == null || (member.getId() != message.getSendId() && member.getId() != message.getReceiveId())) {
			return null;
		}
		MessageDetailVm messageDetailVm = new MessageDetailVm();
		messageDetailVm.id = messageId;
		messageDetailVm.content = message.getContent();
		if (member.getId() == message.getSendId()) {
			messageDetailVm.senderId = message.getSendId();
			messageDetailVm.senderUsername = message.getSendMember().getUsername();
			messageDetailVm.senderNickname = message.getSendMember().getNickname();
		} else {
			messageDetailVm.receiverId = message.getReceiveId();
			messageDetailVm.receiverUsername = message.getReceiveMember().getUsername();
			messageDetailVm.receiverNickname = message.getReceiveMember().getNickname();
		}
		List<Reply> replyList = replyDao.getReplyListByMessageId(messageId);
		if (replyList == null || replyList.size() <= 0) {
			// return null;
			messageDetailVm.replyVmList = null;
		}
		List<ReplyVm> replyVmList = new ArrayList<>();
		for (int i = 0; i < replyList.size(); i++) {
			ReplyVm replyVm = new ReplyVm();
			replyVm.response = (member.getId() == message.getSendId() & replyList.get(i).isResponse()) || (member.getId() == message.getReceiveId() & !replyList.get(i).isResponse());
			replyVm.content = replyList.get(i).getContent();
			replyVmList.add(replyVm);
		}
		messageDetailVm.replyVmList = replyVmList;
		// 消息置为已读
		message.setHasRead(true);
		return messageDetailVm;
	}

	@Override
	public Member insert(Member obj) {
		return memberDao.insert(obj);
	}

	@Override
	public Member update(Member obj) {
		return memberDao.update(obj);
	}

	@Override
	public int delete(int id) {
		return memberDao.delete(id);
	}

	@Override
	public Member getById(int id) {
		return memberDao.getById(id);
	}

	@Override
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc, String word) {
		return memberDao.getPagedList(pageIndex, pageSize, orderBy, asc, word);
	}

}
