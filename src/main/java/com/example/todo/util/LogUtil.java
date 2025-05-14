package com.example.todo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    public static Logger getLogger(Class<?> clazz){
        return LoggerFactory.getLogger(clazz);
    }
    //info 로거
    public static void info(Class<?> clazz, String message, Object... args){
        Logger logger = getLogger(clazz);
        logger.info(message, args);
    }
    //warn 로거
    public static void warn(Class<?> clazz, String message, Object... args){
        Logger logger = getLogger(clazz);
        logger.warn(message, args);
    }
    //error 로거
    public static void error(Class<?> clazz, String message, Object... args){
        Logger logger = getLogger(clazz);
        logger.error(message, args);
    }
    //debug 로거 -- 동작 확인 용
    public static void debug(Class<?> clazz, String message, Object... args){
        Logger logger = getLogger(clazz);
        logger.debug(message, args);
    }
}
