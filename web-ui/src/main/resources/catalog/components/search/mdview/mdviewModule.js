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
  goog.provide('gn_mdview');




  goog.require('gn_md_feedback');
  goog.require('gn_mdview_directive');
  goog.require('gn_mdview_service');
  goog.require('gn_related_observer_directive');

  var module = angular.module('gn_mdview', [
    'gn_mdview_service',
    'gn_mdview_directive',
    'gn_md_feedback',
    'gn_related_observer_directive'
  ]);

  module.controller('GnMdViewController', [
    '$scope', '$http', '$compile', 'gnSearchSettings', 'gnSearchLocation',
    'gnMetadataActions', 'gnAlertService', '$translate', '$location',
    'gnMdView', 'gnMdViewObj', 'gnMdFormatter',
    function($scope, $http, $compile, gnSearchSettings, gnSearchLocation,
             gnMetadataActions, gnAlertService, $translate, $location,
             gnMdView, gnMdViewObj, gnMdFormatter) {

      $scope.formatter = gnSearchSettings.formatter;
      $scope.gnMetadataActions = gnMetadataActions;
      $scope.usingFormatter = false;
      $scope.url = location.href;
      $scope.compileScope = $scope.$new();
      $scope.recordIdentifierRequested = gnSearchLocation.getUuid();

      $scope.search = function(params) {
        $location.path('/search');
        $location.search(params);
      };
      $scope.deleteRecord = function(md) {
        return gnMetadataActions.deleteMd(md).then(function(data) {
          gnAlertService.addAlert({
            msg: $translate.instant('metadataRemoved',
                {title: md.title || md.defaultTitle}),
            type: 'success'
          });
          $scope.closeRecord(md);
        }, function(reason) {
          // Data needs improvements
          // See https://github.com/geonetwork/core-geonetwork/issues/723
          gnAlertService.addAlert({
            msg: reason.data,
            type: 'danger'
          });
        });
      };

      // activate the tabs in the advanded metadata view
      $scope.activateTabs = function() {

        // attach click to tab
        $('.nav-tabs-advanced a').click(function(e) {
          e.preventDefault();
          $(this).tab('show');
        });
        // hide empty tab
        $('.nav-tabs-advanced a').each(function() {

          var tabLink = $(this).attr('href');

          if (tabLink) {
            if ($(tabLink).length === 0) {
              $(this).parent().hide();
            }
          }
        });
        // show the first tab
        $('.nav-tabs-advanced a:first').tab('show');
      };

      $scope.format = function(f) {
        $scope.usingFormatter = f !== undefined;
        $scope.currentFormatter = f;
        if (f) {
          gnMdFormatter.getFormatterUrl(f.url, $scope).then(function(url) {
            $http.get(url, {
              headers: {
                Accept: 'text/html'
              }
            }).then(
                function(response) {
                  var snippet = response.data.replace(
                      '<?xml version="1.0" encoding="UTF-8"?>', '');

                  $('#gn-metadata-display').find('*').remove();

                  $scope.compileScope.$destroy();

                  // Compile against a new scope
                  $scope.compileScope = $scope.$new();
                  var content = $compile(snippet)($scope.compileScope);

                  $('#gn-metadata-display').append(content);

                  // activate the tabs in the full view
                  $scope.activateTabs();
                });
          });
        }
      };

      // Reset current formatter to open the next record
      // in default mode.
      $scope.$watch('mdView.current.record', function() {
        $scope.usingFormatter = false;
        $scope.currentFormatter = null;
      });

      // Know from what path we come from
      $scope.gnMdViewObj = gnMdViewObj;
      $scope.$watch('gnMdViewObj.from', function(v) {
        $scope.fromView = v ? v.substring(1) : v;
      });
    }]);
})();
