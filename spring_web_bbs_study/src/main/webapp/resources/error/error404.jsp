<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	<%@ taglib	uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<c:set value="${pageContext.request.contextPath}" var="cp"/>
<%
String browser = request.getHeader("User-Agent");	// 브라우저 구해오기
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>ERROR 404</title>
</head>
<body>
<table>
<tr>
<td>
</td>
</tr>
<tr><td height="40px"></td></tr>
<tr><td>해당 페이지는 경로가 변경되었거나. 서버에 존재하지 않아 요청하신 페이지를 찾을 수 없습니다.<br /><br />
                주소 표시줄에 페이지 주소를 올바르게 입력했는지 확인하십시오.</td></tr>
</table>
    <div class="error_wrap">
        <div class="error_body">
        <img src="${cp}/resources/images/service/useintro/error_title01.gif"/>
        <img src="${cp}/resources/images/service/useintro/error_img_404.gif"/>
        </div>
        <div class="error_text">
            <div class="error_text_area gray09 font13">
                해당 페이지는 경로가 변경되었거나. 서버에 존재하지 않아 요청하신 페이지를 찾을 수 없습니다.<br /><br />
                주소 표시줄에 페이지 주소를 올바르게 입력했는지 확인하십시오.
            </div>
        </div>
        <div class="space6"></div>
    </div>
</body>
</html>