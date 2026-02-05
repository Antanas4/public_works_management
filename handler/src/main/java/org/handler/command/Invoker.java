package org.handler.command;

public interface Invoker <T extends Command> {
    void invoke(T command);
}
