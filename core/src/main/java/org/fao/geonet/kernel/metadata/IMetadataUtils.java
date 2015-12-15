/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * Addon to {@link DataManager} to handle util metadata actions
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataUtils {
    /**
     * Extract UUID from the metadata record using the schema XSL for UUID
     * extraction)
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    @Deprecated
    public String extractUUID(String schema, Element md) throws Exception;

    /**
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    public String extractDateModified(String schema, Element md)
            throws Exception;

    /**
     *
     * @param schema
     * @param uuid
     * @param md
     * @return
     * @throws Exception
     */
    public Element setUUID(String schema, String uuid, Element md)
            throws Exception;

    /**
     *
     * @param md
     * @return
     * @throws Exception
     */
    public Element extractSummary(Element md) throws Exception;

    /**
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public @Nullable String getMetadataId(@Nonnull String uuid)
            throws Exception;

    /**
     *
     * @param id
     * @return
     * @throws Exception
     */
    public @Nullable String getMetadataUuid(@Nonnull String id)
            throws Exception;

    /**
     *
     * @param id
     * @return
     */
    public String getVersion(String id);

    /**
     *
     * @param id
     * @return
     */
    public String getNewVersion(String id);

    /**
     * TODO javadoc.
     *
     * @param id
     * @param type
     * @param title
     * @throws Exception
     */
    public void setTemplate(final int id, final MetadataType type,
            final String title) throws Exception;

    /**
     * TODO javadoc.
     *
     * @param id
     * @throws Exception
     */
    public void setTemplateExt(final int id, final MetadataType metadataType)
            throws Exception;

    /**
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    void setHarvested(int id, String harvestUuid) throws Exception;

    /**
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    void setHarvestedExt(int id, String harvestUuid) throws Exception;

    /**
     * @param id
     * @param harvestUuid
     * @param harvestUri
     * @throws Exception
     */
    void setHarvestedExt(int id, String harvestUuid,
            Optional<String> harvestUri) throws Exception;

    /**
     *
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    public void updateDisplayOrder(final String id, final String displayOrder)
            throws Exception;

    /**
     *
     * @param srvContext
     * @param id
     * @throws Exception
     *             hmm
     */
    public void increasePopularity(ServiceContext srvContext, String id)
            throws Exception;

    /**
     * Rates a metadata.
     *
     * @param metadataId
     * @param ipAddress
     *            ipAddress IP address of the submitting client
     * @param rating
     *            range should be 1..5
     * @return
     * @throws Exception
     *             hmm
     */
    public int rateMetadata(final int metadataId, final String ipAddress,
            final int rating) throws Exception;

    /**
     * 
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
    public void versionMetadata(ServiceContext context, String id, Element md)
            throws Exception;

    /**
     * 
     * @param md
     * @return
     * @throws Exception
     */
    public Element enumerateTree(Element md) throws Exception;

    /**
     * Returns all the keywords in the system.
     *
     * @return
     * @throws Exception
     */
    public Element getKeywords() throws Exception;

    /**
     *
     * @param metadataId
     * @return
     * @throws Exception
     */
    public Element getThumbnails(ServiceContext context, String metadataId)
            throws Exception;

    /**
     *
     * @param context
     * @param id
     * @param small
     * @param file
     * @throws Exception
     */
    public void setThumbnail(ServiceContext context, String id, boolean small,
            String file, boolean indexAfterChange) throws Exception;

    /**
     *
     * @param context
     * @param id
     * @param small
     * @throws Exception
     */
    public void unsetThumbnail(ServiceContext context, String id, boolean small,
            boolean indexAfterChange) throws Exception;

    /**
     *
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
    public void setDataCommons(ServiceContext context, String id,
            String licenseurl, String imageurl, String jurisdiction,
            String licensename, String type) throws Exception;

    /**
     *
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
    public void setCreativeCommons(ServiceContext context, String id,
            String licenseurl, String imageurl, String jurisdiction,
            String licensename, String type) throws Exception;

    public boolean isUserMetadataOwner(int userId) throws Exception;

    public boolean isUserMetadataStatus(int userId) throws Exception;

    public boolean existsUser(ServiceContext context, int id) throws Exception;

    /**
     * Updates all children of the selected parent. Some elements are protected
     * in the children according to the stylesheet used in
     * xml/schemas/[SCHEMA]/update-child-from-parent-info.xsl.
     *
     * Children MUST be editable and also in the same schema of the parent. If
     * not, child is not updated.
     *
     *
     * @param srvContext
     *            service context
     * @param parentUuid
     *            parent uuid
     * @param children
     *            children
     * @param params
     *            parameters
     * @return
     * @throws Exception
     */
    public Set<String> updateChildren(ServiceContext srvContext,
            String parentUuid, String[] children, Map<String, Object> params)
                    throws Exception;

    /**
     * Add privileges information about metadata record which depends on context
     * and usually could not be stored in db or Lucene index because depending
     * on the current user or current client IP address.
     *
     * @param context
     * @param mdIdToInfoMap
     *            a map from the metadata Id -> the info element to which the
     *            privilege information should be added.
     * @throws Exception
     */
    @VisibleForTesting
    void buildPrivilegesMetadataInfo(ServiceContext context,
            Map<String, Element> mdIdToInfoMap) throws Exception;

    /**
     * 
     * @param md
     * @param metadataId
     * @throws Exception
     */
    public void notifyMetadataChange(Element md, String metadataId)
            throws Exception;

    public void flush();
}
