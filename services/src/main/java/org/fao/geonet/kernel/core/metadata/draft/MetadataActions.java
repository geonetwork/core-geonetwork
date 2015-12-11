/**
 * 
 */
package org.fao.geonet.kernel.core.metadata.draft;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.LuceneQueryBuilder;

/**
 * 
 * Simple implementation that works without drafts.
 * 
 * @author María Arias de Reyna
 * 
 * 
 */
public class MetadataActions implements IMetadataActions {

    /**
     * @see org.fao.geonet.kernel.core.metadata.draft.IMetadataActions#getQueryBuilder(java.util.Set,
     *      java.util.Map,
     *      org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper,
     *      java.lang.String)
     * @param tokenizedFieldSet
     * @param numericFieldSet
     * @param analyzer
     * @param langCode
     * @return
     */
    @Override
    public LuceneQueryBuilder getQueryBuilder(Set<String> tokenizedFieldSet,
            Map<String, LuceneConfigNumericField> numericFieldSet,
            PerFieldAnalyzerWrapper analyzer, String langCode) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.fao.geonet.kernel.core.metadata.draft.IMetadataActions#getStatusActionsFactory()
     * @return
     */
    @Override
    public StatusActionsFactory getStatusActionsFactory(GeonetContext gc) {
        return new StatusActionsFactory(gc.getStatusActionsClass());
    }

    /**
     * @see org.fao.geonet.kernel.core.metadata.draft.IMetadataActions#onSave(boolean)
     * @param finished
     */
    @Override
    public void onSave(boolean finished) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.fao.geonet.kernel.core.metadata.draft.IMetadataActions#onEdit(int,
     *      boolean)
     * @param metadataId
     * @param finished
     * @return
     */
    @Override
    public IMetadata onEdit(int metadataId, boolean finished) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.fao.geonet.kernel.core.metadata.draft.IMetadataActions#onDelete(int,
     *      boolean)
     * @param metadataId
     * @param finished
     */
    @Override
    public void onDelete(int metadataId, boolean finished) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.fao.geonet.kernel.core.metadata.draft.IMetadataActions#onCancelEditSession(int,
     *      boolean)
     * @param metadataId
     * @param finished
     */
    @Override
    public void onCancelEditSession(int metadataId, boolean finished) {
        // TODO Auto-generated method stub

    }

}
