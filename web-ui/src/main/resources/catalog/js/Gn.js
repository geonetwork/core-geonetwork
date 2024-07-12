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

// Define root geonetwork namespace
var geonetwork = {};

(function () {
  goog.provide("gn");

  goog.require("gn_locale");
  goog.require("gn_toolbar");
  goog.require("gn_count_watchers");
  goog.require("gn_polyfills");
  goog.require("gn_static_pages");

  /**
   * Main gn module.
   *
   * Must be included by all uis.
   * Is used in wro4j for $templatecache.
   * Must contains only what is mendatory in all uis:
   * - locale management
   *
   * @type {module|*}
   */
  var module = angular.module("gn", [
    "gn_toolbar",
    "gn_locale",
    "gn_config",
    "cfp.hotkeys",
    "gn_static_pages"
  ]);

  // See https://docs.angularjs.org/guide/migration#commit-aa077e8
  module.config([
    "$locationProvider",
    function ($locationProvider) {
      // Configure existing providers
      $locationProvider.hashPrefix("");
    }
  ]);
})();
