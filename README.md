# spring_web_bbs_study > spring_web_bbs_ajaxPaing

이번글에서는 jQuery를 이용하여 페이징 태그를 만들고 Ajax를 이용하는 방법에 대해서 이야기 합니다.

사실 jQuery를 이용하여 페이징 태그를 만드는건 실제로 적용을 해본적이 없었습니다. 

Ajax를 이용한 페이징은 스크롤페이징 기능을 만들기 위해서 해봤었던 거라서 어떠한 구멍이 있을지는 잘 모르겠네요.

이 글을 보고 이상한점이 발견되면 말씀해주세요. 

그리고 이번글에서 처음에 jsonView를 설정하는 부분이 있습니다. 

기존글에서 jsonView를 설정하는데 에러가 발생한다는 분들도 있어서 해당 라이브러리를

글을 쓰는 현재 최신버전으로 바꿨습니다. 기존에 jsonView가 설정이 되어있던 분들도 해당 내용을 따라해주세요.

------------------------------------------------------------------------------------

1. 공통 기능 
지난번 글에서 작성한 전자정부 프레임워크를 이용한 페이징과 마찬가지로 공통적으로 사용하는 기능을 먼저 만들도록 하자. 
거의 대부분은 비슷하지만 약간씩 다른데, 쿼리는 똑같기 때문에 다시 언급하지 않도록 하겠다.

1. 라이브러리 및 jsonView 설정
먼저 Ajax를 사용하여 클라이언트와 서버의 데이터 통신을 할때 사용할 jsonView를 설정하려고 한다. JSON이 어떤것인지는 대부분 알것이라 생각해서 특별히 언급을 할 필요는 없을것이라고 생각한다. 

1) 라이브러리 추가
먼저 pom.xml에 다음의 내용을 추가한다. 
?
1
2
3
4
5
6
7
8
9
10
11
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.6.4</version>
</dependency>
 
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.6.4</version>
</dependency>
이 라이브러리를 이용하면 어떠한 형태의 데이터도 json 형식의 데이터로 자동으로 변환을 해준다. 이것은 나중에 어떻게 보여지는지 살펴보도록 하겠다.

2) jsonView 설정
위에서 추가한 라이브러리를 이용하여 jsonView를 설정할 차례이다. 
action-servlet.xml을 다음과 같이 변경한다.
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:context="http://www.springframework.org/schema/context"
    xmlns:p="http://www.springframework.org/schema/p"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
       http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd
       http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">
 
    <context:component-scan base-package="first" use-default-filters="false">
        <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller"/>
    </context:component-scan>
     
    <mvc:annotation-driven>
        <mvc:argument-resolvers>
            <bean class="first.common.resolver.CustomMapArgumentResolver"></bean>       
        </mvc:argument-resolvers>
    </mvc:annotation-driven>
     
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean id="loggerInterceptor" class="first.common.logger.LoggerInterceptor"></bean>
        </mvc:interceptor>
    </mvc:interceptors>
     
    <aop:aspectj-autoproxy/>
    <bean id="loggerAspect" class="first.common.logger.LoggerAspect" />
     
    <bean class="org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping"/>
     
    <bean class="org.springframework.web.servlet.view.BeanNameViewResolver" p:order="0" />
    <bean id="jsonView" class="org.springframework.web.servlet.view.json.MappingJackson2JsonView" />
     
    <bean
        class="org.springframework.web.servlet.view.UrlBasedViewResolver" p:order="1"
        p:viewClass="org.springframework.web.servlet.view.JstlView"
        p:prefix="/WEB-INF/jsp/" p:suffix=".jsp">
    </bean>
</beans>
36번째 줄에 jsonView라는것이 추가된것을 확인할 수 있다. 여기서 jsonView라는 것은 추후 Controller에서 사용될 것이다.

