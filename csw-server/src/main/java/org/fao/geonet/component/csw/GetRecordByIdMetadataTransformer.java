package org.fao.geonet.component.csw;

import com.google.common.base.Optional;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.GeonetworkExtension;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 * An extension point to allow plugins to transform the metadata returned by {@link GetRecordById}
 *
 * User: Jesse
 * Date: 11/7/13
 * Time: 3:23 PM
 */
public interface GetRecordByIdMetadataTransformer extends GeonetworkExtension {

    /**
     * Transform the metadata record in some way.  Optional.absent() should be
     * returned if the record does not apply.
     *
     * @param context a service context.
     * @param metadata the metadata to transform
     * @param outputSchema the output schema GetRecords parameter.
     *
     * @return Optional.absent() if transformer does not apply to the metadata or output schema.  Or Optional.of() if a change is
     * made.
     *
     * @throws CatalogException
     */
    @Nonnull
    public Optional<Element> apply(@Nonnull ServiceContext context, @Nonnull Element metadata,
                                   @Nonnull OutputSchema outputSchema) throws CatalogException;
}
