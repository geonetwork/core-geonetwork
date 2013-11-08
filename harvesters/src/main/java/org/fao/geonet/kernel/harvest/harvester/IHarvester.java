package org.fao.geonet.kernel.harvest.harvester;

import java.util.List;

import org.fao.geonet.Logger;

/**
 * Common interface for all Harvesters. T is the return type of the harvest
 * function.
 * 
 * @author delawen
 * 
 */
public interface IHarvester<T extends HarvestResult> {

    /**
     * Returns all the (important?) exceptions that were thrown during the
     * execution
     */
    List<HarvestError> getErrors();

    /**
     * Actual harvest function.
     * 
     * @return
     * @throws Exception
     */
    T harvest(Logger log) throws Exception;

}
