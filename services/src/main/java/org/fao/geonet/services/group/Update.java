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

package org.fao.geonet.services.group;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;


/**
 * Update the information of a group.
 */
@Deprecated
public class Update extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(final Element params, final ServiceContext context) throws Exception {
        final String id = params.getChildText(Params.ID);
        final String name = Util.getParam(params, Params.NAME);
        final String description = Util.getParam(params, Params.DESCRIPTION, "");
        final boolean deleteLogo = Util.getParam(params, "deleteLogo", false);
        final String copyLogo = Util.getParam(params, "copyLogo", null);
        final String email = params.getChildText(Params.EMAIL);
        final String category = Util.getParam(params, Params.CATEGORY, "-1");

        final java.util.List<Integer> allowedCategories = Util.getParamsAsInt(params, "allowedCategories");
        final Boolean enableAllowedCategories = Util.getParam(params, "enableAllowedCategories", false);

        String website = params.getChildText("website");
        if (website != null && website.length() > 0 && !website.startsWith("http://")) {
            website = "http://" + website;
        }

        //Check that we have privileges over this group
        UserSession session = context.getUserSession();
        if (!session.getProfile().equals(Profile.Administrator)) {
            java.util.List<UserGroup> usergroups = context.getBean(UserGroupRepository.class).findAll(
                GroupSpecs.isEditorOrMore(session.getUserIdAsInt()));
            boolean canEditGroup = false;
            if (id != null && !"".equals(id)) {
                Integer i = Integer.valueOf(id);
                for (UserGroup ug : usergroups) {
                    if (ug.getGroup().getId() == i) {
                        canEditGroup = ug.getProfile().equals(Profile.UserAdmin);
                    }
                }
            }
            if (!canEditGroup) {
                throw new SecurityException("You cannot edit this group");
            }
        }
        // Logo management ported/adapted from GeoNovum GeoNetwork app.
        // Original devs: Heikki Doeleman and Thijs Brentjens
        String logoFile = params.getChildText("logofile");

        FilePathChecker.verify(logoFile);
        FilePathChecker.verify(copyLogo);

        final String logoUUID = copyLogo == null ? copyLogoFromRequest(context, logoFile) :
            copyLogoFromHarvesters(context, copyLogo);

        final GroupRepository groupRepository = context.getBean(GroupRepository.class);


        final MetadataCategoryRepository catRepository =
            context.getBean(MetadataCategoryRepository.class);

        MetadataCategory tmpcat = null;

        try {
            tmpcat = catRepository.findOne(Integer.valueOf(category));
        } catch (Throwable t) {
            //Not a valid category id
        }

        final MetadataCategory cat = tmpcat;

        final Element elRes = new Element(Jeeves.Elem.RESPONSE);

        if (id == null || "".equals(id)) {

            Group group = new Group()
                .setName(name)
                .setDescription(description)
                .setEmail(email)
                .setLogo(logoUUID)
                .setWebsite(website)
                .setDefaultCategory(cat)
                .setEnableAllowedCategories(enableAllowedCategories);

            setUpAllowedCategories(allowedCategories, enableAllowedCategories,
                catRepository, group);


            final LanguageRepository langRepository = context.getBean(LanguageRepository.class);
            java.util.List<Language> allLanguages = langRepository.findAll();
            for (Language l : allLanguages) {
                group.getLabelTranslations().put(l.getId(), name);
            }

            groupRepository.save(group);

            elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.ADDED));
        } else {
            final String finalWebsite = website;
            groupRepository.update(Integer.valueOf(id), new Updater<Group>() {
                @Override
                public void apply(final Group entity) {
                    entity.setEmail(email)
                        .setName(name)
                        .setDescription(description)
                        .setWebsite(finalWebsite)
                        .setDefaultCategory(cat)
                        .setEnableAllowedCategories(enableAllowedCategories);

                    setUpAllowedCategories(allowedCategories, enableAllowedCategories,
                        catRepository, entity);

                    if (!deleteLogo && logoUUID != null) {
                        entity.setLogo(logoUUID);
                    }
                    if (deleteLogo) {
                        entity.setLogo(null);
                    }
                }
            });

            elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.UPDATED));
        }

        return elRes;
    }

    private void setUpAllowedCategories(
        final java.util.List<Integer> allowedCategories,
        final Boolean enableAllowedCategories,
        final MetadataCategoryRepository catRepository, Group group) {

        if (enableAllowedCategories) {
            if (group.getAllowedCategories() != null) {
                group.getAllowedCategories().clear();
            }

            for (Integer i : allowedCategories) {
                try {
                    MetadataCategory c = catRepository.findOne(i);
                    group.getAllowedCategories().add(c);
                } catch (Throwable t) {
                    //Not a valid category
                }
            }
        }
    }

    private String copyLogoFromRequest(ServiceContext context, String logoFile) throws IOException {
        String logoUUID = null;
        if (logoFile != null && logoFile.length() > 0) {
            // logo uploaded

            // IE returns complete path of file, while FF only the name (strip path for IE)
            logoFile = stripPath(logoFile);

            Path input = context.getUploadDir().resolve(logoFile);
            try (InputStream in = IO.newInputStream(input)) {
                ImageIO.read(in); // check it parses
            }
            final Resources resources = context.getBean(Resources.class);
            Path logoDir = resources.locateLogosDir(context);
            logoUUID = UUID.randomUUID().toString();
            try(Resources.ResourceHolder outputResource =
                    resources.getWritableImage(context, logoUUID + ".png", logoDir)) {
                java.nio.file.Files.copy(input, outputResource.getPath());
            }
        }

        return logoUUID;
    }

    private String copyLogoFromHarvesters(ServiceContext context, String logoFile) throws IOException {
        final String logoUUID = UUID.randomUUID().toString();
        final Resources resources = context.getBean(Resources.class);
        final Path harvesterDir = resources.locateHarvesterLogosDir(context);
        try (Resources.ResourceHolder harvestLogo = resources.getImage(context, logoFile, harvesterDir)) {
            if (harvestLogo != null) {
                try (InputStream in = IO.newInputStream(harvestLogo.getPath())) {
                    ImageIO.read(in); // check it parses
                }
                String extension = FilenameUtils.getExtension(harvestLogo.getRelativePath());
                try(Resources.ResourceHolder outputResource =
                        resources.getWritableImage(context, logoUUID + "." + extension, resources.locateLogosDir(context))) {
                    java.nio.file.Files.copy(harvestLogo.getPath(), outputResource.getPath());
                }
            } else {
                throw new IOException("Cannot find " + logoFile + " in " + harvesterDir);
            }
        }
        return logoUUID;
    }

    private String stripPath(String file) {
        if (file.indexOf('\\') > 0) {
            String[] pathTokens = file.split("\\\\");
            file = pathTokens[pathTokens.length - 1];
        }

        return file;
    }
}
