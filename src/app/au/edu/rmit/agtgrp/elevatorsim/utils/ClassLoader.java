package au.edu.rmit.agtgrp.elevatorsim.utils;

public class ClassLoader {

    private ClassLoader() {

    }

    /**
     * Loads and instantiates a class from binary name, of given a type
     * @param className fully qualified name of a class to be loaded
     * @param type parent type of the class to be loaded. Could be an interface, abstract class or any other parent class
     * @param <T> class type
     * @return class of type T
     */
    public static <T> T instantiate(final String className, final Class<T> type) {
        try {
            return type.cast(Class.forName(className).newInstance());
        } catch (InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
