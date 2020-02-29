package frame.parents;

import java.io.BufferedReader;
import java.io.Reader;

import util.exception.ExitException;

public class MyBufferedReader extends BufferedReader{ 
	
	public MyBufferedReader(Reader in, int sz) {
		super(in, sz);
	}
	public MyBufferedReader(Reader in) {
		super(in);
	}
	public void makeExitException() throws ExitException{
		throw new ExitException();
	}
	
}
