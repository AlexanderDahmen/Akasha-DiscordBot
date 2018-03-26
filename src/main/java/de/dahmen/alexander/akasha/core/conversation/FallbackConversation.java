package de.dahmen.alexander.akasha.core.conversation;

import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public abstract class FallbackConversation implements Conversation {
    /**
     * Fallback for a conversation that never accepts messages on it's own
     * @param message Ignored
     * @return Always false
     */
    @Override
    public final boolean accept(Message message) {
        return false;
    }
}
