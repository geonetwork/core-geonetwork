/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
  goog.provide("gn_mdview");

  goog.require("gn_mdview_directive");
  goog.require("gn_mdview_service");
  goog.require("gn_related_observer_directive");
  goog.require("gn_userfeedback");
  goog.require("gn_thesaurus");
  goog.require("gn_catalog_service");

  var module = angular.module("gn_mdview", [
    "gn_mdview_service",
    "gn_mdview_directive",
    "gn_related_observer_directive",
    "gn_userfeedback",
    "gn_thesaurus",
    "gn_catalog_service"
  ]);

  module.controller("GnMdViewController", [
    "$scope",
    "$http",
    "$compile",
    "gnSearchSettings",
    "gnSearchLocation",
    "gnMetadataActions",
    "gnAlertService",
    "$translate",
    "$location",
    "gnMdView",
    "gnMdViewObj",
    "gnMdFormatter",
    "gnConfig",
    "gnGlobalSettings",
    "gnConfigService",
    "$rootScope",
    "$filter",
    "gnUtilityService",
    "$window",
    function (
      $scope,
      $http,
      $compile,
      gnSearchSettings,
      gnSearchLocation,
      gnMetadataActions,
      gnAlertService,
      $translate,
      $location,
      gnMdView,
      gnMdViewObj,
      gnMdFormatter,
      gnConfig,
      gnGlobalSettings,
      gnConfigService,
      $rootScope,
      $filter,
      gnUtilityService,
      $window
    ) {
      $scope.formatter = gnSearchSettings.formatter;
      $scope.gnMetadataActions = gnMetadataActions;
      $scope.url = location.href;
      $scope.compileScope = $scope.$new();
      $scope.recordIdentifierRequested = gnSearchLocation.getUuid();
      $scope.isUserFeedbackEnabled = false;
      $scope.isRatingEnabled = false;
      $scope.showCitation = false;
      $scope.isSocialbarEnabled =
        gnGlobalSettings.gnCfg.mods.recordview.isSocialbarEnabled;
      $scope.viewConfig = gnGlobalSettings.gnCfg.mods.recordview;
      $scope.highlightedThesaurus = [].concat(
        gnGlobalSettings.gnCfg.mods.recordview.mainThesaurus,
        gnGlobalSettings.gnCfg.mods.recordview.internalThesaurus,
        gnGlobalSettings.gnCfg.mods.recordview.locationThesaurus
      );
      $scope.showDataBrowser =
        gnGlobalSettings.gnCfg.mods.map.disabledTools.filter === false;
      $scope.showStatusWatermarkFor =
        gnGlobalSettings.gnCfg.mods.recordview.showStatusWatermarkFor;
      $scope.showStatusTopBarFor =
        gnGlobalSettings.gnCfg.mods.recordview.showStatusTopBarFor;

      $scope.showStaticPageMenu =
        gnGlobalSettings.gnCfg.mods.recordview.showStaticPageMenu;


      gnConfigService.load().then(function (c) {
        $scope.isRecordHistoryEnabled = gnConfig["metadata.history.enabled"];
        $scope.isPreferGroupLogo = gnConfig["system.metadata.prefergrouplogo"];

        var statusSystemRating = gnConfig["system.localrating.enable"];

        if (statusSystemRating == "advanced") {
          $scope.isUserFeedbackEnabled = true;
        }
        if (statusSystemRating == "basic") {
          $scope.isRatingEnabled = true;
        }
      });

      /**
       * First matching view for each formatter is returned.
       *
       * @param record
       * @returns {*[]}
       */
      function getFormatterForRecord(record) {
        var list = [];
        if (record == null) {
          return list;
        }
        for (var i = 0; i < gnSearchSettings.formatter.list.length; i++) {
          var f = gnSearchSettings.formatter.list[i];
          if (f.views === undefined) {
            list.push(f);
          } else {
            // Check conditional views
            var isViewSet = false;

            viewLoop: for (var j = 0; j < f.views.length; j++) {
              var v = f.views[j];

              if (v.if) {
                for (var key in v.if) {
                  if (v.if.hasOwnProperty(key)) {
                    var values = angular.isArray(v.if[key]) ? v.if[key] : [v.if[key]];

                    if (values.includes(record[key])) {
                      list.push({ label: f.label, url: v.url });
                      isViewSet = true;
                      break viewLoop;
                    }
                  }
                }
              } else {
                console.warn(
                  "A conditional view MUST have a if property. " +
                    'eg. {"if": {"documentStandard": "iso19115-3.2018"}, "url": "..."}'
                );
              }
            }
            if (f.url !== undefined && !isViewSet) {
              list.push(f);
            }
          }
        }
        return list;
      }

      $scope.recordFormatterList = gnMdFormatter.getFormatterForRecord(
        $scope.mdView.current.record
      );

      $scope.search = function (params) {
        $location.path("/search");
        $location.search(params);
      };

      $scope.filterBy = function (field, value) {
        $location.path("/search");
        var params = {};
        params[field] = {};
        params[field][value] = true;
        gnSearchLocation.lastSearchUrl = null;
        gnSearchLocation.setSearch({ query_string: angular.toJson(params) });
      };

      $scope.deleteRecord = function (md) {
        return gnMetadataActions.deleteMd(md).then(
          function (data) {
            gnAlertService.addAlert({
              msg: $translate.instant("metadataRemoved", { title: md.resourceTitle }),
              type: "success"
            });
            $scope.closeRecord(md);
          },
          function (reason) {
            // Data needs improvements
            // See https://github.com/geonetwork/core-geonetwork/issues/723
            gnAlertService.addAlert({
              msg: reason.data.message || reason.data.description,
              type: "danger"
            });
          }
        );
      };

      $scope.cancelWorkingCopy = function (md) {
        return gnMetadataActions.cancelWorkingCopy(md).then(
          function (data) {
            gnAlertService.addAlert({
              msg: $translate.instant("metadataRemoved", {
                title: md.resourceTitle
              }),
              type: "success"
            });

            // Set a timeout to reload the page, to display the alert
            $window.setTimeout(function () {
              $window.location.href = $location
                .absUrl()
                .replace("/metadraf/", "/metadata/");
              $window.location.reload();
            }, 500);
          },
          function (reason) {
            // Data needs improvements
            // See https://github.com/geonetwork/core-geonetwork/issues/723
            gnAlertService.addAlert({
              msg: reason.data.description,
              type: "danger"
            });
          }
        );
      };

      /**
       * Scroll to an element in the page using it's ID
       *
       * @param id  unique element identifier
       */
      $scope.scrollToSection = function (id) {
        var scrollToElement = document.getElementById(id);

        if (scrollToElement) {
          scrollToElement.scrollIntoView();
        }
      };

      // activate the tabs in the full metadata view
      $scope.activateTabs = function () {
        // attach click to tab
        $(".nav-tabs-advanced a").click(function (e) {
          e.preventDefault();
          $(this).tab("show");
        });
        // hide empty tab
        $(".nav-tabs-advanced a").each(function () {
          var tabLink = $(this).attr("href");

          if (tabLink) {
            if ($(tabLink).length === 0) {
              $(this).parent().hide();
            }
          }
        });
        // show the first visible tab
        $(".nav-tabs-advanced a:visible:first").tab("show");
      };

      $scope.loadFormatter = function (url) {
        var showApproved =
          $scope.mdView.current.record == null
            ? true
            : $scope.mdView.current.record.draft != "y";
        var gn_metadata_display = $("#gn-metadata-display");

        $http
          .get(url, {
            headers: {
              Accept: "text/html"
            },
            params: {
              approved: showApproved
            }
          })
          .then(
            function (response, status) {
              if (response.status != 200) {
                gn_metadata_display.append(
                  "<div class='alert alert-danger top-buffer'>" +
                    $translate.instant("metadataViewLoadError") +
                    "</div>"
                );
              } else {
                var snippet = response.data.replace(
                  '<?xml version="1.0" encoding="UTF-8"?>',
                  ""
                );

                gn_metadata_display.find("*").remove();

                $scope.compileScope.$destroy();

                // Compile against a new scope
                $scope.compileScope = $scope.$new();
                var content = $compile(snippet)($scope.compileScope);

                gn_metadata_display.append(content);

                // activate the tabs in the full view
                $scope.activateTabs();
              }
            },
            function (data) {
              gn_metadata_display.append(
                "<div class='alert alert-danger top-buffer'>" +
                  $translate.instant("metadataViewLoadError") +
                  "</div>"
              );
            }
          );
      };

      function checkIfCitationIsDisplayed(record) {
        $scope.showCitation = false;
        if (
          gnGlobalSettings.gnCfg.mods.recordview.showCitation.enabled === true &&
          gnGlobalSettings.gnCfg.mods.recordview.showCitation.if
        ) {
          gnUtilityService.checkConfigurationPropertyCondition(
            record,
            gnGlobalSettings.gnCfg.mods.recordview.showCitation,
            function () {
              $scope.showCitation = true;
            }
          );
        } else {
          $scope.showCitation =
            gnGlobalSettings.gnCfg.mods.recordview.showCitation.enabled;
        }
      }

      // Reset current formatter to open the next record
      // in default mode.
      function loadFormatter(n, o) {
        if (n === true) {
          $scope.recordFormatterList = gnMdFormatter.getFormatterForRecord(
            $scope.mdView.current.record
          );

          checkIfCitationIsDisplayed($scope.mdView.current.record);

          var f = gnSearchLocation.getFormatterPath($scope.recordFormatterList[0].url);
          $scope.currentFormatter = "";

          gnMdViewObj.usingFormatter = f !== undefined;

          if (f != undefined) {
            $scope.currentFormatter = f.replace(/.*(\/formatters.*)/, "$1");
            $scope.loadFormatter(f);
          }
        }
      }
      $scope.$watch("mdView.recordsLoaded", loadFormatter);

      $scope.sortByCategory = function (cat) {
        return $filter("translate")("cat-" + cat);
      };

      // Know from what path we come from
      $scope.gnMdViewObj = gnMdViewObj;
      $scope.$watch("gnMdViewObj.from", function (v) {
        $scope.fromView = v ? v.substring(1) : v;
      });
    }
  ]);
})();
