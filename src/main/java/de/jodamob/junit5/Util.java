package de.jodamob.junit5;

/* package */ class Util {
    @SuppressWarnings("unchecked")
    public static Object getEnumConstantByName(Class<? extends Enum<?>> enumClass, String name) {
        // This is a workaround for KT-5191. Enum#valueOf cannot be called in Kotlin
        return Enum.valueOf((Class) enumClass, name);
    }
}