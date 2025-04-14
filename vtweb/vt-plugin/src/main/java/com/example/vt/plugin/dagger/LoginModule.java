package com.example.vt.plugin.dagger;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public abstract class LoginModule {

    @Binds
    @IntoMap
    @StringKey("hello")
    abstract Command helloWorldCommand(HelloWorldCommand helloWorld);

    @Binds
    @IntoMap
    @StringKey("login")
    abstract Command loginCommand(LoginCommand helloWorld);
}

@Module
abstract class SystemOutModule {
    @Provides
    static Outputter textOutputter() {
        return System.out::println;
    }
}