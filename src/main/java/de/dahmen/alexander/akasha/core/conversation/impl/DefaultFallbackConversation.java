
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.FallbackConversation;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class DefaultFallbackConversation implements FallbackConversation {
    @Override
    public Message fallback(Message msg) {
        return new MessageBuilder()
                .append("Sorry, I didn't understand that.")
                .build();
    }
}
