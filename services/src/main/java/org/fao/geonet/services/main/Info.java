//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import com.google.common.collect.Maps;
import jeeves.component.ProfileManager;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.SettingSpec;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import static com.google.common.xml.XmlEscapers.xmlContentEscaper;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_HARVESTER_ENABLE_EDITING;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_HARVESTER_ENABLE_PRIVILEGES_MANAGEMENT;

@Deprecated
public class Info implements Service {
    public static final String SYSTEMINFO = "systeminfo";
    public static final String STATUS = "status";
    public static final String AUTH = "auth";
    public static final String ME = "me";
    public static final String TEMPLATES = "templates";
    public static final String USERS = "users";
    public static final String SOURCES = "sources";
    public static final String ISOLANGUAGES = "isolanguages";
    public static final String LANGUAGES = "languages";
    public static final String REGIONS = "regions";
    public static final String OPERATIONS = "operations";
    public static final String GROUPS_INCLUDING_SYSTEM_GROUPS = "groupsIncludingSystemGroups";
    public static final String GROUPS = "groups";
    public static final String GROUPS_ALL = "groupsAll";
    public static final String CATEGORIES = "categories";
    public static final String USER_GROUP_ONLY = "userGroupOnly";
    public static final String HARVESTER = "harvester";
    public static final String INSPIRE = "inspire";
    public static final String CONFIG = "config";
    public static final String SITE = "site";
    private static final String READ_ONLY = "readonly";
    private static final String INDEX = "index";
    private static final String SCHEMAS = "schemas";
    private static final String STAGING_PROFILE = "stagingProfile";

