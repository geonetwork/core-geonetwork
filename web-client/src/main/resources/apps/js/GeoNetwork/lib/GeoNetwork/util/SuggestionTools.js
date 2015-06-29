/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
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
Ext.namespace("GeoNetwork.util");

/** api: (define)
 *  module = GeoNetwork.util
 *  class = SuggestionTools
 */
/** api: example
 *  SuggestionTools updates the suggestion value in a form field, managing multilingual fields.
 *
 *
 *  .. code-block:: javascript
 *
 *   GeoNetwork.util.SuggestionTools.updateSuggestion(suggestion, refId, multilingualRefIds);
 *
 *  ...
 *
 */

GeoNetwork.util.SuggestionTools = {
  /** api: method[updateSuggestion]
   *  Updates the visible form field for a metadata element with the suggestion provided. Manages multilingual elements.
   *
   * :param value: Suggestion value.
   * :param refId: Identifier of the field to update.
   * :param multilingualRefIds: Comma separated list of identifier of the multilingual
   *                            field for a metadata element to update.
   */
  updateSuggestion: function (value, refId, multilingualRefIds) {
    var isVisible = (Ext.getDom("_" + refId).style.display != "none");

    if (isVisible) {
      Ext.getDom("_" + refId).value=value;
      if (Ext.getDom("_" + refId).onkeyup) Ext.getDom("_" + refId).onkeyup();
    } else {
      var relatedIdsList = multilingualRefIds.split(",");
      for (var i = 0; i < relatedIdsList.length; i++) {
        if (relatedIdsList[i] == '') continue;
        var refId = "_" + relatedIdsList[i];
        var isVisible = (Ext.getDom(refId).style.display != "none");

        if (isVisible) {
          Ext.getDom(refId).value=value;
          if (Ext.getDom(refId).onkeyup) Ext.getDom(refId).onkeyup();
        }

      }

    }
  }
};