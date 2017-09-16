package com.lingzerg.poker.ui.webservice.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.service.MemberService;
import com.lingzerg.poker.util.JsonHelper;

@Controller
@RequestMapping("member")
public class MemberController {

	@Autowired
	private MemberService memberService;

	@RequestMapping("test")
	@ResponseBody
	public byte[] test() throws UnsupportedEncodingException {
		return "你好，测试成功！！".getBytes("utf-8");
	}

	// status:1.成功,2.用户名已存在,3.用户名和密码不能为空
	@RequestMapping("register")
	@ResponseBody
	public byte[] register(String username, String password, String email, String inviterUsername) {
		Member member = new Member();
		member.setUsername(username);
		member.setPassword(password);
		member.setEmail(email);
		return JsonHelper.toJson(memberService.register(member, inviterUsername));
	}

	// status:1.成功,2.用户名或密码错误,3.账号已禁用
	@RequestMapping("login")
	@ResponseBody
	public byte[] login(String username, String password) {
		Member member = new Member();
		member.setUsername(username);
		member.setPassword(password);
		return JsonHelper.toJson(memberService.login(username,password));
	}

	// status:1.成功,2未登录,3.账号已禁用
	@RequestMapping("loginByToken")
	@ResponseBody
	public byte[] loginByToken(String token) {
		return JsonHelper.toJson(memberService.loginByToken(token));
	}

	@RequestMapping("loginByQQ")
	@ResponseBody
	public byte[] loginByQQ(String openId, String qqToken, String qqNickname, Boolean male) {
		return JsonHelper.toJson(memberService.loginByQQ(openId, qqToken, qqNickname, male));
	}

	// status:1.成功
	@RequestMapping("logout")
	@ResponseBody
	public byte[] logout(String token) {
		return JsonHelper.toJson(memberService.logout(token));
	}

	// status:1.成功,2玩家不存在,3.发送邮件错误,
	@RequestMapping("getBackPasswordRequest")
	@ResponseBody
	public byte[] getBackPasswordRequest(String username) {
		return JsonHelper.toJson(memberService.getBackPasswordRequest(username));
	}

	// status:1.成功,2.玩家不存在,3.验证码过期
	@RequestMapping("resetPassword")
	@ResponseBody
	public byte[] resetPassword(String username, String newPassword, String validationCode) {
		return JsonHelper.toJson(memberService.resetPassword(username, newPassword, validationCode));
	}

	// status:1.成功,2.玩家不存在,3.未绑定手机
	@RequestMapping("phoneUnbindRequest")
	@ResponseBody
	public byte[] phoneUnbindRequest(String token) {
		return JsonHelper.toJson(memberService.phoneUnbindRequest(token));
	}

	// status:1.成功,2.玩家不存在,3.未绑定手机.4.未写验证码
	@RequestMapping("phoneUnbind")
	@ResponseBody
	public byte[] phoneUnbind(String token, String validationCode) {
		return JsonHelper.toJson(memberService.phoneUnbind(token, validationCode));
	}

	// status:1.成功,2.玩家不存在,3.未绑定手机
	@RequestMapping("phoneBindRequest")
	@ResponseBody
	public byte[] phoneBindRequest(String token, String phone) {
		return JsonHelper.toJson(memberService.phoneBindRequest(token, phone));
	}

	// status:1.成功,2.玩家不存在,3.未绑定手机.4.未写验证码
	@RequestMapping("phoneBind")
	@ResponseBody
	public byte[] phoneBind(String token, String validationCode) {
		return JsonHelper.toJson(memberService.phoneBind(token, validationCode));
	}

	// status:1.成功,2.昵称不能为空,2未登录
	@RequestMapping("modifyMemberNickname")
	@ResponseBody
	public byte[] modifyMemberNickname(String token, String nickname) {
		return JsonHelper.toJson(memberService.modifyMemberNickname(token, nickname));
	}

	// status:1.成功.2.未登录
	@RequestMapping("modifyMemberMale")
	@ResponseBody
	public byte[] modifyMemberMale(String token, boolean male) {
		return JsonHelper.toJson(memberService.modifyMemberMale(token, male));
	}

	// status:1.成功,2未登录
	@RequestMapping("modifyMemberPortrait")
	@ResponseBody
	public byte[] modifyMemberPortrait(String token, int portrait) {
		return JsonHelper.toJson(memberService.modifyMemberPortrait(token, portrait));
	}

	// status:1.成功,2未登录
	@RequestMapping("modifyMemberPortraitBorder")
	@ResponseBody
	public byte[] modifyMemberPortraitBorder(String token, int portraitBorder) {
		return JsonHelper.toJson(memberService.modifyMemberPortraitBorder(token, portraitBorder));
	}

	// status:1.成功,2未登录
	@RequestMapping("modifyMemberSign")
	@ResponseBody
	public byte[] modifyMemberSign(String token, String sign) {
		return JsonHelper.toJson(memberService.modifyMemberSign(token, sign));
	}

	@RequestMapping("getFriendList")
	@ResponseBody
	public byte[] getFriendList(String token) {
		return JsonHelper.toJson(memberService.getFriendList(token));
	}

	// status:1.成功,2.未登录,3.玩家不存在,4.好友已存在,5.系统错误
	@RequestMapping("addFriend")
	@ResponseBody
	public byte[] addFriend(String token, int memberId) {
		return JsonHelper.toJson(memberService.addFriend(token, memberId));
	}

	// status:1.成功,2.未登录,3.玩家不存在,4.好友已存在,5.系统错误
	@RequestMapping("addFriendByUsername")
	@ResponseBody
	public byte[] addFriendByUsername(String token, String username) {
		return JsonHelper.toJson(memberService.addFriendByUsername(token, username));
	}

	// status:1.成功,2.未登录,3.玩家不存在,4.好友不存在,5.系统错误
	@RequestMapping("deleteFriend")
	@ResponseBody
	public byte[] deleteFriend(String token, int memberId) {
		return JsonHelper.toJson(memberService.deleteFriend(token, memberId));
	}

	@RequestMapping("getMessageReceivedList")
	@ResponseBody
	public byte[] getMessageReceivedList(String token, int pageIndex, int pageSize) {
		return JsonHelper.toJson(memberService.getMessageReceivedList(token, pageIndex, pageSize));
	}

	@RequestMapping("getMessageSentList")
	@ResponseBody
	public byte[] getMessageSentList(String token, int pageIndex, int pageSize) {
		return JsonHelper.toJson(memberService.getMessageSentList(token, pageIndex, pageSize));
	}

	@RequestMapping("getMessageDetail")
	@ResponseBody
	public byte[] getMessageDetail(String token, int messageId) {
		return JsonHelper.toJson(memberService.getMessageDetail(token, messageId));
	}

	// status:1.成功,2.未登录
	@RequestMapping("sendMessage")
	@ResponseBody
	public byte[] sendMessage(String token, int memberId, String messageContent) {
		return JsonHelper.toJson(memberService.sendMessage(token, memberId, messageContent));
	}

}
