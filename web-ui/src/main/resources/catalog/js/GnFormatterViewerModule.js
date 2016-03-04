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
  goog.provide('gn_formatter_viewer');











  goog.require('gn');
  goog.require('gn_alert');
  goog.require('gn_catalog_service');
  goog.require('gn_formatter_lib');
  goog.require('gn_mdactions_service');
  goog.require('gn_popup_directive');
  goog.require('gn_popup_service');
  goog.require('gn_search_default_directive');
  goog.require('gn_utility');
  goog.require('gn_utility');








  var module = angular.module('gn_formatter_viewer',
      ['ngRoute', 'gn', 'gn_utility', 'gn_catalog_service',
        'gn_search_default_directive',
        'gn_popup_service', 'gn_mdactions_service', 'gn_alert']);

  // Define the translation files to load
  module.constant('$LOCALES', ['core']);

  module.constant('gnSearchSettings', {});

  module.controller('GnFormatterViewer',
      ['$scope', '$http', '$sce', '$routeParams', 'Metadata',
       function($scope, $http, $sce, $routeParams, Metadata) {
         $scope.md = {
           'geonet:info': {}
         };
         $scope.metadata = '';
         $scope.loading = true;

         var formatter = $routeParams.formatter;
         var mdId = $routeParams.mdId;

         var idParam = isNaN(parseInt(mdId)) ? 'uuid=' + mdId : 'id=' + mdId;

         $http.get('md.format.xml?xsl=' + formatter + '&' + idParam).
         success(function(data) {
           $scope.loading = undefined;
           $scope.metadata = $sce.trustAsHtml(data);
         }).error(function(data) {
           $scope.loading = undefined;
           $scope.metadata = $sce.trustAsHtml(data);
         });
         var indexField = isNaN(mdId) ? '_uuid' : '_id';
         $http.get('qi?_content_type=json&fast=index&' + indexField + '=' +
         mdId).success(function(data) {
           $scope.md = new Metadata(data.metadata);
         });
       }]);
  module.config(['$routeProvider', function($routeProvider) {
    var tpls = '../../catalog/templates/';

    $routeProvider.when('/:formatter/:mdId', { templateUrl: tpls +
          '/formatter-viewer.html', controller: 'GnFormatterViewer'});
  }]);
})();
