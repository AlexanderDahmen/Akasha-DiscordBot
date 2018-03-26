
package de.dahmen.alexander.akasha.config;

import de.dahmen.alexander.akasha.config.lib.ConfigClass;
import de.dahmen.alexander.akasha.config.lib.ConfigField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Alexander
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ConfigClass("database")
public class DatabaseConfig {
    @ConfigField("driver")
    private String driver;
    
    @ConfigField("username")
    private String username;
    
    @ConfigField("password")
    private String password;
    
    @ConfigField("url")
    private String url;
    
    @ConfigField("options")
    private String options;
}
