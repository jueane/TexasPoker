package poker.console;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.charset.Charset;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import poker.console.ProtoFactoryForConsole.RoomInfoReqProto;

public class PokerConsole {
	private static final String IP = "42.96.192.233";
	private static final int PORT = 7000;

	public static PokerConsole client;

	public JTextField fldCCIp;
	public JTextField fldCCPort;
	public JTextField textField;
	public JTextArea textArea;

	public void startup() {
		JFrame frame = new JFrame("Poker game manage");

		JLabel lblCCIp = new JLabel();
		lblCCIp.setText("IP：");
		frame.getContentPane().add(lblCCIp);
		fldCCIp = new JTextField(20);
		fldCCIp.setText(IP);
		frame.getContentPane().add(fldCCIp);

		JLabel lblCCPort = new JLabel();
		lblCCPort.setText("Port：");
		frame.getContentPane().add(lblCCPort);
		fldCCPort = new JTextField(20);
		fldCCPort.setText(String.valueOf(PORT));
		frame.getContentPane().add(fldCCPort);

		JButton btnBind = new JButton("connect");
		frame.getContentPane().add(btnBind);
		btnBind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							new Connector(fldCCIp.getText(), Integer.valueOf(fldCCPort.getText())).startup();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}).start();

			}
		});

		textField = new JTextField(50);
		frame.getContentPane().add(textField);
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					send();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		JButton button = new JButton("send");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		frame.getContentPane().add(button);

		textArea = new JTextArea(24, 70);
		frame.getContentPane().add(textArea);

		frame.getContentPane().setLayout(new FlowLayout());
		frame.setSize(800, 500);
		frame.setLocation(400, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		textField.requestFocus();

	}

	private void send() {
		String cmd = textField.getText().trim();
		if (cmd.isEmpty() == false) {
			if (cmd.charAt(0) == '/') {
				String[] cmds = cmd.substring(1).split(" ");
				// int iCode = Integer.valueOf(cmd.substring(1));
				int iCode = Integer.valueOf(cmds[0]);
				switch (iCode) {
				case 201:
					int roomId = Integer.valueOf(cmds[1]);
					RoomInfoReqProto.Builder roomInfoReqProto = RoomInfoReqProto.newBuilder();
					roomInfoReqProto.setRoomId(roomId);
					ClientHandler.send(iCode, roomInfoReqProto.build().toByteArray());
					break;
				case 202: {
					ClientHandler.send(iCode, null);
					break;
				}
				default:
					break;
				}
				textArea.setText(null);
			} else {
				ClientHandler.send(1, cmd.getBytes(Charset.forName("utf-8")));
				// textField.setText(null);
			}
			textField.requestFocus();
		}
	}

	public static void main(String args[]) {
		client = new PokerConsole();
		client.startup();
	}

}
