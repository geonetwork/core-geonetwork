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
  goog.provide("gn_ui_config_directive");
  goog.require("gn_timezone_selector");

  var module = angular.module("gn_ui_config_directive", [
    "ui.ace",
    "gn_timezone_selector"
  ]);

  module.directive("gnUiConfigHelp", [
    function () {
      return {
        restrict: "E",
        replace: "true",
        template:
          '<p class="help-block"' +
          "           data-ng-show=\"(('ui-' + key + '-help') | translate) != ('ui-' + key + '-help')\"" +
          "           data-ng-bind-html=\"('ui-' + key + '-help') | translate\"></p>"
      };
    }
  ]);

  module.directive("gnUiConfig", [
    "gnGlobalSettings",
    "gnProjService",
    "$translate",
    function (gnGlobalSettings, gnProjService, $translate) {
      return {
        restrict: "A",
        scope: {
          config: "=gnUiConfig",
          id: "="
        },
        templateUrl:
          "../../catalog/components/admin/uiconfig/partials/" + "uiconfig.html",
        link: function (scope, element, attrs) {
          var testAppUrl = "../../catalog/views/api/?config=";

          scope.optionsToAdd = undefined;

          var preferredKey = [
            "mods.header.languages",
            "mods.home.facetConfig",
            "mods.search.facetConfig",
            "mods.editor.facetConfig",
            "mods.search.filters"
          ];

          function buildLabel(tokens) {
            var prefix = $translate.instant(
              "ui-" +
                (tokens[0] === "mods" ? "mod" : tokens[0]) +
                (tokens.length > 1 ? "-" + tokens[1] : "")
            );
            return (
              prefix +
              (tokens.length > 2
                ? " / " + $translate.instant("ui-" + tokens[tokens.length - 1])
                : "")
            );
          }
          function collectConfigOption(jsonConfig) {
            var options = [];
            var preferredOptions = [];
            var paths = gnGlobalSettings.getObjectKeysPaths(
              jsonConfig,
              gnGlobalSettings.stopKeyList,
              false
            );
            for (var i = 0; i < paths.length; i++) {
              var p = paths[i],
                tokens = p.split("."),
                value = _.get(jsonConfig, p),
                label = buildLabel(tokens),
                g =
                  p.indexOf("mods") === 0
                    ? buildLabel(tokens.splice(0, 2))
                    : $translate.instant("ui-detectors"),
                o = {
                  path: p,
                  label: label ? label : p.split(".").splice(2).join("-"),
                  group: g,
                  defaultValue: value,
                  image: p.replace(".", "-") + ".png"
                };

              options.push(o);
              if (preferredKey.indexOf(p) !== -1) {
                preferredOptions.push(
                  angular.merge({}, o, {
                    group: $translate.instant("preferredOptions")
                  })
                );
              }
            }
            preferredOptions.push({
              path: ".",
              label: $translate.instant("ui-full-current-configuration"),
              group: $translate.instant("preferredOptions"),
              defaultValue: gnGlobalSettings.gnCfg
            });
            preferredOptions.push({
              path: ".",
              label: $translate.instant("ui-full-configuration"),
              group: $translate.instant("preferredOptions"),
              defaultValue: jsonConfig
            });
            return preferredOptions.concat(options);
          }

          function setValue(path, val, obj) {
            var fields = path.split(".");
            var result = obj;
            for (var i = 0, n = fields.length; i < n && result !== undefined; i++) {
              var field = fields[i];
              if (field === "__proto__" || field === "constructor") continue;
              if (i === n - 1) {
                result[field] = val;
              } else {
                if (typeof result[field] === "undefined" || !_.isObject(result[field])) {
                  result[field] = {};
                }
                result = result[field];
              }
            }
            return obj;
          }

          function addOptionToConfig(o, c) {
            if (c == null) {
              c = {};
            }
            if (o.path === ".") {
              return o.defaultValue;
            } else {
              return setValue(o.path, o.defaultValue, c);
            }
          }

          scope.$watch("optionsToAdd", function (n, o) {
            if (n && (n.path != (o && o.path) || n.path === ".")) {
              scope.jsonConfig = addOptionToConfig(n, scope.jsonConfig);
              scope.optionsToAdd = undefined;
            }
          });

          scope.configOptions = collectConfigOption(gnGlobalSettings.getDefaultConfig());
          scope.previousConfig = undefined;

          function init(setPrevious) {
            if (setPrevious) {
              scope.previousConfig = angular.fromJson(scope.config);
            }
            scope.jsonConfig = angular.fromJson(scope.config);
          }

          function buildFinalConfig() {
            scope.finalConfig = angular.merge(
              gnGlobalSettings.getDefaultConfig(),
              scope.jsonConfig
            );
          }

          scope.$watch(
            "config",
            function (n, o) {
              if (angular.isDefined(n) && n !== o) {
                init(o === undefined);
              }
            },
            true
          );

          scope.$watch(
            "jsonConfig",
            function (n) {
              scope.config = JSON.stringify(n, null, 2);
              buildFinalConfig();
            },
            true
          );

          scope.sortOrderChoices = ["asc", "desc"];
          scope.searchResultContactChoices = [
            "Org",
            "OrgForResource",
            "OrgForDistribution"
          ];

          // ng-model can't bind to object key, so
          // when key value change, reorganize object.
          scope.updateKey = function (obj, new_key, id) {
            var keys = Object.keys(obj);
            var values = Object.values(obj);
            if (keys.indexOf(new_key) == -1 && new_key.length > 0) {
              for (var i = 0, key; (key = keys[i]); i++) {
                delete obj[key];
              }
              keys[id] = new_key;
              for (var i = 0, key; (key = keys[i]); i++) {
                obj[key] = values[i];
              }
            }
          };

          // Add an item to an array
          // or duplicate last item multiplied by 10 (eg. hitsPerPage)
          scope.addItem = function (array, item) {
            if (angular.isArray(array)) {
              array.push(item);
            } else if (angular.isObject(array)) {
              var key = Object.keys(item)[0];
              array[key] = item[key];
            }
          };

          // Remove item from array
          scope.removeItem = function (array, index) {
            if (angular.isArray(array)) {
              array.splice(index, 1);
            } else if (angular.isObject(array)) {
              delete array[index];
            }
          };

          scope.epsgHelpers = gnProjService.helpers;

          scope.populateProjSettings = function (context) {
            gnProjService
              .getProjectionSettings(context.code)
              .then(function (data) {
                if (!data.code) return;
                for (var key in data) {
                  if (data.hasOwnProperty(key)) context[key] = data[key];
                }
              })
              .catch(function (error) {
                console.error(error.message);
              });
          };

          scope.clean = function () {
            gnGlobalSettings.cleanConfig(scope.jsonConfig);
          };
          scope.reset = function () {
            angular.extend(scope.jsonConfig, gnGlobalSettings.getDefaultConfig());
          };
          scope.currentConfig = function () {
            scope.jsonConfig = gnGlobalSettings.gnCfg;
          };
          scope.empty = function () {
            scope.jsonConfig = {};
          };
          scope.restore = function () {
            scope.jsonConfig = scope.previousConfig;
          };

          scope.testClientConfig = function () {
            window.open(
              testAppUrl + encodeURIComponent(angular.toJson(scope.jsonConfig)),
              "gnClientTestWindow"
            );
          };
        }
      };
    }
  ]);

  // used to edit an object as a JSON string
  module.directive("gnJsonEdit", function () {
    return {
      restrict: "E",
      scope: {
        value: "=model"
      },
      template:
        '<div style="height: {{height}}"' +
        '          ui-ace="{' +
        "  useWrapMode:true, " +
        "  showGutter:true, " +
        "  mode:'json'," +
        "  require: ['ace/ext/language_tools'],\n" +
        "  advanced: {\n" +
        "      enableSnippets: true,\n" +
        "      enableBasicAutocompletion: true,\n" +
        "      enableLiveAutocompletion: true\n" +
        '  }}"' +
        '          data-ng-model="asText"></div>',
      link: function (scope, element, attrs) {
        scope.height = (attrs.height || "300") + "px";

        var internalUpdate = false;

        scope.$watch(
          "value",
          function (value) {
            if (internalUpdate) {
              internalUpdate = false;
              return;
            }
            scope.asText = JSON.stringify(value, null, 2);
          },
          true
        );

        scope.$watch("asText", function (text) {
          if (!text) {
            return;
          }
          var newObj;
          try {
            newObj = JSON.parse(text);
          } catch (e) {
            console.warn("Error parsing JSON: ", e, text);
            return;
          }
          if (JSON.stringify(newObj) === JSON.stringify(scope.value)) {
            return;
          }
          for (var key in scope.value) {
            delete scope.value[key];
          }
          angular.merge(scope.value, newObj);
          internalUpdate = true;
        });
      }
    };
  });
})();
