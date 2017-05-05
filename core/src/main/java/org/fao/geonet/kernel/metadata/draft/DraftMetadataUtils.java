/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import static org.fao.geonet.repository.specification.MetadataDraftSpecs.hasMetadataUuid;

import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.metadata.DefaultMetadataUtils;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.Updater;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * It also uses the
 * 
 * @author delawen
 * 
 * 
 */
public class DraftMetadataUtils extends DefaultMetadataUtils {

  @Autowired
  private MetadataDraftRepository mdRepository;

  @Autowired
  private AccessManager accessManager;

  /**
   * @param context
   */
  @Override
  public void init(ServiceContext context) {
    super.init(context);
    this.mdRepository = context.getBean(MetadataDraftRepository.class);
    this.accessManager = context.getBean(AccessManager.class);
  }

  /**
   * @see org.fao.geonet.kernel.metadata.DefaultMetadataUtils#getMetadataId(java.lang.String)
   * @param uuid
   * @return
   * @throws Exception
   */
  @Override
  public String getMetadataId(String uuid) throws Exception {
    String id = super.getMetadataId(uuid);

    if (id != null && !id.isEmpty()) {
      return id;
    }

    // Theoretically, this should never work. If there is a draft it
    // is because there is a published metadata. But, let's be safe. Who
    // knows.
    List<Integer> idList = mdRepository.findAllIdsBy(hasMetadataUuid(uuid));

    if (idList.isEmpty()) {
      return null;
    }
    return String.valueOf(idList.get(0));
  }

  /**
   * @see org.fao.geonet.kernel.metadata.DefaultMetadataUtils#getMetadataId(java.lang.String,
   *      java.lang.Integer)
   * @param uuid
   * @return
   * @throws Exception
   */
  @Override
  public String getMetadataId(String uuid, int userIdAsInt) throws Exception {

    List<Integer> idList = mdRepository.findAllIdsBy(hasMetadataUuid(uuid));

    if (!idList.isEmpty()) {
      Integer mdId = idList.get(0);
      if (accessManager.canEdit(String.valueOf(mdId))) {
        return mdId.toString();
      }
    }

    return getMetadataId(uuid);

  }

  /**
   * @see org.fao.geonet.kernel.metadata.DefaultMetadataUtils#getMetadataUuid(java.lang.String)
   * @param id
   * @return
   * @throws Exception
   */
  @Override
  public String getMetadataUuid(String id) throws Exception {
    String uuid = super.getMetadataUuid(id);

    if (uuid != null && !uuid.isEmpty()) {
      return uuid;
    }

    MetadataDraft metadata = mdRepository.findOne(id);

    if (metadata == null)
      return null;

    return metadata.getUuid();
  }

  /**
   * @see org.fao.geonet.kernel.metadata.DefaultMetadataUtils#setHarvestedExt(int,
   *      java.lang.String, com.google.common.base.Optional)
   * @param id
   * @param harvestUuid
   * @param harvestUri
   * @throws Exception
   */
  @Override
  public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri)
      throws Exception {
    if (mdRepository.exists(id)) {
      mdRepository.update(id, new Updater<MetadataDraft>() {
        @Override
        public void apply(MetadataDraft metadata) {
          MetadataHarvestInfo harvestInfo = metadata.getHarvestInfo();
          harvestInfo.setUuid(harvestUuid);
          harvestInfo.setHarvested(harvestUuid != null);
          harvestInfo.setUri(harvestUri.orNull());
        }
      });
    } else {
      super.setHarvestedExt(id, harvestUuid, harvestUri);
    }
  }

  @Override
  public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
    if (mdRepository.exists(id)) {
      mdRepository.update(id, new Updater<MetadataDraft>() {
        @Override
        public void apply(@Nonnull MetadataDraft metadata) {
          final MetadataDataInfo dataInfo = metadata.getDataInfo();
          dataInfo.setType(metadataType);
        }
      });
    } else {
      super.setTemplateExt(id, metadataType);
    }

  }

  /**
   * @see org.fao.geonet.kernel.metadata.DefaultMetadataUtils#updateDisplayOrder(java.lang.String,
   *      java.lang.String)
   * @param id
   * @param displayOrder
   * @throws Exception
   */
  @Override
  public void updateDisplayOrder(String id, final String displayOrder) throws Exception {
    if (mdRepository.exists(Integer.valueOf(id))) {
      mdRepository.update(Integer.valueOf(id), new Updater<MetadataDraft>() {
        @Override
        public void apply(MetadataDraft entity) {
          entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder));
        }
      });
    } else {
      super.updateDisplayOrder(id, displayOrder);
    }
  }
}
