package spring_web_bbs_study.common.logger;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggerAspect {
	protected Logger log = Logger.getLogger(this.getClass());
    static String name = "";
    static String type = "";
     
    @Around("execution(* spring_web_bbs_study..controller.*Controller.*(..)) or execution(* spring_web_bbs_study..service.*Impl.*(..)) or execution(* spring_web_bbs_study..dao.*DAO.*(..))")
    public Object logPrint(ProceedingJoinPoint joinPoint) throws Throwable {
        type = joinPoint.getSignature().getDeclaringTypeName();
         
        if (type.indexOf("Controller") > -1) {
            name = "Controller  \t:  ";
        }
        else if (type.indexOf("Service") > -1) {
            name = "ServiceImpl  \t:  ";
        }
        else if (type.indexOf("DAO") > -1) {
            name = "DAO  \t\t:  ";
        }
        
        log.debug(name + type + "." + joinPoint.getSignature().getName() + "()");
        return joinPoint.proceed();
    }
}
