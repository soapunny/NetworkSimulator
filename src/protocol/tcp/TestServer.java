//TCP 서버

package protocol.tcp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class TestServerFrame extends JFrame implements ActionListener{
	private JPanel displayPanel;
	private JPanel inputPanel;
	
	private JTextArea display;
	private JTextField input;
	
	
	public TestServerFrame(){
		this.setTitle("TCP Echo Server");
		this.setSize(500, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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
		input = new JTextField(30);
		Font inputFont = new Font("Serif", Font.BOLD, 20);
		input.setFont(inputFont);
		inputPanel.add(input);
		
		
		
		this.add(displayPanel, BorderLayout.CENTER);
		this.add(inputPanel, BorderLayout.SOUTH);
		
		//input.addActionListener(this); useless code, cause it's a server program!!
		
		this.setVisible(true);
		
		startServer();
	}
	
	public void startServer() {
		ServerSocket serverSocket = null;
		
		display.append("TCP Server Start!!\n");
		
		try {
			serverSocket = new ServerSocket(10001);
			display.append("Create Server Socket # " + 10001 + "\n");
		}catch(Exception e) {
			display.append("Cannot open the port : " + 10001 + "\n");
		}
		
		Socket clientSocket = null;
		
		try {
			clientSocket = serverSocket.accept(); // ready in the TCP environment
			display.append("Accept Okay\n"); //already knows about the counterpart's information
			display.append("client info : " + clientSocket.getRemoteSocketAddress().toString() +":"+ clientSocket.getPort() + "\n");
			//displaying the client's address
			
		}catch(Exception e) {
			display.append("Accept Error\n");
		}
		
		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			display.append("Create PrintWriter\n");
			BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream()));
			//clientSocket  -> read by InputStreamReader -> save in Buffer
			display.append("Create BufferedReader\n");
			String inputLine, outputLine;
			while((inputLine = in.readLine()) != null) {
				
				display.append("RCV DATA : " + inputLine + "\n");
				outputLine = "Server Says : "+inputLine;
				out.println(outputLine); //DATA transmitted to the socket
				display.append(outputLine+"\n");
				
				if(inputLine.equals("quit")) {
					break;
				}
			}
			
			if(out != null)
				out.close();
			if(in !=null)
				in.close();
			if(clientSocket!=null)
				clientSocket.close();
			if(serverSocket!=null)
				serverSocket.close();
			display.append("Resources are closed\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == input) {
			display.append(input.getText()+"\n");
			input.selectAll();
		}
	}
}

public class TestServer {

	public static void main(String[] args) {
		TestServerFrame testServerFrame = new TestServerFrame();
	}

}

