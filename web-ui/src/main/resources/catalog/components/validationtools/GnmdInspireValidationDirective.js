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
  goog.provide("gn_md_validation_tools_directive");

  var module = angular.module("gn_md_validation_tools_directive", []);

  module.directive("gnMdValidationTools", [
    "gnConfig",
    "$http",
    "$interval",
    "gnAlertService",
    "$translate",
    "gnPopup",
    "$timeout",
    "gnCurrentEdit",
    "gnConfigService",
    "gnSearchManagerService",
    "Metadata",
    function (
      gnConfig,
      $http,
      $interval,
      gnAlertService,
      $translate,
      gnPopup,
      $timeout,
      gnCurrentEdit,
      gnConfigService,
      gnSearchManagerService,
      Metadata
    ) {
      return {
        restrict: "AEC",
        replace: true,
        templateUrl:
          "../../catalog/components/validationtools/partials/mdValidationTools.html",
        link: function postLink(scope, element, attrs) {
          scope.isDownloadingRecord = false;
          scope.isDownloadedRecord = false;
          scope.isEnabled = false;
          scope.testSuites = {};
          scope.shaclTestsuites = {};
          scope.shaclReport = {};

          scope.$watch("gnCurrentEdit.uuid", function (newValue, oldValue) {
            if (newValue == undefined) {
              return;
            }
            scope.isEnabled = true;
            scope.inspMdUuid = newValue;
            scope.md = gnCurrentEdit.metadata;

            $http({
              method: "GET",
              url: "../api/records/" + scope.inspMdUuid + "/validate/inspire/testsuites"
            }).then(function (r) {
              scope.testsuites = r.data;
            });

            $http({
              method: "GET",
              url: "../api/records/" + scope.inspMdUuid + "/validate/shacl/testsuites"
            }).then(function (r) {
              scope.shaclTestsuites = r.data;
            });

            gnConfigService.load().then(function (c) {
              // INSPIRE validator only support ISO19139/115-3 records.
              // This assume that those schema have and ISO19139 formatter
              // which is the format supported by the validator
              scope.isInspireValidationEnabled =
                gnConfig[gnConfig.key.isInspireEnabled] &&
                angular.isString(gnConfig["system.inspire.remotevalidation.url"]) &&
                gnCurrentEdit.schema.match(/iso19139|iso19115-3/) != null;

              scope.validationNode =
                gnConfig["system.inspire.remotevalidation.nodeid"] || "";
            });
          });

          scope.validateShacl = function (formatter, testsuite) {
            scope.shaclReport = {};
            var formatterUrl =
              "../api/records/" + scope.inspMdUuid + "/formatters/" + formatter;
            $http
              .get(
                "../api/records/" +
                  scope.inspMdUuid +
                  "/validate/shacl" +
                  "?formatter=" +
                  formatter +
                  "&testsuite=" +
                  testsuite,
                {
                  headers: {
                    Accept: "application/json"
                  }
                }
              )
              .then(function (response) {
                scope.shaclReport = response.data;
                // Count failure with sh:resultSeverity"]["@id"] in shaclReport @graph
                scope.shaclFailureCount = scope.shaclReport["@graph"]
                  ? scope.shaclReport["@graph"].filter(function (g) {
                      return (
                        g["sh:resultSeverity"] &&
                        g["sh:resultSeverity"]["@id"].match("sh:Violation|sh:Warning")
                      );
                    }).length
                  : 0;

                gnPopup.createModal(
                  {
                    class: "disclaimer-popup",
                    title:
                      $translate.instant("shaclValidationPopupReportTitle") +
                      " (" +
                      testsuite +
                      ")",
                    content:
                      "<div>" +
                      "<span data-translate=''>shaclValidationFormat</span> <a href='" +
                      formatterUrl +
                      "' target='_blank'>" +
                      formatter +
                      "</a><br/>" +
                      "<div data-ng-show='shaclFailureCount > 0' class='label label-danger'><span data-translate=''>sh:Violation</span>: {{shaclFailureCount}}</div>" +
                      "<div data-ng-show='shaclReport.valid === true' class='label label-success'><span>{{shaclReport.message}}</span></div>" +
                      "<div data-ng-show='shaclReport.valid === false' class='label label-warning'><span>{{shaclReport.message}}</span></div>" +
                      "<br/>" +
                      "<table class='table table-striped' data-ng-show='shaclFailureCount > 0'>" +
                      "  <tr><th data-translate=''>shaclSeverity</th><th data-translate=''>shaclContext</th><th data-translate=''>shaclMessage</th><th data-translate=''>shaclNode</th></tr>" +
                      "  <tr data-ng-repeat='g in shaclReport[\"@graph\"]'" +
                      "         data-ng-show='g[\"sh:resultMessage\"]'" +
                      '         data-ng-init=\'severity = g["sh:resultSeverity"]["@id"]\'' +
                      "         >" +
                      '     <td data-ng-class=\'{"danger": severity === "sh:Violation", "warning": severity === "sh:Warning"}\'>{{severity | translate}}</td>' +
                      "     <td>{{g['sh:resultPath']['@id']}}</td>" +
                      "     <td>{{g['sh:resultMessage']}}</td>" +
                      "     <td>{{g['sh:focusNode']['@id']}}" +
                      "         <br/>" +
                      '         SHACL rule id: {{g["sh:sourceShape"]["@id"]}}' +
                      "     </td>" +
                      "  </tr>" +
                      "</table>" +
                      "</div>"
                  },
                  scope
                );
              }),
              function (error) {
                console.error("Error during SHACL validation:", error);
              };
          };

          scope.validateInspire = function (test, mode) {
            if (scope.isEnabled) {
              scope.isDownloadingRecord = true;
              scope.token = null;
              var url =
                "../api/records/" +
                scope.inspMdUuid +
                "/validate/inspire?testsuite=" +
                test;
              if (angular.isDefined(mode) && mode !== "") {
                url += "&mode=" + mode;
              }
              $http({
                method: "PUT",
                url: url
              }).then(
                function mySucces(response) {
                  if (angular.isDefined(response.data) && response.data != null) {
                    scope.checkInBackground(response.data);
                  } else {
                    scope.isDownloadingRecord = false;
                    scope.isDownloadedRecord = false;
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireServiceError"),
                      type: "danger"
                    });
                  }
                },
                function myError(error) {
                  scope.isDownloadingRecord = false;
                  scope.isDownloadedRecord = false;
                  if (error.status == 403) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireNotAllowedError"),
                      type: "danger"
                    });
                  } else if (error.status == 404) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireNotFoundError"),
                      type: "danger"
                    });
                  } else if (error.status == 406) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireNotAcceptableError"),
                      type: "danger"
                    });
                  } else if (error.status == 500) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireServiceError"),
                      type: "danger"
                    });
                  } else {
                    gnAlertService.addAlert({
                      msg: error.data.message || error.data.description,
                      type: "danger"
                    });
                  }
                }
              );
            }
          };

          function reloadRecord() {
            gnSearchManagerService
              .gnSearch({
                _id: gnCurrentEdit.id,
                _content_type: "json",
                _isTemplate: "y or n or s",
                _draft: "y or n or e",
                fast: "index"
              })
              .then(function (data) {
                scope.md = new Metadata(data.metadata[0]);
              });
          }
          scope.checkInBackground = function (token) {
            scope.stop = undefined;
            if (token === "") {
              gnAlertService.addAlert({
                msg: $translate.instant("noINSPIRETestTokenAvailable"),
                type: "danger"
              });
              scope.isDownloadingRecord = false;
              scope.isDownloadedRecord = false;
              return;
            }
            scope.stop = $interval(function () {
              $http({
                method: "GET",
                url: "../api/records/" + token + "/validate/inspire"
              }).then(
                function mySucces(response) {
                  if (response.status == 200) {
                    scope.stopChecking();
                    scope.isDownloadingRecord = false;
                    scope.isDownloadedRecord = true;

                    scope.reportStatus = response.data.status;
                    scope.reportURL = response.data.report;
                    scope.showDisclaimer(scope.reportURL, scope.reportStatus);
                    $timeout(function () {
                      reloadRecord();
                    }, 5000);
                  } else if (response.status == 201) {
                    // continue
                  }
                },
                function myError(error) {
                  scope.isDownloadingRecord = false;
                  scope.isDownloadedRecord = false;
                  scope.stopChecking();
                  if (error.status == 403) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireNotAllowedError"),
                      type: "danger"
                    });
                  } else if (error.status == 404) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireNotFoundError"),
                      type: "danger"
                    });
                  } else if (error.status == 500) {
                    gnAlertService.addAlert({
                      msg: $translate.instant("inspireServiceError"),
                      type: "danger"
                    });
                  }
                }
              );
            }, 10000);
          };

          scope.showDisclaimer = function (url, status) {
            gnPopup.createModal(
              {
                class: "disclaimer-popup",
                title: $translate.instant("inspirePopupReportTitle"),
                content:
                  "<div>" +
                  $translate.instant("inspirePopupReportText") +
                  status +
                  "</br></br>" +
                  "<a href='" +
                  url +
                  "' target='_blank'>" +
                  $translate.instant("inspirePopupReportLink") +
                  "</a></div>"
              },
              scope
            );
          };

          scope.stopChecking = function () {
            if (angular.isDefined(scope.stop)) {
              $interval.cancel(scope.stop);
              scope.stop = undefined;
            }
          };
        }
      };
    }
  ]);
})();
