/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.guiservices.versioning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.time.DateFormatUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

//=============================================================================

/**
 * Parses and returns the contents of the local SVN repository.
 */
public class Get implements Service {
    private static final String DATE = "date";
    private static final String USERNAME = "user";
    private static final String IP = "ip";
    private static final String ACTION = "action";
    private static final String SUBJECT = "subject";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(final Path appPath, final ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- API
    //---
    //--------------------------------------------------------------------------

    /**
     * Parses the contents of the local SVN repository and returns an xml Element with the relevant
     * data about what kind of changes were made per metadata.
     *
     * @param params  POST request parameters. Will accept (type, name): boolean 'refresh', integer
     *                'start', integer 'limit', text 'sort', text 'dir'. <br> - refresh - if a
     *                search for new changes will be attempted <br> - start - the start position of
     *                the first entry element to be returned from the sorted collection of entries
     *                <br> - limit - how many entries to return <br> - sort - by which field to
     *                sort. The fields names are date, user, ip, action, subject, id, title <br> -
     *                dir - in which direction to sort. The values can be ASC or DESC <br> If no
     *                parameters are given, defaults to refresh: true, start: 0, limit: 30, sort:
     *                date, dir: DESC.
     * @param context the context for service excecution is needed to get the path of the
     *                repository
     * @return {@link org.jdom.Element} that contains relevant info about the metadata {@code
     * <date>} is the NB! server-local NB! date when the change was done {@code <user>} name of the
     * user in geonetwork who triggered the change {@code <ip>} The IP address, from where the
     * change was triggered {@code <action>} can be either Added, Modified or Deleted {@code
     * <subject>} can be either All, Metadata, Owner, Privileges, Categories or Status. {@code <id>}
     * the geonetwork-local id (not the global id) of the metadata involved {@code <title>} the
     * title of the the metadata involved right after the change occurred An example of two
     * entries:
     * <pre>
     *         {@code   <logentries>
     *          <totalcount>2</totalcount>
     *          <entry>
     *            <date>08-11-2013 09:22:42</date>
     *            <user>admin</user>
     *            <ip>65.222.202.53</ip>
     *            <action>Modified</action>
     *            <subject>Metadata</subject>
     *            <id>22</id>
     *            <title>notModifiedTitle</title>
     *          </entry>
     *          <entry>
     *            <date>08-11-2013 09:22:36</date>
     *            <user>admin</user>
     *            <ip>65.222.202.53</ip>
     *            <action>Modified</action>
     *            <subject>Metadata</subject>
     *            <id>22</id>
     *            <title>modifiedTitle</title>
     *          </entry>
     *         </logentries>
     *         }
     *         </pre>
     * @throws java.io.IOException                 when cannot acquire metadata file from
     *                                             repository
     * @throws jeeves.exceptions.BadParameterEx    in case of badly formed parameter
     * @throws org.jdom.JDOMException              when parsing file into jdom.Document or running
     *                                             xpath on the document fails.
     * @throws org.tmatesoft.svn.core.SVNException when a failure occurs while connecting to
     *                                             repository
     */
    public final Element exec(final Element params, final ServiceContext context) throws BadParameterEx, SVNException, IOException, JDOMException {
        MetadataActionListSingleton singleton = MetadataActionListSingleton.getInstance();
        boolean refresh = Util.getParam(params, "refresh", true);
        if (refresh) {
            GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
            String repoPath = gc.getBean(ServiceConfig.class).getMandatoryValue(
                Geonet.Config.SUBVERSION_PATH);
            SVNURL svnurl = SVNURL.fromFile(new File(repoPath));
            SVNRepository repository = SVNRepositoryFactory.create(svnurl);
            MyLogEntryHandler logEntryHandler = new MyLogEntryHandler();
            List<MetadataAction> oldList = singleton.getMetadataActions();
            Long startRevision = 0L;

            // get the startRevision if we already have some MetadataActions in the list.
            if (oldList != null) {
                startRevision = oldList.get(oldList.size() - 1).getRevision() + 1;
            }

            // is there something to get from repository?
            if (startRevision <= repository.getLatestRevision()) {
                repository.log(new String[]{""}, startRevision, -1, true, true, logEntryHandler);
            }

            List<MetadataAction> newList = logEntryHandler.getMetadataActionList();
            // This method takes a lot of time.
            // That is why using the singleton and getting only the new logs from repository
            addTitles(newList, repoPath);

            if (oldList == null) {
                // since existingList was empty, add only new list, even if it is empty.
                singleton.setMetadataActions(newList);

            } else if (newList.size() > 0) {

                // since oldList and the newList is not empty,
                // add new members to oldList and set it back to singleton.
                oldList.addAll(newList);
                singleton.setMetadataActions(oldList);
            }
        }
        List<MetadataAction> list = new ArrayList<MetadataAction>(singleton.getMetadataActions());
        sort(list, params);
        return xml(list, params);
    }

    private Element xml(final List<MetadataAction> list, final Element params) throws BadParameterEx {
        Element root = new Element("logentries");
        Document doc = new Document(root);
        doc.setRootElement(root);
        Element totalCount = new Element("totalcount").setText(String.valueOf(list.size()));
        doc.getRootElement().addContent(totalCount);
        for (MetadataAction metadataAction : getSmallerList(list, params)) {
            Element entry = new Element("entry");
            entry.addContent(new Element(DATE).setText(DateFormatUtils.format(metadataAction.getDate(), "dd-MM-yyyy HH:mm:ss")));
            entry.addContent(new Element(USERNAME).setText(metadataAction.getUsername()));
            entry.addContent(new Element(IP).setText(metadataAction.getIp()));
            entry.addContent(new Element(ACTION).setText(metadataAction.translatedAction()));
            entry.addContent(new Element(SUBJECT).setText(metadataAction.translatedSubject()));
            entry.addContent(new Element(ID).setText(String.valueOf(metadataAction.getId())));
            entry.addContent(new Element(TITLE).setText(metadataAction.getTitle()));
            doc.getRootElement().addContent(entry);
        }
        return doc.detachRootElement();
    }

    private List<MetadataAction> getSmallerList(final List<MetadataAction> list, final Element params) throws BadParameterEx {
        int startPos = Util.getParam(params, "start", 0);
        int limit = Util.getParam(params, "limit", 30);
        int toIndex = startPos + limit;
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        if (startPos < 0 || toIndex == 0) {
            return list;
        } else {
            return list.subList(startPos, toIndex);
        }
    }

    private void sort(final List<MetadataAction> list, final Element params) {
        String field = Util.getParam(params, "sort", DATE);
        String direction = Util.getParam(params, "dir", DESC);
        if (direction.compareTo(ASC) == 0) {
            if (field.equals(DATE)) {
                Collections.sort(list, MetadataAction.DATE_COMPARATOR_ASC);
            } else if (field.equals(USERNAME)) {
                Collections.sort(list, MetadataAction.USERNAME_COMPARATOR_ASC);
            } else if (field.equals(IP)) {
                Collections.sort(list, MetadataAction.IP_COMPARATOR_ASC);
            } else if (field.equals(ACTION)) {
                Collections.sort(list, MetadataAction.ACTION_COMPARATOR_ASC);
            } else if (field.equals(SUBJECT)) {
                Collections.sort(list, MetadataAction.SUBJECT_COMPARATOR_ASC);
            } else if (field.equals(ID)) {
                Collections.sort(list, MetadataAction.ID_COMPARATOR_ASC);
            } else if (field.equals(TITLE)) {
                Collections.sort(list, MetadataAction.TITLE_COMPARATOR_ASC);
            }
        } else if (direction.compareTo(DESC) == 0) {
            if (field.equals(DATE)) {
                Collections.sort(list, MetadataAction.DATE_COMPARATOR_DESC);
            } else if (field.equals(USERNAME)) {
                Collections.sort(list, MetadataAction.USERNAME_COMPARATOR_DESC);
            } else if (field.equals(IP)) {
                Collections.sort(list, MetadataAction.IP_COMPARATOR_DESC);
            } else if (field.equals(ACTION)) {
                Collections.sort(list, MetadataAction.ACTION_COMPARATOR_DESC);
            } else if (field.equals(SUBJECT)) {
                Collections.sort(list, MetadataAction.SUBJECT_COMPARATOR_DESC);
            } else if (field.equals(ID)) {
                Collections.sort(list, MetadataAction.ID_COMPARATOR_DESC);
            } else if (field.equals(TITLE)) {
                Collections.sort(list, MetadataAction.TITLE_COMPARATOR_DESC);
            }
        }
    }

    void addTitles(final List<MetadataAction> metadataActions, final String localRepoLocation) throws JDOMException, IOException, SVNException {
        final SVNURL svnurl = SVNURL.fromFile(new File(localRepoLocation));
        final SVNRepository repository = SVNRepositoryFactory.create(svnurl);
        for (MetadataAction metadataAction : metadataActions) {
            final String path = metadataAction.getId() + "/metadata.xml";
            OutputStream out = new ByteArrayOutputStream();
            Long revision = metadataAction.getRevision();
            Long revisionToFetch = revision;
            if (metadataAction.getAction() == 'D') {
                revisionToFetch = revision - 1;
            }
            repository.getFile(path, revisionToFetch, null, out);
            out.close();
            metadataAction.setTitle(getTitle(out));
        }
    }

    private String getTitle(final OutputStream out) throws JDOMException, IOException {
        final String xml = out.toString();
        final byte[] xmlBytes = xml.getBytes(Charset.forName("UTF-8"));
        InputStream is = new ByteArrayInputStream(xmlBytes);
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        SAXBuilder sb = new SAXBuilder();
        Document doc;
        doc = sb.build(isr);
        Element element = doc.getRootElement();
        XPath xpath = XPath.newInstance("gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        return xpath.valueOf(element);
    }
}
