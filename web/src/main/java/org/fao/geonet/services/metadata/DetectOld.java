package org.fao.geonet.services.metadata;

import static java.text.MessageFormat.format;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DuplicateFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Email;
import org.fao.geonet.kernel.reusable.Utils;
import org.fao.geonet.kernel.search.LuceneIndexReaderFactory;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A service that detects old metadata and depending on the serviceType
 * configuration will
 * <ul>
 * <li>email the author that it has not been updated in sometime: if type=EMAIL</li>
 * <li>unpublish the metadata: if type=UNPUBLISH</li>
 * <li>return defaults: if type=FORM</li>
 * </ul>
 *
 * <p>
 * If the showui parameter == true then the defaults expiry values are returned.
 * </p>
 * <p>
 * defaults are read from the configuration file and there may be different
 * defaults for EMAIL and UNPUBLISH options
 * </p>
 *
 * @author jeichar
 */
public class DetectOld implements Service
{

    private static final String UNSENT_NOTIFICATIONS       = "unsentNotifications";
    private static final String SENT_NOTIFICATIONS         = "sentNotifications";
    public static final String  AUTHOR_SUBJECT             = "Please update metadata";
    public static final String  ADMIN_OLD_METADATA_SUBJECT = "Metadata not updated recently";
    public static final String  ADMIN_FAILED_EMAIL_SUBJECT = "Failures notifying metadata owners of expiredMetadata";
    public static final String  ADMIN_UNPUBLISH_SUBJECT    = "Old Metadata have been unpublished";

    public static final String  AUTHOR_INTRO               = "The following metadata entries have not been updated within the last {0} months.  They must be updated or they will be unpublished.\n\n  Login as the user: {1} in order to edit the metadata elements\n\n";
    public static final String  ADMIN_OLD_DATA_INTRO       = "The following metadata entries have not been updated in {0} months.";
    public static final String  ADMIN_FAILED_EMAIL_INTRO   = "The following metadata elements need to be updated but authors cannot be contacted:";
    public static final String  ADMIN_UNPUBLISH_INTRO      = "The following metadata entries have not been updated in {0} months and have been unpublished.";
    public static final String  USER_SUMMARY               = "\n\nuser: {0} email: {1} \n====================================================\n";
    public static final String  METADATA_SUMMARY           = "{0}\nURL: {1}\nLast updated: {2}\n\n";

