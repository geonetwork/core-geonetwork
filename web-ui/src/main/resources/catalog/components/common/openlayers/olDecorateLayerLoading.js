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

(function () {

  goog.provide('gn_olDecorateLayerLoading')

  var module = angular.module('gn_olDecorateLayerLoading', [])

  /**
   * Provides a function that adds a 'loading 'property (using
   * `Object.defineProperty`) to an ol.layer.Group or a layer with
   * an ol.source.Tile or an ol.source.Image source.
   * This property is true when the layer is loading and false otherwise.
   *
   * Example:
   *
   *      <span ng-if="layer.loading">please wait</span>
   *
   * @typedef {function(ol.layer.Base, angular.Scope)}
   * @ngdoc service
   * @ngname ngeoDecorateLayerLoading
   */

  /**
   * @param {ol.layer.Base} layer Layer to decorate.
   * @param {angular.Scope} $scope Scope.
   */
  var decorateLayerLoading = function(layer, $scope) {

    var source;

    /**
     * @type {Array<string>|null}
     */
    var incrementEvents = null;

    /**
     * @type {Array<string>|null}
     */
    var decrementEvents = null;

    /**
     * @function
     * @private
     */
    var incrementLoadCount_ = increment_;

    /**
     * @function
     * @private
     */
    var decrementLoadCount_ = decrement_;

    layer.set('load_count', 0, true);

    if (layer instanceof ol.layer.Group) {
      layer.getLayers().on('add', function(olEvent) {
        var newLayer = olEvent.element;
        newLayer.set('parent_group', layer);
      });
    }

    if (layer instanceof ol.layer.Layer) {
      source = layer.getSource();
      if (source === null) {
        return;
      } else if (source instanceof ol.source.Tile) {
        incrementEvents = ['tileloadstart'];
        decrementEvents = ['tileloadend', 'tileloaderror'];
      } else if (source instanceof ol.source.Image) {
        incrementEvents = ['imageloadstart'];
        decrementEvents = ['imageloadend', 'imageloaderror'];
      } else {
        goog.asserts.fail('unsupported source type');
      }

      source.on(incrementEvents, function() {
        incrementLoadCount_(layer);
        $scope.$applyAsync();
      });

      source.on(decrementEvents, function() {
        decrementLoadCount_(layer);
        $scope.$applyAsync();
      });
    }

    Object.defineProperty(layer, 'loading', {
      configurable: true,
      get:
        /**
         * @return {boolean} Loading.
         */
        function() {
          return /** @type {number} */ (layer.get('load_count')) > 0;
        }
    });

    /**
     * @function
     * @param {ol.layer.Base} layer Layer
     * @private
     */
    function increment_(layer) {
      var load_count = layer.get('load_count');
      var parent = layer.get('parent_group');
      layer.set('load_count', ++load_count, true);
      if (parent) {
        increment_(parent);
      }
    }

    /**
     * @function
     * @param {ol.layer.Base} layer Layer
     * @private
     */
    function decrement_(layer) {
      var load_count = layer.get('load_count');
      var parent = layer.get('parent_group');
      layer.set('load_count', --load_count, true);
      if (parent) {
        decrement_(parent);
      }
    }
  };

  module.value('olDecorateLayerLoading', decorateLayerLoading)
})()