2. AbstractDAO
먼저 페이징을 처리하는 로직을 만들도록 하자. AbstractDAO.java에 다음의 내용을 작성하자.
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
@SuppressWarnings("unchecked")
public Object selectPagingList(String queryId, Object params){
    printQueryId(queryId);
    Map<String,Object> map = (Map<String,Object>)params;
     
    String strPageIndex = (String)map.get("PAGE_INDEX");
    String strPageRow = (String)map.get("PAGE_ROW");
    int nPageIndex = 0;
    int nPageRow = 20;
     
    if(StringUtils.isEmpty(strPageIndex) == false){
        nPageIndex = Integer.parseInt(strPageIndex)-1;
    }
    if(StringUtils.isEmpty(strPageRow) == false){
        nPageRow = Integer.parseInt(strPageRow);
    }
    map.put("START", (nPageIndex * nPageRow) + 1);
    map.put("END", (nPageIndex * nPageRow) + nPageRow);
     
    return sqlSession.selectList(queryId, map);
}
간단히 소스를 살펴보자.

먼저 6~16번째 줄이 현재 페이지 번호와 한 페이지에 보여줄 행의 개수를 계산하는 부분이다.
화면에서 PAGE_INDEX와 PAGE_ROW라는 값을 보내주도록 되어있지만, 혹시 모를 예외상황에 대비하여 해당 값을 각각 0과 20으로 설정하였다.
그 다음 17, 18번째 줄에서 페이징 쿼리의 시작과 끝값을 계산하도록 하였다. 
그 후 일반적인 리스트 조회를 호출하도록 하였다.

전자정부 프레임워크를 사용할때와는 다르게 계산할 것이 많이 없고, 반환값도 List인것을 확인할 수 있다. 

3. CSS
사실 여기서는 안해도 되는 부분이지만 나중에 페이징 결과를 좀 더 보기 쉽게 할수있게 스타일시트를 수정한다.
ui.css를 열어서 다음의 내용을 추가한다.
?
1
.pad_5 {padding: 5px;}

4. 자바스크립트
이제 이번글의 핵심인 스크립트의 작성 부분이다. 여기서는 크게 두가지에 대해서 이야기를 할것이다.
첫번째로는 Ajax 호출에 대한 내용이고 두번째는 jQuery를 이용한 페이징 태그를 만드는 함수이다.

1) Ajax
먼저 Ajax가 무엇인지 아주 간단히 이야기를 하려고 한다. 
Ajax는 Asynchronous JavaScript and Xml 의 약자로 클라이언트(웹브라우저)와 서버의 비동기적 통신을 통한 데이터 전송을 이용하는 방법이다. Ajax는 클라이언트와 서버가 내부적으로 데이터 통신을 하고 그 결과를 웹페이지에 프로그래밍적으로 반영한다. 그 결과 화면의 로딩없이 그 결과를 보여줄 수 있다.
jQuery에서는 Ajax 통신을 쉽게 하는 ajax() 함수를 제공하고 있다. 
인터넷에서 ajax 함수에 대한 기본적인 설명 및 사용법은 많이 나와있으니 넘어가고, 여기서는 기존의 ComSubmit과 같이 Ajax를 공통함수로 만들어서 사용하려고 한다.
common.js에 다음의 내용을 작성하자.
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
var gfv_ajaxCallback = "";
function ComAjax(opt_formId){
    this.url = "";      
    this.formId = gfn_isNull(opt_formId) == true ? "commonForm" : opt_formId;
    this.param = "";
     
    if(this.formId == "commonForm"){
        var frm = $("#commonForm");
        if(frm.length > 0){
            frm.remove();
        }
        var str = "<form id='commonForm' name='commonForm'></form>";
        $('body').append(str);
    }
     
    this.setUrl = function setUrl(url){
        this.url = url;
    };
     
    this.setCallback = function setCallback(callBack){
        fv_ajaxCallback = callBack;
    };
 
    this.addParam = function addParam(key,value){ 
        this.param = this.param + "&" + key + "=" + value; 
    };
     
    this.ajax = function ajax(){
        if(this.formId != "commonForm"){
            this.param += "&" + $("#" + this.formId).serialize();
        }
        $.ajax({
            url : this.url,    
            type : "POST",   
            data : this.param,
            async : false, 
            success : function(data, status) {
                if(typeof(fv_ajaxCallback) == "function"){
                    fv_ajaxCallback(data);
                }
                else {
                    eval(fv_ajaxCallback + "(data);");
                }
            }
        });
    };
}
간단히 소스를 보자. 
일단 기본적인 틀은 기존에 만들었던 ComSubmit 객체와 비슷한것을 알 수 있다. 
여기서 추가된것은 크게 setCallback이라는 함수와 submit() 대신에 ajax() 라는 함수가 추가되었다. 


