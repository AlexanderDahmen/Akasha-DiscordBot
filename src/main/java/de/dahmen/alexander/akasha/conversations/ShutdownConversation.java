
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class ShutdownConversation implements Conversation {
    @Override
    public boolean accept(Message message) {
        return message.getContentRaw().equals("SHUTDOWN");
    }

    @Override
    public Instance instance() {
        return (msg) -> {
            new Thread(() -> {
                try {
                    Thread.sleep(2000L);
                    msg.getJDA().shutdown();
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            return "Will shut down.";
        };
    }
}
