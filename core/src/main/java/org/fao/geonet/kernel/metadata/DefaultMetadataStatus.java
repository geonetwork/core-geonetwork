/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataStatus implements IMetadataStatus {

  @Autowired
  private MetadataStatusRepository statusRepository;

  @Autowired
  private StatusValueRepository statusValueRepository;

  @Autowired
  private IMetadataIndexer metadataIndexer;

  @Autowired
  private GroupRepository groupRepository;

  /**
   * @param context
   */
  @Override
  public void init(ServiceContext context) {
    this.metadataIndexer = context.getBean(IMetadataIndexer.class);
    this.statusRepository = context.getBean(MetadataStatusRepository.class);
    this.statusValueRepository = context.getBean(StatusValueRepository.class);
    this.groupRepository = context.getBean(GroupRepository.class);
  }

  /**
   * 
   * @see org.fao.geonet.kernel.metadata.IMetadataStatus#getStatus(int)
   * @param metadataId
   * @return
   * @throws Exception
   */
  @Override
  public MetadataStatus getStatus(int metadataId) throws Exception {
    String sortField = SortUtils.createPath(MetadataStatus_.id, MetadataStatusId_.changeDate);
    List<MetadataStatus> status = statusRepository.findAllById_MetadataId(metadataId,
        new Sort(Sort.Direction.DESC, sortField));
    if (status.isEmpty()) {
      return null;
    } else {
      return status.get(0);
    }
  }

  /**
   * 
   * @see org.fao.geonet.kernel.metadata.IMetadataStatus#getCurrentStatus(int)
   * @param metadataId
   * @return
   * @throws Exception
   */
  @Override
  public String getCurrentStatus(int metadataId) throws Exception {
    MetadataStatus status = getStatus(metadataId);
    if (status == null) {
      return Params.Status.UNKNOWN;
    }

    return String.valueOf(status.getId().getStatusId());
  }

  /**
   * 
   * @see org.fao.geonet.kernel.metadata.IMetadataStatus#setStatus(jeeves.server.context.ServiceContext,
   *      int, int, org.fao.geonet.domain.ISODate, java.lang.String)
   * @param context
   * @param id
   * @param status
   * @param changeDate
   * @param changeMessage
   * @return
   * @throws Exception
   */
  @Override
  public MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage)
      throws Exception {
    MetadataStatus statusObject = setStatusExt(context, id, status, changeDate, changeMessage);
    metadataIndexer.indexMetadata(Integer.toString(id), true);
    return statusObject;
  }

  /**
   * 
   * @see org.fao.geonet.kernel.metadata.IMetadataStatus#setStatusExt(jeeves.server.context.ServiceContext,
   *      int, int, org.fao.geonet.domain.ISODate, java.lang.String)
   * @param context
   * @param id
   * @param status
   * @param changeDate
   * @param changeMessage
   * @return
   * @throws Exception
   */
  @Override
  public MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate,
      String changeMessage) throws Exception {

    MetadataStatus metatatStatus = new MetadataStatus();
    metatatStatus.setChangeMessage(changeMessage);
    metatatStatus.setStatusValue(statusValueRepository.findOne(status));
    int userId = context.getUserSession().getUserIdAsInt();
    MetadataStatusId mdStatusId = new MetadataStatusId().setStatusId(status).setMetadataId(id).setChangeDate(changeDate)
        .setUserId(userId);
    mdStatusId.setChangeDate(changeDate);

    metatatStatus.setId(mdStatusId);

    return statusRepository.save(metatatStatus);
  }

  /**
   * 
   * @see org.fao.geonet.kernel.metadata.IMetadataStatus#activateWorkflowIfConfigured(jeeves.server.context.ServiceContext,
   *      java.lang.String, java.lang.String)
   * @param context
   * @param newId
   * @param groupOwner
   * @throws Exception
   */
  @Override
  public void activateWorkflowIfConfigured(ServiceContext context, String newId, String groupOwner) throws Exception {
    if (StringUtils.isEmpty(groupOwner)) {
      return;
    }
    String groupMatchingRegex = ApplicationContextHolder.get().getBean(SettingManager.class)
        .getValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP);
    if (!StringUtils.isEmpty(groupMatchingRegex)) {
      final Group group = ApplicationContextHolder.get().getBean(GroupRepository.class)
          .findOne(Integer.valueOf(groupOwner));
      String groupName = "";
      if (group != null) {
        groupName = group.getName();
      }

      final Pattern pattern = Pattern.compile(groupMatchingRegex);
      final Matcher matcher = pattern.matcher(groupName);
      if (matcher.find()) {
        setStatus(context, Integer.valueOf(newId), Integer.valueOf(Params.Status.DRAFT), new ISODate(),
            String.format("Workflow automatically enabled for record in group %s. Record status is set to %s.",
                groupName, Params.Status.DRAFT));
      }
    }
  }

  @Override
  public MetadataStatus getPreviousStatus(int metadataId) throws Exception {
    String sortField = SortUtils.createPath(MetadataStatus_.id, MetadataStatusId_.changeDate);
    final MetadataStatusRepository statusRepository = ApplicationContextHolder.get()
        .getBean(MetadataStatusRepository.class);
    List<MetadataStatus> status = statusRepository.findAllById_MetadataId(metadataId,
        new Sort(Sort.Direction.DESC, sortField));
    if (status.size() <= 1) {
      MetadataStatus metatataStatus = new MetadataStatus();

      MetadataStatusId mdStatusId = new MetadataStatusId().setStatusId(Integer.parseInt(Params.Status.DRAFT))
          .setMetadataId(metadataId);

      metatataStatus.setId(mdStatusId);

      return metatataStatus;
    } else {
      return status.get(1);
    }
  }

}
