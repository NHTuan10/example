package com.example.vt.modular.classloader;

import java.util.List;

public interface ArtifactsResolver<T> {

    List<T> resolveMavenDeps(List<String> deps, Class<T> clazz);
}