먼저 setCallback은 ajax를 이용하여 데이터를 전송한 후 호출될 콜백함수의 이름을 지정하는 함수이다. 
Ajax는 클라이언트와 비동기적으로 수행되기 때문에 return을 받을수가 없다. 따라서 클라이언트가 서버에 어떠한 동작을 요청하고 그에 따른 결과가 다시 클라이언트측에 전달될 때 호출되는것이 콜백함수다. 여기서는 setCallback이라는 함수를 이용하여 ajax 요청 후 호출될 함수의 이름을 지정하는 것이다.
이에 대한 내용은 잠시 후 실제 소스에서 다시 보도록 하겠다. 

그 다음으로는 ajax() 함수이다. 이 함수가 실질적인 ajax 기능을 수행한다. 
submit의 경우 어떠한 요청을 하면 화면이 바뀌기때문에 그 안의 기능이 많지 않았지만, ajax의 경우는 설정을 해야할 게 몇가지가 있다. 
jQuery를 이용한 ajax는 여러가지 설정할 수 있는데, 여기서는 간단히 몇가지만 설정을 하였다. 
url은 호출할 url을 의미하고, type은 POST 또는 GET 방식의 통신을 설정한다. 여기서는 그냥 POST로 지정을 하였다. 
그리고 data 부분이 ajax를 이용하여 서버에 요청을 할 때 서버로 전달할 인자(Parameter)를 의미한다. 
원래는 저런 방식으로 하지않고 object 형식으로 data를 지정하지만, 여기서는 addParam또는 form 자체를 전송하기 때문에 저런식으로 하였다.
그 다음 async는 동기식과 비동기식의 통신방식을 의미한다. 
동기식은 클라이언트 -> 서버 -> 클라이언트의 과정에서 서버의 답변이 올때까지 다른 일을 수행하지 못하고 기다리기만 하는 방식이고,
비동기식은 요청을 보내고 다른일을 수행할 수 있다. (이에 대한 좀 더 자세한 설명은 쉽게 찾아볼 수 있으니 이정도만 이야기하려고 한다.)
여기서는 그냥 비동기식으로 설정을 하였다. 
여기서 나온 설정 및 다른 설정은 jQuery 공식홈페이지의 ajax 부분을 보면 자세하게 나와있다. (http://api.jquery.com/jquery.ajax/)

여기까지 해서 ajax 함수에 대한 설명은 간단히 끝났다. 아직은 무슨소리인지 이해가 가지 않는게 많을수도 있고 이걸 어떻게 쓰는지 모르겠지만, 나중에 화면에서 이를 어떻게 사용하는지를 보면서 다시 한번 설명을 하도록 하겠다.

2) 페이징 태그
다음으로는 페이징 태그를 만드는 부분이다. 
프로젝트에서는 페이징을 사용하는 화면이 여러개가 있기 때문에 JSP에서는 함수를 호출하여 간단히 페이징 태그 작성 및 기능을 수행하도록 공통함수로 만들었다.
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
/*
divId : 페이징 태그가 그려질 div
pageIndx : 현재 페이지 위치가 저장될 input 태그 id
recordCount : 페이지당 레코드 수
totalCount : 전체 조회 건수 
eventName : 페이징 하단의 숫자 등의 버튼이 클릭되었을 때 호출될 함수 이름
*/
var gfv_pageIndex = null;
var gfv_eventName = null;
function gfn_renderPaging(params){
    var divId = params.divId; //페이징이 그려질 div id
    gfv_pageIndex = params.pageIndex; //현재 위치가 저장될 input 태그
    var totalCount = params.totalCount; //전체 조회 건수
    var currentIndex = $("#"+params.pageIndex).val(); //현재 위치
    if($("#"+params.pageIndex).length == 0 || gfn_isNull(currentIndex) == true){
        currentIndex = 1;
    }
     
    var recordCount = params.recordCount; //페이지당 레코드 수
    if(gfn_isNull(recordCount) == true){
        recordCount = 20;
    }
    var totalIndexCount = Math.ceil(totalCount / recordCount); // 전체 인덱스 수
    gfv_eventName = params.eventName;
     
    $("#"+divId).empty();
    var preStr = "";
    var postStr = "";
    var str = "";
     
    var first = (parseInt((currentIndex-1) / 10) * 10) + 1;
    var last = (parseInt(totalIndexCount/10) == parseInt(currentIndex/10)) ? totalIndexCount%10 : 10;
    var prev = (parseInt((currentIndex-1)/10)*10) - 9 > 0 ? (parseInt((currentIndex-1)/10)*10) - 9 : 1; 
    var next = (parseInt((currentIndex-1)/10)+1) * 10 + 1 < totalIndexCount ? (parseInt((currentIndex-1)/10)+1) * 10 + 1 : totalIndexCount;
     
    if(totalIndexCount > 10){ //전체 인덱스가 10이 넘을 경우, 맨앞, 앞 태그 작성
        preStr += "<a href='#this' class='pad_5' onclick='_movePage(1)'>[<<]</a>" +
                "<a href='#this' class='pad_5' onclick='_movePage("+prev+")'>[<]</a>";
    }
    else if(totalIndexCount <=10 && totalIndexCount > 1){ //전체 인덱스가 10보다 작을경우, 맨앞 태그 작성
        preStr += "<a href='#this' class='pad_5' onclick='_movePage(1)'>[<<]</a>";
    }
     
    if(totalIndexCount > 10){ //전체 인덱스가 10이 넘을 경우, 맨뒤, 뒤 태그 작성
        postStr += "<a href='#this' class='pad_5' onclick='_movePage("+next+")'>[>]</a>" +
                    "<a href='#this' class='pad_5' onclick='_movePage("+totalIndexCount+")'>[>>]</a>";
    }
    else if(totalIndexCount <=10 && totalIndexCount > 1){ //전체 인덱스가 10보다 작을경우, 맨뒤 태그 작성
        postStr += "<a href='#this' class='pad_5' onclick='_movePage("+totalIndexCount+")'>[>>]</a>";
    }
     
    for(var i=first; i<(first+last); i++){
        if(i != currentIndex){
            str += "<a href='#this' class='pad_5' onclick='_movePage("+i+")'>"+i+"</a>";
        }
        else{
            str += "<b><a href='#this' class='pad_5' onclick='_movePage("+i+")'>"+i+"</a></b>";
        }
    }
    $("#"+divId).append(preStr + str + postStr);
}
 
