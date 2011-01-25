//==============================================================================
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

package org.fao.geonet.kernel.search;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Search parameters that can be provided by a search client.
 *
 * @author heikki doeleman
 * 
 */
public class UserQueryInput {

    private String similarity;
    private String uuid;
    private String any;
    private String all;
    private String or;
    private String without;
    private String phrase;
    private Set<String> topicCategories;
    private String download;
    private String dynamic;
    private String protocol;
    private String featured;
    private Set<String> categories;
    private String template;
    private String dateTo;
    private String dateFrom;
    private String revisionDateTo;
    private String revisionDateFrom;
    private String publicationDateTo;
    private String publicationDateFrom;
    private String creationDateTo;
    private String creationDateFrom;
    private String extTo;
    private String extFrom;
    private String metadataStandardName;
    private String _schema;
    private String parentUuid;
    private String operatesOn;
    private String serviceType;
    private String type;
    private String inspire;
    private Set<String> inspireThemes;
    private String inspireAnnex;
    private String siteId;
    private Set<String> themeKeys;
    private String digital;
    private String paper;
    private String title;
    private String abstrakt;
    private String eastBL;
    private String westBL;
    private String northBL;
    private String southBL;
    private String relation;
    private String editable;
    private String denominator;
    private String denominatorFrom;
    private String denominatorTo;
    private String orgName;
    private String spatialRepresentationType;
    private List<String> taxons;
    private List<String> credits;
    private List<String> dataparams;
    
