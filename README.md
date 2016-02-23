# spring_web_bbs_study
make bbs > spring study!
1. HandlerMethodArgumentResolver 란?
HandlerMethodArgumentResolver 는 스프링 3.1에서 추가된 인터페이스다. 스프링 3.1 이전에는 WebArgumentResolver 라는 인터페이스였는데, 
스프링 3.1 이후부터 HandlerMethodArgumentResolver 라는 이름으로 바뀌었다. 

이것이 하는 역할은 다음과 같다. 
스프링 사용 시, 컨트롤러(Controller)에 들어오는 파라미터(Parameter)를 수정하거나 공통적으로 추가를 해주어야 하는 경우가 있다. 
예를 들어, 로그인을 한 사용자의 사용자 아이디나 닉네임등을 추가하는것을 생각해보자. 
보통 그런 정보는 세션(Session)에 담아놓고 사용하는데, DB에 그러한 정보를 입력할 때에는 
결국 세션에서 값을 꺼내와서 파라미터로 추가를 해야한다.
그런 경우가 뭐 하나나 두번 정도 있다면 몰라도, 여러번 사용되는 값을 그렇게 일일히 세션에서 가져오는건 상당히 번거로운 일이다.
HandlerMethodArgumentResolver 는 사용자 요청이 Controller에 도달하기 전에 그 요청의 파라미터들을 수정할 수 있도록 해준다.
자세한건 이제 소스를 보면서 하나씩 살펴보자.

1. CommandMap 클래스 생성
request에 담겨있는 파라미터를 Map에 담아주는 역할을 하는 클래스다. 지난 글에서 컨트롤러를 다시한번 살펴보자.
public ModelAndView openSampleBoardList(Map<String,Object> commandMap) throws Exception{ 라고 선언을 했었다. 
여기서 Map<String,Object> commandMap에 사용자가 넘겨준 파라미터가 저장되어 있다. (이는 앞으로 그렇게 하겠다는 의미이고, 현재는 저장되지 않는다.)
그런데 여기서 문제는 HandlerMethodArgumentResolver는 컨트롤러의 파라미터가 Map 형식이면 동작하지 않는다. 
엄밀히 말을하면, 스프링 3.1에서 HandlerMethodArgumentResolver를 이용하여 그러한 기능을 만들더라도, 컨트롤러의 파라미터가 Map 형식이면 우리가 설정한 클래스가 아닌, 스프링에서 기본적으로 설정된 ArgumentResolver를 거치게 된다. 
항상 그렇게 동작하는것은 아니고, 스프링의 <mvc:annotation-driven/>을 선언하게 되면 위에서 이야기한것처럼 동작하게 된다. (본인은 처음에 이것을 몰라서 진짜 몇날 몇일을 삽질했다.)
따라서 <mvc:annotation-driven/>을 선언하려면 Map을 그대로 사용할 수 없고, 선언하지 않으면 문제는 없다. 그렇지만 앞으로 포스팅할 내용중에는 <mvc:annotation-driven/>을 선언해야 하는 경우가 있기때문에, 여기서는 Map을 대신할 CommandMap을 작성한다.
first 프로젝트의 common 패키지 밑에 common 패키지를 만들고, 다음을 작성하자.
CommandMap.java
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
package first.common.common;
 
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
 
public class CommandMap {
    Map<String,Object> map = new HashMap<String,Object>();
     
    public Object get(String key){
        return map.get(key);
    }
     
    public void put(String key, Object value){
        map.put(key, value);
    }
     
    public Object remove(String key){
        return map.remove(key);
    }
     
    public boolean containsKey(String key){
        return map.containsKey(key);
    }
     
    public boolean containsValue(Object value){
        return map.containsValue(value);
    }
     
    public void clear(){
        map.clear();
    }
     
    public Set<Entry<String, Object>> entrySet(){
        return map.entrySet();
    }
     
    public Set<String> keySet(){
        return map.keySet();
    }
     
    public boolean isEmpty(){
        return map.isEmpty();
    }
     
    public void putAll(Map<? extends String, ?extends Object> m){
        map.putAll(m);
    }
     
    public Map<String,Object> getMap(){
        return map;
    }
}
클래스는 별다른 부분은 없다. 내부적으로 Map을 하나 생성하고, 그 맵에 모든 데이터를 담는 역할을 한다. 
여기서 중요한점은 절대로 Map을 상속받으면 안된다.
Map을 상속받게 되면, 우리가 작성할 ArgumentResolver를 거치지 않게 되니 주의하자.
여러가지 메서드들이 보이는데, 거의 대부분은 map의 기본기능을 다시 호출하는것에 지나지 않는다. 보통 가장 많이 사용하는 get, put 메서드만 있더도 큰 문제는 없겠지만, 여기서는 필자가 생각할때 필요한 맵의 기능들을 몇가지 골라서 추가했다. 
그리고 다른곳에서는 이 CommandMap을 map과 똑같이 사용할 수 있도록 getMap 메서드를 추가했다.


2. HandlerMethodArgumentResolver 작성
first > common 패키지 밑에 resolver 패키지를 작성 후 다음을 작성하자.
CustomMapArgumentResolver.java
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
package first.common.resolver;
 
import java.util.Enumeration;
 
import javax.servlet.http.HttpServletRequest;
 
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
 
import first.common.common.CommandMap;
 
public class CustomMapArgumentResolver implements HandlerMethodArgumentResolver{
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CommandMap.class.isAssignableFrom(parameter.getParameterType());
    }
 
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CommandMap commandMap = new CommandMap();
         
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Enumeration<?> enumeration = request.getParameterNames();
         
        String key = null;
        String[] values = null;
        while(enumeration.hasMoreElements()){
            key = (String) enumeration.nextElement();
            values = request.getParameterValues(key);
            if(values != null){
                commandMap.put(key, (values.length > 1) ? values:values[0] );
            }
        }
        return commandMap;
    }
}
이제 하나씩 살펴보자. 
HandlerMethodArgumentResolver 인터페이스를 상속(인터페이스도 상속이라고 해야하는지 햇갈리긴 하지만...)하면 두가지 메서드를 반드시 구현해야 하는데, supportsParameter 메서드와 resolveArgument 메서드가 그것이다. 