function _movePage(value){
    $("#"+gfv_pageIndex).val(value);
    if(typeof(gfv_eventName) == "function"){
        gfv_eventName(value);
    }
    else {
        eval(gfv_eventName + "(value);");
    }
}
페이징 태그를 작성하는 함수는 두개로 구성이 되어있다. 
첫번째는 gfn_renderPaging이라는 함수로 페이징 태그를 작성하는 역할을 한다.
두번째로는 내부적으로 사용할 함수로 _movePage라는 함수가 있다. 이는 페이징 태그를 클릭하였을 경우 해당 페이지로 이동하는 역할을 한다. 

이제 소스를 간단히 살펴보도록 하자.
먼저 gfn_renderPaging 함수부터 보자.
1~7번째줄에는 주석이 달려있는것을 볼 수 있다. 이는 이 함수를 사용할 때 필요한 파라미터들을 적어놓은 것이다. 
함수에서 파라미터는 params라는 값 하나만 받는데 어떻게 저런 파라미터들을 받는지는 JSP에서 이야기를 할것이다. 여기서는 그냥 저런 이름들이 있다는 것만 확인하면 된다. 
이 함수에서 코드가 복잡한 부분은 없다. 단지 페이지당 레코드 수나 인덱스 수를 계산하거나, 아니면 태그를 만드는 부분이 살짝 복잡하다. 
27~29번째 줄에는 각각 3개의 변수가 선언되어 있는것을 볼 수 있다. 
이는 각각 맨앞으로 이동 태그,  1~10 등과 같은 인덱스 태그, 맨 뒤로 이동 태그를 담당한다. 
전체의 인덱스가 10을 초과할 경우 preStr 변수에는 맨앞, 앞 태그를 작성하고, 전체의 인덱스가 10 이하일 경우, 맨앞으로 이동 태그만 만들것이다.
마찬가지로 맨뒤, 뒤 태그도 postStr에 작성된다. 이는 전체의 인덱스에 따라 유동적으로 결정될것이다. 
그 다음 str 변수에는 인덱스가 담길것이다. 

