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

package org.fao.geonet.kernel.harvest.harvester;

import com.google.common.collect.Maps;
import org.fao.geonet.utils.Env;
import org.locationtech.jts.util.Assert;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Localized;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.QuartzSchedulerUtils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;

/**
 * Params to configure a harvester. It contains things like url, username, password,...
 */
public abstract class AbstractParams implements Cloneable {
    public static final String TRANSLATIONS = "translations";
    private static final long MAX_EVERY = Integer.MAX_VALUE;
    protected Logger log = Log.createLogger(Geonet.HARVEST_MAN);

    public abstract String getIcon();

    public abstract AbstractParams copy();

    public enum OverrideUuid {
        SKIP, OVERRIDE, RANDOM
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    protected DataManager dm;
    private String name;
    private Map<String, String> translations = Maps.newHashMap();
    private String uuid;
    private boolean useAccount;
    private String username;
    private String password;
    private String every;
    private boolean oneRunOnly;
    private HarvestValidationEnum validate;
    private String importXslt;
    private Element node;
    private String ownerId;
    private String ownerIdGroup;
    private String ownerIdUser;
    private OverrideUuid overrideUuid;

    /**
     *  When more than one harvester harvest the same record, then record is usually rejected.
     *  It can override existing, but the privileges are not preserved. This option
     *  preserve privileges set in the different harvesters.
     */
    private boolean ifRecordExistAppendPrivileges;

    private String batchEdits;

    private List<Privileges> alPrivileges = new ArrayList<>();
    private List<String> alCategories = new ArrayList<>();

    public AbstractParams(DataManager dm) {
        this.dm = dm;
    }

    private static HarvestValidationEnum readValidateFromParams(Element content) {
        String validationString = Util.getParam(content, "validate", HarvestValidationEnum.NOVALIDATION.toString());
        return HarvestValidationEnum.lookup(validationString);
    }

    /**
     * @param node
     * @throws BadInputEx
     */
    public void create(Element node) throws BadInputEx {
        if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "AbstractParams creating from:\n" + Xml.getString(node));
        }
        Element site = node.getChild("site");
        Assert.isTrue(site != null, "Site cannot be null");
        Element opt = node.getChild("options");
        Element content = node.getChild("content");


        Element account = site.getChild("account");

        name = Util.getParam(site, "name", "");
        if (site.getChild(TRANSLATIONS) != null) {
            translations = Localized.translationXmlToLangMap(site.getChild(TRANSLATIONS).getChildren());
        }

        uuid = Util.getParam(site, "uuid", UUID.randomUUID().toString());

        Element ownerIdE = node.getChild("owner");
        if (ownerIdE == null) {
            ownerIdE = node.getChild("ownerId");
        }
        if (ownerIdE != null) {
            setOwnerId(ownerIdE.getChildText("id"));
            if (getOwnerId() == null || getOwnerId().trim().isEmpty()) {
                setOwnerId(ownerIdE.getText());
                if (getOwnerId() == null || getOwnerId().trim().isEmpty()) {
                    setOwnerId(null);
                }
            }
        }

        if (StringUtils.isEmpty(getOwnerId())) {
            Log.warning(Geonet.HARVEST_MAN, "No owner defined for harvester: " + getName() + " (" + getUuid() + ")");
        }

        Element ownerIdUserE = site.getChild("ownerUser");
        if (ownerIdUserE == null) {
            ownerIdUserE = node.getChild("ownerUser");
        }
        if (ownerIdUserE != null) {
            Element idE = ownerIdUserE.getChild("id");
            if (idE != null) {
                setOwnerIdUser(idE.getText());
            } else if (!ownerIdUserE.getTextTrim().isEmpty()) {
                setOwnerIdUser(ownerIdUserE.getTextTrim());
            }
        }

