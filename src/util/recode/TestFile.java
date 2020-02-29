package util.recode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import frame.simulator.NetworkSimulatorFrame;

public class TestFile {
	private NetworkSimulatorFrame parent;
	private JFileChooser fileChooser;
	
	public TestFile(NetworkSimulatorFrame networkSimulatorFrame) {
		parent = networkSimulatorFrame;
	}
	
	public String getTestFileContent(int fileNum) {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		StringBuffer result = null;
		
		try {
			String path = "TestFiles/testFile"+fileNum+".txt";
			fileReader = new FileReader(path);
			bufferedReader = new BufferedReader(fileReader);
			
			parent.getDisplay().setText("");//clear display
			
			String line;
			result = new StringBuffer();
			while((line = bufferedReader.readLine()) != null) {
				result.append(line+"\n");
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("파일보내기 파일열기 실패");
		} catch (IOException e) {
			System.out.println("파일보내기 파일 쓰기 실패");
			e.printStackTrace();
		} finally {
			try {
				if(fileReader != null)
					fileReader.close();
				if(bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}
	
	public void openTestFile(int fileNum) {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		try {
			String path = "TestFiles/testFile"+fileNum+".txt";
			fileReader = new FileReader(path);
			bufferedReader = new BufferedReader(fileReader);
			
			parent.getDisplay().setText("");//clear display
			
			String line;
			while((line = bufferedReader.readLine()) != null) {
				parent.getDisplay().append(line+"\n");
			}
			
		} catch (FileNotFoundException e) {
			parent.getDisplay().setText("testFile"+fileNum+".txt 파일이 존재하지 않거나 읽을 수 없습니다.");
		} catch (IOException e) {
			System.out.println("파일 쓰기 실패");
			e.printStackTrace();
		} finally {
			try {
				if(fileReader != null)
					fileReader.close();
				if(bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void saveTestFile() {
		fileChooser = new JFileChooser("TestFiles");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
		fileChooser.setFileFilter(filter);
		
		int result;
		
		result = fileChooser.showSaveDialog(parent);
		
		if(result == JFileChooser.APPROVE_OPTION) {
			
			String content =  parent.getDisplay().getText();
			String path = fileChooser.getSelectedFile().getPath();
			System.out.println("path : "+path);
			if(!(path.substring(path.length()-4).equals(".txt"))) {
				path += ".txt";
			}
			
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			
			try {
				fileWriter = new FileWriter(path);
				bufferedWriter = new BufferedWriter(fileWriter);
				
				String[] fileContents = content.split("\n");
				for(String data : fileContents) {
					bufferedWriter.write(data);
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(fileWriter != null) 
						fileWriter.close();
					if(bufferedWriter != null)
						bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
		//fileWriter = new FileWriter("");
	}
}
