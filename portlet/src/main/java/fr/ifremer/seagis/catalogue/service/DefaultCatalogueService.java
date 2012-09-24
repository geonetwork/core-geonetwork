package fr.ifremer.seagis.catalogue.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.mapfaces.utils.FacesPortletUtils;

import fr.ifremer.seagis.common.service.DefaultCommonService;

public class DefaultCatalogueService extends DefaultCommonService implements CatalogueService {

    private static final String GEOVIEWER_KEY = "LIFERAY_SHARED_GEOVIEWER";
  
    public void sendLayerToGeoviewer(final FacesContext context, final String wmsurl, final String wmsversion, final String layername, final String layergroup) {
        if ((wmsurl == null) || (wmsversion == null) || (layername == null)) return;
        final String currentCommunity = getCurrentCommunity(context);
        Map<String, List<Map<String, String>>> geoviewerIpc = (Map<String, List<Map<String, String>>>) FacesPortletUtils.getPublicPortletSessionAttribute(context, GEOVIEWER_KEY);

        if (geoviewerIpc == null) {
            geoviewerIpc = new HashMap<String, List<Map<String,String>>>();
            FacesPortletUtils.setPublicPortletSessionAttribute(context, GEOVIEWER_KEY, geoviewerIpc);
        }
        
        List<Map<String, String>> geoviewerCommunity = geoviewerIpc.get(currentCommunity);
        if (geoviewerCommunity == null) {
            geoviewerCommunity = new ArrayList<Map<String,String>>();
            geoviewerIpc.put(currentCommunity, geoviewerCommunity);
        }
        boolean find = false;
        
        for (final Map<String, String> layer : geoviewerCommunity) {
            if (layername.equals(layer.get("layername")) && wmsurl.equals(layer.get("wmsurl"))) {
                find = true;
                break;
            }
        }

        if (!find) {
            // ajout des param�tres de la couche
            final Map<String, String> layer = new HashMap<String, String>();
            layer.put("layername", layername);
            layer.put("wmsurl", wmsurl);
            final String wmsversionTmp;
            if (wmsversion.startsWith("WMS_")) {
                wmsversionTmp = wmsversion.substring(4);
            } else {
                wmsversionTmp = wmsversion;
            }
            layer.put("wmsversion", wmsversionTmp);
            layer.put("layergroup", layergroup);
            geoviewerCommunity.add(layer);
        }
    }
    
    public void sendLayerToPanier(final FacesContext context, final String layername, final String getrecordbyidurl) {
        if ((layername == null) || (getrecordbyidurl == null)) return;
        final String currentCommunity = getCurrentCommunity(context);
        if (currentCommunity == null) return;
        Map<String, Map<String, List<String>>> panier = (Map<String, Map<String, List<String>>>) FacesPortletUtils.getPublicPortletSessionAttribute(context, PANIERKEY);
        if (panier == null) {
            panier = new HashMap<String, Map<String, List<String>>>();
            FacesPortletUtils.setPublicPortletSessionAttribute(context, PANIERKEY, panier);
        }
        /**
         * check panier for this community :
         */
        Map<String, List<String>> panierCommunity = panier.get(currentCommunity);
        if (panierCommunity == null) {
            panierCommunity = new HashMap<String, List<String>>();
            panier.put(currentCommunity, panierCommunity);
        }
        if (panierCommunity.containsKey(getrecordbyidurl)) {
            final List<String> names = panierCommunity.get(getrecordbyidurl);
            if (!names.contains(layername)) names.add(layername);
        } else {
            final List<String> names = new ArrayList<String>();
            names.add(layername);
            panierCommunity.put(getrecordbyidurl, names);
        }
    }
    
    public int getNbPanierLayers(final FacesContext context) {
        int count = 0;
        final String currentCommunity = getCurrentCommunity(context);
        if (currentCommunity != null) {
            final Map<String, List<String>> panier = getItemsFromBasketForCommunity(context, currentCommunity);
            if (panier != null) {
                for (final List<String> layers : panier.values()) {
                    count = count + layers.size();
                }
            }
        }
        return count;
    }

    public int getNbViewerLayers(final FacesContext context) {
        int count = 0;
        final Map<String, List<Map<String, String>>> globalGeoviewer = (Map<String, List<Map<String, String>>>) FacesPortletUtils.getPublicPortletSessionAttribute(context, "LIFERAY_SHARED_GEOVIEWER");
        final String currentCommunity = getCurrentCommunity(context);
        if ((globalGeoviewer != null) && (currentCommunity != null)) {
            final List<Map<String, String>> geoviewer = globalGeoviewer.get(currentCommunity);
            count = geoviewer.size();
        }
        return count;
    }
}
