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
  goog.provide('gn_index_requestmanager');

  goog.require('gn_index_request');
  goog.require('gn_index_request_config');

  var module = angular.module('gn_index_requestmanager', [
    'gn_index_request', 'gn_index_request_config'
  ]);

  /**
   * The IndexRequestManager manage a pool of index request objects. Each object
   * is a state of a current index search.
   *
   * @param {object} $injector angular injector
   * @constructor
   */
  var gnIndexRequestManager = function($injector) {

    var pool_ = [];

    /**
     * Register a index request object with a given type and name.
     * The manager will check in the pool if such an object has already been
     * instantiated, returns it or instantiate it.
     * The index request object is initialize following its type. For the given
     * type, an config angular Value must exist with the corresponding name.
     *
     * @param {string} type used to init the object
     * @param {string} name identify the object in the pool
     * @return {*}
     */
    this.register = function(type, name) {

      if (!(type && name)) {
        console.error(
            'You can\'t register an index object without identifiers');
        return;
      }

      var objId = this.getObjectId_(type, name);
      var configName = 'gnIndex' + type + 'Config';

      // Retrieve the angular value config object
      if (!$injector.has(configName)) {
        console.error('The index config is not defined: ' + configName);
        return;
      }

      // Instanciate the index request object
      if (!pool_[objId]) {
        var indexObj = new geonetwork.gnIndexRequest(
            $injector.get(configName), $injector);
        pool_[objId] = indexObj;
      }
      return pool_[objId];
    };

    /**
     * Get an index request object with a given type and name from the pool.
     *
     * @param {string} type used to init the object
     * @param {string} name identify the object in the pool
     * @return {*} the index request object.
     */

    this.get = function(type, name) {
      if (!(type && name)) {
        return;
      }
      var objId = this.getObjectId_(type, name);
      return pool_[objId];
    };

    /**
     * Unregister the index request object from manager. The object is deleted.
     * @param {string} type
     * @param {string} name
     */
    this.unregister = function(type, name) {
      var objId = type + '_' + name;
      if (pool_[objId]) {
        delete pool_[objId];
      }
    };

    this.getObjectId_ = function(type, name) {
      return type + '_' + name;
    };
  };

  module.service('gnIndexRequestManager', ['$injector', gnIndexRequestManager]);


})();
