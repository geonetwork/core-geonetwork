package org.fao.geonet.kernel.search;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import jeeves.server.context.ServiceContext;

public interface MetadataRecordSelector {
    
    /**
     * Return UUIDs of all the metadata selected by this object.
     *
     * @param maxHits the maximum number to return
     * @param context context to use during selection
     * @throws Exception
     */
    @Nonnull
    List<String> getAllUuids(@Nonnegative int maxHits, @Nonnull ServiceContext context) throws Exception;
}
