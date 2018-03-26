
package de.dahmen.alexander.akasha;

import de.dahmen.alexander.akasha.config.DatabaseConfig;
import de.dahmen.alexander.akasha.config.DiscordConfig;
import de.dahmen.alexander.akasha.config.lib.Config;
import de.dahmen.alexander.akasha.core.conversation.ConversationHandler;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import de.dahmen.alexander.akasha.repository.mysql.MysqlJdaTaskRepository;
import de.dahmen.alexander.akasha.repository.mysql.MysqlJdaUserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import de.dahmen.alexander.akasha.core.AkashaComponents;
import de.dahmen.alexander.akasha.core.AkashaListener;
import lombok.AllArgsConstructor;

/**
 *
 * @author Alexander
 */
@Slf4j
public class Akasha {
    private static final String CONFIG_FILE = "config.properties"; // This file is in .gitignore
    private static final List<Class<?>> CONFIG_CLASSES = Collections.unmodifiableList(Arrays.asList(
            DatabaseConfig.class,
            DiscordConfig.class));
    
    private static Config config = null;
    private static BasicDataSource database = null;
    private static AkashaComponents components = null;
    private static ConversationHandler conversation = null;
    private static JDA jda = null;
    
    public static void main(String[] args) throws Exception {
        config = Config.load(CONFIG_FILE, CONFIG_CLASSES);
        database = createDataSource();
        migrate(database);
        
        conversation = new ConversationHandler();
        jda = createJDA(new AkashaListener(conversation));
        components = new AkashaComponentsImpl(
                new MysqlJdaTaskRepository(database),
                new MysqlJdaUserRepository(jda, database));
        
        Runtime.getRuntime().addShutdownHook(new Thread(Akasha::shutdown));
    }
    
    public AkashaComponents components() {
        return components;
    }
    
    public static void shutdown() {
        safeClose(database);
        jda.shutdownNow();
    }
    
    private static BasicDataSource createDataSource() {
        DatabaseConfig dc = config.get(DatabaseConfig.class);
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dc.getDriver());
        ds.setUsername(dc.getUsername());
        ds.setPassword(dc.getPassword());
        ds.setUrl(dc.getUrl() +
                ((dc.getOptions() == null || dc.getOptions().isEmpty()) ?
                        "" :
                        "?".concat(dc.getOptions())));
        return ds;
    }
    
    private static void migrate(DataSource ds) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.migrate();
    }
    
    private static JDA createJDA(Object... listeners) throws LoginException, InterruptedException {
        return new JDABuilder(AccountType.BOT)
                .setToken(config.get(DiscordConfig.class).getToken())
                .setStatus(OnlineStatus.ONLINE)
                .addEventListener(listeners)
                .setAutoReconnect(true)
                .buildBlocking();
    }
    
    private static void safeClose(AutoCloseable closeable) {
        try { closeable.close(); }
        catch (Exception ex) {
            log.warn("Shutdown Error: " + ex.getMessage(), ex);
        }
    }
    
    @AllArgsConstructor
    private static class AkashaComponentsImpl implements AkashaComponents {
        private final JdaTaskRepository jtr;
        private final JdaUserRepository jur;
        
        @Override public JdaTaskRepository jdaTaskRepository() { return jtr; }
        @Override public JdaUserRepository jdaUserRepository() { return jur; }
    }
}