각 태그는 <a>태그를 사용해서 작성했으며, 각 태그가 클릭되었을 때 _movePage라는 함수를 호출하게 되어있다.
_movePage는 해당 태그가 클릭되었을 때, JSP에서 선언한 함수를 호출하게끔 구성되어있다. 

지금 이러한 태그를 가지고 아무리 자세하게 설명한다고 하더라도 이해하기는 쉽지 않을것이다. 일단은 대충 이런 역할을 한다는것만 살펴보고 넘어가도록 하자.
다음에 나올 내용인 JSP 부분에서 다시 한번 살펴볼 것이다.

2. 개발 소스
이제 위에서 작성한 공통기능을 사용하여 페이징 기능을 사용할 차례이다. 위에서 작성한것을 바탕으로 기존의 게시판을 변경하도록 하겠다. (지난글에서 작성한 전자정부 프레임워크를 사용한 페이징이 아닌 일반 게시판을 기준으로 진행한다.)

1. JSP
먼저 jsp를 변경하자.
boardList.jsp를 다음과 같이 수정한다. 
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99
100
101
102
103
104
105
106
107
108
109
110
111
112
113
114
115
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/include/include-header.jspf" %>
</head>
<body>
    <h2>게시판 목록</h2>
    <table class="board_list">
        <colgroup>
            <col width="10%"/>
            <col width="*"/>
            <col width="15%"/>
            <col width="20%"/>
        </colgroup>
        <thead>
            <tr>
                <th scope="col">글번호</th>
                <th scope="col">제목</th>
                <th scope="col">조회수</th>
                <th scope="col">작성일</th>
            </tr>
        </thead>
        <tbody>
             
        </tbody>
    </table>
     
    <div id="PAGE_NAVI"></div>
    <input type="hidden" id="PAGE_INDEX" name="PAGE_INDEX"/>
     
    <br/>
    <a href="#this" class="btn" id="write">글쓰기</a>
     
    <%@ include file="/WEB-INF/include/include-body.jspf" %>
    <script type="text/javascript">
        $(document).ready(function(){
            fn_selectBoardList(1);
             
            $("#write").on("click", function(e){ //글쓰기 버튼
                e.preventDefault();
                fn_openBoardWrite();
            }); 
             
            $("a[name='title']").on("click", function(e){ //제목 
                e.preventDefault();
                fn_openBoardDetail($(this));
            });
        });
         
         
        function fn_openBoardWrite(){
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/sample/openBoardWrite.do' />");
            comSubmit.submit();
        }
         
        function fn_openBoardDetail(obj){
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/sample/openBoardDetail.do' />");
            comSubmit.addParam("IDX", obj.parent().find("#IDX").val());
            comSubmit.submit();
        }
         
        function fn_selectBoardList(pageNo){
            var comAjax = new ComAjax();
            comAjax.setUrl("<c:url value='/sample/selectBoardList.do' />");
            comAjax.setCallback("fn_selectBoardListCallback");
            comAjax.addParam("PAGE_INDEX",pageNo);
            comAjax.addParam("PAGE_ROW", 15);
            comAjax.ajax();
        }
         
        function fn_selectBoardListCallback(data){
            var total = data.TOTAL;
            var body = $("table>tbody");
            body.empty();
            if(total == 0){
                var str = "<tr>" + 
                                "<td colspan='4'>조회된 결과가 없습니다.</td>" + 
                            "</tr>";
                body.append(str);
            }
            else{
                var params = {
                    divId : "PAGE_NAVI",
                    pageIndex : "PAGE_INDEX",
                    totalCount : total,
                    eventName : "fn_selectBoardList"
                };
                gfn_renderPaging(params);
                 
                var str = "";
                $.each(data.list, function(key, value){
                    str += "<tr>" + 
                                "<td>" + value.IDX + "</td>" + 
                                "<td class='title'>" +
                                    "<a href='#this' name='title'>" + value.TITLE + "</a>" +
                                    "<input type='hidden' name='title' value=" + value.IDX + ">" + 
                                "</td>" +
                                "<td>" + value.HIT_CNT + "</td>" + 
                                "<td>" + value.CREA_DTM + "</td>" + 
                            "</tr>";
                });
                body.append(str);
                 
                $("a[name='title']").on("click", function(e){ //제목 
                    e.preventDefault();
                    fn_openBoardDetail($(this));
                });
            }
        }
    </script> 