이름에서 알수 있듯이 supportsParameter 메서드는 Resolver가 적용 가능한지 검사하는 역할을 하고, resolverArgument 메서드는 파라미터와 기타 정보를 받아서 실제 객체를 반환한다.
supportsparameter 메서드는 컨트롤러의 파라미터가 CommandMap 클래스인지 검사하도록 하였다. 
이를 위해서 추후 Controller의 Map<String,Object> 형식을 CommandMap이라고 변경할 것이다. (잠시후에 다시 좀 더 자세하게 볼 것이다.)

그 다음 중요한것이 resolverArgument 메서드다.
중요한 부분만 살펴보자. 
먼저, 아까 정의했던 CommandMap 객체를 생성하였다. (23번 줄)
그 다음으로, request에 담겨있는 모든 키(key)와 값(value)을 commandMap에 저장하였다. (34번 줄) 
30번 줄부터 32번 줄은 request에 있는 값을 iterator를 이용하여 하나씩 가져오는 로직이다. 
마지막으로 모든 파라미터가 담겨있는 commandMap을 반환하였다. (37번 줄) 

3. CustomMapArgumentResolver 등록
이제 CustomMapArgumentResolver를 등록하자. 
CustomMapArgumentResolver는 root context 영역에 등록이 되어야 한다. 따라서 action-servlet.xml에 등록해야 한다. (root context에 대한 내용은 추후 다시 하겠다.)
action-servlet.xml에 다음과 같이 등록하자.
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
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:context="http://www.springframework.org/schema/context"
    xmlns:p="http://www.springframework.org/schema/p"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
       http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd">
 
    <context:component-scan base-package="first"></context:component-scan>
     
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
     
    <bean class="org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping"/>
     
    <bean class="org.springframework.web.servlet.view.BeanNameViewResolver" p:order="0" />
    <bean id="jsonView" class="org.springframework.web.servlet.view.json.MappingJacksonJsonView" />    
     
    <bean
        class="org.springframework.web.servlet.view.UrlBasedViewResolver" p:order="1"
        p:viewClass="org.springframework.web.servlet.view.JstlView"
        p:prefix="/WEB-INF/jsp/" p:suffix=".jsp">
    </bean>
</beans>
위의 action-servlet.xml은 기존 글에서 작성된것에 CustomMapArgumentResolver를 등록한 내용이다. 실제로 등록을 한 부분은 13~17번째 줄이다. 
<mvc:"argument-resolvers> 태그를 이용하여 우리가 만든 CustomMapArgumentResolver의 빈(bean)을 수동으로 등록했다. 

4. Controller의 수정 및 테스트
이제 위에서 작성한 것들이 정확히 동작하는지 확인해볼 시간이다.
Controller에 다음을 추가하자. 
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
@RequestMapping(value="/sample/testMapArgumentResolver.do")
public ModelAndView testMapArgumentResolver(CommandMap commandMap) throws Exception{
    ModelAndView mv = new ModelAndView("");
     
    if(commandMap.isEmpty() == false){
        Iterator<Entry<String,Object>> iterator = commandMap.getMap().entrySet().iterator();
        Entry<String,Object> entry = null;
        while(iterator.hasNext()){
            entry = iterator.next();
            log.debug("key : "+entry.getKey()+", value : "+entry.getValue());
        }
    }
    return mv;
}
좀 복잡해보일수도 있는데, 간단한 내용이다. 
먼저 확인해야 할것은 public ModelAndView testMapArgumentResolver(CommandMap commandMap) throws Exception{ 부분이다. 
지난 글에서 우리는 Controller를 public ModelAndView openSampleBoardList(Map<String,Object> commandMap) throws Exception{ 와 같이 작성했었다. (http://addio3305.tistory.com/72)
여기서 Map<String,Object>가 방금 만든 CommandMap으로 바뀌었다. 
그 후, commandMap에 있는 모든 파라미터를 iterator를 이용하여 출력하였다. 

이제 서버를 실행시키고 테스트를 해보자. 
주소창에 localhost:8080/first/sample/testMapArgumentResolver.do?aaa=temp 를 입력해보자. 방금 위에서 만든 컨트롤러에 get방식을 이용하여 aaa라는 키로 temp라는 값을 추가하였다. (주소 뒤에 ?를 붙이고 key=value 형식으로 파라미터를 추가할 수 있다.)

이클립스의 콘솔창을 확인하면 다음과 같은 결과를 볼 수 있다.


key : aaa, value : temp라는 로그를 보자. 
아까 위에서 우리가 get으로 전송한 aaa라는 키와 temp라는 값이다. 
위와 같은 결과가 나오면 정상적으로 CustomMapArgumentResolver가 등록된 것이다. 

이번에는 두개의 키와 값을 전송해보자.
localhost:8080/first/sample/testMapArgumentResolver.do?aaa=value1&bbb=value2 라고 입력해보자.


정상적으로 aaa와 bbb에 해당하는 값 value1과 value2가 commandMap에 담겨져서 출력됨을 알 수 있다. 

이것으로 HandlerMethodArgumentResolver에 대한 내용을 마무리한다.

