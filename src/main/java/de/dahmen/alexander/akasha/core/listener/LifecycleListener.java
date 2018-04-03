
package de.dahmen.alexander.akasha.core.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 *
 * @author Alexander
 */
@Slf4j
public class LifecycleListener extends ListenerAdapter {
    
    private final Runnable[] runOnShutdown;

    public LifecycleListener(Runnable... runOnShutdown) {
        this.runOnShutdown = runOnShutdown;
    }
    
    @Override
    public void onReady(ReadyEvent event) {
        log.info("Akasha -- Ready");
    }
    
    @Override
    public void onShutdown(ShutdownEvent event) {
        log.info("Akasha -- Shutdown {} @ {} :: {}",
                event.getCloseCode().name(),
                event.getShutdownTime(),
                event.getCloseCode().getMeaning());
        
        for (Runnable runnable : runOnShutdown) {
            runnable.run();
        }
    }
}
