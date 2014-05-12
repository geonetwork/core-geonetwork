package org.fao.geonet.kernel;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetadataRecordSelector;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.util.MarkupParserCache;
import org.fao.geonet.util.XslUtil;
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
	 * <li>[selected=add-all] : select all elements</li>
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

		// Get ID of the selected/deselected metadata
		String paramid = params.getChildText(Params.ID);
		String selected = params.getChildText(Params.SELECTED);
        String layerName = params.getChildText(Params.LAYER_NAME);

		// Get the selection manager or create it
		LayerSelectionManager manager = getManager(session);

		return manager.updateSelection(type, context, selected, paramid, layerName);
	}

	/**
	 * <p>
	 * Update selected element in session
	 * </p>
	 * 
	 * @param type
	 *            The type of selected element handled in session
	 * @param context
     * @param selected
	 *            true, false, single, all, none
	 * @param paramid
	 *            id of the selected element
	 * 
	 * @return number of selected element
	 */
	public int updateSelection(String type, ServiceContext context, String selected,
                String paramid, String layerName) {

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
            else if (selected.equals(ADD_SELECTED) && (paramid != null))
                selection.add(createLayerElement(layerName,paramid));
            else if (selected.equals(REMOVE_SELECTED) && (paramid != null))
                selection.remove(createLayerElement(layerName,paramid));
            else if (selected.equals(CLEAR_ADD_SELECTED) && (paramid != null)) {
                this.close(type);
                selection.add(createLayerElement(layerName,paramid));
            }
        }

        return selection.size();
    }

    private Hashtable<String, String> createLayerElement(String layerName, String paramid) {
        Hashtable<String, String> layer = new Hashtable<String, String>(0);
        layer.put(Params.LAYER_NAME, layerName);
        layer.put(Params.UUID, paramid);

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