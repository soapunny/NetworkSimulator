package protocol.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.table.DefaultTableModel;

import frame.simulator.NetworkSimulatorFrame;
import util.alias.RunSettingAlias;

public class TCPServerHandler extends Thread{
	private NetworkSimulatorFrame parent;
	private Socket handlerSocket;
	
	private BufferedReader bReader;
	private InputStreamReader isReader;
	private PrintWriter pWriter;
	
	private DefaultTableModel model;
	
	public TCPServerHandler(NetworkSimulatorFrame networkSimulatorFrame, Socket socket){
		parent = networkSimulatorFrame;
		handlerSocket = socket;
		try {
			handlerSocket.setSoTimeout(500);
			handlerSocket.setReuseAddress(true);
			

			try {
				isReader = new InputStreamReader(handlerSocket.getInputStream());
				bReader = new BufferedReader(isReader);
				pWriter = new PrintWriter(handlerSocket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			model = (DefaultTableModel)(parent.getTable().getModel());
			Object[] rowData = new Object[] {
												model.getRowCount()+1,
												"TCP",
												"DOWN",
												handlerSocket.getInetAddress().getHostAddress(),
												handlerSocket.getPort(),
												"<html><body><font color='#ff0000'>클라이언트 연결</font></body></html>"
											};
			model.addRow(rowData);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		boolean isEchoChecked = parent.getEchoCheckBox().isSelected();
		if(!isEchoChecked) {
			System.out.println("receiveWithoutEcho() 실행");
			receiveWithoutEcho();
		}else {
			System.out.println("receiveWithEcho() 실행");
			receiveWithEcho();
		}
		
		exit();
	}
	
	public void receiveWithoutEcho() {
		boolean isDataChanged = false;
		
		while(parent.getStopBtn().isEnabled()) {
			
			try {
				StringBuffer contents = new StringBuffer();
				
				String line = null;
				while(! ((line = bReader.readLine()).equals(RunSettingAlias.EOF))) { //EOF 암호화
					contents.append(line);
					contents.append("\n");
				}
				
				Object[] rowData = new Object[] {
													model.getRowCount()+1,
													"TCP",
													"DOWN",
													handlerSocket.getInetAddress().getHostAddress(),
													handlerSocket.getPort(),
													contents.toString()
												};
				model.addRow(rowData);
				isDataChanged = true;
			} catch (IOException e) {
				if(isDataChanged)
					parent.updateTableCaretPosition();
				isDataChanged = false;
			} catch (Exception e) {
				break;
			}
		}
		
	}
	
	public void receiveWithEcho() {
		boolean isDataChanged = false;
		
		while(parent.getStopBtn().isEnabled()) {
			
			try {
				StringBuffer contents = new StringBuffer();

				String line = null;
				while(!((line = bReader.readLine()).equals(RunSettingAlias.EOF))) {
					contents.append(line);
					contents.append("\n");
				}
				
				Object[] rowData = new Object[] {
													model.getRowCount()+1,
													"TCP",
													"DOWN",
													handlerSocket.getInetAddress().getHostAddress(),
													handlerSocket.getPort(),
													contents.toString()
												};
				model.addRow(rowData);
				
				rowData = new Object[] {
											model.getRowCount()+1,
											"TCP",
											"UP",
											handlerSocket.getInetAddress().getHostAddress(),
											handlerSocket.getPort(),
											contents.toString()
										};

				contents.append(RunSettingAlias.EOF);
				pWriter.println(contents.toString());
				
				model.addRow(rowData);
				model.fireTableDataChanged();
				isDataChanged = true;
			} catch (IOException e) {
				if(isDataChanged)
					parent.updateTableCaretPosition();
				isDataChanged = false;
			} catch (Exception e) {
				break;
			}
		}
	}
	
	public void exit() {
		Object[] rowData = new Object[] {
											model.getRowCount()+1,
											"TCP",
											"DOWN",
											handlerSocket.getInetAddress().getHostAddress(),
											handlerSocket.getPort(),
											"<html><body><font color='#ff0000'>클라이언트 연결 종료</font></body></html>"
										};
		model.addRow(rowData);
		
		try {
			if(isReader != null)
				isReader.close();
			if(bReader != null)
				bReader.close();
			if(pWriter != null)
				pWriter.close();
			if(handlerSocket != null)
				handlerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("TCPServerHandler 정상적인 종료!!");
	}
}
