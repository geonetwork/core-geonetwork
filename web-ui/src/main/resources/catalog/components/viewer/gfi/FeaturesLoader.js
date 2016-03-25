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
  goog.provide('gn_featurestable_loader');

  var module = angular.module('gn_featurestable_loader', []);

  geonetwork.inherits = function(childCtor, parentCtor) {
    function tempCtor() {
    };
    tempCtor.prototype = parentCtor.prototype;
    childCtor.superClass_ = parentCtor.prototype;
    childCtor.prototype = new tempCtor();
    childCtor.prototype.constructor = childCtor;
  };



  /**
   * @abstract
   * @constructor
   */
  geonetwork.GnFeaturesLoader = function(config, $injector) {
    this.$injector = $injector;
    this.$http = this.$injector.get('$http');
    this.gnProxyUrl =  this.$injector.get('gnGlobalSettings').proxyUrl;
    this.data;
  };
  geonetwork.GnFeaturesLoader.prototype.load = function(){};
  geonetwork.GnFeaturesLoader.prototype.loadAll = function(){};

  geonetwork.GnFeaturesLoader.prototype.proxyfyUrl = function(url){
    return this.gnProxyUrl + encodeURIComponent(url);
  };

  /**
   *
   * @constructor
   */
  geonetwork.GnFeaturesGFILoader = function(config, $injector) {

    geonetwork.GnFeaturesLoader.call(this, config, $injector);

    this.layer = config.layer;
    this.map = config.map;
    this.coordinates = config.coordinates;
  };

  geonetwork.inherits(geonetwork.GnFeaturesGFILoader,
      geonetwork.GnFeaturesLoader);

  geonetwork.GnFeaturesGFILoader.prototype.loadAll = function() {
    var layer = this.layer,
        map = this.map,
        coordinates = this.coordinates;

    var uri = layer.getSource().getGetFeatureInfoUrl(coordinates,
        map.getView().getResolution(),
        map.getView().getProjection(), {
          INFO_FORMAT: layer.ncInfo ? 'text/xml' :
              'application/vnd.ogc.gml'
        });

    var proxyUrl = this.proxyfyUrl(uri);
    return this.$http.get(proxyUrl).then(function(response) {
      var format = new ol.format.WMSGetFeatureInfo();
      var features = format.readFeatures(response.data);
      return features;
    });


  };

  /**
   *
   * @constructor
   */
  geonetwork.GnFeaturesSOLRLoader = function(config, $injector) {
  };
  geonetwork.inherits(geonetwork.GnFeaturesSOLRLoader,
      geonetwork.GnFeaturesLoader);


  /**
   *
   * @constructor
   */
  var GnFeaturesTableLoaderService = function($injector) {
    this.$injector = $injector;
  };
  GnFeaturesTableLoaderService.prototype.createLoader = function(type, config) {
    var constructor = geonetwork['GnFeatures' + type.toUpperCase() + 'Loader'];
    if(!angular.isFunction(constructor)) {
      console.warn('Cannot find constructor for loader type : ' + type);
    }
    return new constructor(config, this.$injector);
  };
  module.service('gnFeaturesTableLoader', [
    '$injector',
    GnFeaturesTableLoaderService]);

})();
