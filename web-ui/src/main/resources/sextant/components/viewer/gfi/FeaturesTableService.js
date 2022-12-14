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
  goog.provide("gn_featurestable_service");

  var module = angular.module("gn_featurestable_service", []);

  /**
   *
   * @constructor
   */
  var GnFeaturesTableService = function () {};
  GnFeaturesTableService.prototype.load = function () {};

  /**
   *
   * @constructor
   */
  var GnFeaturesTableManager = function (gnFeaturesTableLoader) {
    this.gnFeaturesTableLoader = gnFeaturesTableLoader;
    this.tables = [];
  };
  GnFeaturesTableManager.prototype.addTable = function (tableConfig, loaderConfig) {
    var table = {
      name: tableConfig.name,
      type: tableConfig.type,
      loader: this.gnFeaturesTableLoader.createLoader(tableConfig.type, loaderConfig)
    };

    this.tables.push(table);
  };

  GnFeaturesTableManager.prototype.clear = function () {
    this.tables.length = 0;
  };

  GnFeaturesTableManager.prototype.getCount = function () {
    var count = 0;
    this.tables.forEach(function (table) {
      count += table.loader.getCount();
    });
    return count;
  };

  GnFeaturesTableManager.prototype.isLoading = function () {
    var loading = false;
    this.tables.forEach(function (table) {
      loading = loading || table.loader.isLoading();
    });
    return loading;
  };

  module.service("gnFeaturesTableManager", [
    "gnFeaturesTableLoader",
    GnFeaturesTableManager
  ]);

  module.service("gnFeaturesTableService", GnFeaturesTableService);
})();
