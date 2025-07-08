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
     * 定义切入点（匹配所有 service 包下的所有 public 方法）
     */
    @Pointcut("execution(public * com.peaknote.demo.service..*(..)) || execution(public * com.peaknote.demo.controller..*(..))")
    public void serviceLog() {}

    /**
     * 方法执行前
     */
    @Before("serviceLog()")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("➡️ 开始执行: {}.{}(), 参数: {}", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * 方法执行后
     */
    @AfterReturning(value = "serviceLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("✅ 执行完成: {}.{}(), 返回: {}", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            result);
    }

    /**
     * 方法抛异常
     */
    @AfterThrowing(value = "serviceLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.error("❌ 异常: {}.{}(), 异常信息: {}", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            e.getMessage(), e);
    }

    /**
     * 方法执行耗时（可选增强）
     */
    @Around("serviceLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - start;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("⏱️ 耗时: {}.{}(), 执行时间: {} ms", 
            signature.getDeclaringTypeName(),
            signature.getName(),
            time);
        return result;
    }
}
