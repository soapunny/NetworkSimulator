package protocol.udp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


class Chatting{
	private DatagramSocket socket; //User Datagram Protocol(UDP)
	private DatagramPacket packet;
	private InetAddress address;
	
	private int myPort = 10002;
	private int yourPort = 10001;
	
	// frame
	private JTextArea display;
	private JTextField input;
	
	public Chatting(){
		new MyFrame();
		
		//set socket
		try {
			address = InetAddress.getLocalHost(); //상대방 IP
			socket = new DatagramSocket(myPort);
			socket.setSoTimeout(500); // 0.5초마다 리시브 재시작
			socket.setReuseAddress(true); // Garbage Collector가  port번호가 이전에 사용하다가 처리하지 않았으면 오류없이 재사용하게 함
			
		} catch (Exception e) {
			display.append("Exception : " + e.getMessage() + "\n");
		}
	}
	public void receive() {
		while(true) {
			try {
				byte[] rcvBuffer = new byte[1024];
				packet = new DatagramPacket(rcvBuffer, rcvBuffer.length); //packet
				socket.receive(packet); //packet
				
				//byte[]
				display.append("<< " + new String(rcvBuffer) +"\n");
				display.append("INFO : " + packet.getAddress().getHostName() + ":" + packet.getPort() + "\n");
			}catch(Exception e) {
				//display.append("RCV Exception : "+e.getMessage()+"\n");
			}
			System.out.println("receive ..." + myPort);
		}
	}
	
	class MyFrame extends JFrame implements ActionListener{
		Font font;
		
		public MyFrame() {
			initFrame();
			
			buildFonts();
			buildDisplayPanel();
			buildInputPanel();
			
			
			this.setVisible(true);
			buildListeners();
		}
		
		public void buildListeners() {
			input.addActionListener(this);
		}
		
		public void buildFonts() {
			font = new Font("Dialog", Font.BOLD, 20);
		}
		
		public void buildDisplayPanel() {
			JPanel displayPanel = new JPanel();

			display = new JTextArea(12, 28);
			display.setEditable(false);
			display.setFont(font);
			JScrollPane scroll = new JScrollPane(display);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
			displayPanel.add(scroll);
			this.add(displayPanel);// add on BorderLayout.CENTER
		}
		
		public void buildInputPanel() {
			JPanel inputPanel = new JPanel();
			input = new JTextField(28);
			input.setFont(font);
			
			inputPanel.add(input);
			this.add(inputPanel, BorderLayout.SOUTH);
		}
		
		public void initFrame() {
			this.setTitle("UDP Chatting : " + myPort);
			this.setSize(500,400);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == input) {
				display.append(">> "+input.getText()+"\n");
				display.setCaretPosition(display.getDocument().getLength());
				
				//input
				byte[] sendBuffer = input.getText().getBytes();
				//packet
				DatagramPacket sendPacket;
				sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, yourPort);
				try {
					socket.send(sendPacket);
				}catch(Exception e2){
					display.append("Send Exception : "+e2.getMessage() + "\n");
				}
				
				input.selectAll();
			}
		}
	}
}

public class Test {
	public static void main(String[] args) {
		Chatting chat = new Chatting();
		chat.receive();
	}
}
