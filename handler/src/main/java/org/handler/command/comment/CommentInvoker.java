package org.handler.command.comment;

import org.handler.command.Invoker;
import org.springframework.stereotype.Component;

@Component
public class CommentInvoker implements Invoker<CommentCommand> {
    @Override
    public void invoke(CommentCommand command) {
        command.execute();
    }
}