</body>
</html>
지난번보다 좀 길어진것을 볼 수 있다. 
먼저 살펴봐야 할것은 29~30번째 줄이다. 
29번째 줄의 <div id="PAGE_NAVI"></div> 부분은 앞으로 페이징 태그가 그려질 부분이다. 
30번째 줄은 현재 페이지 번호가 저장될것이다.

그 다음으로는 38번째 줄에서 fn_selectBoardList(1)을 호출하는것을 봐야한다.
이것은 최초에 화면이 호출되면 1페이지의 내용을 조회하는 것을 의미한다.
그럼 fn_selectBoardList는 어떻게 되어있는지 확인해보자.

65번째 줄의 fn_selectBoardList를 살펴보면 파라미터로 pageNo를 받는것을 알 수 있다. 여기서 pageNo는 호출하고자 하는 페이지 번호를 의미한다.
위에서 만든 ComAjax를 사용하는것을 볼 수 있다.
대부분은 ComSubmit과 비슷하지만 setCallback이라는 함수가 추가된것을 확인할 수 있다. setCallback은 Ajax 요청이 완료된 후 호출될 함수의 이름을 지정하는 함수이다. 
여기서는 콜백함수로 fn_selectBoardListCallback 이라는 이름의 함수를 지정했는데, 이것은 74번째줄의 fn_selectBoardListCallback(data) 함수를 의미한다. 
ComAjax함수에서는 두개의 파라미터를 전송하고 있다. PAGE_INDEX와 PAGE_ROW가 그것인데 각각 현재 페이지 번호와 한 페이지에 보여줄 행(데이터)의 수를 의미한다. 
여기까지만 이야기하고 먼저 실행된 결과화면을 살펴보도록 하자.


나중에 서버단까지 개발을 완료하고 실행을 시키면 다음과 같은 화면을 볼 수 있다. 
한 페이지에 15개의 데이터를 보여주며 맨앞, 맨뒤를 의미하는 [<<], [>>] 버튼과 앞 뒤를 의미하는 [<], [>] 버튼, 그리고 1~10의 페이지 번호가 있는것을 볼 수 있다.
여기서 만들어진 페이징 태그가 앞에서 만들었던 gfn_renderPaging 함수에 의해서 만들어지는 부분이다. JSP에서는 gfn_renderPaging 함수를 fn_selectBoardListCallback 함수에서 호출했는데, 이 부분은 잠시 후에 살펴볼것이다. 
여기서는 JSP에서 단지 <div id="PAGE_NAVI"></div> 태그만 작성해 놓고 공통함수를 이용해서 페이징 태그가 작성되는것을 확인하면 된다.

그다음으로 fn_selectBoardListCallback 함수를 보자. 
이 함수는 ajax 호출이 되고 난 후 실행되는 콜백함수로 여기서는 화면을 다시 그리는 역할을 수행한다. 
Ajax는 기본적으로 비동기식호출이기 때문에 서버에 요청을 하고 그 결과값을 받더라도 화면의 전환이 일어나지 않는다. 따라서 결과값을 받은 후, 데이터의 갱신 등을 따로 해줘야한다. 이것은 submit을 할때와 다른 점으로, 화면 갱신이 일어나지 않기 때문에 JSTL등을 이용하여 목록 등을 만들수가 없다. 

