package org.fao.geonet;

import com.google.common.base.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

/**
 * Configures the H2 database to be a bit more string so mysql and other databases are more likely to work if the tests pass.
 *
 * User: Jesse
 * Date: 10/31/13
 * Time: 10:42 AM
 */
public class GeonetworkH2TestEmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {

    private final Class<? extends Driver> driverClass;
    private String _mode = "";
    private String _username = "sa";
    private String _password = "p";
    private Optional<Callable<String>> _dbPathLocator = Optional.absent();

    public GeonetworkH2TestEmbeddedDatabaseConfigurer() {
        try {
            this.driverClass = (Class<? extends Driver>) ClassUtils.forName("org.h2.Driver", GeonetworkH2TestEmbeddedDatabaseConfigurer.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
        properties.setDriverClass(this.driverClass);
//        properties.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", databaseName));
        if (_dbPathLocator.isPresent()) {
            try {
                properties.setUrl(String.format("jdbc:h2:%s;DB_CLOSE_DELAY=-1%s", _dbPathLocator.get().call(), _mode));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            properties.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1%s", databaseName, _mode));
        }
        properties.setUsername(_username);
        properties.setPassword(_password);
    }
    protected final Log logger = LogFactory.getLog(getClass());

    public void setCompatilityMode(String mode) {
        this._mode = "MODE="+mode;
    }

    public void shutdown(DataSource dataSource, String databaseName) {
        try {
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            stmt.execute("SHUTDOWN");
        }
        catch (SQLException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not shutdown embedded database", ex);
            }
        }
    }

    public void setDatabasePathLocator(Callable<String> locator) {
        this._dbPathLocator = Optional.of(locator);
    }

    public void setUsername(String username) {
        this._username = username;
    }

    public void setPassword(String password) {
        this._password = password;
    }
}