    private Path xslPath;
    private Path otherSheets;
    private ServiceConfig _config;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    /**
     * @param appPath
     * @param config
     * @throws Exception
     */
    public void init(Path appPath, ServiceConfig config) throws Exception {
        xslPath = appPath.resolve(Geonet.Path.STYLESHEETS).resolve("xml");
        otherSheets = appPath.resolve(Geonet.Path.STYLESHEETS);
        _config = config;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    /**
     * @param inParams
     * @param context
     * @return
     * @throws Exception
     */
    public Element exec(Element inParams, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        Element params = (Element) inParams.clone();

        // --- if we have a parameter specified in the config then use it instead
        // --- of the usual params
        String ptype = _config.getValue("type");
        if (ptype != null) {
            params.removeContent();
            params.addContent(new Element("type").setText(ptype));
        }

        Element result = new Element("root");

        @SuppressWarnings("unchecked")
        List<Element> types = params.getChildren("type");
        for (Element el : types) {
            String type = el.getText();

            if (type.equals(SITE)) {
                result.addContent(gc.getBean(SettingManager.class).getValues(
                    new String[]{
                        Settings.SYSTEM_SITE_NAME_PATH,
                        Settings.SYSTEM_SITE_ORGANIZATION,
                        Settings.SYSTEM_SITE_SITE_ID_PATH,
                        Settings.SYSTEM_PLATFORM_VERSION,
                        Settings.SYSTEM_PLATFORM_SUBVERSION
                    }));
            } else if (type.equals(CONFIG)) {
                // Return a set of properties which define what
                // to display or not in the user interface
                final List<Setting> publicSettings = context.getBean(SettingRepository.class).findAllByInternal(false);
                List<String> publicSettingsKey = new ArrayList<String>();
                for (Setting s : publicSettings) {
                    publicSettingsKey.add(s.getName());
                }
                Element configElement = new Element("config");
                Element settingsElement = gc.getBean(SettingManager.class).getValues(
                    publicSettingsKey.toArray(new String[0]));

                String mailServerHost = gc.getBean(SettingManager.class).getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
                Element mailServerElement = new Element("setting");
                mailServerElement.setAttribute("name", "system/mail/enable");
                mailServerElement.setAttribute("value", !ObjectUtils.isEmpty(mailServerHost) + "");
                settingsElement.addContent(mailServerElement);
                configElement.addContent(settingsElement);
                result.addContent(configElement);
            } else if (type.equals(INSPIRE)) {
                result.addContent(gc.getBean(SettingManager.class).getValues(
                    new String[]{
                        "system/inspire/enable"
                    }));
            } else if (type.equals(HARVESTER)) {
                result.addContent(gc.getBean(SettingManager.class).getValues(
                    new String[]{
                        SYSTEM_HARVESTER_ENABLE_EDITING,
                        SYSTEM_HARVESTER_ENABLE_PRIVILEGES_MANAGEMENT}));

            } else if (type.equals(USER_GROUP_ONLY)) {
                result.addContent(gc.getBean(SettingManager.class).getValues(
                    new String[]{Settings.SYSTEM_METADATAPRIVS_USERGROUPONLY}));

            } else if (type.equals(CATEGORIES)) {
                result.addContent(context.getBean(MetadataCategoryRepository.class).findAllAsXml());

            } else if (type.equals(GROUPS)) {
                String profile = params.getChildText("profile");
                Element r = getGroups(context, Profile.findProfileIgnoreCase(profile), false, false);
                result.addContent(r);

            } else if (type.equals(GROUPS_INCLUDING_SYSTEM_GROUPS)) {
                Element r = getGroups(context, null, true, false);
                result.addContent(r);

            } else if (type.equals(GROUPS_ALL)) {
                Element r = getGroups(context, null, false, true);
                result.addContent(r);

            } else if (type.equals(OPERATIONS)) {
                result.addContent(context.getBean(OperationRepository.class).findAllAsXml());

            } else if (type.equals(REGIONS)) {
                RegionsDAO dao = context.getBean(RegionsDAO.class);
                Element regions = dao.createSearchRequest(context).xmlResult();
                result.addContent(regions);
            } else if (type.equals(ISOLANGUAGES)) {
                result.addContent(context.getBean(IsoLanguageRepository.class).findAllAsXml());

            } else if (type.equals(LANGUAGES)) {
                result.addContent(context.getBean(LanguageRepository.class).findAllAsXml());

            } else if (type.equals(SOURCES)) {
                result.addContent(getSources(context, sm));

            } else if (type.equals(USERS)) {
                result.addContent(getUsers(context));

            } else if (type.equals(ME)) {
                result.addContent(getMyInfo(context));

            } else if (type.equals(AUTH)) {
                result.addContent(getAuth(context));

            } else if (type.equals(READ_ONLY)) {
                result.addContent(getReadOnly(gc));
            } else if (type.equals(INDEX)) {
                result.addContent(getIndex(gc));
            } else if (type.equals(SCHEMAS)) {
                result.addContent(getSchemas(gc.getBean(SchemaManager.class)));

            } else if (type.equals(STATUS)) {
                result.addContent(context.getBean(StatusValueRepository.class).findAllAsXml());
            } else if (type.equals(SYSTEMINFO)) {
                result.addContent(context.getBean(SystemInfo.class).toXml());
            } else if (type.equals(STAGING_PROFILE)) {
                result.addContent(new Element(STAGING_PROFILE).setText(context.getBean(SystemInfo.class).getStagingProfile()));
            } else {
                throw new BadParameterEx("Unknown type parameter value.", type);
            }
        }

        result.addContent(getEnv(context));
        Element response = Xml.transform(result, xslPath.resolve("info.xsl"));

        return response;
    }

    /**
     * Returns whether GN is in indexing (true or false).
     */
    private Element getIndex(GeonetContext gc) {
        Element isIndexing = new Element(INDEX);
        isIndexing.setText(Boolean.toString(gc.getBean(DataManager.class).isIndexing()));
        return isIndexing;
    }

    /**
     * Returns whether GN is in read-only mode (true or false).
     */
    private Element getReadOnly(GeonetContext gc) {
        Element readOnly = new Element(READ_ONLY);
        readOnly.setText(Boolean.toString(gc.isReadOnly()));
        return readOnly;
    }

    /**
     * @param context
     * @return
     */
    private Element getAuth(ServiceContext context) {
        Element auth = new Element("auth");
        Element cas = new Element("casEnabled").setText(Boolean.toString(ProfileManager.isCasEnabled()));
        auth.addContent(cas);

        return auth;
    }


    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    /**
     * @param context
     * @return
     */
    private Element getMyInfo(ServiceContext context) {
        Element data = new Element("me");
        UserSession userSession = context.getUserSession();
        if (userSession.isAuthenticated()) {
            data.setAttribute("authenticated", "true");
            final String emailAddr = userSession.getEmailAddr();
            data.addContent(new Element(Geonet.Elem.PROFILE).setText(userSession.getProfile().name()))
                .addContent(new Element(Geonet.Elem.USERNAME).setText(userSession.getUsername()))
                .addContent(new Element(Geonet.Elem.ID).setText(userSession.getUserId()))
                .addContent(new Element(Geonet.Elem.NAME).setText(userSession.getName()))
                .addContent(new Element(Geonet.Elem.SURNAME).setText(userSession.getSurname()))
                .addContent(new Element(Geonet.Elem.EMAIL).setText(emailAddr))
                .addContent(new Element(Geonet.Elem.ORGANISATION).setText(userSession.getOrganisation()));

            if (emailAddr != null) {
                data.addContent(new Element(Geonet.Elem.HASH).setText(org.apache.commons.codec.digest.DigestUtils.md5Hex(emailAddr)));
            } else {
                data.addContent(new Element(Geonet.Elem.HASH).setText(""));
            }
        } else {
            data.setAttribute("authenticated", "false");
        }
        return data;
    }

    private Element getSchemas(SchemaManager schemaMan) throws Exception {

        Element response = new Element(SCHEMAS);

        for (String schema : schemaMan.getSchemas()) {
            Element elem = new Element("schema")
                .addContent(new Element("name").setText(schema))
                .addContent(new Element("id").setText(schemaMan.getIdVersion(schema).one()))
                .addContent(new Element("version").setText(schemaMan.getIdVersion(schema).two()))
                .addContent(new Element("description").setText(schema))
                .addContent(new Element("namespaces").setText(schemaMan.getNamespaceString(schema)));

            // is it editable?
            if (schemaMan.getSchema(schema).canEdit()) {
                elem.addContent(new Element("edit").setText("true"));
            } else {
                elem.addContent(new Element("edit").setText("false"));
            }

            // get the conversion information and add it too
            List<Element> convElems = schemaMan.getConversionElements(schema);
            if (convElems.size() > 0) {
                Element conv = new Element("conversions");
                conv.addContent(convElems);
                elem.addContent(conv);
            }
            response.addContent(elem);
        }

        return response;
    }

    /**
     * Retrieves a user's groups.
     *
     * @param includingSystemGroups if true, also returns the system groups ('GUEST', 'intranet',
     *                              'all')
     * @param all                   if true returns all the groups, even those the user doesn't
     *                              belongs to
     */
    private Element getGroups(ServiceContext context, Profile profile, boolean includingSystemGroups,
                              boolean all) throws SQLException {
        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);
        final Sort sort = SortUtils.createSort(Group_.id);

        UserSession session = context.getUserSession();
        if (all || !session.isAuthenticated()) {
            return groupRepository.findAllAsXml(Specification.not(GroupSpecs.isReserved()), sort);
        }

        Element result;
        // you're Administrator
        if (Profile.Administrator == session.getProfile()) {
            // return all groups
            if (includingSystemGroups) {
                result = groupRepository.findAllAsXml(null, sort);
            } else {
                return groupRepository.findAllAsXml(Specification.not(GroupSpecs.isReserved()), sort);
            }
        } else {
            Specification<UserGroup> spec = Specification.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
            // you're no Administrator
            // retrieve your groups
            if (profile != null) {
                spec = spec.and(UserGroupSpecs.hasProfile(profile));
            }
            Set<Integer> ids = new HashSet<Integer>(userGroupRepository.findGroupIds(spec));

            // include system groups if requested (used in harvesters)
            if (includingSystemGroups) {
                // these DB keys of system groups are hardcoded !
                for (ReservedGroup reservedGroup : ReservedGroup.values()) {
                    ids.add(reservedGroup.getId());
                }
            }

            // retrieve all groups
            Element groups = groupRepository.findAllAsXml(null, sort);

            // filter all groups so only your groups (+ maybe system groups) are retained
            result = Lib.element.pruneChildren(groups, ids);
        }
        return result;
    }

