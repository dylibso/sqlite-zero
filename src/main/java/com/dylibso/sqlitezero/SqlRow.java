package com.dylibso.sqlitezero;

import java.util.HashMap;
import java.util.Map;


public class SqlRow<T> {
    private HashMap<String, String> object;

    public SqlRow() {
        object = new HashMap<>();
    }

    public HashMap<String, String> getObject() {
        return object;
    }

    public void addProperty(String c, String v) {
        object.put(c, v);
    }

    public T cast(Class<T> clazz) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (Map.Entry<String, String> entry : object.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            try {
                var field = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = field.getType();
                if (fieldType.equals(String.class)) {
                    field.set(instance, value);
                } else if (fieldType.equals(int.class)) {
                    field.set(instance, Integer.parseInt(value));
                } else if (fieldType.equals(float.class)) {
                    field.set(instance, Float.parseFloat(value));
                } else {
                    throw new RuntimeException("Don't know how to cast: " + fieldType);
                }
            } catch (NoSuchFieldException e) {
                System.out.println("No setter for field: " + fieldName);
            }
        }

        return instance;
    }
}
