package com.nightmare.aas.helper;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/// 反射工具类
public class ReflectionHelper {


    public static <T> T createInstance(Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return unsafeCast(constructor.newInstance());
    }

    public static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    public static <T> T createInstance(Class<T> clazz, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<?>[] paramTypes = getParameterTypes(args);
        // log param types for debugging
        L.d("Creating instance of " + clazz.getName() + " with param types: ");
        for (Class<?> paramType : paramTypes) {
            L.d(" - " + paramType.getName());
        }
        Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }


    public static void setField(Class<?> clazz, Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        setField(obj.getClass(), obj, fieldName, value);
    }

    public static <T> T getField(Class<?> clazz, Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return unsafeCast(field.get(obj));
    }

    public static <T> T getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return unsafeCast(getField(obj.getClass(), obj, fieldName));
    }


    public static <T> T invokeMethodWithParam(Object obj, String methodName, Class<?>[] paramTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        Object result = method.invoke(obj, args);
        return unsafeCast(result);
    }

    public static <T> T invokeMethodWithParam(Class<?> clazz, Object obj, String methodName, Class<?>[] paramTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        Object result = method.invoke(obj, args);
        return unsafeCast(result);
    }

    /**
     * If no parameter types are provided, they will be inferred from the args
     */
    public static <T> T invokeMethod(Class<?> clazz, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = getParameterTypes(args);
        return invokeMethodWithParam(clazz, null, methodName, paramTypes, args);
    }

    /**
     * If no parameter types are provided, they will be inferred from the args
     */
    public static <T> T invokeMethod(Object obj, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = getParameterTypes(args);
        return invokeMethodWithParam(obj.getClass(), obj, methodName, paramTypes, args);
    }


    /**
     * 将对象数组转换为对应的Class类型数组
     * 特别处理基本类型的包装类（如Integer -> int.class）
     *
     * @param args 方法参数对象数组
     * @return 对应的参数类型数组
     */
    public static Class<?>[] getParameterTypes(Object... args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                parameterTypes[i] = Object.class;
            } else if (args[i] instanceof Integer) {
                parameterTypes[i] = int.class;
            } else if (args[i] instanceof Boolean) {
                parameterTypes[i] = boolean.class;
            } else if (args[i] instanceof Long) {
                parameterTypes[i] = long.class;
            } else if (args[i] instanceof Float) {
                parameterTypes[i] = float.class;
            } else if (args[i] instanceof Double) {
                parameterTypes[i] = double.class;
            } else if (args[i] instanceof Byte) {
                parameterTypes[i] = byte.class;
            } else if (args[i] instanceof Short) {
                parameterTypes[i] = short.class;
            } else if (args[i] instanceof Character) {
                parameterTypes[i] = char.class;
            } else {
                parameterTypes[i] = args[i].getClass();
            }
        }
        return parameterTypes;
    }


    public static <T> T unsafeCast(final Object obj) {
        // noinspection unchecked
        return (T) obj;
    }

}