        Element ownerIdGroupE = site.getChild("ownerGroup");
        if (ownerIdGroupE == null) {
            ownerIdGroupE = node.getChild("ownerGroup");
        }
        if (ownerIdGroupE != null) {
            Element idE = ownerIdGroupE.getChild("id");
            if (idE != null) {
                setOwnerIdGroup(idE.getText());
            } else if (!ownerIdGroupE.getTextTrim().isEmpty()) {
                setOwnerIdGroup(ownerIdGroupE.getTextTrim());
            }
        }

        setUseAccount(Util.getParam(account, "use", false));
        setUsername(Util.getParam(account, "username", ""));
        setPassword(Util.getParam(account, "password", ""));

        setEvery(Util.getParam(opt, "every", "0 0 0 * * ?"));

        setOneRunOnly(Util.getParam(opt, "oneRunOnly", false));
        setOverrideUuid(
                OverrideUuid.valueOf(
                        Util.getParam(opt, "overrideUuid",  OverrideUuid.SKIP.name())));

        setIfRecordExistAppendPrivileges("true".equals(node.getChildTextTrim("ifRecordExistAppendPrivileges")));

        getTrigger();

        setImportXslt(Util.getParam(content, "importxslt", "none"));
        setBatchEdits(Util.getParam(content, "batchEdits", ""));

        this.setValidate(readValidateFromParams(content));

        addPrivileges(node.getChild("privileges"));
        addCategories(node.getChild("categories"));

