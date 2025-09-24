package com.SleepUp.SU.exceptions;

public class ExceptionsMessageHelper {
    public static String entityAlreadyExists(String entityClass, String attributeName, String attributeValue) {
        return String.format("%s with %s \"%s\" already exists", entityClass, attributeName, attributeValue);
    }
    public static String entityNotFound(String entityClass, String attributeName, String attributeValue){
        return String.format("%s with %s \"%s\" not found", entityClass, attributeName, attributeValue);
    }
}
