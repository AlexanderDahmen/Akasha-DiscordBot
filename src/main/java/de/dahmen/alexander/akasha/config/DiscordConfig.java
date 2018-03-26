
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
@ConfigClass("discord")
public class DiscordConfig {
    @ConfigField("token")
    private String token;
}
