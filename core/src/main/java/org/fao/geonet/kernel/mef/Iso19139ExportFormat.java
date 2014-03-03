package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static org.fao.geonet.kernel.mef.MEFConstants.FILE_METADATA_19139;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 11/8/13
 * Time: 3:35 PM
 */
public class Iso19139ExportFormat extends ExportFormat {

    @Override
    public Iterable<Pair<String, String>> getFormats(ServiceContext context, Metadata metadata) throws Exception {
        String schema = metadata.getDataInfo().getSchemaId();
        if (schema.contains("iso19139") && !schema.equals("iso19139")) {
            // ie. this is an ISO profil.
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            DataManager dm = gc.getBean(DataManager.class);
            MetadataSchema metadataSchema = dm.getSchema(schema);
            String path = metadataSchema.getSchemaDir() + "/convert/to19139.xsl";

            String data19139 = formatData(metadata, true, path);
            return Collections.singleton(Pair.read(FILE_METADATA_19139, data19139));
        }

        return Collections.emptyList();

    }

}