    private Element getSources(ServiceContext context, SettingManager sm) throws SQLException {
        Element element = new Element("results");
        final List<Source> sourceList = context.getBean(SourceRepository.class).findAll(SortUtils.createSort(Source_.name));

        Set<String> sourceIds = new HashSet<>();
        for (Source o : sourceList) {
            if (!sourceIds.contains(o.getUuid())) {
                element.addContent(buildRecord(o.getUuid(), o.getName(), o.getLabelTranslations(), null, null));
                sourceIds.add(o.getUuid());
            }
        }

        String siteId = sm.getSiteId();
        if (!sourceIds.contains(siteId)) {
            String siteName = sm.getSiteName();

            final SettingRepository settingRepository = context.getBean(SettingRepository.class);
            final List<Setting> labelSettings = settingRepository.findAll(SettingSpec.nameStartsWith(Settings.SYSTEM_SITE_LABEL_PREFIX));
            Map<String, String> labels = Maps.newHashMap();
            for (Setting setting : labelSettings) {
                labels.put(setting.getName().substring(Settings.SYSTEM_SITE_LABEL_PREFIX.length()), setting.getValue());
            }
            element.addContent(buildRecord(siteId, siteName, labels, null, null));
        }

        return element;
    }

    private Element buildTemplateRecord(String id, String title, String schema) {
        return buildRecord(id, title, Collections.emptyMap(), schema, null);
    }


