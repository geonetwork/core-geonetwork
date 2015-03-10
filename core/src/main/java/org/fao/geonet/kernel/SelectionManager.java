package org.fao.geonet.kernel;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetadataRecordSelector;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.jdom.Element;

import java.util.*;

import javax.annotation.Nonnull;

/**
 * Manage objects selection for a user session.
 */
public class SelectionManager {

	private Hashtable<String, Set<String>> selections = null;

	public static final String SELECTION_METADATA = "metadata";

	// used to limit select all if get system setting maxrecords fails or contains value we can't parse
	public static final int DEFAULT_MAXHITS = 1000;

    private static final String ADD_ALL_SELECTED = "add-all";
	public static final String REMOVE_ALL_SELECTED = "remove-all";
	private static final String ADD_SELECTED = "add";
	private static final String REMOVE_SELECTED = "remove";
	private static final String CLEAR_ADD_SELECTED = "clear-add";

	private SelectionManager() {
		selections = new Hashtable<String, Set<String>>(0);

		Set<String> MDSelection = Collections
				.synchronizedSet(new HashSet<String>(0));
		selections.put(SELECTION_METADATA, MDSelection);
	}

	/**
	 * <p>
	 * Update result elements to present. </br>
	 * <ul>
	 * <li>set selected true if result element in session</li>
	 * <li>set selected false if result element not in session</li>
	 * </ul>
	 * </p>
	 * 
	 * @param result
	 *            the result modified<br/>
	 * 
	 * @see org.fao.geonet.services.main.Result <br/>
	 */
	public static void updateMDResult(UserSession session, Element result) {
		SelectionManager manager = getManager(session);
		@SuppressWarnings("unchecked")
        List<Element> elList = result.getChildren();

		Set<String> selection = manager.getSelection(SELECTION_METADATA);

        for (Element element : elList) {
            if (element.getName().equals(Geonet.Elem.SUMMARY)) {
                continue;
            }
            Element info = element.getChild(Edit.RootChild.INFO,
                    Edit.NAMESPACE);
            String uuid = info.getChildText(Edit.Info.Elem.UUID);
            if (selection.contains(uuid)) {
                info.addContent(new Element(Edit.Info.Elem.SELECTED)
                        .setText("true"));
            }
            else {
                info.addContent(new Element(Edit.Info.Elem.SELECTED)
                        .setText("false"));
            }
        }
		result.setAttribute(Edit.Info.Elem.SELECTED, Integer
				.toString(selection.size()));
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
		List<Element> listOfIdentifiersElement = params.getChildren(Params.ID);
		List<String> listOfIdentifiers = new ArrayList<>(listOfIdentifiersElement.size());
		for (Element e : listOfIdentifiersElement) {
			listOfIdentifiers.add(e.getText());
		}

		String selected = params.getChildText(Params.SELECTED);

		// Get the selection manager or create it
		SelectionManager manager = getManager(session);

		return manager.updateSelection(type, context, selected, listOfIdentifiers, session);
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
	 * @param listOfIdentifiers
	 *            Array of UUIDs
	 * 
	 * @return number of selected element
	 */
	public int updateSelection(String type,
							   ServiceContext context,
							   String selected,
							   List<String> listOfIdentifiers,
							   UserSession session) {

		// Get the selection manager or create it
		Set<String> selection = this.getSelection(type);
		if (selection == null) {
		    selection = Collections.synchronizedSet(new HashSet<String>());
			this.selections.put(type, selection);
		}

        if (selected != null) {
            if (selected.equals(ADD_ALL_SELECTED))
                this.selectAll(type, context, session);
            else if (selected.equals(REMOVE_ALL_SELECTED))
                this.close(type);
            else if (selected.equals(ADD_SELECTED) && listOfIdentifiers.size() > 0) {
				for (String paramid : listOfIdentifiers) {
					selection.add(paramid);
				}
			} else if (selected.equals(REMOVE_SELECTED) && listOfIdentifiers.size() > 0) {
				for (String paramid : listOfIdentifiers) {
					selection.remove(paramid);
				}
			} else if (selected.equals(CLEAR_ADD_SELECTED) && listOfIdentifiers.size() > 0) {
                this.close(type);
				for (String paramid : listOfIdentifiers) {
					selection.add(paramid);
				}
            }
        }

		// Remove empty/null element from the selection
        Iterator<String> iter = selection.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element == null)
                iter.remove();
        }

        return selection.size();
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
	public static SelectionManager getManager(UserSession session) {
		SelectionManager manager = (SelectionManager) session.getProperty(Geonet.Session.SELECTED_RESULT);
		if (manager == null) {
			manager = new SelectionManager();
			session.setProperty(Geonet.Session.SELECTED_RESULT, manager);
		}
		return manager;
	}

	/**
	 * <p>
	 * Selects all element in a LuceneSearcher or CatalogSearcher.
	 * </p>
	 * 
	 * @param type
	 * @param context
	 * 
	 */
	public void selectAll(String type, ServiceContext context, UserSession session) {
		Set<String> selection = selections.get(type);
		SettingInfo si = context.getBean(SettingInfo.class);
		int maxhits = DEFAULT_MAXHITS;

		try {
			maxhits = Integer.parseInt(si.getSelectionMaxRecords());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (selection != null)
			selection.clear();

		if (type.equals(SELECTION_METADATA)) {
		    Element request = (Element)session.getProperty(Geonet.Session.SEARCH_REQUEST);
			Object searcher = null;
			
			// Run last search if xml.search or q service is used (ie. last searcher is not stored in current session).
			if (request != null) {
	            request = (Element) request.clone();
	            request.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).setText("false"));
				GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
				SearchManager searchMan = gc.getBean(SearchManager.class);
				try {
					searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
					ServiceConfig sc = new ServiceConfig();
					((LuceneSearcher)searcher).search(context, request, sc);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				searcher = session.getProperty(Geonet.Session.SEARCH_RESULT);
			}
			if (searcher == null)
				return;

			List<String> uuidList;
			try {
				if (searcher instanceof MetadataRecordSelector)
					uuidList = ((MetadataRecordSelector) searcher).getAllUuids(maxhits, context);
				else
					return;

                if (selection != null) {
                    selection.addAll(uuidList);
                }

            } catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>
	 * Closes the current selection manager for the given element type.
	 * </p>
	 * 
	 * @param type
	 */
	public void close(String type) {
		Set<String> selection = selections.get(type);
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
        for (Set<String> selection : selections.values()) {
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
	 * @return Set<String>
	 */
	public Set<String> getSelection(String type) {
		return selections.get(type);
	}

	/**
	 * <p>
	 * Adds new element to the selection.
	 * </p>
	 * 
	 * @param type
	 *            The type of selected element handled in session
	 * @param uuid
	 *            Element identifier to select
	 * 
	 * @return boolean
	 */
	public boolean addSelection(String type, String uuid) {
		return selections.get(type).add(uuid);
	}

	/**
	 * <p>
	 * Adds a collection to the selection.
	 * </p>
	 * 
	 * @param type
	 *            The type of selected element handled in session
	 * @param uuids
	 *            Collection of uuids to select
	 * 
	 * @return boolean
	 */
	public boolean addAllSelection(String type, Set<String> uuids) {
		return selections.get(type).addAll(uuids);
	}

}