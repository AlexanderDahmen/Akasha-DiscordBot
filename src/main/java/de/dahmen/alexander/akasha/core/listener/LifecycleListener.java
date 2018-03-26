
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
    }
}
