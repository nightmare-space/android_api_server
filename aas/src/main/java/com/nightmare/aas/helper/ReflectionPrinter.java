package com.nightmare.aas.helper;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class ReflectionPrinter {
    private static final String ANSI_RESET = "[0m";
    private static final String ANSI_BLUE = "[34m";
    private static final String ANSI_CYAN = "[36m";
    private static final String ANSI_GREEN = "[32m";
    private static final String ANSI_YELLOW = "[33m";
    private static final String ANSI_MAGENTA = "[35m";

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

}
