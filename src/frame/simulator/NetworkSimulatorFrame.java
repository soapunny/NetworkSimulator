package frame.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import protocol.tcp.TCPClient;
import protocol.tcp.TCPServer;
import protocol.udp.UDPClient;
import protocol.udp.UDPServer;
import util.recode.TestFile;

public class NetworkSimulatorFrame extends JFrame implements ActionListener, MouseListener{
	public static final int TEST_FILE_CNT = 5;
	private Color color;
	
	//메인 패널
	private JPanel headPanel;
	private JPanel bodyPanel;
	private JPanel tailPanel;
	
	//headPanel의 패널
	private JPanel serverClientPanel;
	private JPanel protocolPanel;
	private JPanel startStopPanel;
	private JPanel ipPortPanel;
	
	private JRadioButton serverBtn;
	private JRadioButton clientBtn;
	private JCheckBox echoCheckBox;
	private JCheckBox rcvCheckBox;
	
	private JRadioButton tcpBtn;
	private JRadioButton udpBtn;
	
	private JButton startBtn;
	private JButton stopBtn;
	
	private JLabel ipL;
	private JLabel portL;
	private JLabel debugL;
	private JTextField ipT;
	private JTextField portT;
	private JTextField debugT;
	
	//bodyPanel의 패널
	private JPanel tablePanel;
	private JPanel filePanel;
	private JPanel displayPanel;
	
	private JTable table;
	private Object[] tableColumnNames;
	
	private JButton[] testFileBtnArray;
	private JTextArea display;
	
	//tailPanel
	private JLabel sleepL;
	private JSpinner sleepS;
	private JLabel countL;
	private JSpinner countS;
	private JCheckBox randomCheckBox;
	private JButton sendBtn;
	
	//Networking
	private UDPServer udpServer;
	private UDPClient udpClient;
	private TCPServer tcpServer;
	private TCPClient tcpClient;
	
	//TestFile
	private TestFile testFile;
	//Thread pool
	private ThreadPoolExecutor threadPoolExecutor;
	
	
	public NetworkSimulatorFrame() {
		
		initFrame();
		
		createHeadPanel();
		createBodyPanel();
		createTailPanel();
		
		setVisible(true);
		interfaceInit();
		listenerPackage();
	}
//================================== init ======================================================
	public void initFrame() {
		setTitle("Network Simulator By Eui-Seob");
		setBounds(300, 200, 600, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE); //temporary
		setResizable(false);
		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		threadPoolExecutor = (ThreadPoolExecutor)threadPool;
		//color = new Color(0, (int)(Math.random()*256), (int)(Math.random()*256));
		//
	}
	
	public void interfaceInit() {
		serverBtn.setSelected(true);
		tcpBtn.setSelected(true);
		stopBtn.setEnabled(false);
		sendBtn.setEnabled(false);
		ipT.setText("127.0.0.1");
		portT.setText("10001");
		debugT.setText("STOPPED");
		
		testFile = new TestFile(this);
	}
	
	public void startSetting() {
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);
		
		serverBtn.setEnabled(false);
		echoCheckBox.setEnabled(false);
		clientBtn.setEnabled(false);
		rcvCheckBox.setEnabled(false);
		tcpBtn.setEnabled(false);
		udpBtn.setEnabled(false);
		if(clientBtn.isSelected()) //It will be activated only if it run as Client
			sendBtn.setEnabled(true);
	}
	
	public void stopSetting() {
		startBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		
		serverBtn.setEnabled(true);
		echoCheckBox.setEnabled(true);
		clientBtn.setEnabled(true);
		rcvCheckBox.setEnabled(true);
		tcpBtn.setEnabled(true);
		udpBtn.setEnabled(true);
		sendBtn.setEnabled(false);
		if(udpServer != null)
			debugT.setText("UDP Server is terminated");
		if(udpClient != null) {
			udpClient.setSendCnt(0); //쓰레드 탈출시키고
			debugT.setText("UDP Client is terminated");
			udpClient.exit();//socket을 닫아준다.
		}
		if(tcpServer != null) {
			debugT.setText("TCP Server is terminated");
		}
		if(tcpClient != null) {
			tcpClient.setSendCnt(0); //쓰레드 탈출시키고
			debugT.setText("TCP Client is terminated");
			tcpClient.exit();//socket을 닫아준다.
		}
		udpServer = null;
		udpClient = null;
		tcpServer = null;
		tcpClient = null;
		
	}
