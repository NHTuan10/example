package com.example.vt.modular.model;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@AllArgsConstructor
public class ModularContext {
    private Object moduleLoader;

    public <S> Collection<? extends S> getModularServices(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (Collection<? extends S>) moduleLoader.getClass().getDeclaredMethod("getModularServices", Class.class).invoke(moduleLoader, clazz);
    }

}
