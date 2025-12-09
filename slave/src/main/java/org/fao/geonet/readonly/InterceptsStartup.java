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

package org.fao.geonet.readonly;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.events.server.ServerStartup;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.fao.geonet.repository.UserRepository;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * 
 * This instance is a slave, which means: No user login (no filter configured
 * to authenticate) and no user creation {@link InterceptsUserAdd}
 * {@link InterceptsUserUpdate}
 * 
 * On this class we will load a harvester defined by an url on a source file, if
 * not already added, and remove all users except the admin user, just in case.
 * 
 * @author delawen
 *
 */
@Component
public class InterceptsStartup implements ApplicationListener<ServerStartup> {

	@Autowired
	private UserRepository userRepository;

	private @Value("${slave.removeHarvesters:true}") boolean removeHarvesters;
	private @Value("${slave.removeUsers:true}") boolean removeUsers;
	private @Value("${slave.masterURL}") String masterURL;
	private @Value("${slave.masterNode}") String masterNode;
	private @Value("${slave.frequency}") String frequency;

	private final String key = "MASTER";
	private final Logger log = LoggerFactory.getLogger(Geonet.GEONETWORK);

	public void onApplicationEvent(ServerStartup arg0) {
		removeHarvesters(arg0);
		addHarvester(arg0);
		removeUsers(arg0);
	}

	/**
	 * Remove all harvesters that don't have the name "MASTER"
	 * 
	 * @param arg0
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private void removeHarvesters(ServerStartup arg0) {
		if (removeHarvesters) {
			HarvesterSettingRepository settingsRepo = arg0.getContext().getBean(HarvesterSettingRepository.class);

			List<HarvesterSetting> harvesters = settingsRepo.findAllChildren(1);
			for (HarvesterSetting h : harvesters) {
				for (HarvesterSetting hs : settingsRepo.findAllChildren(h.getId())) {
					if (hs.getName().equals("site")) {
						for (HarvesterSetting s : settingsRepo.findAllChildren(hs.getId())) {
							if (s.getName().equals("name") && !s.getValue().equals(key)) {
								try {
									HarvestManager hm = arg0.getContext().getBean(HarvestManager.class);
									hm.remove(String.valueOf(h.getId()));
								} catch (Exception e) {
									log.error("Error removing old harvester in readonly instance: " + h, e);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Remove all users with an ID above 1 (the default admin user we need to run
	 * GeoNetwork).
	 * 
	 * @param arg0
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private void removeUsers(ServerStartup arg0) {
		if (removeUsers) {
			userRepository.deleteAll(new Specification<User>() {
				@Override
				public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					Path<Integer> userIdAttributePath = root.get(User_.id);
					Predicate userIdEqualPredicate = cb.gt(userIdAttributePath, cb.literal(1));
					return userIdEqualPredicate;
				}
			});
		}
	}

	/**
	 * Create the "MASTER" harvester to replicate all content
	 * 
	 * @param arg0
	 */
	private void addHarvester(ServerStartup arg0) {
		int id = harvesterAlreadyCreated(arg0);
		
		if (id < 0) {

			Element harvester = new Element("node");
			harvester.setAttribute("id", "");
			harvester.setAttribute("type", "geonetwork");

			Element ownerGroup = new Element("ownerGroup");
			addElementWithContentString(ownerGroup, "id", "2");
			harvester.addContent(ownerGroup);

			Element ownerUser = new Element("ownerUser");
			addElementWithContentString(ownerUser, "id", "undefined");
			harvester.addContent(ownerUser);

			harvester.addContent(createSite());
			harvester.addContent(createEmptySearches());
			harvester.addContent(createOptions());
			harvester.addContent(createContent());
			harvester.addContent(createPrivileges());
			Element categories = new Element("categories");
			addElementWithAttribute(categories, "category", "id", "");
			harvester.addContent(categories);

			id = addMasterHarvester(arg0, harvester);
		}
		
		try {
			HarvestManager hm = arg0.getContext().getBean(HarvestManager.class);
			hm.run(String.valueOf(id));
		} catch (Exception e) {
			log.error("Error running MASTER harvester in readonly instance", e);
		}
	}

