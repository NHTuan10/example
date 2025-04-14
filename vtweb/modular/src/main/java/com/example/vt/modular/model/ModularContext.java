package com.example.vt.modular.model;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@AllArgsConstructor
public class ModularContext {
    private Object moduleLoader;

    public <S> List<S> getModularServicesFromSpring(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        return (Collection<? extends S>) moduleLoader.getClass().getDeclaredMethod("getModularServices", Class.class).invoke(moduleLoader, clazz);
        return (List<S>) moduleLoader.getClass().getDeclaredMethod("getModularServicesFromSpring", Class.class).invoke(moduleLoader, clazz);
    }

    public <S> List<S> getModularServices(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        return (Collection<? extends S>) moduleLoader.getClass().getDeclaredMethod("getModularServices", Class.class).invoke(moduleLoader, clazz);
        return (List<S>) moduleLoader.getClass().getDeclaredMethod("getModularServices", Class.class).invoke(moduleLoader, clazz);
    }
}
