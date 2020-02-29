package protocol.udp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.table.DefaultTableModel;

import frame.simulator.NetworkSimulatorFrame;

public class UDPServer extends Thread{
	private DatagramSocket socket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	
	private int serverPort;
	private NetworkSimulatorFrame parent;
	
	private Object[] rowData;
	private DefaultTableModel model;
	private ArrayList<ArrayList<DatagramPacket>> buffer;
	private ThreadPoolExecutor threadPoolExecutor;
	private Runnable processBuffer;
	private int bufferCnt = 0;
	
	public UDPServer(NetworkSimulatorFrame networkSimulatorFrame) {
		try {
			parent = networkSimulatorFrame;
			parent.getIpT().setText(InetAddress.getLocalHost().getHostAddress()); //change the ipT to localhost
			serverPort = Integer.parseInt(parent.getPortT().getText().trim());
			
			socket = new DatagramSocket(serverPort);
			socket.setSoTimeout(500); //wait data 0.5 second
			socket.setReuseAddress(true);
			model = (DefaultTableModel)(parent.getTable().getModel());
			
			ExecutorService threadPool = Executors.newFixedThreadPool(1);
			threadPoolExecutor = (ThreadPoolExecutor)threadPool;
			processBuffer = new Runnable() {
				@Override
				synchronized public void run() {
					int cnt=0;
					DatagramPacket tmp = null;
					while(!(buffer.isEmpty())) {
						while(!(buffer.get(0).isEmpty())) {
							try {
								tmp = buffer.get(0).remove(0);
								rowData = new Object[] {
															model.getRowCount()+1,
															"UDP",
															"DOWN",
															tmp.getAddress().getHostAddress(),
															tmp.getPort(),
															new String(tmp.getData(), "UTF-8")
														};
								model.addRow(rowData);
								cnt++;
								parent.updateTableCaretPosition();
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						buffer.remove(0);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("테이블에 담은 패킷 개수 : "+cnt);
					parent.updateTableCaretPosition();
				}
			};
			
		} catch (UnknownHostException e) {
			parent.getDebugT().setText("Exception : UnknownAddress");
		} catch (SocketException e) {
			parent.getDebugT().setText("Exception : CreateSocket");
		}
	}
	
	@Override
	public void run() {
		
		boolean echo = parent.getEchoCheckBox().isSelected();
		
		if(!echo) { //when echo function is off(rcv only)
			receiveWithoutEcho();
		}else { //when echo function is on(rcv and send)
			receiveWithEcho();
		}
		
		exit();
	}
	
	private void receiveWithoutEcho() {
		buffer = new ArrayList<ArrayList<DatagramPacket>>();
		byte[] receiveBuf = null;
		
		while(parent.getStopBtn().isEnabled()) { //if stop button is not pushed
			try {
				receiveBuf = new byte[128]; //계속 생성하면 에러
				receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
				socket.receive(receivePacket);
				if(bufferCnt%100000 == 0)
					buffer.add(new ArrayList<DatagramPacket>());
				buffer.get(bufferCnt++/100000).add(receivePacket);
			} catch (IOException e) {
				if(bufferCnt != 0) {
					System.out.println("받은 패킷 개수 : "+bufferCnt);
					threadPoolExecutor.execute(processBuffer);
					bufferCnt = 0;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void receiveWithEcho() {
		boolean isDataChanged = false;
		
		while(parent.getStopBtn().isEnabled()) { //if stop button is not pushed
			
			try {
				//받는 쪽
				
				byte[] receiveBuf = new byte[1024];
				receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
				socket.receive(receivePacket);
				
				rowData = new Object[] {	
											model.getRowCount()+1,
											"UDP",
											"DOWN", 
											receivePacket.getAddress().getHostAddress(), 
											receivePacket.getPort(),
											new String(receiveBuf)
										};
				model.addRow(rowData);
				
				//보내는 쪽
				byte[] sendBuf = receiveBuf; //return the received data
				//making sendPacket
				sendPacket = new DatagramPacket(sendBuf,
												sendBuf.length,
												receivePacket.getAddress(),
												receivePacket.getPort());
				socket.send(sendPacket);
				
				rowData = new Object[] {
											model.getRowCount()+1,
											"UDP",
											"UP", 
											receivePacket.getAddress().getHostAddress(), 
											receivePacket.getPort(),
											new String(sendBuf)
										};
				
				model.addRow(rowData);
			} catch (IOException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				parent.updateTableCaretPosition();
			}
		}
	}

	
	public void exit() {
		// 모든 클라이언츠들에게 서버가 종료된다고 알려주고 broadcasting to all client that server will exit soon
		if(socket != null) {
			socket.close();
		}
		System.out.println("UDP 서버 종료 !!!");
	}
}