	private int harvesterAlreadyCreated(ServerStartup arg0) {
		HarvesterSettingRepository settingsRepo = arg0.getContext().getBean(HarvesterSettingRepository.class);

		List<HarvesterSetting> harvesters = settingsRepo.findAllChildren(1);
		for (HarvesterSetting h : harvesters) {
			for (HarvesterSetting hs : settingsRepo.findAllChildren(h.getId())) {
				if (hs.getName().equals("site")) {
					for (HarvesterSetting s : settingsRepo.findAllChildren(hs.getId())) {
						if (s.getName().equals("name") && s.getValue().equals(key)) {
							return h.getId();
						}
					}
				}
			}
		}
		return -1;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	private int addMasterHarvester(ServerStartup arg0, Element harvester) {
		try {
			HarvestManager hm = arg0.getContext().getBean(HarvestManager.class);
			return Integer.valueOf(hm.addHarvesterReturnId(harvester, "1"));
		} catch (Exception e) {
			log.error("Error initializing MASTER harvester in readonly instance", e);
		}
		return -1;
	}

	private Element createPrivileges() {
		Element privileges = new Element("privileges");
		Element group = new Element("group");
		group.setAttribute("id", "1");
		addElementWithAttribute(group, "operation", "name", "view");
		addElementWithAttribute(group, "operation", "name", "dynamic");
		addElementWithAttribute(group, "operation", "name", "download");
		privileges.addContent(group);
		return privileges;
	}

	private void addElementWithAttribute(Element group, String name, String attribute, String value) {
		Element operation = new Element(name);
		operation.setAttribute(attribute, value);
		group.addContent(operation);
	}

	private Element createContent() {
		Element content = new Element("content");
		addElementWithContentString(content, "validate", "NOVALIDATION");
		addElementWithContentString(content, "importxslt", "none");
		return content;
	}

	private Element createOptions() {
		Element options = new Element("options");
		addElementWithContentString(options, "oneRunOnly", "false");
		addElementWithContentString(options, "overrideUuid", "OVERRIDE");
		addElementWithContentString(options, "every", frequency);
		addElementEmpty(options, "status");
		return options;
	}

	private Element createSite() {
		Element site = new Element("site");
		addElementWithContentString(site, "name", key);
		addElementWithContentString(site, "host", masterURL);
		addElementWithContentString(site, "node", masterNode);
		addElementWithContentString(site, "useChangeDateForUpdate", "true");
		addElementWithContentString(site, "createRemoteCategory", "true");
		addElementWithContentString(site, "icon", "undefined");
		addElementWithContentString(site, "mefFormatFull", "false");
		addElementEmpty(site, "xslfilter");
		Element account = new Element("account");
		addElementWithContentString(account, "use", "false");
		addElementEmpty(account, "username");
		addElementEmpty(account, "password");
		site.addContent(account);
		return site;
	}

	private Element createEmptySearches() {
		Element searches = new Element("searches");
		Element search = new Element("search");
		addElementEmpty(search, "freeText");
		addElementEmpty(search, "title");
		addElementEmpty(search, "abstract");
		addElementEmpty(search, "keywords");
		addElementEmpty(search, "digital");
		addElementEmpty(search, "hardcopy");
		addElementEmpty(search, "anyField");
		addElementEmpty(search, "anyValue");
		Element source = new Element("source");
		addElementEmpty(source, "uuid");
		addElementEmpty(source, "name");
		search.addContent(source);
		searches.addContent(search);
		return searches;
	}

	private void addElementEmpty(Element parent, String name) {
		Element el = new Element(name);
		parent.addContent(el);
	}

	private void addElementWithContentString(Element parent, String name, String value) {
		Element el = new Element(name);
		el.addContent(value);
		parent.addContent(el);
	}
}