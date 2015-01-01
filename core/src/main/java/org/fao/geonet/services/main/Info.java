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

import jeeves.component.ProfileManager;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.Source_;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.User_;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.services.util.z3950.RepositoryInfo;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Info implements Service {
    private static final String READ_ONLY = "readonly";
    private static final String INDEX = "index";
    
    private Path xslPath;
	private Path otherSheets;
	private ServiceConfig _config;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

    /**
     *
     * @param appPath
     * @param config
     * @throws Exception
     */
	public void init(Path appPath, ServiceConfig config) throws Exception
	{
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
     *
     * @param inParams
     * @param context
     * @return
     * @throws Exception
     */
	public Element exec(Element inParams, ServiceContext context) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		Element params = (Element)inParams.clone();

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

			if (type.equals("site")) {
				result.addContent(gc.getBean(SettingManager.class).getValues(
                        new String[]{
                                SettingManager.SYSTEM_SITE_NAME_PATH,
                                "system/site/organization", 
                                SettingManager.SYSTEM_SITE_SITE_ID_PATH,
                                "system/platform/version", 
                                "system/platform/subVersion"
                                }));
			} else if (type.equals("config")) {
			  // Return a set of properties which define what
			  // to display or not in the user interface
              final List<Setting> publicSettings = context.getBean(SettingRepository.class).findAllByInternal(false);
              List<String> publicSettingsKey = new ArrayList<String>();
              for(Setting s : publicSettings) {
                publicSettingsKey.add(s.getName());
              }
              result.addContent(new Element("config").addContent(gc.getBean(SettingManager.class).getValues(
                  publicSettingsKey.toArray(new String[0]))));
            } else if (type.equals("inspire")) {
				result.addContent(gc.getBean(SettingManager.class).getValues(
				            new String[]{
				                         "system/inspire/enableSearchPanel", 
				                         "system/inspire/enable"
				                         }));
            } else if (type.equals("harvester")) {
			    result.addContent(gc.getBean(SettingManager.class).getValues(
                        new String[]{ "system/harvester/enableEditing"}));
			
			} else if (type.equals("userGroupOnly")) {
                result.addContent(gc.getBean(SettingManager.class).getValues(
                        new String[]{"system/metadataprivs/usergrouponly"}));

            } else if (type.equals("categories")) {
				result.addContent(context.getBean(MetadataCategoryRepository.class).findAllAsXml());

            } else if (type.equals("groups"))   {
                String profile = params.getChildText("profile");
                Element r = getGroups(context, Profile.findProfileIgnoreCase(profile), false);
				result.addContent(r);

            } else if (type.equals("groupsIncludingSystemGroups")) {
                Element r = getGroups(context, null, true);
                result.addContent(r);

            } else if (type.equals("operations")) {
				result.addContent(context.getBean(OperationRepository.class).findAllAsXml());

            } else if (type.equals("regions")) {
		        RegionsDAO dao = context.getApplicationContext().getBean(RegionsDAO.class);
		        Element regions = dao.createSearchRequest(context).xmlResult();
				result.addContent(regions);
			} else if (type.equals("isolanguages")) {
                result.addContent(context.getBean(IsoLanguageRepository.class).findAllAsXml());

            } else if (type.equals("sources")) {
				result.addContent(getSources(context, sm));

            } else if (type.equals("users")) {
				result.addContent(getUsers(context));

            } else if (type.equals("templates"))   {
				result.addContent(getTemplates(context));

            } else if (type.equals("z3950repositories")) {
				result.addContent(getZRepositories(context, sm));

            } else if (type.equals("me")) {
				result.addContent(getMyInfo(context));
			
            } else if (type.equals("auth")) {
				result.addContent(getAuth(context));

            } else if (type.equals(READ_ONLY)) {
                result.addContent(getReadOnly(gc));
            } else if (type.equals(INDEX)) {
                result.addContent(getIndex(gc));
            } else if (type.equals("schemas")) {
				result.addContent(getSchemas(gc.getBean(SchemaManager.class)));

            } else if (type.equals("status")) {
				result.addContent(context.getBean(StatusValueRepository.class).findAllAsXml());

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
    * @param gc 
    * @return 
    */ 
    private Element getIndex(GeonetContext gc) { 
        Element isIndexing = new Element(INDEX); 
        isIndexing.setText(Boolean.toString(gc.getBean(DataManager.class).isIndexing())); 
        return isIndexing; 
    }
    
    /**
     * Returns whether GN is in read-only mode (true or false).
     * @param gc
     * @return
     */
    private Element getReadOnly(GeonetContext gc) {
        Element readOnly = new Element(READ_ONLY);
        readOnly.setText(Boolean.toString(gc.isReadOnly()));
        return readOnly;
    }

    /**
     *
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
     *
     * @param context
     * @return
     */
	private Element getMyInfo(ServiceContext context) {
		Element data = new Element("me");
		UserSession userSession = context.getUserSession();
		if (userSession.isAuthenticated()) {
			data.setAttribute("authenticated","true");
            final String emailAddr = userSession.getEmailAddr();
            data.addContent(new Element(Geonet.Elem.PROFILE).setText(userSession.getProfile().name()))
				.addContent(new Element(Geonet.Elem.USERNAME).setText(userSession.getUsername()))
				.addContent(new Element(Geonet.Elem.ID).setText(userSession.getUserId()))
				.addContent(new Element(Geonet.Elem.NAME).setText(userSession.getName()))
				.addContent(new Element(Geonet.Elem.SURNAME).setText(userSession.getSurname()))
				.addContent(new Element(Geonet.Elem.EMAIL).setText(emailAddr));

            if (emailAddr != null) {
                data.addContent(new Element(Geonet.Elem.HASH).setText(org.apache.commons.codec.digest.DigestUtils.md5Hex(emailAddr)));
            } else {
                data.addContent(new Element(Geonet.Elem.HASH).setText(""));
            }
		} else {
			data.setAttribute("authenticated","false");
		}
		return data;
	}

	private Element getSchemas(SchemaManager schemaMan) throws Exception {

    Element response = new Element("schemas");

    for (String schema : schemaMan.getSchemas()) {
      Element elem = new Element("schema")
       .addContent(new Element("name")            .setText(schema))
       .addContent(new Element("id")              .setText(schemaMan.getIdVersion(schema).one()))
       .addContent(new Element("version")         .setText(schemaMan.getIdVersion(schema).two()))
       .addContent(new Element("description")     .setText(schema))
       .addContent(new Element("namespaces")      .setText(schemaMan.getNamespaceString(schema)));

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
     * @param context
     * @param profile
     * @param includingSystemGroups if true, also returns the system groups ('GUEST', 'intranet', 'all')
     * @return
     * @throws java.sql.SQLException
     */
    private Element getGroups(ServiceContext context, Profile profile, boolean includingSystemGroups) throws SQLException {
        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);
        final Sort sort = SortUtils.createSort(Group_.id);

        UserSession session = context.getUserSession();
        if (!session.isAuthenticated()) {
            return groupRepository.findAllAsXml(Specifications.not(GroupSpecs.isReserved()), sort);
        }

        Element result;
        // you're Administrator
        if (Profile.Administrator == session.getProfile()) {
            // return all groups
            result = groupRepository.findAllAsXml(null, sort);
        } else {
            Specifications<UserGroup> spec = Specifications.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
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

    /**
     *
     *
     * @param context
     * @param sm
     * @return
     * @throws java.sql.SQLException
     */
	private Element getSources(ServiceContext context, SettingManager sm) throws SQLException
	{
        Element element = new Element("results");
        final List<Source> sourceList = context.getBean(SourceRepository.class).findAll(SortUtils.createSort(Source_.name));

		String siteId   = sm.getSiteId();
        String siteName = sm.getSiteName();

		for (Source o : sourceList) {
            element.addContent(buildRecord(o.getUuid(), o.getName(), null, null));
		}

        element.addContent(buildRecord(siteId, siteName, null, null));

		return element;
	}

	//--------------------------------------------------------------------------
	//--- ZRepositories
	//--------------------------------------------------------------------------

	public Element getZRepositories(ServiceContext context, SettingManager sm) throws Exception
	{
		boolean z3950Enable   = sm.getValue("system/z3950/enable").equals("true");

		List<RepositoryInfo> repoList = new ArrayList<RepositoryInfo>(RepositoryInfo.getRepositories(context));

		Element response = new Element("z3950repositories");

		for (RepositoryInfo repo : repoList) {
			if (!z3950Enable && repo.getClassName().startsWith("org.fao.geonet") ) {
				continue; // skip Local GeoNetwork Z server if not enabled
			} else {
				response.addContent(buildRecord(repo.getDn(),repo.getName(),repo.getCode(),repo.getServerCode()));
			}
		}

		return response;
	}

	//--------------------------------------------------------------------------
	//--- Templates
	//--------------------------------------------------------------------------

	private Element getTemplates(ServiceContext context) throws Exception
	{
		Path styleSheet = otherSheets.resolve("portal-present.xsl");
		Element result = search(context).setName(Jeeves.Elem.RESPONSE);
		Element root   = new Element("root");

		root.addContent(result);

		@SuppressWarnings("unchecked")
        List<Element> list = Xml.transform(root, styleSheet).getChildren();

		Element response = new Element("templates");

		for (Element elem : list) {
			Element info = elem.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);

			if (!elem.getName().equals("metadata"))
				continue;

			String id       = info.getChildText(Edit.Info.Elem.ID);
			String template = info.getChildText(Edit.Info.Elem.IS_TEMPLATE);
			String schema   = info.getChildText(Edit.Info.Elem.SCHEMA);

			if (template.equals("y"))
				response.addContent(buildTemplateRecord(id, elem.getChildText("title"), schema));
		}

		return response;
	}

	//--------------------------------------------------------------------------

	private Element search(ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		context.info("Creating searcher");

		Element       params = buildParams();
		ServiceConfig config = new ServiceConfig();

		SearchManager searchMan = gc.getBean(SearchManager.class);
		MetaSearcher  searcher  = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);

		searcher.search(context, params, config);

		params.addContent(new Element("from").setText("1"));
		params.addContent(new Element("to").setText(searcher.getSize() +""));

		Element result = searcher.present(context, params, config);

		searcher.close();

		return result;
	}

	//--------------------------------------------------------------------------

	private Element buildParams()
	{
		Element params = new Element(Jeeves.Elem.REQUEST);
		String arParams[] = {
			"extended", "off",
			"remote",   "off",
			"attrset",  "geo",
			"template", "y",
			"any",      "",
		};

		for(int i=0; i<arParams.length/2; i++)
			params.addContent(new Element(arParams[i*2]).setText(arParams[i*2 +1]));

		return params;
	}

	//--------------------------------------------------------------------------

	private Element buildTemplateRecord(String id, String title, String schema)
	{
		return buildRecord(id, title, schema, null);
	}


	//--------------------------------------------------------------------------

	private Element buildRecord(String id, String name, String code, String serverCode)
	{
		Element el = new Element("record");

		Element idE = new Element("id").setText(id);
		if (code != null) idE.setAttribute("code", code);
		if (serverCode != null) idE.setAttribute("serverCode", serverCode);
		el.addContent(idE);
		el.addContent(new Element("name").setText(name));

		return el;
	}

	//--------------------------------------------------------------------------
	//--- Users
	//--------------------------------------------------------------------------

	private Element getUsers(ServiceContext context) throws SQLException
	{
		UserSession us   = context.getUserSession();
		List<Element> list = getUsers(context, us);

		Element users = new Element("users");

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
            List<Element> identifiedUsers = userRepository.findAllAsXml(UserSpecs.hasUserId(userId)).getChildren();;
            return identifiedUsers;
        }

		//--- we have a user admin

		Set<Integer> hsMyGroups = getUserGroups(context, userId);

        Set<String> profileSet = us.getProfile().getAllNames();

		//--- retrieve all users

		Element elUsers = userRepository.findAllAsXml(null, SortUtils.createSort(User_.name));

		//--- now filter them

		ArrayList<Element> alToRemove = new ArrayList<Element>();

		for(Object o : elUsers.getChildren())
		{
			Element elRec = (Element) o;

			String sUserId = elRec.getChildText("id");
			String profile= elRec.getChildText("profile");

			if (!profileSet.contains(profile))
				alToRemove.add(elRec);

			else if (!hsMyGroups.containsAll(getUserGroups(context, Integer.parseInt(sUserId))))
				alToRemove.add(elRec);
		}

		//--- remove unwanted users

		for(int i=0; i<alToRemove.size(); i++)
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

	private Element getEnv(ServiceContext context)
	{
		return new Element("env")
						.addContent(new Element("baseURL").setText(context.getBaseUrl()))
                        .addContent(new Element("node").setText(context.getNodeId()));
	}
}
