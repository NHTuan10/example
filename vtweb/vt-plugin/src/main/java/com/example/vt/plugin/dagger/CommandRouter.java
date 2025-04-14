package com.example.vt.plugin.dagger;

import com.example.vt.common.service.DaggerSampleService;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


final public class CommandRouter implements DaggerSampleService {
    @Override
    public String toString() {
        return "CommandRouter{" +
                "commands=" + commands +
                '}';
    }

    //    private final Map<String, Command> commands =  new HashMap<>();;
    private final Map<String, Command> commands;

    //    @Inject
//    public CommandRouter(HelloWorldCommand helloWorldCommand) {
//        commands.put(helloWorldCommand.key(), helloWorldCommand);
//    }
//        @Inject
//    public CommandRouter(Command command) {
//        commands.put(command.key(), command);
//    }
    @Inject
    public CommandRouter(Map<String, Command> commands) {
        this.commands = commands;
    }

    Command.Result route(String input) {
        List<String> splitInput = split(input);
        if (splitInput.isEmpty()) {
            return invalidCommand(input);
        }

        String commandKey = splitInput.get(0);
        Command command = commands.get(commandKey);
        if (command == null) {
            return invalidCommand(input);
        }

        List<String> args = splitInput.subList(1, splitInput.size());
        Command.Result result = command.handleInput(args);
        return result.status().equals(Command.Status.INVALID) ?
                invalidCommand(input) : result;
    }

    private Command.Result invalidCommand(String input) {
        System.out.println(
                String.format("couldn't understand \"%s\". please  try again.", input));
        return Command.Result.invalid();
    }

    // Split on whitespace
    private static List<String> split(String input) {
        return Arrays.asList(input.trim().split("\\s+"));
    }

    @Override
    public void test() {
        System.out.println("Test DaggerSampleService implementation");
    }
}

class CommandLineAtm {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        CommandRouter commandRouter = new CommandRouter();
        CommandRouterFactory commandRouterFactory =
                DaggerCommandRouterFactory.create();
        CommandRouter commandRouter = commandRouterFactory.router();
        while (scanner.hasNextLine()) {
            Command.Result unused = commandRouter.route(scanner.nextLine());
        }
    }
}