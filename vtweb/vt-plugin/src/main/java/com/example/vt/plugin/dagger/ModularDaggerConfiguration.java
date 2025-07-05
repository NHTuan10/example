package com.example.vt.plugin.dagger;

import io.github.nhtuan10.modular.api.annotation.ModularConfiguration;
import io.github.nhtuan10.modular.api.annotation.ModularService;

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
