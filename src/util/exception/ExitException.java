package util.exception;

public class ExitException extends Exception{
	public ExitException() {
		super("종료를 위해 Exception을 발생합니다.");
	}
}