그 과정을 이제 하나씩 살펴보도록 하겠다. 
여기서는 바로 전글(http://addio3305.tistory.com/89)에서 테이블의 <tbody> 부분과 비교를 하면서 보는것이 좋다.
가장 먼저 fn_selectBoardListCallback의 파라미터인 data는 서버에서 전송된 json 형식의 결과값이다. 이 값을 어떻게 보내주는지는 잠시 후에 보도록 하겠다. 
만약 조회된 결과가 0일 경우 (data.TATAL == 0) 조회된 결과가 없기 때문에 화면에는 조회된 결과가 없다고 표시한다. 
이것은 바로 이전글에서 <c:otherwise> 태그에 해당한다. 

반대로 데이터가 존재할 경우 84번째 줄부터가 실행된다. 
85~90번째줄은 앞에서 만든 gfn_renderPaging 함수를 수행하기 위해서 파라미터를 만드는 과정이다. Javascript에서 "var 변수명 = {} " 이렇게 선언을 하면 Object가 만들어지고, 거기에 각각 key와 value 형식으로 값을 추가할 수 있다.
divId, pageIndex, totalCount, eventName은 key가 되고, "PAGE_NAVI", "PAGE_INDEX", totla, "fn_selectBoardList"는 value가 된다. 
그 후 gfn_renderPaging 함수를 호출하면 object의 값을 이용하여 페이징 태그를 만들게 된다.

94번째 줄 부터는 이전글의 <c:forEach> 태그를 이용하여 테이블의 목록을 만든것과 같은 역할을 수행한다. 94번째 줄의 data.list 가 서버에서 보내준 데이터이고, 이를 이용해서 jQuery의 .each 함수를 사용하여 HTML 태그를 만들어주는것을 볼 수 있다.

그리고 마지막으로 새롭게 추가된 각각의 목록의 제목에 상세보기로 이동할 수 있도록 click 이벤트를 바인딩 해주게 된다. 

2. JAVA
1) Contoller
SampleContoller를 열어서 다음을 작성하자. 
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
@RequestMapping(value="/sample/openBoardList.do")
public ModelAndView openBoardList(CommandMap commandMap) throws Exception{
    ModelAndView mv = new ModelAndView("/sample/boardList");
     
    return mv;
}
 
