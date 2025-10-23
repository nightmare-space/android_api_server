package com.android.internal.os;
public class ClassLoaderFactory {
    public static ClassLoader createClassLoader(
            String dexPath,
            String librarySearchPath,
            String libraryPermittedPath,
            ClassLoader parent,
            int targetSdkVersion,
            boolean isNamespaceShared,
            String classLoaderName
    ) {
        throw new RuntimeException("STUB: This method is not implemented in the stub version of ClassLoaderFactory.");
    }
}
