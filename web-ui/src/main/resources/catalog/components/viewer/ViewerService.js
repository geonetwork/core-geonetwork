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
  goog.provide('gn_viewer_service');

  var module = angular.module('gn_viewer_service', []);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_viewer.service:gnViewerService
   * @requires $http
   *
   * @description
   * The `gnViewerService` service provides methods to access viewer functions
   * from everywhere in the application.
   */
  module.service('gnViewerService', [
    '$http',
    function($http) {
      // Profile graph handling
      this._profileGraph = undefined; // this will be watched by the directive

      /**
       * Renders JSON data as a graph in the viewer. The expected format is:
       * {
       *   "profile": [{
       *       "lat": 48.38589820151259,
       *       "dist": 0.0,
       *       "lon": -4.4995880126953125,
       *       "values": {
       *           "z": 23.0
       *       }
       *   }, {
       *      ...
       *   }]
       * }
       *
       * @param  {type} rawData raw graph data
       */
      this.displayProfileGraph = function(rawData) {
        // TODO: check validity
        this._profileGraph = rawData;
      };

      this.getProfileGraphData = function() {
        return this._profileGraph;
      };
      this.clearProfileGraph = function() {
        return this._profileGraph = undefined;
      };

      this.activeTool = {
        name: '',
        tab: '',
        url: ''
      };

      /**
       * This will open a tool in the viewer (and close the others).
       * Use this exclusively instead of DOM manipulation.
       * Note: this will not change the window URL
       *
       * @param {string} toolName addLayers, layers, filter, processes,
       *  contexts, print, measure, annotations
       * @param {string} tabName optional, tab name
       * @param {string} url optional, url parameter
       */
      this.openTool = function(toolName, tabName, url) {
        this.activeTool.name = toolName || '';
        this.activeTool.tab = tabName || '';
        this.activeTool.url = url || '';
      };

      /**
       * This will be watched by the directive so that it can change its opened
       * tool and adjust properties inside it.
       * @return {object} return an object with properties name, url, tab
       *  (for the tabs that support it)
       */
      this.getOpenedTool = function() {
        // fetch correct url according to the opened tool
        return this.activeTool;
      };

      /**
       * Opens the "Add Layer" tab, switch to WMS and specify URL (optional)
       * @param {string} serviceUrl optional, do GetCap on specified URL
       */
      this.openWmsTab = function(serviceUrl) {
        this.openTool('addLayers', 'wms', serviceUrl);
      };

      /**
       * Opens the "Add Layer" tab, switch to WMTS and specify URL (optional)
       * @param {string} serviceUrl optional, do GetCap on specified URL
       */
      this.openWmtsTab = function(serviceUrl) {
        this.openTool('addLayers', 'wmts', serviceUrl);
      };

      /**
       * Opens the "WPS Processes" tab, and specify URL (optional)
       * @param {string} serviceUrl optional, do GetCap on specified URL
       */
      this.openProcessesTool = function(serviceUrl) {
        this.openTool('processes', '', serviceUrl);
      };
    }]
  );
})();
