
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class TestConversation implements Conversation {

    @Override
    public boolean accept(Message message) {
        return message.getContentRaw().toLowerCase().startsWith("test");
    }

    @Override
    public Instance instance() {
        return new Impl();
    }

    class Impl implements Instance {
        ConvState state = ConvState.BEGIN;
        int counter = 0;
        
        @Override
        public Object apply(Message message) {
            switch (state) {
                case BEGIN:
                    state = ConvState.GOING;
                    return "You started a test conversation.";
                case GOING:
                    if (message.getContentRaw().equalsIgnoreCase("goodbye")) {
                        state = ConvState.END;
                        return "Ending the conversation now. Messages: " + counter;
                    } else {
                        return new MessageBuilder()
                                .append("To end this conversation, enter \"goodbye\"\n")
                                .appendFormat("This is Message #%d", counter++)
                                .build();
                    }
                case END:
                    return "THIS CONVERSATION HAS ENDED, YOU SHOULD NOT SEE THIS";
                default:
                    throw new AssertionError(state.name());
            }
        }

        @Override
        public boolean isFinished() {
            return state == ConvState.END;
        }
    }
    
    static enum ConvState {
        BEGIN,
        GOING,
        END;
    }
}
