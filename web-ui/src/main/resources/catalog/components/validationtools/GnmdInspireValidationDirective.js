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
  goog.provide('gn_md_validation_tools_directive');

  var module = angular.module('gn_md_validation_tools_directive', []);

  module.directive(
      'gnMdValidationTools', ['gnConfig', '$http', '$interval',
      'gnAlertService', '$translate', 'gnPopup',
      'gnCurrentEdit', 'gnConfigService',
        function(gnConfig, $http, $interval, gnAlertService,
                 $translate, gnPopup, gnCurrentEdit, gnConfigService) {
          return {
            restrict: 'AEC',
            replace: true,
            templateUrl:
            '../../catalog/components/validationtools/partials/mdValidationTools.html',
            link: function postLink(scope, element, attrs) {
              scope.isDownloadingRecord = false;
              scope.isDownloadedRecord = false;
              scope.isEnabled = false;
              scope.testSuites = {}



              scope.$watch('gnCurrentEdit.uuid', function(newValue, oldValue) {
                if (newValue == undefined) {
                  return;
                }
                scope.isEnabled = true;
                scope.inspMdUuid = newValue;
                $http({
                  method: 'GET',
                  url: '../api/records/' + scope.inspMdUuid +
                    '/validate/inspire/testsuites'
                }).then(function(r) {
                  scope.testsuites = r.data;
                });

                gnConfigService.load().then(function(c) {
                  // INSPIRE validator only support ISO19139/115-3 records.
                  // This assume that those schema have and ISO19139 formatter
                  // which is the format supported by the validator
                  scope.isInspireValidationEnabled =
                    gnConfig[gnConfig.key.isInspireEnabled] &&
                    angular.isString(gnConfig['system.inspire.remotevalidation.url']) &&
                    gnCurrentEdit.schema.match(/iso19139|iso19115-3/) != null;
                });
              });

              scope.validateInspire = function(test) {

                if (scope.isEnabled) {

                  scope.isDownloadingRecord = true;
                  scope.token = null;

                  $http({
                    method: 'PUT',
                    url: '../api/records/' + scope.inspMdUuid +
                    '/validate/inspire?testsuite=' + test
                  }).then(function mySucces(response) {
                    if (angular.isDefined(response.data) && response.data != null) {
                      scope.checkInBackgroud(response.data);
                    } else {
                      scope.isDownloadingRecord = false;
                      scope.isDownloadedRecord = false;
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireServiceError'),
                        type: 'danger'
                      });
                    }
                  }, function myError(error) {
                    scope.isDownloadingRecord = false;
                    scope.isDownloadedRecord = false;
                    if (error.status == 403) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireNotAllowedError'),
                        type: 'danger'
                      });
                    } else if (error.status == 404) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireNotFoundError'),
                        type: 'danger'
                      });
                    } else if (error.status == 406) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireNotAcceptableError'),
                        type: 'danger'
                      });
                    } else if (error.status == 500) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireServiceError'),
                        type: 'danger'
                      });
                    }
                  });
                }
              };

              scope.checkInBackgroud = function(token) {
                scope.stop = undefined;
                scope.stop = $interval(function() {
                  $http({
                    method: 'GET',
                    url: '../api/records/' + token + '/validate/inspire'
                  }).then(function mySucces(response) {

                    if (response.status == 200) {
                      scope.stopChecking();
                      scope.isDownloadingRecord = false;
                      scope.isDownloadedRecord = true;

                      scope.reportStatus = response.data.status;
                      scope.reportURL = response.data.report;
                      scope.showDisclaimer(scope.reportURL, scope.reportStatus);

                    } else if (response.status == 201) {
                      // continue
                    }
                  }, function myError(error) {
                    scope.isDownloadingRecord = false;
                    scope.isDownloadedRecord = false;
                    scope.stopChecking();
                    if (error.status == 403) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireNotAllowedError'),
                        type: 'danger'
                      });
                    } else if (error.status == 404) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireNotFoundError'),
                        type: 'danger'
                      });
                    } else if (error.status == 500) {
                      gnAlertService.addAlert({
                        msg: $translate.instant('inspireServiceError'),
                        type: 'danger'
                      });
                    }
                  });

                },10000);
              };

              scope.showDisclaimer = function(url, status) {
                gnPopup.createModal({
                  class: 'disclaimer-popup',
                  title: $translate.instant('inspirePopupReportTitle'),
                  content: '<div>' +
                  $translate.instant('inspirePopupReportText') +
                  status + '</br></br>' +
                  '<a href=\'' + url + '\' target=\'_blank\'>' +
                  $translate.instant('inspirePopupReportLink') + '</a></div>'
                }, scope);
              };

              scope.stopChecking = function() {
                if (angular.isDefined(scope.stop)) {
                  $interval.cancel(scope.stop);
                  scope.stop = undefined;
                }
              };
            }
          };
        }]);

})();
