package dev.isxander.controlify.test;

public class Assertions {
    public static void assertEquals(Object expected, Object actual) {
        assertTrue(expected.equals(actual), "Expected " + expected + " but got " + actual);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        assertTrue(expected.equals(actual), message);
    }

    public static void assertNotNull(Object object) {
        assertTrue(object != null, "Expected object to not be null");
    }

    public static void assertNotNull(Object object, String message) {
        assertTrue(object != null, message);
    }

    public static void assertNull(Object object) {
        assertTrue(object == null, "Expected object to be null");
    }

    public static void assertNull(Object object, String message) {
        assertTrue(object == null, message);
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Condition not met");
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition) {
        assertTrue(!condition, "Condition not met");
    }

    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
