package com.nightmare.aas.helper;

import java.lang.reflect.InvocationTargetException;

/**
 * @noinspection UnusedReturnValue
 */
public class RH {

    public static <T> T gF(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionHelper.getField(obj, fieldName);
    }


    public static <T> T gF(Class<?> clazz, Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionHelper.getField(clazz, obj, fieldName);
    }

    // Invoke Method
    public static <T> T iM(Object obj, String methodName, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return ReflectionHelper.invokeMethod(obj, methodName, args);
    }

    public static <T> T iM(Class<?> clazz, String methodName, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return ReflectionHelper.invokeMethod(clazz, methodName, args);
    }

    public static void iM(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ReflectionHelper.invokeMethod(obj, methodName, parameterTypes, args);
    }

    // Invoke Method with Parameter types
    public static <T> T iMWP(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return ReflectionHelper.invokeMethodWithParam(obj, methodName, parameterTypes, args);
    }

    // Invoke Method with Parameter types
    public static <T> T iMWP(Class<?> clazz, Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return ReflectionHelper.invokeMethodWithParam(clazz, obj, methodName, parameterTypes, args);
    }

    // Set Field
    public static void sF(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        ReflectionHelper.setField(obj, fieldName, value);
    }

    public static void l(Object object) {
        l(object.getClass());
    }

    public static void l(Class<?> clazz) {
        ReflectionPrinter.listAllObject(clazz);
    }

    public static <T> T cI(Class<T> clazz, Class<?>[] parameterTypes, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return ReflectionHelper.createInstance(clazz, parameterTypes, args);
    }

    public static <T> T cI(Class<T> clazz, Object... args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return ReflectionHelper.createInstance(clazz, args);
    }
}

