package fr.ifremer.seagis.catalogue.service;

import javax.faces.context.FacesContext;

import fr.ifremer.seagis.common.service.CommonService;

public interface CatalogueService extends CommonService {
    
    void sendLayerToGeoviewer(final FacesContext context, final String wmsurl, final String wmsversion, final String layername, final String layergroup);

    void sendLayerToPanier(final FacesContext context, final String layername, final String getrecordbyidurl);
    
    int getNbPanierLayers(final FacesContext context);
    
    int getNbViewerLayers(final FacesContext context);
}
