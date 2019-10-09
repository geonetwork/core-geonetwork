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
  goog.require('gn_mdview');
  goog.require('gn_popup_directive');
  goog.require('gn_popup_service');
  goog.require('gn_related_directive');
  goog.require('gn_search_default_directive');
  goog.require('gn_utility');
  goog.require('gn_viewer');

  var module = angular.module('gn_formatter_viewer', [
    'ngRoute',
    'gn',
    'gn_alert',
    'gn_related_directive',
    'gn_catalog_service',
    'gn_mdactions_service',
    'gn_utility',
    'gn_viewer',
    'gn_mdview'
  ]);

  module.config(['$LOCALES', 'gnGlobalSettings',
    function($LOCALES) {
      $LOCALES.push('search');
    }]);

  module.constant('gnSearchSettings', {});

  module.controller('GnFormatterViewer',
      ['$scope', '$http', '$sce', '$routeParams', '$location',
        'Metadata', 'gnMdFormatter',
       function($scope, $http, $sce, $routeParams, $location,
                Metadata, gnMdFormatter) {

         var formatter = $routeParams.formatter;
         var mdId = $routeParams.mdId;

         $scope.loading = true;
         $scope.$on('mdLoadingEnd', function() {
           $scope.loading = false;
         });

         var url = '../api/records' + $location.url().replace('/metadata', '');

         gnMdFormatter.load(mdId, '.formatter-container', $scope, url);
       }]);

  module.config(['$routeProvider', function($routeProvider) {
    var tpls = '../../catalog/templates/';

    $routeProvider
      .when(
        '/metadata/:mdId/formatters/:formatter', {
          templateUrl: tpls + 'formatter-viewer.html',
          controller: 'GnFormatterViewer'})
      .when(
        '/:mdId/formatters/:formatter', {
          templateUrl: tpls + 'formatter-viewer.html',
          controller: 'GnFormatterViewer'})
      .when(
        '/metadata/:mdId', {
          templateUrl: tpls + 'formatter-viewer.html',
          controller: 'GnFormatterViewer'})
      .when(
      '/:mdId', {
        templateUrl: tpls + 'formatter-viewer.html',
        controller: 'GnFormatterViewer'});
  }]);
})();