//================================== head ======================================================
	public void createHeadPanel() {
		headPanel = new JPanel(new BorderLayout());
		
		createServerClientPanel();
		createProtocolPanel();
		createStartStopPanel();
		createIpPortPanel();
		
		this.add(headPanel, BorderLayout.NORTH);
	}
	
	public void createServerClientPanel() {
		serverClientPanel = new JPanel();
		
		serverBtn = new JRadioButton("Server");
		echoCheckBox = new JCheckBox("Echo");
		clientBtn = new JRadioButton("Client");
		rcvCheckBox = new JCheckBox("Receive");
		ButtonGroup group = new ButtonGroup();
		group.add(serverBtn);
		group.add(clientBtn);
		
		Border border = BorderFactory.createTitledBorder("Option1");
		serverClientPanel.setBorder(border);
		
		serverClientPanel.add(serverBtn);
		serverClientPanel.add(echoCheckBox);
		serverClientPanel.add(clientBtn);
		serverClientPanel.add(rcvCheckBox);
		
		headPanel.add(serverClientPanel, BorderLayout.WEST);
	}
	
	public void createProtocolPanel() {
		protocolPanel = new JPanel();
		
		tcpBtn = new JRadioButton("TCP");
		udpBtn = new JRadioButton("UDP");
		
		ButtonGroup group = new ButtonGroup();
		group.add(tcpBtn);
		group.add(udpBtn);
		
		Border border = BorderFactory.createTitledBorder("Option2");
		protocolPanel.setBorder(border);
		
		protocolPanel.add(tcpBtn);
		protocolPanel.add(udpBtn);
		
		headPanel.add(protocolPanel, BorderLayout.CENTER);
	}
	
	public void createStartStopPanel() {
		startStopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		startBtn = new JButton("Start");
		stopBtn = new JButton("Stop");
		
		Border border = BorderFactory.createTitledBorder("Run");
		startStopPanel.setBorder(border);
		
		startStopPanel.add(startBtn);
		startStopPanel.add(stopBtn);
		
		
		headPanel.add(startStopPanel, BorderLayout.EAST);
	}
	
	public void createIpPortPanel() {
		ipPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		ipL = new JLabel("IP");
		ipT = new JTextField(10);
		portL = new JLabel("Port");
		portT = new JTextField(10);
		debugL = new JLabel("Debug");
		debugT = new JTextField(20);
		debugT.setEditable(false);
		
		ipPortPanel.add(ipL);
		ipPortPanel.add(ipT);
		ipPortPanel.add(portL);
		ipPortPanel.add(portT);
		ipPortPanel.add(debugL);
		ipPortPanel.add(debugT);
		
		headPanel.add(ipPortPanel, BorderLayout.SOUTH);
	}
