package net.ccbluex.liquidbounce.injection;

public class NativeWrapper {
    public static native void retransformClass(Class<?> clazz);
    public static native Class<?> defineClass(ClassLoader loader,byte[] array);
}
