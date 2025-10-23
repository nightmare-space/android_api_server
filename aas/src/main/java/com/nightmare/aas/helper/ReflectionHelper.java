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

    private static final String ANSI_RESET = "[0m";
    private static final String ANSI_BLUE = "[34m";
    private static final String ANSI_CYAN = "[36m";
    private static final String ANSI_GREEN = "[32m";
    private static final String ANSI_YELLOW = "[33m";
    private static final String ANSI_MAGENTA = "[35m";


    public static <T> T createInstance(Class<T> clazz, Object... args) {
        try {
            Class<?>[] parameterTypes = getParameterTypes(args);
            Log.d("ReflectionHelper", "Creating instance of " + clazz.getName() + " with parameter types: " + Arrays.toString(parameterTypes));
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T createInstanceWithType(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T getHiddenField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return unsafeCast(field.get(obj));
        } catch (Exception e) {
            try {
                Field field = Objects.requireNonNull(obj.getClass().getSuperclass()).getDeclaredField(fieldName);
                field.setAccessible(true);
                return unsafeCast(field.get(obj));
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
    }

    public static void setHiddenField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            L.d("setHiddenField error: " + e.getMessage());
            try {
                Field field = Objects.requireNonNull(obj.getClass().getSuperclass()).getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = getParameterTypes(args);
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return unsafeCast(method.invoke(null, args));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeStaticMethodWithType(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return unsafeCast(method.invoke(null, args));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeHiddenMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = getParameterTypes(args);
            Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(obj, args);
            // L.d("result -> " + result);
            return unsafeCast(result);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @noinspection UnusedReturnValue
     */
    public static <T> T invokeHiddenMethodWithType(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return unsafeCast(method.invoke(obj, args));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeHiddenMethodWithThrow(Object obj, String methodName, Object... args) throws
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        Class<?>[] parameterTypes = getParameterTypes(args);
        Method method;
        try {
            // 首先尝试在当前类中查找方法
            method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            // 如果当前类中没找到，尝试从父类中查找
            try {
                method = obj.getClass().getSuperclass().getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException | NullPointerException ex) {
                // 如果父类也没有或父类为null，抛出原始异常
                throw e;
            }
        }
        method.setAccessible(true);
        return unsafeCast(method.invoke(obj, args));
    }

    public static void listAllObject(Object object) {
        listAllObject(object.getClass());
    }

    public static void listAllObject(Class<?> clazz) {
        HashMap<String, String> map = new HashMap<>();
        map.put("class java.lang.String", "String");
        map.put("java.util.List", "List");
        try {
            print("Class " + clazz.getName());

            // 反射方法字段
            Method[] methods = clazz.getDeclaredMethods();

            // 反射构造器
            Constructor<?>[] constuctors = clazz.getDeclaredConstructors();
            print("Constructor========\n");
            for (Constructor<?> c : constuctors) {
                // 判断访问修饰符
                boolean isStatic = Modifier.isStatic(c.getModifiers());
                boolean isPrivate = Modifier.isPrivate(c.getModifiers());
                boolean isProtected = Modifier.isProtected(c.getModifiers());
                boolean isHidden = !Modifier.isPublic(c.getModifiers()) && !isPrivate && !isProtected;

                // 获取构造函数所属的类名作为返回类型
                String className = c.getDeclaringClass().getSimpleName();

                // 打印访问修饰符信息
                String accessModifier = "";
                if (isPrivate) {
                    accessModifier = "private ";
                } else if (isProtected) {
                    accessModifier = "protected ";
                } else if (isHidden) {
                    accessModifier = "package ";
                }

                printColored(ANSI_MAGENTA, accessModifier);

                if (isStatic) {
                    System.out.print("static ");
                }

                // 打印返回类型（类名）和构造函数名称
                printColored(ANSI_CYAN, className + " ");
                printColored(ANSI_YELLOW, className);

                // 打印参数
                System.out.print("(");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    for (int i = 0; i < c.getParameterCount(); i++) {
                        String type = c.getParameters()[i].getParameterizedType().toString();
                        if (map.containsKey(type)) {
                            type = map.get(type);
                        } else {
                            type = type.replace("class ", "");
                        }
                        printColored(ANSI_GREEN, type + " " + c.getParameters()[i].getName());
                        if (i != c.getParameterCount() - 1) {
                            System.out.print(", ");
                        }
                    }
                }
                System.out.println(")");
            }
            System.out.print("\n");
            // 反射属性字段
            Field[] fields = clazz.getDeclaredFields();
            print("Field========\n");
            for (Field f : fields) {
                boolean isStatic = Modifier.isStatic(f.getModifiers());
                boolean isPrivate = Modifier.isPrivate(f.getModifiers());
                boolean isProtected = Modifier.isProtected(f.getModifiers());

                // 打印访问修饰符信息
                String accessModifier = "";
                if (isPrivate) {
                    accessModifier = "private ";
                } else if (isProtected) {
                    accessModifier = "protected ";
                } else {
                    accessModifier = " public ";
                }
                printColored(ANSI_MAGENTA, accessModifier);
                if (isStatic) {
                    printColored(ANSI_CYAN, "static ");
                }

                // 格式化类型名称
                String typeName = f.getType().getName();
                // getTypeName 直接就是 int[] boolean[] 这样的字符串
                // 处理数组类型
                if (typeName.startsWith("[")) {
                    // 转换数组类型表示
                    String componentType = "";
                    int dimensions = 0;
                    while (typeName.startsWith("[")) {
                        dimensions++;
                        typeName = typeName.substring(1);
                    }

                    if (typeName.equals("I")) componentType = "int";
                    else if (typeName.equals("Z")) componentType = "boolean";
                    else if (typeName.equals("B")) componentType = "byte";
                    else if (typeName.equals("C")) componentType = "char";
                    else if (typeName.equals("D")) componentType = "double";
                    else if (typeName.equals("F")) componentType = "float";
                    else if (typeName.equals("J")) componentType = "long";
                    else if (typeName.equals("S")) componentType = "short";
                    else if (typeName.startsWith("L")) {
                        componentType = typeName.substring(1, typeName.length() - 1).replace('/', '.');
                        // 如果需要简化完整类名
                        int lastDot = componentType.lastIndexOf('.');
                        if (lastDot > 0) {
                            componentType = componentType.substring(lastDot + 1);
                        }
                    }

                    typeName = componentType;
                    for (int i = 0; i < dimensions; i++) {
                        typeName += "[]";
                    }
                } else {
                    // 简化完整类名
                    if (map.containsKey("class " + typeName)) {
                        typeName = map.get("class " + typeName);
                    } else {
                        int lastDot = typeName.lastIndexOf('.');
                        if (lastDot > 0) {
                            typeName = typeName.substring(lastDot + 1);
                        }
                    }
                }

                printColored(ANSI_CYAN, typeName);
                printColoredLine(ANSI_GREEN, " " + f.getName());
            }
            System.out.print("\n");
            print("Method========\n");

            for (Method m : methods) {
                boolean isStatic = Modifier.isStatic(m.getModifiers());
                boolean isPrivate = Modifier.isPrivate(m.getModifiers());
                boolean isProtected = Modifier.isProtected(m.getModifiers());
                boolean isHidden = !Modifier.isPublic(m.getModifiers()) && !isPrivate && !isProtected;

                String returnType = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    returnType = m.getReturnType().getTypeName();
                }
                if (map.containsKey(returnType)) {
                    returnType = map.get(returnType);
                }

                // 打印访问修饰符信息
                String accessModifier = "";
                if (isPrivate) {
                    accessModifier = "private ";
                } else if (isProtected) {
                    accessModifier = "protected ";
                } else if (isHidden) {
                    accessModifier = "package ";
                } else {
                    accessModifier = " public ";
                }

                printColored(ANSI_MAGENTA, accessModifier);

                if (isStatic) {
                    printColored(ANSI_CYAN, "static ");
                }

                printColored(ANSI_CYAN, returnType + " ");
                printColored(ANSI_YELLOW, m.getName());
                System.out.print("(");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    for (int i = 0; i < m.getParameterCount(); i++) {
                        String type = m.getParameters()[i].getParameterizedType().toString();
                        if (map.containsKey(type)) {
                            type = map.get(type);
                        } else {
                            type = type.replace("class ", "");
                        }
                        printColored(ANSI_GREEN, type + " " + m.getParameters()[i].getName());
                        if (i != m.getParameterCount() - 1) {
                            System.out.print(",");
                        }
                    }
                }

                System.out.println(")");
            }

        } catch (Exception e) {
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
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
                parameterTypes[i] = Object.class; // 处理null值的情况
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

    /**
     * 输出带颜色的文本
     *
     * @param color ANSI 颜色代码
     * @param text  要输出的文本
     */
    private static void printColored(String color, String text) {
        System.out.print((char) 0x1b + color + text + (char) 0x1b + ANSI_RESET);
        System.out.flush();
    }

    /**
     * 输出带颜色的一行文本（带换行）
     *
     * @param color ANSI 颜色代码
     * @param text  要输出的文本
     */
    private static void printColoredLine(String color, String text) {
        printColored(color, text);
        System.out.println();
    }

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
    }

    public static <T> T unsafeCast(final Object obj) {
        // noinspection unchecked
        return (T) obj;
    }

}