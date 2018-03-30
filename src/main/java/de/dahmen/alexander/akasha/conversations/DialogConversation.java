
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.GeneratorConversationInstance;
import de.dahmen.alexander.akasha.core.conversation.message.MessageMultiTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.dv8tion.jda.core.entities.Message;

/**
 * Intermediary class for generator conversation instances, adding some
 * useful methods for user dialog interactions.
 * 
 * @author Alexander
 */
public abstract class DialogConversation extends GeneratorConversationInstance {
    
    protected static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("HH:mm:ss").toFormatter();
    
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

    protected boolean askYesNo(
            Object yesNoPrompt, Pattern yes, Pattern no)
            throws InterruptedException
    {
        while (true) {
            yield(yesNoPrompt);
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
            if (askYesNo(okayPrompt, yes, no)) {
                return result;
            }
        }
    }
    
    protected String cronDialog(
            Pattern yes, Pattern no,
            MessageMultiTemplate cronTemplates,
            Function<String, Boolean> checkValid)
            throws InterruptedException
    {
        MessageTemplate
                canYouDoCron = cronTemplates.get("CanYouDoCron"),
                askCron = cronTemplates.get("AskCron"),
                invalidCron = cronTemplates.get("InvalidCron"),
                cantCronThis = cronTemplates.get("CantCronThis"),
                askDayOfMonth = cronTemplates.get("AskDayOfMonth"),
                askMonth = cronTemplates.get("AskMonth"),
                askDayOfWeek = cronTemplates.get("AskDayOfWeek");
        MessageTemplate.BuildMessageTemplate
                askSeconds = cronTemplates.get("AskSeconds").set("max", 60),
                askMinutes = cronTemplates.get("AskMinutes").set("max", 60),
                askHours = cronTemplates.get("AskHours").set("max", 24);
        
        // Do direct input if possible
        boolean directInput = askYesNo(canYouDoCron, yes, no);
        if (!directInput)
            respond(cantCronThis);
        
        while (true) {
            String resultCron;
            
            if (directInput) {
                resultCron = yieldMessage(askCron).getContentRaw().trim();
            } else {
                String seconds = contentWithoutWhitespace(yieldMessage(askSeconds));
                String minutes = contentWithoutWhitespace(yieldMessage(askMinutes));
                String hours = contentWithoutWhitespace(yieldMessage(askHours));
                String dom = contentWithoutWhitespace(yieldMessage(askDayOfMonth));
                String month = contentWithoutWhitespace(yieldMessage(askMonth));
                String dow = contentWithoutWhitespace(yieldMessage(askDayOfWeek));
                resultCron = new StringJoiner(" ")
                        .add(seconds).add(minutes).add(hours).add(dom).add(month).add(dow)
                        .toString();
            }
            
            boolean okay = checkValid.apply(resultCron);
            if (okay) {
                return resultCron;
            } else {
                boolean tryAgain = askYesNo(
                        invalidCron.set("cron", resultCron),
                        yes, no);
                
                if (!tryAgain) {
                    stop();
                }
            }
        }
    }
    
    private String contentWithoutWhitespace(Message message) {
        return message.getContentRaw().replaceAll("\\s", "");
    }
    
    @FunctionalInterface
    protected static interface InterruptableSupplier<T> {
        T get() throws InterruptedException;
    }
}
