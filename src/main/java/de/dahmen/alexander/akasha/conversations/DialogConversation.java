
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.core.conversation.GeneratorConversationInstance;
import de.dahmen.alexander.akasha.core.repository.RepositoryException;
import java.util.regex.Pattern;

/**
 * Intermediary class for generator conversation instances, adding some
 * useful methods for user dialog interactions.
 * 
 * @author Alexander
 */
public abstract class DialogConversation extends GeneratorConversationInstance {

    public DialogConversation(ConversationConfig config) {
        super(config);
    }
    
    public DialogConversation(long timeoutMillis) {
        super(timeoutMillis);
    }

    protected int askInt(
            Object prompt, int from, int to)
            throws InterruptedException
    {
        while (true) {
            yield(prompt);
            try {
                int result = Integer.parseInt(message().getContentStripped());
                if (result >= from && result <= to) {
                    return result;
                }
            } catch (NumberFormatException ex) {
            }
        }
    }

    protected boolean askOkay(
            Object okayPrompt, Pattern yes, Pattern no)
            throws InterruptedException
    {
        while (true) {
            yield(okayPrompt);
            String answer = message().getContentStripped();
            if (yes.matcher(answer).matches()) {
                return true;
            }
            if (no.matcher(answer).matches()) {
                return false;
            }
        }
    }

    protected <T> T untilOkay(
            Object okayPrompt, Pattern yes, Pattern no,
            InterruptableSupplier<T> function)
            throws InterruptedException
    {
        while (true) {
            T result = function.get();
            if (askOkay(okayPrompt, yes, no)) {
                return result;
            }
        }
    }
    
    @FunctionalInterface
    protected static interface InterruptableSupplier<T> {
        T get() throws InterruptedException;
    }
}
