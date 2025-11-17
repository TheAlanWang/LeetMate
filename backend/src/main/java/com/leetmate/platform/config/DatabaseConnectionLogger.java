package com.leetmate.platform.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * Verifies database connectivity at startup and logs the target URL/user.
 */
@Component
public class DatabaseConnectionLogger {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionLogger.class);

    private final DataSource dataSource;

    public DatabaseConnectionLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Database connection successful. url={}, user={}", metaData.getURL(), metaData.getUserName());
        } catch (SQLException ex) {
            log.error("Database connection failed at startup", ex);
            // rethrow if you prefer to fail hard: throw new IllegalStateException(ex);
        }
    }
}
