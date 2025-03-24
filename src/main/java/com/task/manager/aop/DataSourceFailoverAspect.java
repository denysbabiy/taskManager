package com.task.manager.aop;

import com.task.manager.util.DataSourceContext;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component
public class DataSourceFailoverAspect {

    @Around("execution(* com.task.manager.repository..*(..))")
    public Object handleDataSourceFailover(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataAccessException exc) {
            log.error("Main data source failed, switching to backup: ", exc);

            DataSourceContext.switchDataSourceToBackup();

            return joinPoint.proceed();
        } finally {
            DataSourceContext.clear();
        }
    }
}
