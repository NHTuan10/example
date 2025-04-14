package com.example.vt.plugin.dagger;

import jakarta.inject.Inject;

import java.util.List;

/**
 * Abstract command that accepts a single argument.
 */
abstract class SingleArgCommand implements Command {

    @Override
    public final Result handleInput(List<String> input) {
        return input.size() == 1 ? handleArg(input.get(0)) : Result.invalid();
    }

    /**
     * Handles the single argument to the command.
     */
    protected abstract Result handleArg(String arg);
}

final class LoginCommand extends SingleArgCommand {
    private final Outputter outputter;

    @Inject
    LoginCommand(Outputter outputter) {
        this.outputter = outputter;
    }

    @Override
    public String key() {
        return "login";
    }

    @Override
    public Result handleArg(String username) {
        outputter.output(username + " is logged in.");
        return Result.handled();
    }
}

final class HelloWorldCommand implements Command {
    private final Outputter outputter;

    @Inject
    HelloWorldCommand(Outputter outputter) {
        this.outputter = outputter;
    }

    @Override
    public String key() {
        return "hello";
    }

    @Override
    public Result handleInput(List<String> input) {
        if (!input.isEmpty()) {
            return Result.invalid();
        }
        System.out.println("world!");
        return Result.handled();
    }
}
