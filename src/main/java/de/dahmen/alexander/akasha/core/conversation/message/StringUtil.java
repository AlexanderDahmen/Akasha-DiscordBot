
package de.dahmen.alexander.akasha.core.conversation.message;

/**
 *
 * @author Alexander
 */
public class StringUtil {
    
    public static final String ELLIPSIS = "...";
    
    public static String truncateEllipsis(String string, int length) {
        // Zero or less is interpreted as full string
        if (length < 1)
            return string;
        
        // Thing characters count as half an additional character
        length += Math.ceil(string.replaceAll("[^iIl1]", "").length());
        
        return (string.length() > length) ?
                string.substring(0, length - ELLIPSIS.length()) + ELLIPSIS :
                string;
    }
    
    public static String firstLine(String string) {
        return string.split("\\n", 2)[0];
    }
    
    private StringUtil() { }
}