    //--------------------------------------------------------------------------

    private Element buildRecord(String id, String name, Map<String, String> labelTranslations, String code, String serverCode) {
        Element el = new Element("record");

        Element idE = new Element("id").setText(id);
        if (code != null) idE.setAttribute("code", code);
        if (serverCode != null) idE.setAttribute("serverCode", serverCode);
        el.addContent(idE);
        el.addContent(new Element("name").setText(name));
        Element translations = new Element("label");
        el.addContent(translations);
        for (Map.Entry<String, String> entry : labelTranslations.entrySet()) {
            translations.addContent(new Element(entry.getKey()).setText(xmlContentEscaper().escape(entry.getValue())));
        }
        return el;
    }

    //--------------------------------------------------------------------------
    //--- Users
    //--------------------------------------------------------------------------

    private Element getUsers(ServiceContext context) throws SQLException {
        UserSession us = context.getUserSession();
        List<Element> list = getUsers(context, us);

        Element users = new Element(USERS);

        for (Element user : list) {
            user = (Element) user.clone();
            user.removeChild("password");
            user.setName("user");

            users.addContent(user);
        }

        return users;
    }

    //--------------------------------------------------------------------------

    private List<Element> getUsers(ServiceContext context, UserSession us) throws SQLException {
        if (!us.isAuthenticated())
            return new ArrayList<Element>();

        int userId = Integer.parseInt(us.getUserId());

        final UserRepository userRepository = context.getBean(UserRepository.class);
        if (us.getProfile() == Profile.Administrator) {
            @SuppressWarnings("unchecked")
            List<Element> allUsers = userRepository.findAllAsXml().getChildren();
            return allUsers;
        }

        if (us.getProfile() != Profile.UserAdmin) {
            @SuppressWarnings("unchecked")
            List<Element> identifiedUsers = userRepository.findAllAsXml(UserSpecs.hasUserId(userId)).getChildren();
            return identifiedUsers;
        }

        //--- we have a user admin

        Set<Integer> hsMyGroups = getUserGroups(context, userId);

        Set<String> profileSet = us.getProfile().getAllNames();

        //--- retrieve all users

        Element elUsers = userRepository.findAllAsXml(null, SortUtils.createSort(User_.name));

        //--- now filter them

        ArrayList<Element> alToRemove = new ArrayList<Element>();

        for (Object o : elUsers.getChildren()) {
            Element elRec = (Element) o;

            String sUserId = elRec.getChildText("id");
            String profile = elRec.getChildText("profile");

            if (!profileSet.contains(profile))
                alToRemove.add(elRec);

            else if (!hsMyGroups.containsAll(getUserGroups(context, Integer.parseInt(sUserId))))
                alToRemove.add(elRec);
        }

        //--- remove unwanted users

        for (int i = 0; i < alToRemove.size(); i++)
            alToRemove.get(i).detach();

        //--- return result

        @SuppressWarnings("unchecked")
        List<Element> usersEls = elUsers.getChildren();
        return usersEls;
    }

    //--------------------------------------------------------------------------

    private Set<Integer> getUserGroups(ServiceContext context, int userId) throws SQLException {
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);

        return new HashSet<Integer>(userGroupRepository.findGroupIds(UserGroupSpecs.hasUserId(userId)));
    }

    //--------------------------------------------------------------------------
    //---
    //--- General purpose methods
    //---
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------

    private Element getEnv(ServiceContext context) {
        return new Element("env")
            .addContent(new Element("baseURL").setText(context.getBaseUrl()))
            .addContent(new Element("node").setText(context.getNodeId()));
    }
}
