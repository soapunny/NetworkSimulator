package protocol.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

import javax.swing.table.DefaultTableModel;

import frame.simulator.NetworkSimulatorFrame;
import util.alias.RunSettingAlias;

public class TCPClient {
	private NetworkSimulatorFrame parent;
	
	private Socket sendSocket;
	private PrintWriter pWriter;
	private BufferedReader bReader;
	private InputStreamReader isReader;
	private DefaultTableModel model;
	
	private int sendCnt;
	private int millis;
	
	public TCPClient(NetworkSimulatorFrame parentFrame) {
		parent = parentFrame;
		
		try {
			sendSocket = new Socket(InetAddress.getByName(parent.getIpT().getText().trim()), Integer.parseInt(parent.getPortT().getText().trim()));
			pWriter = new PrintWriter(sendSocket.getOutputStream(), true);
			isReader = new InputStreamReader(sendSocket.getInputStream());
			bReader = new BufferedReader(isReader);
			sendSocket.setSoTimeout(500);
			sendSocket.setReuseAddress(true);
			
			model = (DefaultTableModel)(parent.getTable().getModel());
			Object[] rowData = new Object[] {
												model.getRowCount()+1,
												"TCP",
												"DOWN",
												sendSocket.getInetAddress().getHostAddress(),
												sendSocket.getPort(),
												"<html><body><font color='#ff0000'>서버 연결</font></body></html>"
											};
			model.addRow(rowData);
		} catch (Exception e) {
			parent.stopSetting();
			parent.getDebugT().setText("Exception : TCPClient 구축 실패");
			System.out.println("TCP 서버와 통신할 수 없습니다.");
		}
	}
	
	public void send() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean hasReceive = parent.getRcvCheckBox().isSelected();
				if(hasReceive) {
					sendWithReceive();
				}else {
					sendWithoutReceive();
				}
			}
		});
		thread.start();
	}
	
	public String[] getMsg() {
		String[] msg = new String[parent.TEST_FILE_CNT];
		boolean isRandom = parent.getRandomCheckBox().isSelected();
		
		if(isRandom) {
			for(int i =0;i<msg.length;i++)
				msg[i] = parent.getTestFile().getTestFileContent(i+1);
		}else {
			for(int i =0;i<msg.length;i++)
				msg[i] = parent.getDisplay().getText();
		}
		
		return msg;
	}
	
	
	public void sendWithReceive() {
		parent.getSendBtn().setEnabled(false);
		
		millis = (int)parent.getSleepS().getValue();
		sendCnt = (int)(parent.getCountS().getValue());
		String[] msg = getMsg();
		
		Object[] rowData=null;
		System.out.println("send 시작시간 : "+ new Date());
		while(sendCnt-- > 0) {
			try {
				String content = msg[(int)(Math.random()*(msg.length))];
				pWriter.println( content + "\n" + RunSettingAlias.EOF);
				
				rowData = new Object[] {
											model.getRowCount()+1,
											"TCP",
											"UP",
											sendSocket.getInetAddress().getHostAddress(),
											sendSocket.getPort(),
											content
										};
				model.addRow(rowData);
				
				String rsvMsg = "";
				String line;
				while(!( (line = bReader.readLine()).equals(RunSettingAlias.EOF)) ) { //receive area
					rsvMsg += (line+"\n");
				}
				rowData = new Object[] {
											model.getRowCount()+1,
											"TCP",
											"DOWN",
											sendSocket.getInetAddress().getHostAddress(),
											sendSocket.getPort(),
											rsvMsg
										};
				model.addRow(rowData);
			} catch (IOException e) {
				System.out.println("TCPClient receive 대기중 !!");
			} catch (Exception e) {
				System.out.println("TCPClient 연결 끊어짐 !!");
				parent.stopSetting();
			}
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} //while close
		System.out.println("send 종료시간 : "+ new Date());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		parent.updateTableCaretPosition();
		parent.getSendBtn().setEnabled(true);
	}
	
	public void sendWithoutReceive() {
		parent.getSendBtn().setEnabled(false);
		
		millis = (int)parent.getSleepS().getValue();
		sendCnt = (int)(parent.getCountS().getValue());
		String[] msg = getMsg();
		
		System.out.println("send 시작시간 : "+ new Date());
		while(sendCnt-- > 0) {
			String content = msg[(int)(Math.random() * msg.length)];
			pWriter.println( content + "\n" + RunSettingAlias.EOF);
			
			Object[] rowData = new Object[] {
												model.getRowCount()+1,
												"TCP",
												"UP",
												sendSocket.getInetAddress().getHostAddress(),
												sendSocket.getPort(),
												content
											};
			model.addRow(rowData);
			
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		System.out.println("send 종료시간 : "+ new Date());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		parent.updateTableCaretPosition();
		parent.getSendBtn().setEnabled(true);
	}
	
	public void exit() {
		if(model != null) { //서버 연결이 됐을때
			Object[] rowData = new Object[] {
												model.getRowCount()+1,
												"TCP",
												"DOWN",
												sendSocket.getInetAddress().getHostAddress(),
												sendSocket.getPort(),
												"<html><body><font color='#ff0000'>서버 연결 종료</font></body></html>"
											};
			model.addRow(rowData);
		}
		try {
			if(pWriter != null)
				pWriter.close();
			if(isReader != null)
				isReader.close();
			if(bReader != null)
				bReader.close();
			if(sendSocket != null)
				sendSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("TCP Client 정상적인 종료 !!");
	}

	public int getSendCnt() {
		return sendCnt;
	}
	public void setSendCnt(int sendCnt) {
		this.sendCnt = sendCnt;
	}
	
}
