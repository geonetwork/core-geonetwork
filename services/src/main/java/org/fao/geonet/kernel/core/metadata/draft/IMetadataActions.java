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
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.LuceneQueryBuilder;

/**
 * Interface that handles Metadata actions. For example, different
 * implementations will use draft or not.
 * 
 * @author María Arias de Reyna
 * 
 * 
 */
public interface IMetadataActions {
    /**
     * 
     * @param tokenizedFieldSet
     *            names of tokenized fields
     * @param numericFieldSet
     *            names of numeric fields
     * @param analyzer
     *            Lucene analyzer
     * @param langCode
     *            language of search terms
     * @return
     */
    LuceneQueryBuilder getQueryBuilder(Set<String> tokenizedFieldSet,
            Map<String, LuceneConfig.LuceneConfigNumericField> numericFieldSet,
            PerFieldAnalyzerWrapper analyzer, String langCode);

    /**
     * Get the StatusActionsFactory defined on this environment
     * 
     * @param gc
     * 
     */
    StatusActionsFactory getStatusActionsFactory(GeonetContext gc);

    /**
     * @param finished
     *            if true the editing is finished
     */
    void onSave(boolean finished);

    /**
     * @param metadataId
     *            Metadata that is going to be edited
     * @param finished
     *            if true the editing is finished
     * @return
     */
    IMetadata onEdit(int metadataId, boolean finished);

    /**
     * @param metadataId
     *            Metadata that is going to be removed
     * @param finished
     *            if true the editing is finished
     */
    void onDelete(int metadataId, boolean finished);

    /**
     * @param metadataId
     *            Metadata that was being edited
     * @param finished
     *            if true the editing is finished
     */
    void onCancelEditSession(int metadataId, boolean finished);
}