package com.SleepUp.SU.exception;

public class ExceptionsMessageHelper {
    static String entityAlreadyExists(String entityClass, String attributeName, String attributeValue) {
        return String.format("%s with %s \"%s\" already exists", entityClass, attributeName, attributeValue);
    }
    static String entityNotFound(String entityClass, String attributeName, String attributeValue){
        return String.format("%s with %s \"%s\" not found", entityClass, attributeName, attributeValue);
    }
}
