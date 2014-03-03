package org.fao.geonet.kernel;

import static org.junit.Assert.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * Tests for the DataManager.
 * <p/>
 * User: Jesse
 * Date: 10/24/13
 * Time: 5:30 PM
 */
public class DataManagerIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    DataManager _dataManager;
    @Autowired
    MetadataRepository _metadataRepository;

    @Test
    public void testDeleteMetadata() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final String mdId = _dataManager.insertMetadata(serviceContext, "iso19193", new Element("MD_Metadata"), "uuid",
                userSession.getUserIdAsInt(),
                "" + ReservedGroup.all.getId(), "sourceid", "n", "doctype", null, new ISODate().getDateAndTime(), new ISODate().getDateAndTime(),
                false, false);

        assertEquals(1, _metadataRepository.count());

        _dataManager.deleteMetadata(serviceContext, mdId);

        assertEquals(0, _metadataRepository.count());
    }

    @Test
    public void testCreateMetadataWithTemplateMetadata() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final User principal = serviceContext.getUserSession().getPrincipal();

        final GroupRepository bean = serviceContext.getBean(GroupRepository.class);
        Group group = bean.findAll().get(0);

        MetadataCategory category = serviceContext.getBean(MetadataCategoryRepository.class).findAll().get(0);

        final SourceRepository sourceRepository = serviceContext.getBean(SourceRepository.class);
        Source source = sourceRepository.save(new Source().setLocal(true).setName("GN").setUuid("sourceuuid"));

        final Element sampleMetadataXml = super.getSampleMetadataXml();
        final Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml)
            .setUuid(UUID.randomUUID().toString());
        metadata.getCategories().add(category);
        metadata.getDataInfo().setSchemaId("iso19139");
        metadata.getSourceInfo().setSourceId(source.getUuid());

        final Metadata templateMd = _metadataRepository.save(metadata);
        final String newMetadataId = _dataManager.createMetadata(serviceContext, "" + metadata.getId(), "" + group.getId(), source.getUuid(),
                principal.getId(), templateMd.getUuid(), MetadataType.METADATA.codeString, true);

        Metadata newMetadata = _metadataRepository.findOne(newMetadataId);
        assertEquals(1, newMetadata.getCategories().size());
        assertEquals(category, newMetadata.getCategories().iterator().next());
        assertEqualsText(metadata.getUuid(), newMetadata.getXmlData(false), "gmd:parentIdentifier/gco:CharacterString");

    }

    @Test
    public void testSetStatus() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element sampleMetadataXml = super.getSampleMetadataXml();
        final ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        final int metadataId = importMetadataXML(serviceContext, "uuidSetStatus", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.GENERATE_UUID);

        final MetadataStatus status = _dataManager.getStatus(metadataId);

        assertEquals(null, status);

        final ISODate changeDate = new ISODate();
        final String changeMessage = "Set to draft";
        _dataManager.setStatus(serviceContext, metadataId, 0, changeDate, changeMessage);

        final MetadataStatus loadedStatus = _dataManager.getStatus(metadataId);

        assertEquals(changeDate, loadedStatus.getId().getChangeDate());
        assertEquals(changeMessage, loadedStatus.getChangeMessage());
        assertEquals(0, loadedStatus.getStatusValue().getId());
        assertEquals(metadataId, loadedStatus.getId().getMetadataId());
        assertEquals(0, loadedStatus.getId().getStatusId());
        assertEquals(serviceContext.getUserSession().getUserIdAsInt(), loadedStatus.getId().getUserId());



    }
}
