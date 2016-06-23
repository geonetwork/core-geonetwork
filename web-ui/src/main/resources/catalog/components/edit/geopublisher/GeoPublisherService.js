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

(function() {
  goog.provide('gn_geopublisher_service');


  var module = angular.module('gn_geopublisher_service', [
  ]);

  module.factory('gnGeoPublisher', [
    'gnCurrentEdit',
    '$http',
    function(gnCurrentEdit, $http) {

      return {

        getList: function() {
          return $http.get('../api/mapservers');
        },

        checkNode: function(node, fileName) {
          return $http.get('../api/mapservers/' + node +
              '/records/' + gnCurrentEdit.uuid, {
                params: {
                  resource: fileName
                }});
        },

        publishNode: function(node, fileName,
                              title, moreInfo) {
          return $http.put('../api/mapservers/' + node +
              '/records/' + gnCurrentEdit.uuid, null, {
                params: {
                  metadataTitle: title,
                  metadataAbstract: moreInfo,
                  resource: fileName
                }});
        },

        unpublishNode: function(node, fileName) {
          return $http.delete('../api/mapservers/' + node +
              '/records/' + gnCurrentEdit.uuid, {
                params: {
                  resource: fileName
                }});
        }
      };
    }]);
})();
