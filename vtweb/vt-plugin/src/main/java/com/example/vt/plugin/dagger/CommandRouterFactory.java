package com.example.vt.plugin.dagger;

import dagger.Component;

@Component(modules = {LoginModule.class, SystemOutModule.class})
//@Component
interface CommandRouterFactory {
    CommandRouter router();
}

