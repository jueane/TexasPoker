package com.lingzerg.gamecenter.main.player.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lingzerg.gamecenter.data.MemberData;
import com.lingzerg.gamecenter.entity.MemberInfo;
import com.lingzerg.gamecenter.main.player.ActionResult;
import com.lingzerg.gamecenter.main.player.Player;
import com.lingzerg.gamecenter.main.player.PlayerStatus;
import com.lingzerg.gamecenter.proto.CardsPro.CardsProto;
import com.lingzerg.gamecenter.proto.RaisePro.RaiseProto;
import com.lingzerg.gamecenter.proto.ValidationPro.ValidationProto;
import com.lingzerg.gamecenter.util.JLog;

public class PlayerImpl implements Player {
	private int id;
	private MemberInfo detailInfo;
	private int bankroll;
	private int gained;
	private int[] anteList = new int[4];// 玩家下注列表，共4轮
	private byte[] holeCards = new byte[2];
	MemberData memberData = new MemberData();

	// 下注期间，客户端等待15秒，玩家无操作则自动发送弃牌请求。服务端等待18秒。若无反应则视为退出。间隔3秒作为网络延迟上限。
	private PlayerStatus status;// 0.待删除，1.正常。2.等待加入，3.让牌，4.弃牌，5.站起旁观。。//超时，超时自动删除。退出：自动删除。

	Socket s = null;
	private final int SOCKET_TIMEOUT = 20000;

	class ReceiveObj {
		public int code;
		public int length;
		public byte[] buff;
	}

	public PlayerImpl(Socket socket) {
		this.s = socket;
		try {
			s.setSoTimeout(SOCKET_TIMEOUT);
		} catch (SocketException e1) {
			JLog.debugln("Set timeout failed.");
		}
		this.status = PlayerStatus.READY;
	}

	// 每局开始，玩家状态初始化
	@Override
	public int init() {
		// 若玩家筹码不足，则自动补充，并持久化。如果金币不足则将玩家移入观众席
		detailInfo = memberData.getById(id);// 在此每局重读一次玩家数据是因为玩家金币不足时可能会进行充值。
		return 1;
	}

