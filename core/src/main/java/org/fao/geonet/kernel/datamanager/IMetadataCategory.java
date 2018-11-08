package org.fao.geonet.kernel.datamanager;

import java.util.Collection;

import org.fao.geonet.domain.MetadataCategory;

import jeeves.server.context.ServiceContext;

/**
 * Interface to handle categories of records.
 * 
 * @author delawen
 *
 */
public interface IMetadataCategory {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     * 
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Given a record id and a category id, returns if the record has that category assigned.
     * 
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    boolean isCategorySet(String mdId, int categId) throws Exception;

    /**
     * Given a record id and a category id, assign that category to the previous record
     * 
     * @param context
     * @param mdId
     * @param categId
     * @return if the category was assigned
     * @throws Exception
     */
    boolean setCategory(ServiceContext context, String mdId, String categId) throws Exception;

    /**
     * Given a record id and a category id, unassign that category from the previous record
     * 
     * @param context
     * @param mdId
     * @param categId
     * @return if the category was deassigned
     * @throws Exception
     */
    boolean unsetCategory(ServiceContext context, String mdId, int categId) throws Exception;

    /**
     * Given a record id, return the list of categories associated to that record
     * 
     * @param mdId
     * @return
     * @throws Exception
     */
    Collection<MetadataCategory> getCategories(String mdId) throws Exception;
}
