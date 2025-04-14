package com.example.vt.plugin.dagger;

import com.example.vt.modular.annotation.ModularConfiguration;
import com.example.vt.modular.annotation.ModularService;

@ModularConfiguration
public class ModularDaggerConfiguration {

    @ModularService
    public CommandRouter commandRouter() {
        CommandRouterFactory commandRouterFactory =
                DaggerCommandRouterFactory.create();
        CommandRouter commandRouter = commandRouterFactory.router();
        return commandRouter;
    }
}
