package org.fao.geonet.api.records.model;

import static org.junit.Assert.assertEquals;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;

public class MetadataStatusResponseTest extends AbstractServiceIntegrationTest  {

    @Test
    public void testMetadataStatusIdSerializationAndDeserialization() throws Exception {

        MetadataStatus status = new MetadataStatus();
        MetadataStatusId id1 = new MetadataStatusId();
        status.setId(id1);

        id1.setChangeDate(new ISODate());
        id1.setMetadataId(123432);
        id1.setStatusId(50);
        id1.setUserId(1);

        MetadataStatusResponse response = new MetadataStatusResponse(status);

        String serializedString = response.getIdSerialized();

        MetadataStatusId id2 = MetadataStatusResponse.convertSerializedStringInMetadataStatusId(serializedString);

        assertEquals(id1, id2);
    }

}

