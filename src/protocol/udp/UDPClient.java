package protocol.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import frame.simulator.NetworkSimulatorFrame;

public class UDPClient{
	
	private NetworkSimulatorFrame parent;
	private DatagramSocket socket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	
	private InetAddress destinationAddress;
	private int destinationPort;
	
	private Object[] rowData;
	private int sendCnt;
	private DefaultTableModel model;
	
	public UDPClient(NetworkSimulatorFrame networkSimulatorFrame){
		parent = networkSimulatorFrame;
		
		try {
			destinationAddress = InetAddress.getByName(parent.getIpT().getText().trim()); //from Server's IP textField
			destinationPort = Integer.parseInt(parent.getPortT().getText().trim());
			socket = new DatagramSocket(); //auto port number distribution
			if(parent.getRcvCheckBox().isSelected()) {
				socket.setSoTimeout(500);// when receive function is selected
			}
			socket.setReuseAddress(true);
			model = (DefaultTableModel)(parent.getTable().getModel());
		} catch (UnknownHostException e) {
			parent.getDebugT().setText("Exception : UnknownAddress");
		} catch (SocketException e) {
			parent.getDebugT().setText("Exception : CreateSocket");
		}
	}

	public void send() {
		
		Thread thread;

		thread = new Thread(new Runnable() {
			public void run()
			{
				boolean rcv = parent.getRcvCheckBox().isSelected();
				if(!rcv) {
					sendWithoutRcv();
				}else {
					sendWithRcv();
				}
			}
		});

		thread.start();
	}
	
	private String[] getMsg() {
		String[] msg = new String[parent.TEST_FILE_CNT]; //by random msg array
		Boolean byRandom = parent.getRandomCheckBox().isSelected();
		
		if(byRandom) {
			for(int i=0;i<msg.length;i++) {
				msg[i] = parent.getTestFile().getTestFileContent(i+1);
			}
		}else {
			for(int i=0;i<msg.length;i++) {
				msg[i] = parent.getDisplay().getText();
			}
		}
		return msg;
	}
	
	private void sendWithoutRcv() {
		int millis = (int)parent.getSleepS().getValue();
		sendCnt = (int)parent.getCountS().getValue();
		int cnt=0;
		String[] msg = getMsg();
		
		parent.getSendBtn().setEnabled(false);
		System.out.println("통신 시작 시간 : "+new Date());
		System.out.println("send : "+sendCnt+"개 전송 시작");
		
		while((sendCnt--)>0) {
			try {
				byte[] sendBuf = msg[(int)(Math.random()*(msg.length))].getBytes(); //display의 내용을 byte[]로 변경함.
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length, destinationAddress, destinationPort);
				socket.send(sendPacket);
				cnt++;
				
				rowData = new Object[] {
											model.getRowCount()+1,
											"UDP",
											"UP",
											sendPacket.getAddress().getHostAddress(),
											sendPacket.getPort(),
											new String(sendBuf)
										};
				
				model.addRow(rowData); //add a row to table
				Thread.sleep(millis); //sleep
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("통신 종료 시간 : "+new Date());
		System.out.println("추가된 row : "+cnt+"개");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("------------------------------------------------");
		parent.getSendBtn().setEnabled(true);
		parent.updateTableCaretPosition();
	}
	
	private void sendWithRcv() {
		int millis = (int)parent.getSleepS().getValue();
		sendCnt = (int)parent.getCountS().getValue();
		String[] msg = getMsg();
		
		System.out.println("send : "+sendCnt+"개, 추가 될 row : "+sendCnt*2+"개");
		parent.getSendBtn().setEnabled(false);
		System.out.println("통신 시작 시간 : "+new Date());
		
		while((sendCnt--)>0) {
			try {
				//send
				byte[] sendBuf = msg[(int)(Math.random()*msg.length)].getBytes();
				sendPacket = new DatagramPacket(sendBuf, sendBuf.length, destinationAddress, destinationPort);
				socket.send(sendPacket);
				
				
				rowData = new Object[] {
											model.getRowCount()+1,
											"UDP",
											"UP",
											sendPacket.getAddress().getHostAddress(),
											sendPacket.getPort(),
											new String(sendBuf)
										};
				
				model.addRow(rowData); //add a row to table
				
				
				//receive
				byte[] receiveBuf = new byte[1024];
				receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
				socket.receive(receivePacket);
				
				
				rowData = new Object[] {
											model.getRowCount()+1,
											"UDP",
											"DOWN",
											receivePacket.getAddress().getHostName().toString(),
											receivePacket.getPort(),
											new String(receiveBuf)
										};
				model.addRow(rowData);
				
				Thread.sleep(millis); //sleep
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("통신 종료 시간 : "+new Date());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		parent.getSendBtn().setEnabled(true);
		parent.updateTableCaretPosition();
	}


	public void exit() {
		if(socket != null) {
			socket.close();
		}
		System.out.println("UDP Client 종료 !!!");
	}

//================================== setter/getter ===============================================================
	public int getSendCnt() {
		return sendCnt;
	}
	public void setSendCnt(int sendCnt) {
		this.sendCnt = sendCnt;
	}
}