    private Element             uiRequestResponse;
    private ServiceType         serviceType;

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
    	boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));
        int limit;
        Multimap<String, EmailInfo> results;
        switch (serviceType)
        {
        case FORM:
            return uiRequestResponse;
        case EMAIL:
            limit = parseLimit(params);
            results = doSearch(limit, context);
            return emailResults(testing, gc.getEmail(), limit, results);
        default:
            limit = parseLimit(params);
            results = doSearch(limit, context);
            Element notifications = emailResults(testing, gc.getEmail(), limit, results).getChild(SENT_NOTIFICATIONS);
            notifications.setName("unpublishedItems");
            List<String> ids = new ArrayList<String>();
            for (EmailInfo info : results.values()) {
                ids.add(info.metadataId);
            }
            Utils.unpublish(ids, context);
            return notifications;

        }

    }

    private int parseLimit(Element params) throws BadInputEx
    {
        String limit;
        limit = Util.getParam(params, "limit");
        if (limit == null) {
            throw new IllegalArgumentException("The parameter 'limit' is required.");
        }

        return Integer.parseInt(limit);
    }

    private Multimap<String, EmailInfo> doSearch(int limit, ServiceContext context) throws Exception
    {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager sm = gc.getSearchmanager();

        String host = gc.getSettingManager().getValue("system/server/host");
        String portNumber = gc.getSettingManager().getValue("system/server/port");

        try {
            new URL(host);
        } catch (MalformedURLException e) {
            try {
                new URL("http://" + host);
                host = "http://" + host;
            } catch (MalformedURLException e2) {
                throw e;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1 * limit);

        ISODate to = new ISODate(calendar.getTimeInMillis());
        TermRangeQuery query = new TermRangeQuery("_changeDate", null, to.getDate(), false, false);

        DuplicateFilter filter = new DuplicateFilter(query.getField());

        IndexReader reader = sm.getIndexReader(null);
	    Searcher searcher = new IndexSearcher(reader);

        try {
            TopDocs hits = searcher.search(query, filter,Integer.MAX_VALUE);

            Multimap<String, EmailInfo> results = HashMultimap.create();
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document document = reader.document(scoreDoc.doc);
                String basicAddress = host + ":" + portNumber + context.getBaseUrl() + "/" + context.getLanguage() + "/";
                EmailInfo emailInfo = new EmailInfo(document, basicAddress);

                results.put(emailInfo.ownerId, emailInfo);
            }
            removeUnpublishedElements(results, context);
            updateEmails(results, context);

            return results;
        } finally {
            try{searcher.close();}finally{sm.releaseIndexReader(reader);}
        }
    }

    private void removeUnpublishedElements(Multimap<String, EmailInfo> results, ServiceContext context)
            throws Exception
    {
        if (results.size() > 0) {
            StringBuilder query = new StringBuilder(
                    "SELECT metadataId FROM OperationAllowed WHERE groupId=1 AND (metadataId=");

            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            Iterator<EmailInfo> iter = results.values().iterator();
            query.append(iter.next().metadataId);
            while (iter.hasNext()) {
                query.append(" OR metadataId=");
                query.append(iter.next().metadataId);
            }

            query.append(")");

            List<Element> listAllow = dbms.select(query.toString()).getChildren();
            HashSet<String> set = new HashSet<String>();

            for (Element element : listAllow) {
                set.add(element.getChildText("metadataid"));
            }

            for (Iterator<EmailInfo> infoIter = results.values().iterator(); infoIter.hasNext();) {
                EmailInfo info = infoIter.next();
                if (!set.contains(info.metadataId)) {
                    infoIter.remove();
                }
            }
        }
    }

    private void updateEmails(Multimap<String, EmailInfo> results, ServiceContext context) throws Exception
    {
        if (results.size() > 0) {
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            Iterator<String> iter = results.keySet().iterator();
            StringBuilder query = new StringBuilder("select id,username,email from Users where id=");
            query.append(iter.next());
            while (iter.hasNext()) {
                query.append(" or id=");
                query.append(iter.next());
            }
            Iterator users = dbms.select(query.toString()).getChildren().iterator();
            while (users.hasNext()) {
                Element user = (Element) users.next();
                String id = user.getChildText("id");
                String username = user.getChildText("username");
                String email = user.getChildText("email");
                Collection<EmailInfo> records = results.get(id);
                for (EmailInfo emailInfo : records) {
                    emailInfo.email = email;
                    emailInfo.ownerUsername = username;
                }
            }

        }
    }

    private Element emailResults(boolean testing, Email email, int limit, Multimap<String, EmailInfo> results)
            throws Exception
    {

        Element xmlResponse = new Element("oldElements");
        Element failedNotifications = new Element(UNSENT_NOTIFICATIONS);
        Element notifications = new Element(SENT_NOTIFICATIONS);
        xmlResponse.addContent(failedNotifications);
        xmlResponse.addContent(notifications);

        StringBuilder adminMessage;
        if (ServiceType.EMAIL == serviceType) {
            adminMessage = new StringBuilder(format(ADMIN_OLD_DATA_INTRO, limit));
        } else {
            adminMessage = new StringBuilder(format(ADMIN_UNPUBLISH_INTRO, limit));
        }
        // dont need a specific message for unpublish because this is used only
        // by email
        StringBuilder failedMessage = new StringBuilder(ADMIN_FAILED_EMAIL_INTRO);

        for (Map.Entry<String, Collection<EmailInfo>> data : results.asMap().entrySet()) {

            EmailInfo sampleInfo = null;

            Element owner = new Element("owner");

            final StringBuilder metadataSummary = new StringBuilder();
            for (EmailInfo info : data.getValue()) {
                Element metadata = new Element("metadata");
                sampleInfo = info;

                metadata.setAttribute("changeDate", info.changeDate);
                metadata.setAttribute("id", info.metadataId);
                if (info.title != null) {
                    metadata.setAttribute("title", info.title);
                }
                Element url = new Element("url");
                url.setText(info.metadataUrl.toExternalForm());
                metadata.addContent(url);
                owner.addContent(metadata);

                metadataSummary.append(format(METADATA_SUMMARY, info.title, info.metadataUrl, info.changeDate));
            }

            String emailTo = sampleInfo.email;

            owner.setAttribute("ownerUsername", sampleInfo.ownerUsername);
            owner.setAttribute("id", sampleInfo.ownerId);
            owner.setAttribute("email", emailTo);

            String userSummary = format(USER_SUMMARY, sampleInfo.ownerUsername, sampleInfo.email);

            if (serviceType == ServiceType.EMAIL && (emailTo == null || emailTo.length() == 0)) {
                failedNotifications.addContent(owner);
                failedMessage.append(userSummary);
                failedMessage.append(metadataSummary);
            } else {
                if (serviceType == ServiceType.EMAIL) {
                    String msg = MessageFormat.format(AUTHOR_INTRO, limit, sampleInfo.ownerUsername) + metadataSummary;
                    email.send(emailTo, AUTHOR_SUBJECT, msg, testing);
                }
                notifications.addContent(owner);
                adminMessage.append(userSummary);
                adminMessage.append(metadataSummary);
            }
        }

        if (serviceType == ServiceType.EMAIL) {
            email.sendToAdmin(ADMIN_OLD_METADATA_SUBJECT, adminMessage.toString(), testing);
        } else {
            email.sendToAdmin(ADMIN_UNPUBLISH_SUBJECT, adminMessage.toString(), testing);
        }

        if (!failedNotifications.getChildren().isEmpty()) {
            email.sendToAdmin(ADMIN_FAILED_EMAIL_SUBJECT, failedMessage.toString(), testing);
        }
        return xmlResponse;

    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
        serviceType = ServiceType.valueOf(params.getMandatoryValue("serviceType").toUpperCase());
        switch (serviceType)
        {
        case FORM:
            String emailParam = params.getMandatoryValue("email");
            String unpublishParam = params.getMandatoryValue("unpublish");
            uiRequestResponse = new Element("defaults");
            Element email = new Element("email");
            Element unpublish = new Element("unpublish");

            uiRequestResponse.addContent(email);
            uiRequestResponse.addContent(unpublish);
            email.setText(emailParam);
            unpublish.setText(unpublishParam);
            break;
        default:
        {
            break;
        }
        }

    }

    private enum ServiceType
    {
        EMAIL, UNPUBLISH, FORM
    }

    private final static class EmailInfo
    {
        String       email;
        String       ownerUsername;
        final String ownerId;
        final URL    metadataUrl;
        final String title;
        final String changeDate;
        final String metadataId;
        final String language;

        public EmailInfo(Document doc, String basicAddress) throws MalformedURLException
        {
            ownerId = doc.get("_owner");
            metadataUrl = new URL(basicAddress + "metadata.show?id=" + doc.get("_id"));
            if (doc.get("_defaultTitle") == null) {
                title = doc.get("_title");
            } else {
                title = doc.get("_defaultTitle");
            }
            changeDate = doc.get("_changeDate");
            metadataId = doc.get("_id");
            language = doc.get("_locale");
        }

    }
}
