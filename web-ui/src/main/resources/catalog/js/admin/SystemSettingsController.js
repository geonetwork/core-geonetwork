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
  goog.provide("gn_system_settings_controller");

  goog.require("gn_ui_config");
  goog.require("gn_timezone_selector");

  var module = angular.module("gn_system_settings_controller", [
    "gn_ui_config",
    "gn_timezone_selector"
  ]);

  module.filter("hideLanguages", function () {
    return function (input) {
      var filtered = [];
      angular.forEach(input, function (el) {
        if (el.name.indexOf("system/site/labels/") === -1) {
          filtered.push(el);
        }
      });
      return filtered;
    };
  });

  /**
   * Filters non-editable settings used by GeoNetwork,
   * not intended to be configured by the user.
   */
  module.filter("hideGeoNetworkNonEditableSettings", function () {
    return function (input) {
      var filtered = [];
      angular.forEach(input, function (el) {
        if (el.editable === true) {
          filtered.push(el);
        }
      });
      return filtered;
    };
  });

  module.filter("orderObjectBy", function () {
    return function (input, attribute) {
      if (!angular.isObject(input)) return input;

      var array = [];
      for (var objectKey in input) {
        array.push(input[objectKey]);
      }

      array.sort(function (a, b) {
        a = parseInt(a[attribute]);
        b = parseInt(b[attribute]);
        return a - b;
      });
      return array;
    };
  });
  /**
   * GnSystemSettingsController provides management interface
   * for catalog settings.
   *
   * TODO:
   *  * Add custom forms for some settings (eg. contact for CSW,
   *  Metadata views > default views, Search only in requested language)
   */
  module.controller("GnSystemSettingsController", [
    "$scope",
    "$http",
    "$rootScope",
    "$translate",
    "$location",
    "gnUtilityService",
    "$timeout",
    "gnGlobalSettings",
    "gnConfig",
    "gnESClient",
    "Metadata",
    function (
      $scope,
      $http,
      $rootScope,
      $translate,
      $location,
      gnUtilityService,
      $timeout,
      gnGlobalSettings,
      gnConfig,
      gnESClient,
      Metadata
    ) {
      $scope.selectTemplate = function (setting, md) {
        setting.value = md.uuid;
        $scope.defaultMetadataTemplate = md;
      };

      // Metadata template to select by default when
      // creating new metadata
      $scope.defaultMetadataTemplate = null;

      $scope.metadataTemplateSearchObj = {
        internal: true,
        any: "",
        defaultParams: {
          any: "",
          from: 1,
          to: 50,
          isTemplate: "y",
          sortBy: "resourceType,resourceTitleObject.default.sort",
          sortOrder: "asc,asc"
        }
      };
      $scope.metadataTemplateSearchObj.params = angular.extend(
        {},
        $scope.metadataTemplateSearchObj.defaultParams
      );

      $scope.settings = [];
      $scope.initalSettings = [];
      $scope.uiConfigurations = [];
      $scope.sectionsLevel1 = {};
      $scope.systemUsers = null;
      $scope.processTitle = "";
      $scope.orderProperty = "position";
      $scope.reverse = false;
      $scope.systemInfo = {
        stagingProfile: "production"
      };
      $scope.stagingProfiles = ["production", "development", "testing"];
      $scope.updateProfile = function () {
        $http.put("../api/site/info/staging/" + $scope.systemInfo.stagingProfile).then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("profileUpdated"),
              timeout: 2,
              type: "success"
            });
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("profileUpdatedFailed"),
              timeout: 2,
              type: "danger"
            });
          }
        );
      };

      $scope.defaultConfigId = "srv";

      $scope.loadTplReport = null;
      $scope.atomFeedType = "";

      $scope.isGroupPublicationNotificationLevel = false;
      $scope.isGroupLocalRatingNotificationLevel = false;

      $scope.changeLocalRatingNotificationLevel = function (value) {
        $scope.isGroupLocalRatingNotificationLevel = value === "recordGroupEmail";
      };

      $scope.changePublicationNotificationLevel = function (value) {
        $scope.isGroupPublicationNotificationLevel = value === "recordGroupEmail";
      };

      /**
       * Load catalog settings as a flat list and
       * extract firs and second level sections.
       *
       * Form field name is also based on settings
       * key replacing "/" by "." (to not create invalid
       * element name in XML Jeeves request element).
       */
      function loadSettings() {
        $http.get("../api/site/info/proxy").then(function (response) {
          $scope.isProxyConfiguredInSystemProperties =
            response.data.proxyConfiguredInSystemProperties;
        });

        $http.get("../api/site/info/build").then(function (response) {
          $scope.systemInfo = response.data;
        });

        $http.get("../api/site/info/notificationLevels").then(function (response) {
          $scope.notificationLevels = response.data;
          $scope.notificationLevels.unshift("");
        });

        // load log files
        $http.get("../api/site/logging").then(function (response) {
          $scope.logfiles = response.data;
        });

        $http.get("../api/site/settings/details").then(
          function (response) {
            var data = response.data;

            var sectionsLevel1 = [];
            var sectionsLevel2 = [];

            // Stringify JSON for editing in text area
            angular.forEach(data, function (s) {
              if (s.dataType === "JSON") {
                s.value = angular.toJson(s.value);
              }
            });

            $scope.settings = data;
            angular.copy(data, $scope.initalSettings);

            $scope.inspireApiUrl = undefined;
            $scope.inspireApiKey = undefined;

            for (var i = 0; i < $scope.settings.length; i++) {
              if ($scope.settings[i].name == "metadata/workflow/enable") {
                $scope.workflowEnable = $scope.settings[i].value == "true";
              } else if (
                $scope.settings[i].name == "metadata/workflow/draftWhenInGroup"
              ) {
                $scope.draftInAllGroups = $scope.settings[i].value == ".*";
              } else if (
                $scope.settings[i].name ==
                "system/metadataprivs/publication/notificationLevel"
              ) {
                $scope.isGroupPublicationNotificationLevel =
                  $scope.settings[i].value === "recordGroupEmail";
              } else if (
                $scope.settings[i].name == "system/localrating/notificationLevel"
              ) {
                $scope.isGroupLocalRatingNotificationLevel =
                  $scope.settings[i].value === "recordGroupEmail";
              } else if (
                $scope.settings[i].name == "system/inspire/remotevalidation/url"
              ) {
                $scope.inspireApiUrl = $scope.settings[i].value;
              } else if (
                $scope.settings[i].name == "system/inspire/remotevalidation/apikey"
              ) {
                $scope.inspireApiKey = $scope.settings[i].value;
              }

              var tokens = $scope.settings[i].name.split("/");
              // Extract level 1 and 2 sections
              if (tokens) {
                var level1name = tokens[0];
                if (sectionsLevel1.indexOf(level1name) === -1) {
                  sectionsLevel1.push(level1name);
                  $scope.sectionsLevel1[level1name] = {
                    name: level1name,
                    position: $scope.settings[i].position,
                    children: []
                  };
                }
                var level2name = level1name + "/" + tokens[1];
                if (sectionsLevel2.indexOf(level2name) === -1) {
                  sectionsLevel2.push(level2name);

                  var sectionChildren;

                  // Remove the system proxy information if using Java system properties
                  if (
                    level2name === "system/proxy" &&
                    $scope.isProxyConfiguredInSystemProperties
                  ) {
                    sectionChildren = [];
                  } else {
                    sectionChildren = filterBySection($scope.settings, level2name);
                  }

                  $scope.sectionsLevel1[level1name].children.push({
                    name: level2name,
                    position: $scope.settings[i].position,
                    children: sectionChildren
                  });
                }
              }

              var target = $location.search()["scrollTo"];
              if (target) {
                $timeout(function () {
                  gnUtilityService.scrollTo(target);
                }, 900);
              }
            }
          },
          function (response) {
            // TODO
          }
        );
        loadUiConfigurations();
      }

      $scope.lastUiConfiguration = undefined;

      function loadUiConfigurations() {
        $scope.uiConfiguration = undefined;
        $scope.uiConfigurationId = "";
        $scope.uiConfigurationIdIsValid = false;
        return $http.get("../api/ui").then(function (response) {
          var data = response.data;

          for (var i = 0; i < data.length; i++) {
            data[i].configuration = angular.fromJson(data[i].configuration || {});

            // Select last one updated or created
            if (
              angular.isDefined($scope.lastUiConfiguration) &&
              $scope.lastUiConfiguration == data[i].id
            ) {
              $scope.uiConfiguration = data[i];
            }
          }
          $scope.uiConfigurations = data;

          // Select the current node if defined in the configuration, otherwise the first one
          if (
            $scope.uiConfigurations.length > 0 &&
            angular.isUndefined($scope.uiConfiguration)
          ) {
            var defaultUiIndex = _.findIndex($scope.uiConfigurations, function (ui) {
              return ui.id === gnGlobalSettings.nodeId;
            });

            if (defaultUiIndex > -1) {
              $scope.uiConfiguration = $scope.uiConfigurations[defaultUiIndex];
            } else {
              $scope.uiConfiguration = $scope.uiConfigurations[0];
            }
          }
        });
      }

      $scope.$watch("uiConfigurationId", function (n, o) {
        if (n !== o) {
          $http.get("../api/ui/" + n).then(
            function (r) {
              $scope.uiConfigurationIdIsValid = r.status === 404;
            },
            function (r) {
              $scope.uiConfigurationIdIsValid = r.status === 404;
            }
          );
        }
      });

      /**
       * Create the default configuration based on the
       * one defined in CatController.
       */
      $scope.createDefaultUiConfig = function () {
        $scope.lastUiConfiguration = $scope.defaultConfigId;
        $scope.createOrUpdateUiConfiguration(false, $scope.defaultConfigId);
      };

      $scope.canDeleteUiConfig = function () {
        if ($scope.uiConfiguration) {
          // UI configuration for 'srv' can be deleted only by Administrator users
          return (
            $scope.uiConfiguration.id !== $scope.defaultConfigId ||
            ($scope.uiConfiguration.id === $scope.defaultConfigId &&
              $scope.user.isAdministratorOrMore())
          );
        } else {
          return false;
        }
      };

      $scope.updateUiConfig = function () {
        return $scope.createOrUpdateUiConfiguration(true);
      };
      $scope.createOrUpdateUiConfiguration = function (isUpdate, id) {
        var newid = id || $scope.uiConfiguration.id;
        $scope.lastUiConfiguration = newid;
        if (newid) {
          return $http
            .put(
              "../api/ui" + (isUpdate ? "/" + newid : ""),
              {
                id: newid,
                configuration: isUpdate ? $scope.uiConfiguration.configuration : null
              },
              { responseType: "text" }
            )
            .then(
              function (r) {
                loadUiConfigurations();
              },
              function (r) {
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("uiConfigUpdateError"),
                  error: r.data.message || r.data.description,
                  timeout: 0,
                  type: "danger"
                });
              }
            );
        }
      };

      $scope.deleteUiConfig = function () {
        $("#gn-confirm-remove-ui").modal("show");
      };

      $scope.confirmDeleteUiConfig = function () {
        $scope.lastUiConfiguration = undefined;
        return $http.delete("../api/ui/" + $scope.uiConfiguration.id).then(
          function (r) {
            loadUiConfigurations();
          },
          function (r) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("uiConfigDeleteError"),
              error: r.data.message || r.data.description,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      function loadUsers() {
        $http.get("../api/users").then(function (response) {
          $scope.systemUsers = response.data;
        });
      }

      /**
       * Filter all settings for a section
       */
      var filterBySection = function (elements, section) {
        var settings = [];
        var regexp = new RegExp("^" + section + "/.*|^" + section + "$");
        for (var i = 0; i < elements.length; i++) {
          var s = elements[i];
          if (regexp.test(s.name)) {
            settings.push(s);
          }
        }
        return settings;
      };

      /**
       * Save the form containing all settings. When saved,
       * broadcast success status and reload catalog info.
       */
      $scope.saveSettings = function (formId) {
        // Used to disable some UI form elements that should not be submitted.
        $(".gn-no-setting").attr("disabled", true);

        $http
          .post("../api/site/settings", gnUtilityService.serialize(formId), {
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
          })
          .then(
            function (response) {
              $(".gn-no-setting").attr("disabled", false);

              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("settingsUpdated"),
                timeout: 2,
                type: "success"
              });

              $scope.loadCatalogInfo();
            },
            function (response) {
              $(".gn-no-setting").attr("disabled", false);

              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("settingsUpdateError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
      };

      $scope.filterForm = function (e, formId) {
        var filterValue = e.target.value.toLowerCase();

        $(formId + " .form-group").filter(function () {
          var filterText = $(this).find("label").text().toLowerCase();
          var matchStart = filterText.indexOf("" + filterValue.toLowerCase() + "");

          if (matchStart > -1) {
            $(this).show();
          } else {
            $(this).hide();
          }
          // check parent
          $scope.filterParent($(this));
        });
      };

      $scope.resetFilter = function (formId) {
        $(formId + " .form-group").each(function () {
          // clear filter
          $("#filter-settings").val("");
          // show the element
          $(this).show();
          // show the fieldsets
          $(formId + " fieldset").show();
        });
      };

      // check the parent for visible children
      $scope.filterParent = function (element) {
        var doFilterMain = true;
        // go back to the fieldset
        var fieldsetParent = element.parent().parent();
        // check for UI settings
        if (fieldsetParent.prop("nodeName").toLowerCase() != "fieldset") {
          fieldsetParent = element.parent();
          doFilterMain = false;
        }
        // reset
        fieldsetParent.show();
        fieldsetParent.parent().show();
        // count visible elements
        var counter = fieldsetParent.children("div").children(":visible").length;

        if (counter > 0) {
          fieldsetParent.show();
        } else {
          fieldsetParent.hide();
        }
        if (doFilterMain) {
          $scope.filterMain(fieldsetParent);
        }
      };

      // filter the main parent (for Settings)
      $scope.filterMain = function (element) {
        var parentMain = element.parent();
        var counter = parentMain.children("fieldset:visible").length;

        if (counter > 0) {
          parentMain.show();
        } else {
          parentMain.hide();
        }
      };

      $scope.testMailConfiguration = function () {
        $http.get("../api/tools/mail/test").then(
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: response.data,
              timeout: 2
            });
          },
          function (response) {
            $rootScope.$broadcast("StatusUpdated", {
              title: response.data,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };
      var buildUrl = function (settings) {
        var port = filterBySection(settings, "system/server/port")[0].value;
        var host = filterBySection(settings, "system/server/host")[0].value;
        var protocol = filterBySection(settings, "system/server/protocol")[0].value;

        return (
          protocol + "://" + host + (isPortRequired(procotol, port) ? ":" + port : "")
        );
      };

      var isPortRequired = function (protocol, port) {
        if (protocol == "http" && port == "80") {
          return false;
        } else if (protocol == "https" && port == "443") {
          return false;
        } else {
          return true;
        }
      };

      /**
       * Execute Atom feed harvester
       */
      $scope.executeAtomHarvester = function () {
        return $http.get("../api/atom/scan").then(
          function (response) {
            $scope.loadTplReport = response.data;

            $("#atomHarvesterModal").modal();
          },
          function (response) {
            $scope.loadTplReport = response.data;

            $("#atomHarvesterModal").modal();
          }
        );
      };

      /**
       * Scroll to an element.
       */
      $scope.scrollTo = gnUtilityService.scrollTo;

      loadUsers();
      loadSettings();
    }
  ]);

  /**
   * GnMapContextRecordController provides de search object to query
   * metadata with public (download privilege to group ALL)
   * map context resources (OGC:OWS-C protocol).
   */
  module.controller("GnMapContextRecordController", [
    "$scope",
    "gnGlobalSettings",
    function ($scope, gnGlobalSettings) {
      $scope.searchObj = {
        internal: true,
        any: "",
        defaultParams: {
          any: "",
          from: 1,
          to: 50,
          op1: 1,
          linkProtocol: "OGC:OWS-C",
          sortBy: "resourceTitleObject.default.sort",
          sortOrder: "asc"
        }
      };
      $scope.searchObj.params = angular.extend({}, $scope.searchObj.defaultParams);
      $scope.updateParams = function () {
        $scope.searchObj.params.any = $scope.searchObj.any;
      };
    }
  ]);
})();
