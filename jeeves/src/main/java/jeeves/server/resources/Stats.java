package jeeves.server.resources;

import java.util.Collections;
import java.util.Map;

/**
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

    public Stats(Integer numActive, Integer numIdle, Integer maxActive, Map<String, String> resourceSpecificStats) {
        this.maxActive = maxActive;
        this.numActive = numActive;
        this.numIdle = numIdle;
        this.resourceSpecificStats = Collections.synchronizedMap(resourceSpecificStats);
    }
    public Stats(Integer numActive, Integer numIdle, Integer maxActive) {
        this.maxActive = maxActive;
        this.numActive = numActive;
        this.numIdle = numIdle;
        this.resourceSpecificStats = Collections.emptyMap();
    }
}
