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


import org.tmatesoft.svn.core.*;

import java.util.*;

/**
 * Converts {@link org.tmatesoft.svn.core.SVNLogEntry} objects to {@link
 * org.fao.geonet.guiservices.versioning.MetadataAction} objects. One {@link
 * org.tmatesoft.svn.core.SVNLogEntry} may result in many {@link org.fao.geonet.guiservices.versioning.MetadataAction}
 * objects. Adds each {@link org.fao.geonet.guiservices.versioning.MetadataAction} to the containing
 * list which is returnable by the getMetadataActionList() method.
 */
public class MyLogEntryHandler implements ISVNLogEntryHandler {

    private final List<MetadataAction> metadataActionList = new ArrayList<MetadataAction>();

    /**
     * Handles a {@link org.tmatesoft.svn.core.SVNLogEntry} passed to it. Converts a {@link
     * org.tmatesoft.svn.core.SVNLogEntry} object to {@link org.fao.geonet.guiservices.versioning.MetadataAction}
     * object(s) and adds it/these to list.
     *
     * @param logEntry a {@link org.tmatesoft.svn.core.SVNLogEntry} object that represents per
     *                 revision information (committed paths, log message, etc.)
     */
    @Override
    public void handleLogEntry(final SVNLogEntry logEntry) {
        final List<MetadataAction> metadataActions = createMetadataActions(logEntry);
        if (metadataActions != null && metadataActions.size() > 0) {
            metadataActionList.addAll(metadataActions);
        }
    }

    /**
     * Create list because the relation between MetadataAction and SVNLogEntry may be many-to-one.
     *
     * @param logEntry The {@link org.tmatesoft.svn.core.SVNLogEntry} to be converted to the
     *                 returnable list.
     * @return {@link java.util.List}<{@link org.fao.geonet.guiservices.versioning.MetadataAction}>
     */
    private List<MetadataAction> createMetadataActions(final SVNLogEntry logEntry) {
        List<MetadataAction> list = new ArrayList<MetadataAction>();
        MetadataAction ma = new MetadataAction();
        final SVNProperties revisionProperties = logEntry.getRevisionProperties();
        final String userName = getUserName(revisionProperties);
        if (userName == null) {
            return null;
        }
        ma.setRevision(logEntry.getRevision());

        ma.setUsername(userName);

        ma.setIp(getIp(revisionProperties));

        final Date date = logEntry.getDate();
        ma.setDate(getDate(date));

        final Map changedPaths = logEntry.getChangedPaths();
        Integer previousId = null;

        for (Object o : changedPaths.values()) {
            SVNLogEntryPath svnLogEntryPath;

            // only the changes that occur with SVNLogEntryPath are relevant
            if (o instanceof SVNLogEntryPath) {
                svnLogEntryPath = (SVNLogEntryPath) o;
            } else {
                return null;
            }

            final String path = svnLogEntryPath.getPath();
            final char action = getAction(svnLogEntryPath.getType());
            final String subject = getSubject(path);

            // when there is no subject, the only relevant action can be D - deletion
            if (subject == null && action != 'D') {
                return null;
            }
            ma.setAction(action);

            // When action is D or A, except status, the subject is all.
            // When added, all four files are added (metadata.xml, owner.xml, categories.xml, privileges.xml).
            // When deleted, all four files are deleted.
            // Only status.xml can be added separately.
            if (action == 'M' || (action == 'A' && subject.equals("status"))) {
                ma.setSubject(subject);
            } else {
                ma.setSubject("all");
            }

            Integer id = getId(path);
            if (id == null) {
                break;
            }
            if (changedPaths.size() == 1) {
                ma.setId(id);
                list.add(ma);

                // If SVNLogEntry seems to contain info about more than one metadata (more than one different ID)...
                // then create a new MetadataAction with the existing properties.
                // also do the same when more than one subject had the modify action.
            } else if (!id.equals(previousId) || (action == 'M' && !subjectsAreSame(changedPaths))) {
                MetadataAction newMetadataAction = new MetadataAction(ma);
                newMetadataAction.setId(id);
                list.add(newMetadataAction);
            }
            previousId = id;
        }
        return list;
    }

    private boolean subjectsAreSame(Map changedPaths) {
        final Collection values = changedPaths.values();
        boolean allAreSame = true;
        String subject = null;
        for (Object next : values) {
            SVNLogEntryPath svnLogEntryPath = (SVNLogEntryPath) next;
            String currentSubject = getSubject(svnLogEntryPath.getPath());
            if (subject == null) {
                subject = currentSubject;
            } else {
                allAreSame = allAreSame && subject.compareTo(currentSubject) == 0;
            }
            if (!allAreSame) {
                return allAreSame;
            }
        }
        return allAreSame;
    }

    private String getSubject(final String path) {
        final String[] split = path.split("/");
        String subject = null;
        if (split.length > 2) {
            subject = split[2].split(".xml")[0];
        }
        return subject;
    }

    private Integer getId(final String path) {
        final String[] split = path.split("/");
        Integer id = null;
        if (split.length > 1) {
            id = Integer.valueOf(split[1]);
        }
        return id;
    }

    private char getAction(final char action) {
        return action;
    }

    private Date getDate(final Date date) {
        return date;
    }

    private String getUserName(final SVNProperties revProp) {
        final SVNPropertyValue svnPropertyValue = revProp.getSVNPropertyValue("svn:log");
        String s = null;
        if (svnPropertyValue != null && svnPropertyValue.isString()) {
            final String[] split = svnPropertyValue.getString().split("Username: ");
            if (split.length > 1) {
                s = split[1].split(" ")[0];
            }
        }
        return s;
    }

    private String getIp(final SVNProperties revProp) {
        final SVNPropertyValue svnPropertyValue = revProp.getSVNPropertyValue("svn:log");
        String s = null;
        if (svnPropertyValue != null && svnPropertyValue.isString()) {
            final String[] split = svnPropertyValue.getString().split("IP address ");
            if (split.length > 1) {
                s = split[1].split(" ")[0];
            }
        }
        return s;
    }

    /**
     * Returns the List that contains MetadataActions.
     *
     * @return {@link java.util.List}<{@link org.fao.geonet.guiservices.versioning.MetadataAction}>
     * where may be 0 or more {@link org.fao.geonet.guiservices.versioning.MetadataAction} objects.
     */
    public final List<MetadataAction> getMetadataActionList() {
        return metadataActionList;
    }
}
