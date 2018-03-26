
package de.dahmen.alexander.akasha;

import de.dahmen.alexander.akasha.config.DatabaseConfig;
import de.dahmen.alexander.akasha.config.DiscordConfig;
import de.dahmen.alexander.akasha.config.lib.Config;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;

/**
 *
 * @author Alexander
 */
public class Akasha {
    private static final String CONFIG_FILE = "config.properties"; // This file is in .gitignore
    private static final List<Class<?>> CONFIG_CLASSES = Collections.unmodifiableList(Arrays.asList(
            DatabaseConfig.class,
            DiscordConfig.class));
    
    public static void main(String[] args) throws Exception {
        Akasha akasha = new Akasha();
        AkashaListener listener = new AkashaListener(akasha);
        akasha.jda.addEventListener(listener);
    }
    
    private final Config config;
    private final DataSource database;
    private final JDA jda;
    
    private Akasha() throws Exception {
        this(Config.load(CONFIG_FILE, CONFIG_CLASSES));
    }
    
    private Akasha(Config config) throws Exception {
        this.config = config;
        this.database = createDataSource();
        this.jda = createJDA();
    }
    
    private DataSource createDataSource() {
        DatabaseConfig dc = config.get(DatabaseConfig.class);
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dc.getDriver());
        ds.setUsername(dc.getUsername());
        ds.setPassword(dc.getPassword());
        ds.setUrl(dc.getUrl() +
                ((dc.getOptions() == null || dc.getOptions().isEmpty()) ?
                        "" :
                        "?".concat(dc.getOptions())));
        migrate(ds);
        return ds;
    }
    
    private JDA createJDA() throws LoginException, InterruptedException {
        return new JDABuilder(AccountType.BOT)
                .setToken(config.get(DiscordConfig.class).getToken())
                .setStatus(OnlineStatus.ONLINE)
                .setAutoReconnect(true)
                .buildBlocking();
    }
    
    private void migrate(DataSource ds) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.migrate();
    }
    
    
    
    public void shutdown() {
        
    }
}
