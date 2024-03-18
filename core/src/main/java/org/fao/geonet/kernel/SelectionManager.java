/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_UUID;

/**
 * Manage objects selection for a user session.
 */
public class SelectionManager {

    public static final String SELECTION_METADATA = "metadata";
    public static final String SELECTION_BUCKET = "bucket";
    // used to limit select all if get system setting maxrecords fails or contains value we can't parse
    public static final int DEFAULT_MAXHITS = 1000;
    public static final String ADD_ALL_SELECTED = "add-all";
    public static final String REMOVE_ALL_SELECTED = "remove-all";
    public static final String ADD_SELECTED = "add";
    public static final String REMOVE_SELECTED = "remove";
    public static final String CLEAR_ADD_SELECTED = "clear-add";
    private Hashtable<String, Set<String>> selections = null;

    private SelectionManager() {
        selections = new Hashtable<String, Set<String>>(0);

        Set<String> MDSelection = Collections
            .synchronizedSet(new HashSet<String>(0));
        selections.put(SELECTION_METADATA, MDSelection);
    }


    public Map<String, Integer> getSelectionsAndSize() {
        return selections.entrySet().stream().collect(Collectors.toMap(
            e -> e.getKey(),
            e -> e.getValue().size()
        ));
    }

    /**
     * <p> Update result elements to present. </br> <ul> <li>set selected true if result element in
     * session</li> <li>set selected false if result element not in session</li> </ul> </p>
     *
     * @param result the result modified<br/>
     */
    public static void updateMDResult(UserSession session, Element result) {
        updateMDResult(session, result, SELECTION_METADATA);
    }
    public static void updateMDResult(UserSession session, Element result, String bucket) {
        SelectionManager manager = getManager(session);
        @SuppressWarnings("unchecked")
        List<Element> elList = result.getChildren();

        Set<String> selection = manager.getSelection(bucket);

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
            } else {
                info.addContent(new Element(Edit.Info.Elem.SELECTED)
                    .setText("false"));
            }
        }
        result.setAttribute(Edit.Info.Elem.SELECTED,
            selection == null ? "0" : Integer.toString(selection.size()));
    }

    /**
     * <p> Updates selected element in session. <ul> <li>[selected=add] : add selected element</li>
     * <li>[selected=remove] : remove non selected element</li> <li>[selected=add-all] : select all
     * elements</li> <li>[selected=remove-all] : clear the selection</li> <li>[selected=clear-add] :
     * clear the selection and add selected element</li> <li>[selected=status] : number of selected
     * elements</li> </ul> </p>
     *
     * @param type    The type of selected element handled in session
     * @param session Current session
     * @param params  Parameters
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

    public static int updateSelection(String type, UserSession session, String actionOnSelection, List<String> listOfIdentifiers, ServiceContext context) {
        // Get the selection manager or create it
        SelectionManager manager = getManager(session);

        return manager.updateSelection(type, context, actionOnSelection, listOfIdentifiers, session);
    }

    /**
     * <p> Gets selection manager in session, if null creates it. </p>
     *
     * @param session Current user session
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
     * <p> Update selected element in session </p>
     *
     * @param type              The type of selected element handled in session
     * @param selected          true, false, single, all, none
     * @param listOfIdentifiers Array of UUIDs
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
                // TODO ? Should we check that the element exist first ?
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
     * <p> Selects all element in the last search
     * which is stored in session based on the bucket name.
     * Sends the query with a max hits and build a collection of UUIDs.</p>
     */
    public void selectAll(String type, ServiceContext context, UserSession session) {
        Set<String> selection = selections.get(type);
        int maxhits = DEFAULT_MAXHITS;
        SettingInfo settingInfo = ApplicationContextHolder.get().getBean(SettingInfo.class);
        try {
            maxhits = Integer.parseInt(settingInfo.getSelectionMaxRecords());
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Select all - invalid max hits value, error: " + e.getMessage(), e);
        }

        if (selection != null) {
            selection.clear();
        }

        if (StringUtils.isNotEmpty(type)) {
            JsonNode request = (JsonNode) session.getProperty(Geonet.Session.SEARCH_REQUEST + type);
            if (request == null) {
                return;
            } else {
                final SearchResponse searchResponse;
                try {
                    EsSearchManager searchManager = context.getBean(EsSearchManager.class);
                    searchResponse = searchManager.query(request.get("query"), FIELDLIST_UUID, 0, maxhits);
                    List<String> uuidList = new ArrayList();
                    ObjectMapper objectMapper = new ObjectMapper();
                    for (Hit h : (List<Hit>) searchResponse.hits().hits()) {
                        uuidList.add((String) objectMapper.convertValue(h.source(), Map.class).get(Geonet.IndexFieldNames.UUID));
                    }

                    if (selection != null) {
                        selection.addAll(uuidList);
                    }
                } catch (Exception e) {
                    Log.error(Geonet.GEONETWORK,
                        "Select all - query error: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * <p> Closes the current selection manager for the given element type. </p>
     */
    public void close(String type) {
        Set<String> selection = selections.get(type);
        if (selection != null)
            selection.clear();
    }

    /**
     * <p> Close the current selection manager </p>
     */
    public void close() {
        for (Set<String> selection : selections.values()) {
            selection.clear();
        }
    }

    /**
     * <p> Gets selection for given element type. </p>
     *
     * @param type The type of selected element handled in session
     * @return Set<String>
     */
    public Set<String> getSelection(String type) {
        Set<String> sel = selections.get(type);
        if (sel == null) {
            Set<String> MDSelection = Collections
                .synchronizedSet(new HashSet<String>(0));
            selections.put(type, MDSelection);
        }
        return selections.get(type);
    }

    /**
     * <p> Adds new element to the selection. </p>
     *
     * @param type The type of selected element handled in session
     * @param uuid Element identifier to select
     * @return boolean
     */
    public boolean addSelection(String type, String uuid) {
        return selections.get(type).add(uuid);
    }

    /**
     * <p> Adds a collection to the selection. </p>
     *
     * @param type  The type of selected element handled in session
     * @param uuids Collection of uuids to select
     * @return boolean
     */
    public boolean addAllSelection(String type, Set<String> uuids) {
        return selections.get(type).addAll(uuids);
    }

}
