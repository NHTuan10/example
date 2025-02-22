package com.example.vt.modular.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(exclude = {"proxyObject", "interfaceClass"})
@Getter
@AllArgsConstructor
public final class ModularServiceHolder {
    private final Class serviceClass;
    private final String name;
    private final Object proxyObject;
    private final Class<?> interfaceClass;

}