//===================================== body ===================================================
	public void createBodyPanel() {
		bodyPanel = new JPanel(new BorderLayout());
		
		createTablePanel();
		createFilePanel();
		createDisplayPanel();
		
		this.add(bodyPanel, BorderLayout.CENTER);
	}
	
	public void createTablePanel() {
		tablePanel = new JPanel();
		
		tableColumnNames = new Object[]{"No","Protocol", "Up/Down", "IP", "Port", "Message"};
		TableModel tableModel = new DefaultTableModel(tableColumnNames, 0);
		table = new JTable(tableModel);
		
		table.setPreferredScrollableViewportSize(new Dimension(460,230));
		table.setFillsViewportHeight(true);
		//table.setAutoCreateRowSorter(true); //add sorting function warning 발생
		
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		table.getColumnModel().getColumn(1).setPreferredWidth(25);
		table.getColumnModel().getColumn(2).setPreferredWidth(25);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setPreferredWidth(25);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		
		JScrollPane scroll = new JScrollPane(table);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		tablePanel.add(scroll);
		
		bodyPanel.add(tablePanel, BorderLayout.CENTER);
	}
	public void createFilePanel() {
		filePanel = new JPanel(new GridLayout(10, 0));
		
		createTestFileBtnArray();
		
		Border border = BorderFactory.createTitledBorder("Test Message");
		filePanel.setBorder(border);
		
		bodyPanel.add(filePanel, BorderLayout.EAST);
	}
	public void createTestFileBtnArray() {
		testFileBtnArray = new JButton[9];
		for(int i = 0;i<TEST_FILE_CNT;i++) {
			testFileBtnArray[i] = new JButton("Test File "+(i+1));
			filePanel.add(testFileBtnArray[i]);
		}
		for(int i=TEST_FILE_CNT; i<testFileBtnArray.length-1; i++) {
			testFileBtnArray[i] = new JButton("Test File "+(i+1));
			testFileBtnArray[i].setEnabled(false);
			filePanel.add(testFileBtnArray[i]);
		}
		
		filePanel.add(new JLabel());
		//filePanel.add(new JLabel(""));
		testFileBtnArray[8] = new JButton("Save File");
		filePanel.add(testFileBtnArray[8]);
	}
	
	public void createDisplayPanel() {
		displayPanel = new JPanel();
		
		display = new JTextArea(20, 50);
		JScrollPane scroll = new JScrollPane(display);

		Border border = BorderFactory.createTitledBorder("Message Display");
		displayPanel.setBorder(border);
		
		displayPanel.add(scroll);
		
		bodyPanel.add(displayPanel, BorderLayout.SOUTH);
	}
//===================================== tail ===================================================
	public void createTailPanel() {
		tailPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		Border border = BorderFactory.createTitledBorder("Simulation Option");
		tailPanel.setBorder(border);
		
		sleepL = new JLabel("Sleep(msec)");
		SpinnerModel model = new SpinnerNumberModel(100, 0, 10000000, 10);
		sleepS = new JSpinner(model);
		
		countL = new JLabel("Count");
		model = new SpinnerNumberModel(100, 1, 100000000, 10);
		countS = new JSpinner(model);
		
		randomCheckBox = new JCheckBox("By Random");
		sendBtn = new JButton("Send");
		
		tailPanel.add(sleepL);
		tailPanel.add(sleepS);
		tailPanel.add(countL);
		tailPanel.add(countS);
		tailPanel.add(randomCheckBox);
		tailPanel.add(sendBtn);
		
		this.add(tailPanel, BorderLayout.SOUTH);
	}
//================================ Utilities ====================================================
	public void updateTableCaretPosition(){//JTable caret 위치 조정해주는 메서드 ****
		Rectangle rect = table.getCellRect(table.getRowCount()-1, table.getColumnCount()-1, true);
		
		table.scrollRectToVisible(rect);
	}
	
//================================ Listener Package ==============================================
	public void listenerPackage() {
		startBtn.addActionListener(this);
		stopBtn.addActionListener(this);
		
		for(int i=0;i<testFileBtnArray.length;i++) {
			testFileBtnArray[i].addActionListener(this);
		}
		sendBtn.addActionListener(this);
		
		table.addMouseListener(this);
		/*table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				updateTableCaretPosition();
			}
		});*/
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopSetting();
				
				for(int i=3;i>0;i--) {
					try {
						System.out.println("종료 "+i+"초전");
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				
				System.out.println("네트워크 시뮬레이터 종료!!");
				System.exit(0);
			}
		});
	}
	
