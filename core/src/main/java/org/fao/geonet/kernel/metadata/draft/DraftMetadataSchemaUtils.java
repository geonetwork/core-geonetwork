/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.metadata.DefaultMetadataSchemaUtils;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DraftMetadataSchemaUtils extends DefaultMetadataSchemaUtils {
    @Autowired
    private MetadataDraftRepository mdRepository;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        super.init(context);
        this.mdRepository = context.getBean(MetadataDraftRepository.class);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataSchemaUtils#getMetadataSchema(java.lang.String)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public String getMetadataSchema(String id) throws Exception {
        try {
            String schema = super.getMetadataSchema(id);

            if (schema != null && !schema.isEmpty()) {
                return schema;
            }
        } catch (IllegalArgumentException e) {
            // Then it is a draft
        }
        MetadataDraft md = mdRepository.findOne(id);

        if (md == null) {
            throw new IllegalArgumentException(
                    "Metadata not found for id : " + id);
        } else {
            return md.getDataInfo().getSchemaId();
        }
    }
}
