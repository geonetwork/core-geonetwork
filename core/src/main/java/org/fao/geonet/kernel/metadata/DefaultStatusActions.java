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

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ISODate;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultStatusActions implements StatusActions {

    protected String host, port, from, fromDescr, replyTo, replyToDescr;
    protected ServiceContext context;
    protected AccessManager am;
    protected String language;
    protected DataManager dm;
    protected Dbms dbms;
    protected String siteUrl;
    protected String siteName;
    protected UserSession session;
    protected boolean emailNotes = true;

    /**
     * Constructor.
     */
    public DefaultStatusActions() {
    }

    /**
     * Initializes the StatusActions class with external info from GeoNetwork.
     * 
     * @param context
     * @param dbms
     * @throws IOException
     * @throws JDOMException
     */
    public void init(ServiceContext context, Dbms dbms) throws Exception {

        this.context = context;
        this.dbms = dbms;
        this.language = context.getLanguage();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);
        am = gc.getBean(AccessManager.class);

        siteName = sm.getValue("system/site/name");
        host = sm.getValue("system/feedback/mailServer/host");
        port = sm.getValue("system/feedback/mailServer/port");
        from = sm.getValue("system/feedback/email");

        if (host.length() == 0) {
            context.error("Mail server host not configure");
            emailNotes = false;
        }

        if (port.length() == 0) {
            context.error("Mail server port not configured, email notifications won't be sent.");
            emailNotes = false;
        }

        if (from.length() == 0) {
            context.error("Mail feedback address not configured, email notifications won't be sent.");
            emailNotes = false;
        }

        fromDescr = siteName + LangUtils.translate(context, "statusTitle").get(this.language);

        session = context.getUserSession();
        replyTo = session.getEmailAddr();
        if (replyTo != null) {
            replyToDescr = session.getName() + " " + session.getSurname();
        } else {
            replyTo = from;
            replyToDescr = fromDescr;
        }

        dm = gc.getBean(DataManager.class);
        siteUrl = dm.getSiteURL(context);
    }

    /**
     * Called when a record is edited to set/reset status.
     * 
     * @param id The metadata id that has been edited.
     * @param minorEdit If true then the edit was a minor edit.
     */
    public void onEdit(int id, boolean minorEdit) throws Exception {

        if (!minorEdit && dm.getCurrentStatus(dbms, id).equals(Params.Status.APPROVED)) {
            String changeMessage = String.format(LangUtils.translate(context, "statusUserEdit").get(this.language), replyToDescr,
                    replyTo, id);
            unsetAllOperations(id);
            dm.setStatus(context, dbms, id, Integer.valueOf(Params.Status.DRAFT), new ISODate().toString(), changeMessage);
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
    public Set<Integer> statusChange(String status, Set<Integer> metadataIds, String changeDate, String changeMessage) throws Exception {

        Set<Integer> unchanged = new HashSet<Integer>();

        // -- process the metadata records to set status
        for (Integer mid : metadataIds) {
            String currentStatus = dm.getCurrentStatus(dbms, mid);

            // --- if the status is already set to value of status then do nothing
            if (status.equals(currentStatus)) {
                if (context.isDebug())
                    context.debug("Metadata " + mid + " already has status " + mid);
                unchanged.add(mid);
            }

            if (status.equals(Params.Status.APPROVED)) {
                // setAllOperations(mid); - this is a short cut that could be enabled
            } else if (status.equals(Params.Status.DRAFT) || status.equals(Params.Status.REJECTED)) {
                unsetAllOperations(mid);
            }

            // --- set status, indexing is assumed to take place later
            dm.setStatusExt(context, dbms, mid, Integer.valueOf(status), changeDate, changeMessage);
        }

        // --- inform content reviewers if the status is submitted
        if (status.equals(Params.Status.SUBMITTED)) {
            informContentReviewers(metadataIds, changeDate, changeMessage);
            // --- inform owners if status is approved
        } else if (status.equals(Params.Status.APPROVED) || status.equals(Params.Status.REJECTED)) {
            informOwners(metadataIds, changeDate, changeMessage, status);
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
    protected void unsetAllOperations(int mdId) throws Exception {
        String allGroup = "1";
        dm.unsetOperation(context, dbms, mdId + "", allGroup, AccessManager.OPER_VIEW);
        dm.unsetOperation(context, dbms, mdId + "", allGroup, AccessManager.OPER_DOWNLOAD);
        dm.unsetOperation(context, dbms, mdId + "", allGroup, AccessManager.OPER_NOTIFY);
        dm.unsetOperation(context, dbms, mdId + "", allGroup, AccessManager.OPER_DYNAMIC);
        dm.unsetOperation(context, dbms, mdId + "", allGroup, AccessManager.OPER_FEATURED);
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
        Element contentRevs = am.getContentReviewers(dbms, metadata);

        String subject = String.format(LangUtils.translate(context, "statusInform").get(this.language), siteName, 
                LangUtils.dbtranslate(context, "StatusValues", Params.Status.SUBMITTED).get(this.language), 
                replyToDescr, replyTo, changeDate);

        processList(contentRevs, subject, Params.Status.SUBMITTED,
                changeDate, changeMessage);
    }

    /**
     * Inform owners of metadata records that the records have approved or rejected.
     * 
     * @param metadata The selected set of metadata records
     * @param changeDate The date that of the change in status
     * @param changeMessage Message supplied by the user that set the status
     */
    protected void informOwners(Set<Integer> metadata, String changeDate, String changeMessage, String status)
            throws Exception {

        // --- get metadata owners (sorted on owner userid)
        Element owners = am.getOwners(dbms, metadata);

        String subject = String.format(LangUtils.translate(context, "statusInform").get(this.language), siteName,
                LangUtils.dbtranslate(context, "StatusValues", status).get(this.language), replyToDescr, replyTo, changeDate);

        processList(owners, subject, status, changeDate, changeMessage);

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
    protected void processList(Element users, String subject, String status, String changeDate, String changeMessage) throws Exception {

        @SuppressWarnings("unchecked")
        List<Element> userList = users.getChildren();

        Set<String> emails = new HashSet<String>();

        for (Element user : userList) {
            emails.add(user.getChildText("email"));
        }

        for (String email : emails) {
            sendEmail(email, subject, status, changeDate, changeMessage);
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
    protected void sendEmail(String sendTo, String subject, String status, String changeDate, String changeMessage) throws Exception {
        String message = String.format(LangUtils.translate(context, "statusSendEmail").get(this.language), changeMessage, siteUrl, status,
                changeDate);

        if (!emailNotes) {
            context.info("Would send email \nTo: " + sendTo + "\nSubject: " + subject + "\n Message:\n" + message);
        } else {
            MailSender sender = new MailSender(context);
            sender.sendWithReplyTo(host, Integer.parseInt(port), from, fromDescr, sendTo, null, replyTo, replyToDescr, subject, message);
        }
    }
}
