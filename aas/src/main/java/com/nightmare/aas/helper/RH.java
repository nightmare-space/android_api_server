package com.nightmare.aas.helper;

/**
 * @noinspection UnusedReturnValue
 */
public class RH {

    public static <T> T gHF(Object obj, String fieldName) {
        return ReflectionHelper.getHiddenField(obj, fieldName);
    }

    public static <T> T iHM(Object obj, String methodName, Object... args) {
        return ReflectionHelper.invokeHiddenMethod(obj, methodName, args);
    }

    public static <T> T iSM(Class<?> clazz, String methodName, Object... args) {
        return ReflectionHelper.invokeStaticMethod(clazz, methodName, args);
    }

    public static <T> T iSMWT(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        return ReflectionHelper.invokeStaticMethodWithType(clazz, methodName, parameterTypes, args);
    }

    // setHiddenField
    public static void sHF(Object obj, String fieldName, Object value) {
        ReflectionHelper.setHiddenField(obj, fieldName, value);
    }

    public static void iHMWT(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        ReflectionHelper.invokeHiddenMethodWithType(obj, methodName, parameterTypes, args);
    }

    public static void l(Object object) {
        l(object.getClass());
    }

    public static void l(Class<?> clazz) {
        ReflectionHelper.listAllObject(clazz);
    }

    public static <T> T cIWT(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
        return ReflectionHelper.createInstanceWithType(clazz, parameterTypes, args);
    }
}

