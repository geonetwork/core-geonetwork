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
  goog.provide('gn_profile_service');






  var module = angular.module('gn_profile_service', []);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_viewer.service:gnProfileService
   * @requires gnMap
   * @requires gnOwsCapabilities
   * @requires gnEditor
   * @requires gnViewerSettings
   *
   * @description
   * The `gnProfileService` service provides utility to display profile graphs
   * in the viewer. It can be used to pass data to render from anywhere in
   * the app.
   */
  module.service('gnProfileService', [
    function() {

      this._layer = null;

      /**
       * @ngdoc method
       * @methodOf gn_viewer.service:gnProfileService
       * @name gnProfileService#getOverlayLayer
       *
       * @description
       * This fetches a OL vector layer that is used for profile overlay
       *
       * @param {ol.Map} map open layers map
       * @return {ol.layer.Vector} vector layer
       */
      this.getOverlayLayer = function(map) {
        if (this._layer) {
          return this._layer;
        }

        // layer & source
        var source = new ol.source.Vector({
          useSpatialIndex: true,
          features: new ol.Collection()
        });
        this._layer = new ol.layer.Vector({
          source: source,
          name: 'profile-overlay-layer',
          style: [
            new ol.style.Style({  // this is the default editing style
              fill: new ol.style.Fill({
                color: 'rgba(255, 255, 255, 0.5)'
              }),
              stroke: new ol.style.Stroke({
                color: 'white',
                width: 5
              })
            }),
            new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: 'rgba(0, 255, 80, 1)',
                width: 3
              }),
              image: new ol.style.Circle({
                radius: 6,
                fill: new ol.style.Fill({
                  color: 'rgba(0, 255, 80, 1)'
                }),
                stroke: new ol.style.Stroke({
                  color: 'white',
                  width: 1.5
                })
              })
            })
          ]
        });

        // add our layer to the map
        this._layer.setMap(map);

        return this._layer;
      };

      this._profileGraph = undefined; // this will be watched by the directive
      this._profileGraphOptions = undefined;

      /**
       * Renders JSON data as a graph in the viewer. This will be passed to
       * a gnProfile directive if there is one in the application.
       *
       * @param {Object} jsonData raw JSON graph data
       * @param {Object} graphOptions options used for rendering the graph
       */
      this.displayProfileGraph = function(jsonData, graphOptions) {
        // TODO: check validity
        this._profileGraph = jsonData;
        this._profileGraphOptions = graphOptions;
      };

      // getters; these will be watched by the gnProfile directive
      this.getProfileGraphData = function() {
        return this._profileGraph;
      };
      this.getProfileGraphOptions = function() {
        return this._profileGraphOptions;
      };
    }
  ]);
})();
