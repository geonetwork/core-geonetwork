package fr.ifremer.seagis.catalogue.controller;

import java.io.Serializable;
import java.lang.String;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.portlet.PortletSession;

import fr.ifremer.seagis.catalogue.service.CatalogueService;
import fr.ifremer.seagis.catalogue.service.DefaultCatalogueService;
import fr.ifremer.seagis.model.SextantConfig;

/**
 * Controller for the Panier portlet.
 * @author leopratlong
 */
public class CatalogueController implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(CatalogueController.class.getName());
    private static CatalogueService CATALOGUESERVICE = new DefaultCatalogueService();
    
    private String layername;
    private String wmsurl;
    private String layergroup;
    private String wmsversion;
    private String getrecordbyidurl;
    private String maxextent; 
    private String geonetworkurl;
    private String what;
    private String who;
    private String mdviewerurl;
    private String geoviewerurl;
    private String panierurl;
    private String typeSearch;
    private String listThesaurus;
    
    public void sendLayerToGeoviewer() {
        CATALOGUESERVICE.sendLayerToGeoviewer(FacesContext.getCurrentInstance(), wmsurl, wmsversion, layername, layergroup);        
        resetVariable();
    }
    
    public void sendLayerToPanier() {
        CATALOGUESERVICE.sendLayerToPanier(FacesContext.getCurrentInstance(), layername, getrecordbyidurl);
        resetVariable();
    };
    
    private void resetVariable() {
        layername = null;
        wmsurl = null;
        wmsversion = null;
        layergroup = null;
        getrecordbyidurl = null;
    }
    
    private void makeConfig() {
        final SextantConfig sextantConfig = CATALOGUESERVICE.getConfiguration(FacesContext.getCurrentInstance());
        
    	if (sextantConfig != null) {
    		maxextent = CATALOGUESERVICE.getMaxExtentFromString(sextantConfig.getCatalogueWest(), sextantConfig.getCatalogueSouth(), sextantConfig.getCatalogueEast(), sextantConfig.getCatalogueNorth());
            geonetworkurl = sextantConfig.getCatalogueGeonetwork();
            what = sextantConfig.getCatalogueWhat();
            who = sextantConfig.getCatalogueWho();
            mdviewerurl = sextantConfig.getMdViewerUrl();
            geoviewerurl = sextantConfig.getGeoviewerUrl();
            panierurl = sextantConfig.getPanierUrl();
            typeSearch= sextantConfig.getCatalogueTypeSearch();
            listThesaurus = sextantConfig.getCatalogueListThesaurus();
        } else {
            maxextent = null;
            geonetworkurl = null;
            what = null;
            who = null;
            mdviewerurl = null;
            geoviewerurl = null;
            panierurl = null;
	        typeSearch = null;
            listThesaurus = null;
        }
    	
    }
    
    public int getNbPanierLayers() {
        return CATALOGUESERVICE.getNbPanierLayers(FacesContext.getCurrentInstance());
    }
    
    public int getNbViewerLayers() {
        return CATALOGUESERVICE.getNbViewerLayers(FacesContext.getCurrentInstance());
    }
    
    public String getInitCatalogue() {
        makeConfig();
        return null;
    }
    
    public String getTypeSearch() {
        return typeSearch;
    }
    
    public void setTypeSearch(String typeSearch) {
        this.typeSearch = typeSearch;
    }

    public String getListThesaurus() {
        return listThesaurus;
    }

    public void setListThesaurus(String listThesaurus) {
        this.listThesaurus = listThesaurus;
    }

    public String getLayername() {
        return layername;
    }
    
    public void setLayername(String layername) {
        this.layername = layername;
    }
    
    public String getWmsurl() {
        return wmsurl;
    }
    
    public void setWmsurl(String wmsurl) {
        this.wmsurl = wmsurl;
    }
    
    public String getLayergroup() {
        return layergroup;
    }
    
    public void setLayergroup(String layergroup) {
        this.layergroup = layergroup;
    }
    
    public String getWmsversion() {
        return wmsversion;
    }
    
    public void setWmsversion(String wmsversion) {
        this.wmsversion = wmsversion;
    }

    public String getGetrecordbyidurl() {
        return getrecordbyidurl;
    }

    public void setGetrecordbyidurl(String getrecordbyidurl) {
        this.getrecordbyidurl = getrecordbyidurl;
    }

    public String getMaxextent() {
        return maxextent;
    }

    public void setMaxextent(String maxextent) {
        this.maxextent = maxextent;
    }

    public String getGeonetworkurl() {
        return geonetworkurl;
    }

    public void setGeonetworkurl(String geonetworkurl) {
        this.geonetworkurl = geonetworkurl;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getMdviewerurl() {
        return mdviewerurl;
    }

    public void setMdviewerurl(String mdviewerurl) {
        this.mdviewerurl = mdviewerurl;
    }

    public String getGeoviewerurl() {
        return geoviewerurl;
    }

    public void setGeoviewerurl(String geoviewerurl) {
        this.geoviewerurl = geoviewerurl;
    }

    public String getPanierurl() {
        return panierurl;
    }

    public void setPanierurl(String panierurl) {
        this.panierurl = panierurl;
    }
}