        this.setNodeElement(node);
    }

    /**
     * @param node
     * @throws BadInputEx
     */
    public void update(Element node) throws BadInputEx {
        Element site = node.getChild("site");
        Element opt = node.getChild("options");
        Element content = node.getChild("content");

        final String ACCOUNT_EL_NAME = "account";
        Element account = (site == null) ? null : site.getChild(ACCOUNT_EL_NAME);
        if (account == null) {
            account = node.getChild(ACCOUNT_EL_NAME);
        }
        Element privil = node.getChild("privileges");
        Element categ = node.getChild("categories");

        setName(Util.getParam(site, "name", getName()));
        if (site != null && site.getChild(TRANSLATIONS) != null) {
            setTranslations(Localized.translationXmlToLangMap(site.getChild(TRANSLATIONS).getChildren()));
        }

        Element ownerIdE = node.getChild("owner");
        if (ownerIdE == null) {
            ownerIdE = node.getChild("ownerId");
        }
        if (ownerIdE != null) {
            setOwnerId(ownerIdE.getChildText("id"));
            if (getOwnerId() == null || getOwnerId().isEmpty()) {
                setOwnerId(ownerIdE.getTextNormalize());
            }
        } else {
            Log.warning(Geonet.HARVEST_MAN, "No owner defined for harvester: " + getName() + " (" + getUuid() + ")");
        }

        Element ownerIdUserE = node.getChild("ownerUser");
        if (ownerIdUserE != null) {
            Element idE = ownerIdUserE.getChild("id");
            if (idE != null) {
                setOwnerIdUser(idE.getText());
            } else {
                setOwnerIdUser(ownerIdUserE.getText());
            }
        }

        Element ownerIdGroupE = node.getChild("ownerGroup");
        if (ownerIdGroupE != null) {
            Element idE = ownerIdGroupE.getChild("id");
            if (idE != null) {
                setOwnerIdGroup(idE.getText());
            } else {
                setOwnerIdGroup(ownerIdGroupE.getText());
            }
        }

        setUseAccount(Util.getParam(account, "use", isUseAccount()));
        setUsername(Util.getParam(account, "username", getUsername()));
        setPassword(Util.getParam(account, "password", getPassword()));

        setEvery(Util.getParam(opt, "every", getEvery()));
        setOneRunOnly(Util.getParam(opt, "oneRunOnly", isOneRunOnly()));

        setOverrideUuid(
                OverrideUuid.valueOf(
                        Util.getParam(opt, "overrideUuid", getOverrideUuid().name())));
        setIfRecordExistAppendPrivileges("true".equals(node.getChildTextTrim("ifRecordExistAppendPrivileges")));

        getTrigger();

        setImportXslt(Util.getParam(content, "importxslt", "none"));
        setBatchEdits(Util.getParam(content, "batchEdits", getBatchEdits()));
        this.setValidate(readValidateFromParams(content));

        if (privil != null) {
            addPrivileges(privil);
        }

        if (categ != null) {
            addCategories(categ);
        }

        this.setNodeElement(node);
    }

    /**
     * @return
     */
    public Iterable<Privileges> getPrivileges() {
        return alPrivileges;
    }

    /**
     * @return
     */
    public Iterable<String> getCategories() {
        return alCategories;
    }


    /**
     * @param copy
     */
    protected void copyTo(AbstractParams copy) {
        copy.setName(getName());
        copy.setUuid(getUuid());
        copy.setTranslations(getTranslations());
        copy.setOwnerId(getOwnerId());
        copy.setOwnerIdUser(getOwnerIdUser());
        copy.setOwnerIdGroup(getOwnerIdGroup());

        copy.setUseAccount(isUseAccount());
        copy.setUsername(getUsername());
        copy.setPassword(getPassword());

        copy.setEvery(getEvery());
        copy.setOneRunOnly(isOneRunOnly());
        copy.setOverrideUuid(getOverrideUuid());
        copy.setIfRecordExistAppendPrivileges(isIfRecordExistAppendPrivileges());

        copy.setImportXslt(getImportXslt());
        copy.setBatchEdits(getBatchEdits());
        copy.setValidate(getValidate());

        for (Privileges p : alPrivileges) {
            copy.addPrivilege(p.copy());
        }

        for (String s : alCategories) {
            copy.addCategory(s);
        }

        copy.setNodeElement(getNodeElement());
    }

    /**
     * @return
     */
    public JobDetail getJob() {
        return newJob(HarvesterJob.class).withIdentity(getUuid(), AbstractHarvester.HARVESTER_GROUP_NAME).usingJobData(HarvesterJob
            .ID_FIELD, getUuid()).build();
    }

    /**
     * @return
     */
    public Trigger getTrigger() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String timeZoneSetting = settingManager.getValue(Settings.SYSTEM_SERVER_TIMEZONE, true);
        TimeZone tz = TimeZone.getDefault();
        if (StringUtils.isNotBlank(timeZoneSetting)) {
            try {
                ZoneId zoneId = ZoneId.of(timeZoneSetting);
                tz = TimeZone.getTimeZone(zoneId);
                log.debug("Using timezone in settings to set the Trigger timezone: " + zoneId);
            } catch (DateTimeException e) {
                log.error(e);
            }
        }

        boolean enableScheduledHarvesters = Env
            .getPropertyFromEnv("harvester.scheduler.enabled", "true")
            .equalsIgnoreCase("true");
        final String schedule;
        if (enableScheduledHarvesters) {
            // the configured value for the harvester
            schedule = getEvery();
        } else {
            // override with 'never'
            schedule = "0 0 0 * * ? 2099";
        }

        return QuartzSchedulerUtils.getTrigger(getUuid(), AbstractHarvester.HARVESTER_GROUP_NAME, schedule, MAX_EVERY, tz);
    }

    /**
     * @param port
     * @throws BadParameterEx
     */
    protected void checkPort(int port) throws BadParameterEx {
        if (port < 1 || port > 65535) {
            throw new BadParameterEx("port", port);
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- Privileges and categories API methods
    //---
    //---------------------------------------------------------------------------

    /**
     * Fills a list with Privileges that reflect the input 'privileges' element. The 'privileges'
     * element has this format:
     * <p/>
     * <privileges> <group id="..."> <operation name="..."> ... </group> ... </privileges>
     * <p/>
     * Operation names are: view, download, edit, etc... User defined operations are taken into
     * account.
     */
    private void addPrivileges(Element privil) throws BadInputEx {
        alPrivileges.clear();

        if (privil == null) {
            return;
        }

        for (Object o : privil.getChildren("group")) {
            Element group = (Element) o;
            String groupID = group.getAttributeValue("id");

            if (groupID == null) {
                throw new MissingParameterEx("attribute:id", group);
            }

            Privileges p = new Privileges(groupID);

            for (Object o1 : group.getChildren("operation")) {
                Element oper = (Element) o1;
                int op = getOperationId(oper);

                p.add(op);
            }

            addPrivilege(p);
        }
    }

    public void addPrivilege(Privileges p) {
        alPrivileges.add(p);
    }

    /**
     * @param oper
     * @return
     * @throws BadInputEx
     */
    private int getOperationId(Element oper) throws BadInputEx {
        String operName = oper.getAttributeValue("name");

        if (operName == null) {
            throw new MissingParameterEx("attribute:name", oper);
        }

        int operID = dm.getAccessManager().getPrivilegeId(operName);

        if (operID == -1) {
            throw new BadParameterEx("attribute:name", operName);
        }

        if (operID == 2 || operID == 4) {
            throw new BadParameterEx("attribute:name", operName);
        }

        return operID;
    }

    /**
     * Fills a list with category identifiers that reflect the input 'categories' element. The
     * 'categories' element has this format:
     * <p/>
     * <categories> <category id="..."/> ... </categories>
     */
    private void addCategories(Element categ) throws BadInputEx {
        alCategories.clear();

        if (categ == null) {
            return;
        }

        for (Object o : categ.getChildren("category")) {
            Element categElem = (Element) o;
            String categId = categElem.getAttributeValue("id");

            if (categId == null || categId.trim().isEmpty()) {
                // categoryId is not mandatory.
                continue;
            }
            if (!Lib.type.isInteger(categId)) {
                throw new BadParameterEx("attribute:id", categElem);
            }

            addCategory(categId);
        }
    }

    public void addCategory(String categId) {
        alCategories.add(categId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isUseAccount() {
        return useAccount;
    }

    public void setUseAccount(boolean useAccount) {
        this.useAccount = useAccount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEvery() {
        return every;
    }

    public void setEvery(String every) {
        this.every = every;
    }

    public boolean isOneRunOnly() {
        return oneRunOnly;
    }

    public void setOneRunOnly(boolean oneRunOnly) {
        this.oneRunOnly = oneRunOnly;
    }

    public HarvestValidationEnum getValidate() {
        return validate;
    }

    public void setValidate(HarvestValidationEnum validate) {
        this.validate = validate;
    }

    public String getImportXslt() {
        return importXslt;
    }

    public void setImportXslt(String importXslt) {
        this.importXslt = importXslt;
    }

    public Element getNodeElement() {
        return node;
    }

    public void setNodeElement(Element node) {
        this.node = node;
    }

    /**
     * id of the user who created or updated this harvester node.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * id of the group selected by the user who created or updated this harvester node.
     */
    public String getOwnerIdGroup() {
        return ownerIdGroup;
    }

    public void setOwnerIdGroup(String ownerIdGroup) {
        this.ownerIdGroup = ownerIdGroup;
    }

    /**
     * User who should own the harvested records
     * @return
     */
    public String getOwnerIdUser() {
        return ownerIdUser;
    }

    public void setOwnerIdUser(String ownerIdUser) {
        this.ownerIdUser = ownerIdUser;
    }

    public OverrideUuid getOverrideUuid() {
        return overrideUuid;
    }

    public void setOverrideUuid(OverrideUuid overrideUuid) {
        this.overrideUuid = overrideUuid;
    }

    public boolean isIfRecordExistAppendPrivileges() {
        return ifRecordExistAppendPrivileges;
    }

    public void setIfRecordExistAppendPrivileges(boolean ifRecordExistAppendPrivileges) {
        this.ifRecordExistAppendPrivileges = ifRecordExistAppendPrivileges;
    }

    public String getBatchEdits() {
        return batchEdits;
    }

    public void setBatchEdits(String batchEdits) {
        this.batchEdits = batchEdits;
    }
}
