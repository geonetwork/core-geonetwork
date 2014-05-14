package org.fao.geonet.kernel;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Manage objects selection for a user session.
 */
public class LayerSelectionManager {

	private Hashtable<String, Set<Hashtable<String, String>>> selections = null;
	private UserSession session = null;

	public static final String SELECTION_METADATA = "metadata";

	private static final String REMOVE_ALL_SELECTED = "remove-all";
	private static final String ADD_SELECTED = "add";
	private static final String REMOVE_SELECTED = "remove";
	private static final String CLEAR_ADD_SELECTED = "clear-add";

	private LayerSelectionManager(UserSession session) {
		selections = new Hashtable<String, Set<Hashtable<String, String>>>(0);
		this.session = session;
	}


	/**
	 * <p>
	 * Updates selected element in session.
	 * <ul>
	 * <li>[selected=add] : add selected element</li>
	 * <li>[selected=remove] : remove non selected element</li>
	 * <li>[selected=remove-all] : clear the selection</li>
	 * <li>[selected=clear-add] : clear the selection and add selected element</li>
	 * <li>[selected=status] : number of selected elements</li>
	 * </ul>
	 * </p>
	 * 
	 * @param type
	 *            The type of selected element handled in session
	 * @param session
	 *            Current session
	 * @param params
	 *            Parameters
	 * @param context
	 * 
	 * @return number of selected elements
	 */
	public static int updateSelection(String type, UserSession session, Element params, ServiceContext context) {

		String selected = params.getChildText(Params.SELECTED);
        String layerName = params.getChildText(Params.NAME);
        String descr = params.getChildText(Params.DESCRIPTION);
        String version = params.getChildText(Params.VERSION);
        String serviceUrl = params.getChildText(Params.URL);

		// Get the layer selection manager or create it
		LayerSelectionManager manager = getManager(session);

		return manager.updateSelection(type, context, selected, layerName, serviceUrl, descr, version);
	}

	/**
	 * <p>
	 * Update selected element in session
	 * </p>
	 * 
	 * @param type
	 *            The type of selected element handled in session
	 * @param context
     * @param selected true, false, single, all, none
	 * @param serviceUrl url of the OWS service
     * @param layerDescr description of the layer
     * @param serviceVersion versice of the service (WMS_1.3.0)
     *
     * @return number of selected element
	 */
	public int updateSelection(String type, ServiceContext context, String selected,
                               String layerName, String serviceUrl, String layerDescr, String serviceVersion) {

		// Get the selection manager or create it
		Set<Hashtable<String, String>> selection = this.getSelection(type);
		if (selection == null) {
		    selection = Collections
                    .synchronizedSet(new HashSet<Hashtable<String, String>>());
			this.selections.put(type, selection);
		}

        if (selected != null) {
            if (selected.equals(REMOVE_ALL_SELECTED))
                this.close(type);
            else if (selected.equals(ADD_SELECTED) && (layerName != null))
                selection.add(createLayerElement(layerName, serviceUrl, layerDescr, serviceVersion));
            else if (selected.equals(REMOVE_SELECTED) && (layerName != null))
                selection.remove(createLayerElement(layerName, serviceUrl, layerDescr, serviceVersion));
            else if (selected.equals(CLEAR_ADD_SELECTED) && (layerName != null)) {
                this.close(type);
                selection.add(createLayerElement(layerName, serviceUrl, layerDescr, serviceVersion));
            }
        }

        return selection.size();
    }

    private Hashtable<String, String> createLayerElement(String layerName,
                    String serviceUrl, String layerDescr, String serviceVersion) {

        Hashtable<String, String> layer = new Hashtable<String, String>(0);
        layer.put(Params.NAME, layerName);
        layer.put(Params.URL, serviceUrl);
        layer.put(Params.DESCRIPTION, layerDescr);
        layer.put(Params.VERSION, serviceVersion);

        return layer;
    }

    /**
	 * <p>
	 * Gets selection manager in session, if null creates it.
	 * </p>
	 * 
	 * @param session
	 *            Current user session
	 * @return selection manager
	 */
	@Nonnull
	public static LayerSelectionManager getManager(UserSession session) {
		LayerSelectionManager manager = (LayerSelectionManager) session.getProperty(Geonet.Session.SELECTED_LAYER_RESULT);
		if (manager == null) {
			manager = new LayerSelectionManager(session);
			session.setProperty(Geonet.Session.SELECTED_LAYER_RESULT, manager);
		}
		return manager;
	}


	/**
	 * <p>
	 * Closes the current selection manager for the given element type.
	 * </p>
	 * 
	 * @param type
	 */
	public void close(String type) {
		Set<Hashtable<String, String>> selection = selections.get(type);
		if (selection != null)
			selection.clear();
	}

	/**
	 * <p>
	 * Close the current selection manager
	 * </p>
	 * 
	 */
	public void close() {
        for (Set<Hashtable<String, String>> selection : selections.values()) {
            selection.clear();
        }
	}

	/**
	 * <p>
	 * Gets selection for given element type.
	 * </p>
	 * 
	 * @param type
	 *            The type of selected element handled in session
	 * 
	 * @return Set<Hashtable<String, String>>
	 */
	public Set<Hashtable<String, String>> getSelection(String type) {
		return selections.get(type);
	}

    /**
     * <p>
     * Gets all types of selection.
     * </p>
     *
     *
     * @return Set<String>
     */
    public Set<String> getSelections () {
        return selections.keySet();
    }

}