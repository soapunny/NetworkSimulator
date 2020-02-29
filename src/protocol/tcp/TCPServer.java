package protocol.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import frame.simulator.NetworkSimulatorFrame;
import util.alias.RunSettingAlias;
import util.exception.ExitException;

public class TCPServer extends Thread{
	private NetworkSimulatorFrame parent;
	
	private InetAddress serverAddress;
	private int serverPort;
	
	private ServerSocket serverSocket;
	//private ArrayList<TCPServerHandler> handlerList;

	public TCPServer(NetworkSimulatorFrame networkSimulatorFrame){
		parent = networkSimulatorFrame;
		//handlerList = new ArrayList<>();
		try {
			serverAddress = InetAddress.getByName(parent.getIpT().getText().trim());
			serverPort = Integer.parseInt(parent.getPortT().getText().trim());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {
		Socket socket = null;
		
		System.out.println("서버 소켓 생성");
		try {
			serverSocket = new ServerSocket(serverPort);
			serverSocket.setSoTimeout(500);
			serverSocket.setReuseAddress(true);
		} catch (IOException e1) {
			parent.getDebugT().setText("Exception : cannot create the ServerSocket with this port");
		} //serverSocket은 계속 새로 생성해야 하는가???
		
		while(parent.getStopBtn().isEnabled()) {
			try {
				socket = null;
				socket = serverSocket.accept();
				if(socket != null) { //낚아채기가 성공했을경우
					System.out.println("핸들러 생성");
					TCPServerHandler handler = new TCPServerHandler(parent, socket);
					//handlerList.add(handler);
					handler.start();
				}
			} catch (IOException e) {
				//System.out.println("TCP Server 낚아채기 대기중 ...");
			} catch (Exception e) {
				break; //서버 강제 종료시 탈출
			}
		}
		exit();
	}
	
	public void exit() {
		
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("TCPServer 정상적인 종료!!");
	}

	
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
}
