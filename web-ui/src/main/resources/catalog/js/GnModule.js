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
  goog.provide('gn_module');


  goog.require('gn');
  goog.require('gn_admin_menu');
  goog.require('gn_alert');
  goog.require('gn_cat_controller');
  goog.require('gn_cors_interceptor');
  goog.require('gn_formfields');
  goog.require('gn_indexingtask');
  goog.require('gn_batchtask');
  goog.require('gn_language_switcher');
  goog.require('gn_locale');
  goog.require('gn_map');
  goog.require('gn_metadata_manager');
  goog.require('gn_needhelp');
  goog.require('gn_pagination');
  goog.require('gn_search_form_controller');
  goog.require('gn_search_manager');
  goog.require('gn_utility');
  goog.require('gn_openlayers');

  /**
   * GnModule just manage angular injection with
   * other commonly shared modules.
   * Is used in mostly all uis, and provide common
   * components.
   *
   * @type {module|*}
   */
  var module = angular.module('gn_module', [
    'gn',
    'ngRoute',
    'gn_language_switcher',
    'gn_utility',
    'gn_search_manager',
    'gn_metadata_manager',
    'gn_pagination',
    'gn_cat_controller',
    'gn_formfields',
    'gn_map',
    'gn_search_form_controller',
    'gn_needhelp',
    'gn_alert',
    'gn_admin_menu',
    'gn_cors_interceptor',
    'gn_openlayers',
    'gn_indexingtask',
    'gn_batchtask'
  ]);

})();
