
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.GeneratorConversationInstance;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Pattern;

/**
 * Intermediary class for generator conversation instances, adding some
 * useful methods for user dialog interactions.
 * 
 * @author Alexander
 */
public abstract class DialogConversation extends GeneratorConversationInstance {
    
    protected static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder().appendPattern("HH:mm").parseCaseInsensitive().toFormatter();
    protected static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").parseCaseInsensitive().toFormatter();
    protected static final DateTimeFormatter DATETIME_FORMAT = new DateTimeFormatterBuilder().appendPattern("yyy-MM-dd HH:mm").parseCaseInsensitive().toFormatter();
    
    public DialogConversation(long timeoutMillis) {
        super(timeoutMillis);
    }
    
    protected int askInt(Object prompt) throws InterruptedException {
        return askInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    protected int askInt(Object prompt, int bound) throws InterruptedException {
        return askInt(prompt, 0, bound);
    }
    
    protected int askInt(Object prompt, int from, int to) throws InterruptedException {
        while (true) {
            yield(prompt);
            try {
                int result = Integer.parseInt(message().getContentStripped());
                if (result >= from && result <= to) {
                    return result;
                }
            } catch (NumberFormatException ex) { }
        }
    }
    
    protected boolean askYesNo(
            Object yesNoPrompt, Pattern yes, Pattern no)
            throws InterruptedException
    {
        while (true) {
            String answer = yieldMessage(yesNoPrompt).getContentStripped();
            if (yes.matcher(answer).matches()) return true;
            if (no.matcher(answer).matches()) return false;
        }
    }
    
    protected TemporalAccessor askDate(Object prompt) throws InterruptedException {
        return askTemporal(prompt, DATE_FORMAT);
    }
    
    protected TemporalAccessor askTime(Object prompt) throws InterruptedException {
        return askTemporal(prompt, TIME_FORMAT);
    }
    
    protected TemporalAccessor askTemporal(Object prompt, DateTimeFormatter format) throws InterruptedException {
        while (true) {
            try { return format.parse(yieldMessage(prompt).getContentRaw()); }
            catch (DateTimeParseException ex) { }
        }
    }
    
    protected <T> T untilOkay(
            Object okayPrompt, Pattern yes, Pattern no,
            InterruptableSupplier<T> function)
            throws InterruptedException
    {
        while (true) {
            T result = function.get();
            if (askYesNo(okayPrompt, yes, no)) {
                return result;
            }
        }
    }
    
    @FunctionalInterface
    protected static interface InterruptableSupplier<T> {
        T get() throws InterruptedException;
    }
}