    /**
     * Creates this from a JDOM element.
     * 
     * @param jdom input
     */
    public UserQueryInput(Element jdom) {
        setSimilarity(jdom.getChildText(SearchParameter.SIMILARITY));
        setUuid(jdom.getChildText(SearchParameter.UUID));
        setAny(jdom.getChildText(SearchParameter.ANY));
        setAll(jdom.getChildText(SearchParameter.ALL));
        setOr(jdom.getChildText(SearchParameter.OR));
        setWithout(jdom.getChildText(SearchParameter.WITHOUT));
        setPhrase(jdom.getChildText(SearchParameter.PHRASE));

        @SuppressWarnings("unchecked")
        List<Element> isoTopicCategoriesE = (List<Element>)jdom.getChildren(SearchParameter.TOPICCATEGORY);
        Set<String> isoTopicCategories = new HashSet<String>();
        for(Element isoTopicCategoryE : isoTopicCategoriesE) {
            isoTopicCategories.add(isoTopicCategoryE.getText());
        }
        setTopicCategories(isoTopicCategories);

        setDownload(jdom.getChildText(SearchParameter.DOWNLOAD));
        setDynamic(jdom.getChildText(SearchParameter.DYNAMIC));
        setProtocol(jdom.getChildText(SearchParameter.PROTOCOL));
        setFeatured(jdom.getChildText(SearchParameter.FEATURED));

        @SuppressWarnings("unchecked")
        List<Element> categoriesE = (List<Element>)jdom.getChildren(SearchParameter.CATEGORY);
        Set<String> categories = new HashSet<String>();
        for(Element categoryE : categoriesE) {
            categories.add(categoryE.getText());
        }
        setCategories(categories);

        setTemplate(jdom.getChildText(SearchParameter.TEMPLATE));
        setDateTo(jdom.getChildText(SearchParameter.DATETO));
        setDateFrom(jdom.getChildText(SearchParameter.DATEFROM));
        setRevisionDateTo(jdom.getChildText(SearchParameter.REVISIONDATETO));
        setRevisionDateFrom(jdom.getChildText(SearchParameter.REVISIONDATEFROM));
        setPublicationDateTo(jdom.getChildText(SearchParameter.PUBLICATIONDATETO));
        setPublicationDateFrom(jdom.getChildText(SearchParameter.PUBLICATIONDATEFROM));
        setCreationDateTo(jdom.getChildText(SearchParameter.CREATIONDATETO));
        setCreationDateFrom(jdom.getChildText(SearchParameter.CREATIONDATEFROM));
        setExtTo(jdom.getChildText(SearchParameter.EXTTO));
        setExtFrom(jdom.getChildText(SearchParameter.EXTFROM));
        setMetadataStandardName(jdom.getChildText(SearchParameter.METADATASTANDARDNAME));
        set_schema(jdom.getChildText(SearchParameter._SCHEMA));
        setParentUuid(jdom.getChildText(SearchParameter.PARENTUUID));
        setOperatesOn(jdom.getChildText(SearchParameter.OPERATESON));
        setServiceType(jdom.getChildText(SearchParameter.SERVICETYPE));
        setType(jdom.getChildText(SearchParameter.TYPE));
        setInspire(jdom.getChildText(SearchParameter.INSPIRE));

        @SuppressWarnings("unchecked")
        List<Element> inspireThemesE = (List<Element>)jdom.getChildren(SearchParameter.INSPIRETHEME);
        Set<String> inspireThemes = new HashSet<String>();
        for(Element inspireThemeE : inspireThemesE) {
            inspireThemes.add(inspireThemeE.getText());
        }
        setInspireThemes(inspireThemes);

        setInspireAnnex(jdom.getChildText(SearchParameter.INSPIREANNEX));
        setSiteId(jdom.getChildText(SearchParameter.SITEID));

        @SuppressWarnings("unchecked")
        List<Element> themeKeysE = (List<Element>)jdom.getChildren(SearchParameter.THEMEKEY);
        Set<String> themeKeys = new HashSet<String>();
        for(Element themeKeyE : themeKeysE) {
            themeKeys.add(themeKeyE.getText());
        }
        setThemeKeys(themeKeys);

        setDigital(jdom.getChildText(SearchParameter.DIGITAL));
        setPaper(jdom.getChildText(SearchParameter.PAPER));
        setTitle(jdom.getChildText(SearchParameter.TITLE));
        setAbstrakt(jdom.getChildText(SearchParameter.ABSTRACT));
        setEastBL(jdom.getChildText(SearchParameter.EASTBL));
        setWestBL(jdom.getChildText(SearchParameter.WESTBL));
        setNorthBL(jdom.getChildText(SearchParameter.NORTHBL));
        setSouthBL(jdom.getChildText(SearchParameter.SOUTHBL));
        setRelation(jdom.getChildText(SearchParameter.RELATION));
        setEditable(jdom.getChildText(SearchParameter.EDITABLE));

        setDenominator(jdom.getChildText(SearchParameter.DENOMINATOR));
        setDenominatorFrom(jdom.getChildText(SearchParameter.DENOMINATORFROM));
        setDenominatorTo(jdom.getChildText(SearchParameter.DENOMINATORTO));
        setOrgName(jdom.getChildText(SearchParameter.ORGNAME));
        setSpatialRepresentationType(jdom.getChildText(SearchParameter.SPATIALREPRESENTATIONTYPE));
        @SuppressWarnings("unchecked")
        List<Element> taxonsE = (List<Element>)jdom.getChildren(SearchParameter.TAXON);
        List<String> taxons = new ArrayList<String>();
        for(Element taxonE : taxonsE) {
             taxons.add(taxonE.getText());
        }
        setTaxons(taxons);
        @SuppressWarnings("unchecked")
        List<Element> creditsE = (List<Element>)jdom.getChildren(SearchParameter.CREDIT);
        List<String> credits = new ArrayList<String>();
        for(Element creditE : creditsE) {
             credits.add(creditE.getText());
        }
        setCredits(credits);
        @SuppressWarnings("unchecked")
        List<Element> dataparamsE = (List<Element>)jdom.getChildren(SearchParameter.DATAPARAM);
        List<String> dataparams = new ArrayList<String>();
        for(Element dataparamE : dataparamsE) {
             dataparams.add(dataparamE.getText());
        }
        setDataparams(dataparams);

    }

