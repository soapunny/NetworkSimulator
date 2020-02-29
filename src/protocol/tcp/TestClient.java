//TCP 서버

package protocol.tcp;

//TCP 클라이언트

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.alias.RunSettingAlias;

class TestClientFrame extends JFrame implements ActionListener, WindowListener{
	private JPanel displayPanel;
	private JPanel inputPanel;
	
	private JTextArea display;
	private JTextArea input;
	
	private Socket sendSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	private JButton sendBtn;
	private boolean isExited;
	
	
	public TestClientFrame(){
		this.setTitle("TCP Chatting Client");
		this.setSize(600, 800);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//this.setLayout(new BorderLayout());
		
		JPanel displayPanel = new JPanel();
		//displayPanel.setLayout(new FlowLayout());
		display = new JTextArea(11,30);
		Font displayFont = new Font("Serif", Font.BOLD, 20);
		display.setFont(displayFont);
		display.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(display);
		
		displayPanel.add(scroll);
		
		JPanel inputPanel = new JPanel();
		//inputPanel.setLayout(new FlowLayout());
		input = new JTextArea(11,30);
		Font inputFont = new Font("Serif", Font.BOLD, 20);
		input.setFont(inputFont);
		sendBtn = new JButton("send");
		
		inputPanel.add(input);
		inputPanel.add(sendBtn);
		
		
		
		this.add(displayPanel, BorderLayout.CENTER);
		this.add(inputPanel, BorderLayout.SOUTH);
		
		sendBtn.addActionListener(this);
		this.addWindowListener(this);
		
		this.setVisible(true);
		
		startClient();
		
	}
	
	public void startClient() {
		//make socket for Connect -> Global Variable
		display.append("Start Client\n");
		
		try {
			sendSocket = new Socket(InetAddress.getLocalHost(), 10001);// IP address, port no.
			out = new PrintWriter(sendSocket.getOutputStream(), true); //outputStream, autoFlash
			in = new BufferedReader( new InputStreamReader( sendSocket.getInputStream()));
			sendSocket.setSoTimeout(500);
			
		}catch(Exception e) {
			display.append("Exception : "+ e.getMessage());//unknown host name
			System.exit(1);
		}
		display.append("Connection OK\n");
		
	}
	
	public void receive() {
		
		while(!isExited) {
			
			try {
				String serverMsg = null;
				String msg = "";
				
				while(! ((serverMsg = in.readLine()).equals(RunSettingAlias.EOF))){
					msg += (serverMsg+"\n");
				}
				System.out.println("client msg : "+msg);
				display.append(msg);
			}catch(IOException e) {
				
			}catch(Exception e) {
				break;
			}
		}
		
		closeClient();
	}
	
	public void send() {
		//out.println("보내짐");
		out.println(input.getText()+"\n"+RunSettingAlias.EOF); // pIn the side of Client you need to use print() rather than println()
	}
	
	public void closeClient() {
		try {
			if(in !=null)
				in.close();
			if(out!=null)
				out.close();
			if(sendSocket!=null)
				sendSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("test 클라이언트 종료!!");
			System.exit(1);
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == sendBtn) {
			send();
			display.append(input.getText()+"\n");
			input.selectAll();
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		out.println("<html><body><font color='#ff0000'>클라이언트 종료</font></body></html>\n"+RunSettingAlias.EOF);
		
		isExited = true;
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}

public class TestClient {

	public static void main(String[] args) {
		TestClientFrame testClientFrame = new TestClientFrame();
		testClientFrame.receive();
	}

}