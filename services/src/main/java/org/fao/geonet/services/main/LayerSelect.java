package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.LayerSelectionManager;
import org.jdom.Element;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * Select a list of layers stored in session
 * Returns status 
 */

public class LayerSelect implements Service {

    String init_type;
    String init_action;

    public static final String ACTION_GET = "get";
    public static final String ELEMENT_LAYER = "layer";

	public void init(String appPath, ServiceConfig params) throws Exception {

        init_type = params.getValue(Params.TYPE);
        init_action = params.getValue(Params.ACTION);
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {

        String type = Util.getParam(params, Params.TYPE, init_type );
        String action = Util.getParam(params, Params.ACTION, init_action );

        UserSession session = context.getUserSession();
        Element response = new Element(Jeeves.Elem.RESPONSE);

        // Return all layers from all types
        if(action.equals(ACTION_GET)) {

            for(String allType : LayerSelectionManager.getManager(session).getSelections()) {
                Element typeElt =  new Element(allType);
                Set<Hashtable<String, String>> selection =
                        LayerSelectionManager.getManager(session).getSelection(allType);

                if(selection != null && !selection.isEmpty()) {
                    for (Hashtable<String, String> layer : selection) {
                        Element layerElt = new Element(ELEMENT_LAYER);
                        for (String key : layer.keySet()) {
                            layerElt.addContent(new Element(key).setText("" + layer.get(key)));
                        }
                        typeElt.addContent(layerElt);
                    }
                    response.addContent(typeElt);
                }
            }
        }
        // Update the layer selection (add/remove)
        else {
            int nbSelected = LayerSelectionManager.updateSelection(type, session, params, context);
            response.addContent(new Element("LayersSelected").setText(""+nbSelected));
        }

		return response;
	}
}

// =============================================================================

