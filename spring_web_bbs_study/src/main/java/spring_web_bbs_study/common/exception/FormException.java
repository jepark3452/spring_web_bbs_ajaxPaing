package spring_web_bbs_study.common.exception;

/**
 * Form 토큰 관련 exception class
 * 
 * @author jepark
 */
public class FormException extends Exception{
	private final int ERR_CODE;	// 생성자를 통해 초기화 한다.
	
	public FormException(String msg, int errCode) {	// 생성자
		super(msg);
		ERR_CODE = errCode;
	}
	
	public FormException(String msg) {	// 생성자
		this(msg, 100);	// ERR_CODE를 100(기본값) 으로 초기화 한다.
	}
	
	public int getErrCode() {	// 에러 코드를 얻을 수 있는 메서드도 추가한다.
		return ERR_CODE;
	}
}
