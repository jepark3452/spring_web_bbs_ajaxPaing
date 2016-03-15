<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="spring_web_bbs_study.common.util.FormToken" %>
<%
	// 토큰 검사
	if(!FormToken.isValid(request)) {
		out.print("이미 작업이 실행되었거나 비정상적인 요청입니다.");
		return;
	}
	
	out.print(FormToken.set(request));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>FormToken TEST</title>
</head>
<body>
	<h2>글작성완료</h2>
</body>
</html>