@RequestMapping(value="/sample/selectBoardList.do")
public ModelAndView selectBoardList(CommandMap commandMap) throws Exception{
    ModelAndView mv = new ModelAndView("jsonView");
     
    List<Map<String,Object>> list = sampleService.selectBoardList(commandMap.getMap());
    mv.addObject("list", list);
    if(list.size() > 0){
        mv.addObject("TOTAL", list.get(0).get("TOTAL_COUNT"));
    }
    else{
        mv.addObject("TOTAL", 0);
    }
     
    return mv;
}
지난글에서 작성한 것과는 약간 다른것을 확인할 수 있다.
지난글에서는 openBoardList에서 sampleService.selectBoardList를 호출했었는데(http://addio3305.tistory.com/89 의 2.Java > 1) Controller 참조), 이번에는 openBoardList는 단순히 boardList.jsp를 호출하는 역할만 수행한다.

그 다음으로 selectBoardList.do가 새로 생겼다. 
먼저 10번째 줄을 살펴보면 여태까지와는 약간 다른점을 확인할 수 있다. 
기존에는 ModelAndView에서 호출할 JSP 파일명이나 redirect를 수행했는데, 이번에는 jsonView라는 값이 들어가있는 것을 볼 수 있다. 
이는 앞에서 action-servlet.xml에 <bean id="jsonView" class="org.springframework.web.servlet.view.json.MappingJackson2JsonView" /> 를 선언했었던것을 기억해야 한다. 여기서 bean id가 jsonView였는데, 여기서 선언된 bean을 사용하는 것이다. 
이 jsonView는 데이터를 json 형식으로 변환해주는 역할을 수행한다. 
주소창에서 http://localhost:8080/first/sample/selectBoardList.do를 한번 호출해보면 다음과 같은 화면을 볼 수 있다.


생소한 화면인데, 여기서 볼 수 있는 데이터가 바로 json 형식의 데이터다. 여기서는 데이터의 크기를 줄이기 위해서 Indent(띄어쓰기) 등의 처리가 안되어있어서 데이터의 형식을 보기가 힘들다. 이러한 JSON 형식의 데이터가 올바른지 여부를 검사하고 포맷팅도 해주는 사이트들이 있다. 
http://jsonlint.com/ 역시 그러한 사이트 중에 하나다. 여기에 들어가서 앞에서 조회한 데이터를 모두 복사해서 Validate를 눌러보면 다음과 같은 화면을 볼 수 있다.

아까의 데이터가 위에서 보는것과 같이 정렬되어서 나온다. 지금은 JSON에 대해서 이야기를 하는것이 아니기 때문에 간단히 이런게 있다는것만 살펴보고 넘어가도록 하겠다. 이에 대한 자세한 설명은 인터넷에서 많이 찾을수 있다.

다시 소스로 돌아와서 selectBoardList.do는 sampleService.selectBoardList를 호출하여 목록 정보를 조회하고 그 값을 화면에 전달하는 역할을 한다. 
여기서 mv.addObject에("list", list)와 mv.addObject("TOTAL", 어떤 값) 두가지의 값을 화면에 보내주는것을 확인하자. 
앞에서 ajax callback 함수에서 data.TOTAL과 data.list가 있었던 것을 다시 한번 찾아보자. Controller에서 json 형식의 데이터를 화면에 전달하는데 그 값은 data라는 이름으로 화면에 전달된다. (꼭 data일 필요는 없다. 그렇지만 필자는 ComAjax에서 callback을 수행할 때, data라는 이름으로 보내주도록 해놨다. ComAjax를 보면 확인할 수 있다.) 그리고 mv에는 각각 list와 TOTAL이라는 key로 값을 보내줬고 이는 다시 화면에서 각각 data.list, data.TOTAL이라는 형식으로 값에 접근할 수 있다.

그 이후 service, serviceImpl, DAO, sql은 지난번과 거의 같다. 

2) SampleService
SampleService.java에 다음을 작성한다.
?
1
List<Map<String, Object>> selectBoardList(Map<String, Object> map) throws Exception;
3) SampeServiceImpl
SampleServiceImpl.java에 다음을 작성한다.
?
1
2
3
4
@Override
public List<Map<String, Object>> selectBoardList(Map<String, Object> map) throws Exception {
    return sampleDAO.selectBoardList(map);
}
4) SampleDAO
SampleDAO.java에 다음을 작성한다.
?
1
2
3
4
@SuppressWarnings("unchecked")
public List<Map<String, Object>> selectBoardList(Map<String, Object> map) throws Exception{
    return (List<Map<String, Object>>)selectPagingList("sample.selectBoardList", map);
}
3. SQL
SQL은 지난글과 비교해서 바뀐게 없다.
?
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
<select id="selectBoardList" parameterType="hashmap" resultType="hashmap">
    <include refid="common.pagingPre"/> 
    <![CDATA[
        SELECT
            ROW_NUMBER() OVER (ORDER BY IDX DESC) RNUM,
            IDX,
            TITLE,
            HIT_CNT,
            TO_CHAR(CREA_DTM, 'YYYY.MM.DD') AS CREA_DTM
        FROM
            TB_BOARD
        WHERE
            DEL_GB = 'N'
    ]]>
    <include refid="common.pagingPost"/> 
</select>
여기까지 하고 실행을 시키면 앞에서 봤던 화면을 볼 수 있다.
실행화면과 로그를 다시 한번 살펴보자.




페이징 태그의 버튼을 이것저것 누르면서 화면이 제대로 이동을 하는지, 쿼리 역시 제대로 동작하는지 살펴보면 된다.

------------------------------------------------------------------------------------