//================================ Overrided Methods ==============================================
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == startBtn) {
			startSetting();

			//해당 protocol의 server/client 를 실행
			if(serverBtn.isSelected()) { //서버일때
				
				if(tcpBtn.isSelected()) {//tcp
					debugT.setText("TCP Server started !!");
					tcpServer = new TCPServer(this);
					threadPoolExecutor.execute(tcpServer);
				}else { //udp
					debugT.setText("UDP Server started !!");
					udpServer = new UDPServer(this);
					threadPoolExecutor.execute(udpServer);
				}
				
			}else { //클라이언트일때
				
				if(tcpBtn.isSelected()) {//tcp
					debugT.setText("TCP Client started !!");
					tcpClient = new TCPClient(this);
				}else { //udp
					debugT.setText("UDP Client started !!");
					udpClient = new UDPClient(this);
				}
			}
			
		}else if(e.getSource() == stopBtn) {
			stopSetting();
			//실행중인 server/client를 종료
		}else if(e.getSource() == sendBtn) {
			//선택한 protocol Client의 send 역할을 한다.
			if(udpClient != null) {
				udpClient.send();
			}
			if(tcpClient != null) {
				tcpClient.send();
			}
		}else if(e.getSource() == testFileBtnArray[0]) {
			testFile.openTestFile(1);
		}else if(e.getSource() == testFileBtnArray[1]) {
			testFile.openTestFile(2);
		}else if(e.getSource() == testFileBtnArray[2]) {
			testFile.openTestFile(3);
		}else if(e.getSource() == testFileBtnArray[3]) {
			testFile.openTestFile(4);
		}else if(e.getSource() == testFileBtnArray[4]) {
			testFile.openTestFile(5);
		}else if(e.getSource() == testFileBtnArray[5]) {
			testFile.openTestFile(6);
		}else if(e.getSource() == testFileBtnArray[6]) {
			testFile.openTestFile(7);
		}else if(e.getSource() == testFileBtnArray[7]) {
			testFile.openTestFile(8);
		}else if(e.getSource() == testFileBtnArray[testFileBtnArray.length-1]) {
			testFile.saveTestFile();
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
			int row = table.getSelectedRow();
			int column = table.getSelectedColumn();
			if(column==5)
				display.setText(table.getValueAt(row, column).toString());
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
//================================ Setter / Getter ==============================================
	
	public JCheckBox getEchoCheckBox() {
		return echoCheckBox;
	}
	public void setEchoCheckBox(JCheckBox echoCheckBox) {
		this.echoCheckBox = echoCheckBox;
	}
	public JCheckBox getRcvCheckBox() {
		return rcvCheckBox;
	}
	public void setRcvCheckBox(JCheckBox rcvCheckBox) {
		this.rcvCheckBox = rcvCheckBox;
	}
	public JTextField getIpT() {
		return ipT;
	}
	public void setIpT(JTextField ipT) {
		this.ipT = ipT;
	}
	public JTextField getPortT() {
		return portT;
	}
	public void setPortT(JTextField portT) {
		this.portT = portT;
	}
	public JTextField getDebugT() {
		return debugT;
	}
	public void setDebugT(JTextField debugT) {
		this.debugT = debugT;
	}
	public JSpinner getSleepS() {
		return sleepS;
	}
	public void setSleepS(JSpinner sleepS) {
		this.sleepS = sleepS;
	}
	public JSpinner getCountS() {
		return countS;
	}
	public void setCountS(JSpinner countS) {
		this.countS = countS;
	}
	public JCheckBox getRandomCheckBox() {
		return randomCheckBox;
	}
	public void setRandomCheckBox(JCheckBox randomCheckBox) {
		this.randomCheckBox = randomCheckBox;
	}
	public JButton getSendBtn() {
		return sendBtn;
	}
	public void setSendBtn(JButton sendBtn) {
		this.sendBtn = sendBtn;
	}
	public JTable getTable() {
		return table;
	}
	public void setTable(JTable table) {
		this.table = table;
	}
	public JButton getStopBtn() {
		return stopBtn;
	}
	public void setStopBtn(JButton stopBtn) {
		this.stopBtn = stopBtn;
	}
	public JTextArea getDisplay() {
		return display;
	}
	public void setDisplay(JTextArea display) {
		this.display = display;
	}
	public TestFile getTestFile() {
		return testFile;
	}
	public void setTestFile(TestFile testFile) {
		this.testFile = testFile;
	}
}
