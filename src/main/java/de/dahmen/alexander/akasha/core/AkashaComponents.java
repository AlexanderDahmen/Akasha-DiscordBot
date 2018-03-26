package de.dahmen.alexander.akasha.core;

import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;

/**
 *
 * @author Alexander
 */
public interface AkashaComponents {
    JdaTaskRepository jdaTaskRepository();
    
    JdaUserRepository jdaUserRepository();
}
