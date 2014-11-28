//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.metadata;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.util.MailSender;
import org.fao.geonet.util.XslUtil;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultStatusActions implements StatusActions {

    private String host, port, username, password, from, fromDescr, replyTo, replyToDescr;
    private boolean useSSL;    protected ServiceContext context;
    protected String language;
    protected DataManager dm;
    protected String siteUrl;
    protected String siteName;
    protected UserSession session;
    protected boolean emailNotes = true;
    private StatusValueRepository _statusValueRepository;

    /**
     * Constructor.
     */
    public DefaultStatusActions() {
    }

    /**
     * Initializes the StatusActions class with external info from GeoNetwork.
     * 
     *
     * @param context
     * @throws IOException
     * @throws JDOMException
     */
    public void init(ServiceContext context) throws Exception {

        this.context = context;
        this._statusValueRepository = context.getBean(StatusValueRepository.class);
        this.language = context.getLanguage();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        siteName = sm.getSiteName();
        host = sm.getValue("system/feedback/mailServer/host");
        port = sm.getValue("system/feedback/mailServer/port");
        from = sm.getValue("system/feedback/email");
        username = sm.getValue("system/feedback/mailServer/username");
        password = sm.getValue("system/feedback/mailServer/password");
        useSSL = sm.getValueAsBool("system/feedback/mailServer/ssl");
        
        if (host == null || host.length() == 0) {
            context.error("Mail server host not configure");
            emailNotes = false;
        }

        if (port == null || port.length() == 0) {
            context.error("Mail server port not configured, email notifications won't be sent.");
            emailNotes = false;
        }

        if (from == null || from.length() == 0) {
            context.error("Mail feedback address not configured, email notifications won't be sent.");
            emailNotes = false;
        }

        fromDescr = siteName + LangUtils.translate(context.getApplicationContext(), "statusTitle").get(this.language);

        session = context.getUserSession();
        replyTo = session.getEmailAddr();
        if (replyTo != null) {
            replyToDescr = session.getName() + " " + session.getSurname();
        } else {
            replyTo = from;
            replyToDescr = fromDescr;
        }

        dm = gc.getBean(DataManager.class);
        siteUrl = context.getBean(SettingManager.class).getSiteURL(context);
    }

    /**
     * Called when a record is edited to set/reset status.
     * 
     * @param id The metadata id that has been edited.
     * @param minorEdit If true then the edit was a minor edit.
     */
    public void onEdit(int id, boolean minorEdit) throws Exception {

        if (!minorEdit && dm.getCurrentStatus(id).equals(Params.Status.APPROVED)) {
            String changeMessage = String.format(LangUtils.translate(context.getApplicationContext(),
                            "statusUserEdit").get(this.language), replyToDescr, replyTo, id);
            unsetAllOperations(id);
            dm.setStatus(context, id, Integer.valueOf(Params.Status.DRAFT), new ISODate(), changeMessage);
        }

    }

    /**
     * Called when need to set status on a set of metadata records.
     * 
     * @param status The status to set.
     * @param metadataIds The set of metadata ids to set status on.
     * @param changeDate The date the status was changed.
     * @param changeMessage The message explaining why the status has changed.
     */
    public Set<Integer> statusChange(String status, Set<Integer> metadataIds, ISODate changeDate, String changeMessage) throws Exception {

        Set<Integer> unchanged = new HashSet<Integer>();

        // -- process the metadata records to set status
        for (Integer mid : metadataIds) {
            String currentStatus = dm.getCurrentStatus(mid);

            // --- if the status is already set to value of status then do nothing
            if (status.equals(currentStatus)) {
                if (context.isDebugEnabled())
                    context.debug("Metadata " + mid + " already has status " + mid);
                unchanged.add(mid);
            }

            if (status.equals(Params.Status.APPROVED)) {
                // setAllOperations(mid); - this is a short cut that could be enabled
            } else if (status.equals(Params.Status.DRAFT) || status.equals(Params.Status.REJECTED)) {
                unsetAllOperations(mid);
            }

            // --- set status, indexing is assumed to take place later
            dm.setStatusExt(context, mid, Integer.valueOf(status), changeDate, changeMessage);
        }

        // --- inform content reviewers if the status is submitted
        if (status.equals(Params.Status.SUBMITTED)) {
            informContentReviewers(metadataIds, changeDate.toString(), changeMessage);
            // --- inform owners if status is approved
        } else if (status.equals(Params.Status.APPROVED) || status.equals(Params.Status.REJECTED)) {
            informOwners(metadataIds, changeDate.toString(), changeMessage, status);
        }

        return unchanged;
    }

    // -------------------------------------------------------------------------
    // Private methods
    // -------------------------------------------------------------------------

  /**
    * Unset all operations on 'All' Group. Used when status changes from approved to something else. 
    *
    * @param mdId The metadata id to unset privileges on
    */
  private void unsetAllOperations(int mdId) throws Exception {
      String allGroup = "1";
      for (ReservedOperation op : ReservedOperation.values()) {
          dm.unsetOperation(context, mdId+"", allGroup, op);
      }
  }

    /**
     * Inform content reviewers of metadata records in list that they need to review the record.
     * 
     * @param metadata The selected set of metadata records
     * @param changeDate The date that of the change in status
     * @param changeMessage Message supplied by the user that set the status
     */
    protected void informContentReviewers(Set<Integer> metadata, String changeDate, String changeMessage) throws Exception {

        // --- get content reviewers (sorted on content reviewer userid)
        UserRepository userRepository = context.getBean(UserRepository.class);
        List<Pair<Integer, User>> results = userRepository.findAllByGroupOwnerNameAndProfile(metadata,
                Profile.Reviewer, SortUtils.createSort(User_.name));

        List<User> users = Lists.transform(results, new Function<Pair<Integer, User>, User>() {
            @Nullable
            @Override
            public User apply(@Nonnull Pair<Integer, User> input) {
                    return input.two();
            }
        });
        String mdChanged = buildMetadataChangedMessage(metadata);
        String translatedStatusName = getTranslatedStatusName(Params.Status.SUBMITTED);
        String subject = String.format(LangUtils.translate(context.getApplicationContext(), "statusInform").get(this.language), siteName,
                translatedStatusName, replyToDescr, replyTo, changeDate);

        processList(users, subject, Params.Status.SUBMITTED,
                changeDate, changeMessage, mdChanged);
    }

    public static final Pattern metadataLuceneField = Pattern.compile("\\{\\{index:([^\\}]+)\\}\\}");
    
    private String buildMetadataChangedMessage(Set<Integer> metadata) {
    	String statusMetadataDetails = null;
    	String message = "";
 
    	try {
    		statusMetadataDetails = LangUtils.translate(context.getApplicationContext(), "statusMetadataDetails").get(this.language);
		} catch (Exception e) {}
    	// Fallback on a default value if statusMetadataDetails not resolved
    	if (statusMetadataDetails == null) {
    		statusMetadataDetails = "* {{index:title}} ({{serverurl}}/search?uuid={{index:_uuid}})";
    	}
    	
    	ArrayList<String> fields = new ArrayList<String>();
    	
    	Matcher m = metadataLuceneField.matcher(statusMetadataDetails);
        Iterable<Metadata> mds = this.context.getBean(MetadataRepository.class).findAll(metadata);
        
    	while (m.find()) {
    		fields.add(m.group(1));
    	}
    	
    	for (Metadata md : mds) {
    		String curMdDetails = statusMetadataDetails;
    		// First substitution for variables not stored in the index
    		curMdDetails = curMdDetails.replace("{{serverurl}}", siteUrl);
    		
    		for (String f: fields) {
    			String mdf = XslUtil.getIndexField(null, md.getUuid(), f, this.language);
    			curMdDetails = curMdDetails.replace("{{index:" + f + "}}", mdf);
    		}
    		message = message.concat(curMdDetails + "\r\n");
    	}
		return message;
	}

	private String getTranslatedStatusName(String statusValueId) {
        String translatedStatusName = "";
        StatusValue s = _statusValueRepository.findOneById(Integer.valueOf(statusValueId));
        if (s == null) {
            translatedStatusName = statusValueId;
        } else {
          translatedStatusName = s.getLabel(this.language);
        }
        return translatedStatusName;
    }

    /**
     * Inform owners of metadata records that the records have approved or rejected.
     * 
     * @param metadataIds The selected set of metadata records
     * @param changeDate The date that of the change in status
     * @param changeMessage Message supplied by the user that set the status
     */
    protected void informOwners(Set<Integer> metadataIds, String changeDate, String changeMessage, String status)
            throws Exception {

        String translatedStatusName = getTranslatedStatusName(status);
        // --- get metadata owners (sorted on owner userid)
        String subject = String.format(LangUtils.translate(context.getApplicationContext(), "statusInform").get(this.language), siteName,
                translatedStatusName, replyToDescr, replyTo, changeDate);
        String mdChanged = buildMetadataChangedMessage(metadataIds);
        
        Iterable<Metadata> metadata = this.context.getBean(MetadataRepository.class).findAll(metadataIds);
        List<User> owners = new ArrayList<User>();
        UserRepository userRepo = this.context.getBean(UserRepository.class);

        for (Metadata md : metadata) {
            int ownerId = md.getSourceInfo().getOwner();
            owners.add(userRepo.findOne(ownerId));
        }

        processList(owners, subject, status, changeDate, changeMessage, mdChanged);

    }

    /**
     * Process the users and metadata records for emailing notices.
     * 
     * @param users The selected set of users
     * @param subject Subject to be used for email notices
     * @param status The status being set
     * @param changeDate Datestamp of status change
     * @param changeMessage The message indicating why the status has changed
     */
    protected void processList(List<User> users, String subject, String status, String changeDate,
                               String changeMessage, String mdChanged) throws Exception {

        for (User user : users) {
            sendEmail(user.getEmail(), subject, status, changeDate, changeMessage, mdChanged);
        }
    }

    /**
     * Send the email message about change of status on a group of metadata records.
     * 
     * @param sendTo The recipient email address
     * @param subject Subject to be used for email notices
     * @param status The status being set on the records
     * @param changeDate Datestamp of status change
     * @param changeMessage The message indicating why the status has changed
     */
    protected void sendEmail(String sendTo, String subject, String status, String changeDate, String changeMessage, String mdChanged) throws Exception {
        String message = String.format(LangUtils.translate(context.getApplicationContext(), "statusSendEmail").get(this.language),
                changeMessage, mdChanged, siteUrl, status, changeDate);

        if (!emailNotes) {
            context.info("Would send email \nTo: " + sendTo + "\nSubject: " + subject + "\n Message:\n" + message);
        } else {
            MailSender sender = new MailSender(context);
            sender.sendWithReplyTo(host, Integer.parseInt(port), username, password, useSSL, from, fromDescr,
            		sendTo, null, replyTo, replyToDescr, subject, message);
        }
    }
}
