
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
        public Message apply(Message message) {
            switch (state) {
                case BEGIN:
                    state = ConvState.GOING;
                    return new MessageBuilder()
                            .append("You started a test conversation.")
                            .build();
                case GOING:
                    if (message.getContentRaw().equalsIgnoreCase("goodbye")) {
                        state = ConvState.END;
                        return new MessageBuilder()
                                .append("Ending the conversation now. Messages: ")
                                .append(counter)
                                .build();
                    } else {
                        return new MessageBuilder()
                                .append("To end this conversation, enter \"GOODBYE\"\n")
                                .appendFormat("This is Message #%d", counter++)
                                .build();
                    }
                case END:
                    return new MessageBuilder()
                            .append("THIS CONVERSATION HAS ENDED, YOU SHOULD NOT SEE THIS")
                            .build();
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
