/**
웹프로그램을 좀 더 견고하게 - Token 사용
우리나라는 웹프로그램을 상당히 경시하는 경향이 있습니다. 
실례로 웹프로그램 기술을 특별히 어려운 기술로 인식하고 있기 않기에 현재 제작되고 있는 프로그램들을 
주로 초보 프로그래머들이 개발을 진행합니다.

이러한 결과로 현재 운용되는 사이트의 많은 프로그램들에는 많은 버그들을 가지고 있습니다.
관리자 페이지에 대한 보안정책, 쿠키정보를 통한 사이트 해킹, 자료 추가, 삭제시의 에러등의 많은 문제를 가지고 있습니다.

이번 강좌에서는 이러한 문제중에서 자료를 삭제 및 추가시에 견고성을 위한 Token를 소개하려고 합니다.

Token 사용의 이유

 - 해당 사이트가 아닌 다른 로컬 Form에서 자료 추가 및 삭제 또는 자동 프로그램을 통한 자료 추가 방지
 - 자료의 이중 등록 방지

실례로 자료를 추가시에 우리는 자료가 이중으로 등록되는 경우를 종종 볼 수 있습니다.

게시판과 같은 프로그램에서는 크게 중요하지 않겠지만 은행권이나 소핑몰에서는 이와

같은 문제는 상당히 큰 문제를 발생 시킬수 있습니다.

Token 사용 방법  - 소스 

-- CommandToken.java

package ihelpers.util;

import javax.servlet.http.*;
import java.security.*;

public class CommandToken {
	public static String set(HttpServletRequest req){
		HttpSession session = req.getSession(true);
		long systime = System.currentTimeMillis();
		byte[] time = new Long(systime).toString().getBytes();
		byte[] id = session.getId().getBytes();
		String token = "";
		
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(id);
			md5.update(time);
			
			token = toHex(md5.digest());
			//req.setAttribute("TOKEN",token);
			session.setAttribute("TOKEN",token);
		} catch( Exception e){
			System.err.println("Unable to calculate MD5 Diguests");
		}
		return token;
	}
	
	public static boolean isValid(HttpServletRequest req){
		HttpSession session = req.getSession(true);
		String requestToken = req.getParameter("TOKEN");
		String sessionToken = (String)session.getAttribute("TOKEN");
		
		if(requestToken == null || sessionToken == null)
			return false;
		else 
			return requestToken.equals(sessionToken);
	}
	
	private static String toHex(byte[] digest){
		StringBuffer buf = new StringBuffer();
		
		for(int i=0;i< digest.length;i++)
			buf.append(Integer.toHexString((int)digest[i] & 0x00ff));
		return buf.toString();
	}
}

-- AddDetail.[html]jsp]

<%@ page import="ihelpers.util.*" %>
...
<form method=post action=add.jsp>
<input type=hidden name=TOKEN value="<%=CommandToken.set(request)%>">
..


-- add.jsp

<%@ page import="ihelpers.util.*" %>
<% 

// 토큰 검사
if (!CommandToken.isValid(request)) {
	out.println("이미 작업이 실행되었거나 비정상적인 요청입니다");
	return;
}
CommandToken.set(request);

// 추가 로직 구현

...

Token 사용 방법  - 설명

가장 큰 기본 원리는 Session 과 Parameter의 Unique한 identifier를 통하여 

추가 또는 삭제 로직쪽에서 전달된 Data에 대한 검증을 위하여 

Session에 저장된 Token 값과 Parameter를 통하여 전달된 Token값을 비교 합니다.

AddDetail.jsp or AddDetail.html 에서

hidden tag를 사용하여 아래와 같이 Token 값을 Parameter에 설정합니다.


<input type=hidden name=TOKEN value="<%=CommandToken.set(request)%>">


Add.jsp 에서는 아래와 같이 Session과 Parameter의 값을 비교후 Session에 새로운 Token값을 설정합니다.


// 토큰 검사
if (!CommandToken.isValid(request)) {
	out.println("이미 작업이 실행되었거나 비정상적인 요청입니다");
	return;
}
CommandToken.set(request);


-- REF

	public static final String tokenName = "springWebBbs";

	public static void set(HttpServletRequest req, String tokName) 
	{

		HttpSession session = req.getSession(true); 

		long systime = System.currentTimeMillis();		
		byte[] time = new Long(systime).toString().getBytes();		
		byte[] id = session.getId().getBytes();
		
		try {		
			 MessageDigest md5 = MessageDigest.getInstance("MD5");			
			 md5.update(id);			
			 md5.update(time);			
			 String token = toHex(md5.digest());			
			 req.setAttribute(tokName, token);
			 session.setAttribute(tokName, token);
		} catch (Exception e) {			
			 System.err.println("Unable to calculate MD5 Digests");		
		}
	}

	public static boolean isValid(HttpServletRequest req, String tokName) 
	{

		HttpSession session = req.getSession(true);
		String requestToken = req.getParameter(tokName);
		String sessionToken = (String)session.getAttribute(tokName);
		if (requestToken == null || sessionToken == null)
			return false;
		else
			return requestToken.equals(sessionToken);
	}

	
	public static void set(HttpServletRequest req) 
	{

		HttpSession session = req.getSession(true); 

		long systime = System.currentTimeMillis();		
		byte[] time = new Long(systime).toString().getBytes();		
		byte[] id = session.getId().getBytes();
		
		try {		
			 MessageDigest md5 = MessageDigest.getInstance("MD5");			
			 md5.update(id);			
			 md5.update(time);			
			 String token = toHex(md5.digest());			
			 req.setAttribute(tokenName, token);
			 session.setAttribute(tokenName, token);
		} catch (Exception e) {			
			 System.err.println("Unable to calculate MD5 Digests");		
		}
	}

	public static boolean isValid(HttpServletRequest req) 
	{

		HttpSession session = req.getSession(true);
		String requestToken = req.getParameter(tokenName);
		String sessionToken = (String)session.getAttribute(tokenName);
		if (requestToken == null || sessionToken == null)
			return false;
		else
			return requestToken.equals(sessionToken);
	}

	private static String toHex(byte[] digest) 
	{
		StringBuffer buf = new StringBuffer();
		for (int i=0; i < digest.length; i++)
			buf.append(Integer.toHexString((int)digest[i] & 0x00ff));
		return buf.toString();

	}

위와 같은 원리를 이용하여 간단한 프로그램을 작성후에 자료를 추가후에 다시 브라우저에서
reload를 해 보십시요.
 */
package spring_web_bbs_study.common.util;

import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author jepark
 *
 */
public class FormToken {
	public static String set(HttpServletRequest req){
		HttpSession session = req.getSession(true);
		long systime = System.currentTimeMillis();
		byte[] time = new Long(systime).toString().getBytes();
		byte[] id = session.getId().getBytes();
		String token = "";
		
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(id);
			md5.update(time);
			
			token = toHex(md5.digest());
			//req.setAttribute("TOKEN",token);
			session.setAttribute("TOKEN",token);
		} catch( Exception e){
			System.err.println("Unable to calculate MD5 Diguests");
		}
		return token;
	}
	
	public static boolean isValid(HttpServletRequest req){
		HttpSession session = req.getSession(true);
		String requestToken = req.getParameter("TOKEN");
		String sessionToken = (String)session.getAttribute("TOKEN");
		
		if(requestToken == null || sessionToken == null)
			return false;
		else 
			return requestToken.equals(sessionToken);
	}
	
	private static String toHex(byte[] digest){
		StringBuffer buf = new StringBuffer();
		
		for(int i=0;i< digest.length;i++)
			buf.append(Integer.toHexString((int)digest[i] & 0x00ff));
		return buf.toString();
	}
}
