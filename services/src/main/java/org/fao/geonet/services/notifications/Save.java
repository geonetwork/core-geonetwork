//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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

package org.fao.geonet.services.notifications;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.MetadataNotifier;
import org.fao.geonet.repository.MetadataNotificationRepository;
import org.fao.geonet.repository.MetadataNotifierRepository;
import org.fao.geonet.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.notifications.domain.NotificationTarget;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that persists notification targets.
 *
 * @author heikki doeleman
 */
public class Save extends NotInReadOnlyModeService {

    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * Saves notification targets.
     *
     * The HTML form in notifications-list.xsl deals with pre-existing notification targets, newly
     * added notification targets, and deleted notification targets all at once in the following
     * way:
     *
     * 1. PRE-EXISTING NOTIFICATION TARGETS (existed already when form was sent to user, they
     * already have IDs).
     *
     * For all pre-existing notification targets, a hidden id parameter is sent, of the form
     *
     * id-<identifier>
     *
     * This method collects the other parameters (name, url, enabled) corresponding to this one, as
     * they're sent like
     *
     * name-<identifier>, url-<identifier>, and enabled-<identifier>.
     *
     * The pre-existing notification targets are updated.
     *
     * 2. NEW NOTIFICATION TARGETS (newly added by submitter, they carry a temporary id but no
     * id-<identifier> params).
     *
     * For remaining parameters in that format but without corresponding id-<identifier> parameter,
     * it follows they are newly added by the user. These new notification targets are inserted, if
     * their name and url params are not empty.
     *
     * 3. DELETED NOTIFICATION TARGETS (deleted by submitter).
     *
     * For notification targets deleted by the submitter, only id-<identifier> is sent, no
     * corresponding name-<identifier>, url-<identifier>, or enabled-<identifier>. These are
     * identified by this, and deleted.
     *
     * 4. ENABLED/DISABLED (unchecked checkbox values are not sent with form).
     *
     * Where name-<identifier> and/or url-<identifier> params exist without enabled-<identifier>, it
     * means they're disabled (checkbox off in form is not submitted).
     *
     * 5. EXAMPLE
     *
     * As an example:
     *
     * <request> <name-1287189222038>QQQ</name-1287189222038> <id-3>3</id-3>
     * <enabled-1287189222038>on</enabled-1287189222038> <id-2>2</id-2> <id-1>1</id-1>
     * <enabled-3>on</enabled-3> <url-3>http://zzz</url-3> <url-2>http://yyy:8081</url-2>
     * <name-3>CCC</name-3> <name-2>BBB</name-2> <url-1287189222038>http://qqq</url-1287189222038>
     * </request>
     *
     * Here, there are notification targets:
     *
     * 1                pre-existing, to be deleted 2                pre-existing, disabled, to be
     * updated 3                pre-existing, enabled, to be updated 1287189222038    new, enabled,
     * to be updated
     */
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        Map<String, NotificationTarget> notificationTargets = new HashMap<String, NotificationTarget>();
        @SuppressWarnings("unchecked")
        List<Element> parameters = params.getChildren();
        for (Element parameter : parameters) {
            String identifier = parameter.getName().substring(parameter.getName().lastIndexOf('-') + 1);
            if (notificationTargets.containsKey(identifier)) {
                NotificationTarget notificationTarget = notificationTargets.get(identifier);
                notificationTarget = parameterToNotificationTarget(parameter, notificationTarget);
            } else {
                NotificationTarget notificationTarget = new NotificationTarget();
                notificationTarget.setId(identifier);
                notificationTarget = parameterToNotificationTarget(parameter, notificationTarget);
                notificationTargets.put(identifier, notificationTarget);
            }

        }
        final MetadataNotificationRepository notificationRepository = context.getBean(MetadataNotificationRepository.class);
        final MetadataNotifierRepository notifierRepository = context.getBean(MetadataNotifierRepository.class);

        for (NotificationTarget notificationTarget : notificationTargets.values()) {
            final MetadataNotifier metadataNotifier = new MetadataNotifier();
            metadataNotifier.setName(notificationTarget.getName());
            metadataNotifier.setUsername(notificationTarget.getUsername());
            metadataNotifier.setPassword(notificationTarget.getPassword());
            metadataNotifier.setUrl(notificationTarget.getUrl());
            metadataNotifier.setEnabled(notificationTarget.isEnabled());

            // insert
            if (!notificationTarget.isPreExisting() && StringUtils.isNotBlank(notificationTarget.getName())
                && StringUtils.isNotBlank(notificationTarget.getUrl())) {

                notifierRepository.save(metadataNotifier);
            } else if (notificationTarget.isPreExisting()) {
                // pre-existing
                String id = notificationTarget.getId();
                // delete
                if (notificationTarget.getName() == null) {
                    int iid = Integer.parseInt(id);
                    notificationRepository.deleteAllWithNotifierId(iid);
                    notifierRepository.delete(iid);
                } else {
                    // update
                    notifierRepository.save(metadataNotifier);
                }
            }
        }

        return new Element("ok");
    }


    /**
     * TODO javadoc
     */
    private NotificationTarget parameterToNotificationTarget(Element parameter, NotificationTarget notificationTarget) {
        String member = parameter.getName().substring(0, parameter.getName().lastIndexOf('-'));

        if (member.equals("id")) {
            notificationTarget.setId(parameter.getText());
            notificationTarget.setPreExisting(true);
        } else if (member.equals("name")) {
            notificationTarget.setName(parameter.getText());
        } else if (member.equals("url")) {
            notificationTarget.setUrl(parameter.getText());
        } else if (member.equals("username")) {
            notificationTarget.setUsername(parameter.getText());
        } else if (member.equals("password")) {
            notificationTarget.setPassword(parameter.getText());
        } else if (member.equals("enabled")) {
            notificationTarget.setEnabled(true);
        }
        return notificationTarget;
    }


}
