
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.FallbackConversation;
import net.dv8tion.jda.core.MessageBuilder;

/**
 *
 * @author Alexander
 */
public class DefaultFallbackConversation extends FallbackConversation {
    @Override
    public Instance instance() {
        return ((msg) -> {
            return new MessageBuilder()
                    .append("Sorry, I didn't understand that.")
                    .build();
        });
    }
}