    /**
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer topicCategoriesToString = new StringBuffer();
        for(String topicCategory : topicCategories) {
            topicCategoriesToString.append(" topicCategory: ").append(topicCategory);
        }
        StringBuffer categoriesToString = new StringBuffer();
        for(String category : categories) {
            categoriesToString.append(" category: ").append(category);
        }
        StringBuffer inspireThemesToString = new StringBuffer();
        for(String inspireTheme : inspireThemes) {
            inspireThemesToString.append(" inspireTheme: ").append(inspireTheme);
        }
        StringBuffer themeKeysToString = new StringBuffer();
        for(String themeKey : themeKeys) {
            themeKeysToString.append(" themeKey: ").append(themeKey);
        }
        return new StringBuffer().append(" similarity: ").append(similarity)
                .append(" uuid: ").append(uuid)
                .append(" any: ").append(any)
                .append(" all: ").append(all)
                .append(" or: ").append(or)
                .append(" without: ").append(without)
                .append(" phrase: ").append(phrase)
                .append(topicCategoriesToString)
                .append(" download: ").append(download)
                .append(" dynamic: ").append(dynamic)
                .append(" protocol: ").append(protocol)
                .append(" featured: ").append(featured)
                .append(categoriesToString)
                .append(" template: ").append(template)
                .append(" dateTo: ").append(dateTo)
                .append(" dateFrom: ").append(dateFrom)
                .append(" revisionDateTo: ").append(revisionDateTo)
                .append(" revisionDateFrom: ").append(revisionDateFrom)
                .append(" publicationDateTo: ").append(publicationDateTo)
                .append(" publicationDateFrom: ").append(publicationDateFrom)
                .append(" creationDateTo: ").append(creationDateTo)
                .append(" creationDateFrom: ").append(creationDateFrom)
                .append(" extTo: ").append(extTo)
                .append(" extFrom: ").append(extFrom)
                .append(" metadataStandardName: ").append(metadataStandardName)
                .append("_schema: ").append(_schema)
                .append(" parentUuid: ").append(parentUuid)
                .append(" operatesOn: ").append(operatesOn)
                .append(" serviceType: ").append(serviceType)
                .append(" type: ").append(type)
                .append(" inspire: ").append(inspire)
                .append(inspireThemesToString)
                .append(" inspireAnnex: ").append(inspireAnnex)
                .append(" siteId: ").append(siteId)
                .append(themeKeysToString)
                .append(" digital: ").append(digital)
                .append(" paper: ").append(paper)
                .append(" title: ").append(title)
                .append(" abstract: ").append(abstrakt)
                .append(" eastBL: ").append(eastBL)
                .append(" westBL: ").append(westBL)
                .append(" northBL: ").append(northBL)
                .append(" southBL: ").append(southBL)
                .append("relation: ").append(relation)
                .append("denominator: ").append(denominator)
                .append("denominatorTo: ").append(denominatorTo)
                .append("denominatorFrom: ").append(denominatorFrom)
                .append("orgName: ").append(orgName)
                .append("spatialRepresentationType: ").append(spatialRepresentationType)
                .toString();
    }    

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAny() {
        return any;
    }

    public void setAny(String any) {
        this.any = any;
    }

    public String getOr() {
        return or;
    }

    public void setOr(String or) {
        this.or = or;
    }

    public String getWithout() {
        return without;
    }

    public void setWithout(String without) {
        this.without = without;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public Set<String> getTopicCategories() {
        return topicCategories;
    }

    public void setTopicCategories(Set<String> topicCategories) {
        if(this.topicCategories == null) {
            this.topicCategories = new HashSet<String>();
        }
        this.topicCategories = topicCategories;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(String dynamic) {
        this.dynamic = dynamic;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getFeatured() {
        return featured;
    }

    public void setFeatured(String featured) {
        this.featured = featured;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        if(this.categories == null) {
            this.categories = new HashSet<String>();
        }
        this.categories = categories;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getRevisionDateTo() {
        return revisionDateTo;
    }

    public void setRevisionDateTo(String revisionDateTo) {
        this.revisionDateTo = revisionDateTo;
    }

    public String getRevisionDateFrom() {
        return revisionDateFrom;
    }

    public void setRevisionDateFrom(String revisionDateFrom) {
        this.revisionDateFrom = revisionDateFrom;
    }

    public String getPublicationDateTo() {
        return publicationDateTo;
    }

    public void setPublicationDateTo(String publicationDateTo) {
        this.publicationDateTo = publicationDateTo;
    }

    public String getPublicationDateFrom() {
        return publicationDateFrom;
    }

    public void setPublicationDateFrom(String publicationDateFrom) {
        this.publicationDateFrom = publicationDateFrom;
    }

    public String getCreationDateTo() {
        return creationDateTo;
    }

    public void setCreationDateTo(String creationDateTo) {
        this.creationDateTo = creationDateTo;
    }

    public String getCreationDateFrom() {
        return creationDateFrom;
    }

    public void setCreationDateFrom(String creationDateFrom) {
        this.creationDateFrom = creationDateFrom;
    }

    public String getExtTo() {
        return extTo;
    }

    public void setExtTo(String extTo) {
        this.extTo = extTo;
    }

    public String getExtFrom() {
        return extFrom;
    }

    public void setExtFrom(String extFrom) {
        this.extFrom = extFrom;
    }

    public String getMetadataStandardName() {
        return metadataStandardName;
    }

    public void setMetadataStandardName(String metadataStandardName) {
        this.metadataStandardName = metadataStandardName;
    }

    public String get_schema() {
        return _schema;
    }

    public void set_schema(String _schema) {
        this._schema = _schema;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getOperatesOn() {
        return operatesOn;
    }

    public void setOperatesOn(String operatesOn) {
        this.operatesOn = operatesOn;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInspire() {
        return inspire;
    }

    public void setInspire(String inspire) {
        this.inspire = inspire;
    }

    public Set<String> getInspireThemes() {
        return inspireThemes;
    }

    public void setInspireThemes(Set<String> inspireThemes) {
        if(this.inspireThemes == null) {
            this.inspireThemes = new HashSet<String>();
        }
        this.inspireThemes = inspireThemes;
    }

    public String getInspireAnnex() {
        return inspireAnnex;
    }

    public void setInspireAnnex(String inspireAnnex) {
        this.inspireAnnex = inspireAnnex;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Set<String> getThemeKeys() {
        return themeKeys;
    }

    public void setThemeKeys(Set<String> themeKeys) {
        if(this.themeKeys == null) {
            this.themeKeys = new HashSet<String>();
        }
        this.themeKeys = themeKeys;
    }

    public String getDigital() {
        return digital;
    }

    public void setDigital(String digital) {
        this.digital = digital;
    }

    public String getPaper() {
        return paper;
    }

    public void setPaper(String paper) {
        this.paper = paper;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstrakt() {
        return abstrakt;
    }

    public void setAbstrakt(String abstrakt) {
        this.abstrakt = abstrakt;
    }

    public String getEastBL() {
        return eastBL;
    }

    public void setEastBL(String eastBL) {
        this.eastBL = eastBL;
    }

    public String getWestBL() {
        return westBL;
    }

    public void setWestBL(String westBL) {
        this.westBL = westBL;
    }

    public String getNorthBL() {
        return northBL;
    }

    public void setNorthBL(String northBL) {
        this.northBL = northBL;
    }

    public String getSouthBL() {
        return southBL;
    }

    public void setSouthBL(String southBL) {
        this.southBL = southBL;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
    
    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }

    public String getDenominator() {
        return denominator;
    }

    public void setDenominator(String denominator) {
        this.denominator = denominator;
    }

    public String getDenominatorFrom() {
        return denominatorFrom;
    }

    public void setDenominatorFrom(String denominatorFrom) {
        this.denominatorFrom = denominatorFrom;
    }

    public String getDenominatorTo() {
        return denominatorTo;
    }

    public void setDenominatorTo(String denominatorTo) {
        this.denominatorTo = denominatorTo;
    }

    public List<String> getTaxons() {
        return taxons;
    }

    public void setTaxons(List<String> taxons) {
        this.taxons = taxons;
    }

    public List<String> getCredits() {
        return credits;
    }

    public void setCredits(List<String> credits) {
        this.credits = credits;
    }

    public List<String> getDataparams() {
        return dataparams;
    }

    public void setDataparams(List<String> dataparams) {
        this.dataparams = dataparams;
    }

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setSpatialRepresentationType(String spatialRepresentationType) {
		this.spatialRepresentationType = spatialRepresentationType;
	}

	public String getSpatialRepresentationType() {
		return spatialRepresentationType;
	}
}
