package jeeves.server.resources;

import jeeves.server.context.ServiceContext;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate statistics of a connection.
 *
 * User: jeichar
 * Date: 4/5/12
 * Time: 10:15 AM
 */
public class Stats {
    public final Integer numActive;
    public final Integer numIdle;
    public final Integer maxActive;
    public final Map<String, String> resourceSpecificStats;


    public Stats(final ServiceContext context) {
        DataSource source = context.getBean(DataSource.class);
        if (source instanceof BasicDataSource) {
            BasicDataSource basicDataSource = (BasicDataSource) source;
            numActive = basicDataSource.getNumActive();
            numIdle = basicDataSource.getNumIdle();
            maxActive = basicDataSource.getMaxActive();
        } else {
            maxActive = numActive = numIdle = -1;
        }
        resourceSpecificStats = new HashMap<String, String>();
    }
}