	@Override
	public void resetStatus() {
		// 重置状态
		if (status == PlayerStatus.CHECK || status == PlayerStatus.FOLD) {
			status = PlayerStatus.NORMAL;
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getBankroll() {
		return this.bankroll;
	}

	@Override
	public void setBankroll(int bankroll) {
		this.bankroll = bankroll;
	}

	@Override
	public PlayerStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(PlayerStatus status) {
		this.status = status;
	}

	@Override
	public ValidationProto validate() throws IOException {
		ReceiveObj receiveObj = receive();
		ValidationProto validationProto = null;
		if (receiveObj.buff == null) {
			JLog.infoln("Player " + s.getInetAddress().getHostAddress() + " validate Data error.Data is null.");
			return null;
		}
		try {
			validationProto = ValidationProto.parseFrom(receiveObj.buff);
			System.out.println(validationProto.getToken());
		} catch (InvalidProtocolBufferException e) {
			JLog.infoln("Player " + s.getInetAddress().getHostAddress() + " validate Data error.Data format error.");
			return null;
		}
		detailInfo = memberData.getByToken(validationProto.getToken());
		if (detailInfo == null) {
			this.send(20015, null);
			JLog.infoln("Player " + s.getInetAddress().getHostAddress() + " Token expired.");
			return null;
		} else {
			this.send(20010, null);
			this.id = detailInfo.getId();
			return validationProto;
		}
	}

	// 轮到玩家下注。返回<=0则表示未下注
	@Override
	public ActionResult action(int turn, int seat, int minCall, boolean checkable) {
		ActionResult actionResult = new ActionResult();

		ReceiveObj receiveObj = receive();// 接收下注应答，超时为35ms
		if (this.status == PlayerStatus.BEING_DELETE) {
			actionResult.act = 5;
			return actionResult;
		}

		switch (receiveObj.code) {
		case 10051:
			// 让牌，若不可让，则自动转为弃牌
			if (checkable) {
				this.status = PlayerStatus.CHECK;
				actionResult.act = 1;
			} else {
				JLog.debugln("Could not check.");
				this.status = PlayerStatus.FOLD;
				actionResult.act = 5;
			}
			break;
		case 10052:
			// 最小注额为0的情况下如果跟注，则视为弃牌
			if (minCall <= 0) {
				actionResult.act = 5;
				status = PlayerStatus.BEING_DELETE;
				break;
			}
			// 跟注，服务端计算出注额
			if (bankroll > 0 && bankroll < minCall) {
				// all in
				anteList[turn] += bankroll;
				actionResult.act = 4;
				actionResult.ante = bankroll;
				bankroll = 0;
			} else if (bankroll >= minCall) {
				// 正常跟注
				anteList[turn] += minCall;
				actionResult.act = 2;
				actionResult.ante = minCall;
				bankroll -= minCall;
			} else {
				// 不够则弃牌
				JLog.debugln("Bankroll is less than 0.(in call).");
				actionResult.act = 5;
			}
			break;
		case 10053:
			// 加注
			RaiseProto raiseProto = null;
			try {
				if (receiveObj.buff == null) {
					throw new InvalidProtocolBufferException("Buff is null.");
				}
				raiseProto = RaiseProto.parseFrom(receiveObj.buff);
			} catch (InvalidProtocolBufferException e) {
				JLog.debug("Raise data format error.");
				actionResult.act = 5;
				this.status = PlayerStatus.BEING_DELETE; // 删不删呢？
				break;
			}
			int wannaToBet = raiseProto.getAnte();
			// 判断玩家能否加注
			if (bankroll >= wannaToBet) {
				anteList[turn] += wannaToBet;
				bankroll -= wannaToBet;
				actionResult.act = 3;
				actionResult.ante = wannaToBet;
			} else {
				JLog.debug("Bankroll is not enough(in raise).");
				actionResult.act = 5;
				status = PlayerStatus.FOLD;
			}
			break;
		case 10054:
			// 有人all in
			if (bankroll > 0) {
				anteList[turn] += bankroll;
				actionResult.act = 4;
				actionResult.ante = bankroll;
				bankroll = 0;
			} else {
				actionResult.act = 5;
				status = PlayerStatus.FOLD;
			}
			break;
		case 10055:
			// 弃牌
			actionResult.act = 5;
			status = PlayerStatus.FOLD;
			break;
		default:
			actionResult.act = 5;
			status = PlayerStatus.BEING_DELETE;
			break;
		}
		return actionResult;
	}

	@Override
	public int send(int code, byte[] bytes) throws IOException {
		int length = bytes == null ? 0 : bytes.length;
		try {
			OutputStream os = s.getOutputStream();
			byte[] header = new byte[8];
			header[0] = (byte) (code & 0xff);
			header[1] = (byte) ((code >> 8) & 0xff);
			header[2] = (byte) ((code >> 16) & 0xff);
			header[3] = (byte) ((code >> 24) & 0xff);
			header[4] = (byte) (length & 0xff);
			header[5] = (byte) ((length >> 8) & 0xff);
			header[6] = (byte) ((length >> 16) & 0xff);
			header[7] = (byte) ((length >> 24) & 0xff);
			os.write(header);
			if (bytes != null) {
				os.write(bytes);
			}
			os.flush();
		} catch (IOException e) {
			JLog.debugln("Player " + id + " error in send，set status  to be delete！");
			this.status = PlayerStatus.BEING_DELETE;
			throw e;
		}
		return 0;
	}

	public ReceiveObj receive() {
		ReceiveObj receiveObj = new ReceiveObj();
		try {
			byte[] buffHead = new byte[8];
			int iCode = 0;
			int iLength = 0;
			InputStream is = s.getInputStream();
			is.read(buffHead, 0, 8);
			iCode |= buffHead[0] & 0xff;
			iCode |= (buffHead[1] << 8) & 0xffff;
			iCode |= (buffHead[2] << 16) & 0xffffff;
			iCode |= (buffHead[3] << 24) & 0xffffffff;
			receiveObj.code = iCode;
			// JLog.debug("code：" + receiveObj.code);
			iLength |= buffHead[4] & 0xff;
			iLength |= (buffHead[5] << 8) & 0xffff;
			iLength |= (buffHead[6] << 16) & 0xffffff;
			iLength |= (buffHead[7] << 24) & 0xffffffff;
			receiveObj.length = iLength;
			// JLog.debug("，length：" + receiveObj.length);
			// JLog.debugln("\r\n");
			if (receiveObj.length > 40000) {
				JLog.debug("Receive length more than 40K, ignore.");
			} else if (receiveObj.length > 0) {
				receiveObj.buff = new byte[receiveObj.length];
				is.read(receiveObj.buff, 0, receiveObj.length);
			}
		} catch (SocketTimeoutException e) {
			JLog.debugln("Timeout in receive(). Id：" + this.id);
			status = PlayerStatus.BEING_DELETE;
		} catch (IOException e) {
			JLog.debugln("IO exception in receive()：Id：" + this.id);
			status = PlayerStatus.BEING_DELETE;
		}
		return receiveObj;
	}

	@Override
	public void dealHoleCards(byte[] cards) {
		if (cards.length < 2) {
			JLog.debugln("Hole cards error with length：" + cards.length);
			return;
		}
		this.holeCards[0] = cards[0];
		this.holeCards[1] = cards[1];
		CardsProto.Builder cardsBuilder = CardsProto.newBuilder();
		cardsBuilder.setCards(ByteString.copyFrom(cards));
		try {
			this.send(20040, cardsBuilder.build().toByteArray());
		} catch (IOException e) {
			JLog.debugln("An error occurred when sending hole cards.");
		}
	}

	@Override
	public byte[] getHoleCards() {
		byte[] holeCardsTmp = new byte[2];
		holeCardsTmp[0] = this.holeCards[0];
		holeCardsTmp[1] = this.holeCards[1];
		return holeCardsTmp;
	}

	@Override
	public boolean ConnectionOk() {
		boolean ifok = false;
		try {
			s.setSoTimeout(5000);
		} catch (SocketException e) {
			JLog.debugln("Set timeout failed.");
			return false;
		}
		try {
			send(20001, null);
			if (receive().code == 10001) {
				ifok = true;
			} else {
				this.status = PlayerStatus.BEING_DELETE;
			}
		} catch (IOException e1) {
			this.status = PlayerStatus.BEING_DELETE;
		}
		// 恢复timeout.
		try {
			s.setSoTimeout(SOCKET_TIMEOUT);
		} catch (SocketException e) {
			JLog.debugln("Set timeout failed.");
		}
		return ifok;
	}

	@Override
	public int getAnte(int turn) {
		return anteList[turn];
	}

	@Override
	public int getAnteTotal() {
		int total = 0;
		for (int i = 0; i < anteList.length; i++) {
			total += anteList[i];
		}
		return total;
	}

	@Override
	public void resetAnteList() {
		for (int i = 0; i < anteList.length; i++) {
			anteList[i] = 0;
		}
	}

	@Override
	public MemberInfo getDetailInfo() {
		return detailInfo;
	}

	@Override
	public int blindBet(int count) {
		bankroll -= count;
		anteList[0] = count;
		return anteList[0];
	}

	@Override
	public void setGained(int gainedGold) {
		gained = gainedGold;
	}

	@Override
	public int getGained() {
		return gained;
	}

}
