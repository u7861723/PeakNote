package com.peaknote.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * Define pointcut (matches all public methods in all service packages)
     */
    @Pointcut("execution(public * com.peaknote.demo.service..*(..)) || execution(public * com.peaknote.demo.controller..*(..))")
    public void serviceLog() {}

    /**
     * Before method execution
     */
    @Before("serviceLog()")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("➡️ Starting execution: {}.{}(), parameters: {}", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * After method execution
     */
    @AfterReturning(value = "serviceLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("✅ Execution completed: {}.{}(), return: {}", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            result);
    }

    /**
     * Method throws exception
     */
    @AfterThrowing(value = "serviceLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.error("❌ Exception: {}.{}(), exception info: {}", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            e.getMessage(), e);
    }

    /**
     * Method execution time (optional enhancement)
     */
    @Around("serviceLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - start;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("⏱️ Time taken: {}.{}(), execution time: {} ms", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            time);
        return result;
    }
}
