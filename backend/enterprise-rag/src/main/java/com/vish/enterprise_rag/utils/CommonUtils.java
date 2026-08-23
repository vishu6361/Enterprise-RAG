package com.vish.enterprise_rag.utils;

import jakarta.persistence.Table;

public class CommonUtils {
    public static String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).name();
        }
        return clazz.getSimpleName().toLowerCase();
    }
}
