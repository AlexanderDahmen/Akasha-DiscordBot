
package de.dahmen.alexander.akasha.config.lib;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Help display for config classes
 * @author Alexander
 */
public class ConfigHelp {
    private final static PrintStream DEFAULT_OUT = System.out;
    private final static int CENTER_BLANKS = 2;
    
    /**
     * Print a help message about the application 
     * condiguration to <code>System.out</code>
     * @param configClasses 
     */
    public static void printHelp(List<Class<?>> configClasses) {
        printHelp(DEFAULT_OUT, configClasses);
    }
    
    /**
     * 
     * @param out
     * @param configClasses 
     */
    public static void printHelp(PrintStream out, List<Class<?>> configClasses) {
        out.println("Command line arguments:");
        printArgs(out, configClasses);
        out.println();
        out.println("Environment variables:");
        printEnv(out, configClasses);
    }
    
    private static void printArgs(PrintStream out, List<Class<?>> configClasses) {
        // Build alphabetically sorted argument map
        SortedMap<String, String> args = new TreeMap<>((a,b) -> a.compareToIgnoreCase(b));
        for (Class<?> clazz : configClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                ArgConfigField annotation = field.getAnnotation(ArgConfigField.class);
                if (annotation != null) args.put(annotation.arg(), annotation.description());
            }
        }
        
        // Find the longest argument length to format the output
        int argMaxLen = maxStringLength(args.keySet());
        
        // Go through arguments, print description entries
        for (Map.Entry<String, String> arg : args.entrySet()) {
            String argName = arg.getKey();
            String argDesc = arg.getValue();
            int blanks = argMaxLen - argName.length() + CENTER_BLANKS;
            
            // Print: [blanks]{argName}[blanks]{argDesc}[\n]
            out.print("  ");
            out.print(argName);
            for (int i = 0; i < blanks; i++) out.print(' ');
            out.print(argDesc);
            out.print('\n');
        }
    }
    
    private static void printEnv(PrintStream out, List<Class<?>> configClasses) {
        // Build alphabetically sorted environment variable set
        SortedSet<String> envs = new TreeSet<>((a,b) -> a.compareToIgnoreCase(b));
        for (Class<?> clazz : configClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                EnvironmentField annotation = field.getAnnotation(EnvironmentField.class);
                if (annotation != null) envs.add(annotation.value());
            }
        }
        
        // Go through the environment variables, print names
        for (String env : envs) {
            out.print("  ");
            out.print(env);
            out.print('\n');
        }
    }
    
    private static int maxStringLength(Collection<String> strings) {
        int maxLen = 0;
        for (String string : strings) {
            int len = string.length();
            if (len > maxLen) maxLen = len;
        }
        return maxLen;
    }
    
    private ConfigHelp() {}
}
