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
  goog.provide("gn_formfields_directive");

  angular
    .module("gn_formfields_directive", [])
    /**
     * @ngdoc directive
     * @name gn_formfields.directive:gnTypeahead
     * @restrict A
     *
     * @description
     * It binds a tagsinput to the input for multi select.
     * By default, the list is shown on click even if the input value is
     * empty.
     */
    .directive("gnTypeahead", [
      function () {
        /**
         * If data are prefetched, get the label from the value
         * Uses for model -> ui
         * @param {array} a
         * @param {string} v
         * @return {string|undefined}
         */
        var findLabel = function (a, v) {
          for (var i = 0; i < a.length; i++) {
            if (a[i].id == v) {
              return a[i].name;
            }
          }
        };

        return {
          restrict: "A",
          scope: {
            options: "=gnTypeahead",
            gnValues: "="
          },
          link: function (scope, element, attrs) {
            var config = scope.options.config || {};
            var doLink = function (data, remote) {
              var bloodhoundConf = {
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace("name"),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                limit: 10000000,
                sorter: function (a, b) {
                  if (a.name < b.name) return -1;
                  else if (a.name > b.name) return 1;
                  else return 0;
                }
              };

              if (data) {
                bloodhoundConf.local = data;
              } else if (remote) {
                bloodhoundConf.remote = remote;
                // Remove from suggestion values already selected in remote mode
                if (angular.isFunction(remote.filter)) {
                  var filterFn = remote.filter;
                  bloodhoundConf.remote.filter = function (data) {
                    var filtered = filterFn(data);
                    var datums = [];
                    for (var i = 0; i < filtered.length; i++) {
                      if (stringValues.indexOf(filtered[i].id) < 0) {
                        datums.push(filtered[i]);
                      }
                    }
                    return datums;
                  };
                }
              }
              var engine = new Bloodhound(bloodhoundConf);
              engine.initialize();

              // Remove from suggestion values already selected for local mode
              var refreshDatum = function () {
                if (bloodhoundConf.local) {
                  engine.clear();
                  for (var i = 0; i < data.length; i++) {
                    if (stringValues.indexOf(data[i].id) < 0) {
                      engine.add(data[i]);
                    }
                  }
                }
              };

              // Initialize tagsinput in the element
              $(element).tagsinput({
                itemValue: "id",
                itemText: "name"
              });

              // Initialize typeahead
              var field = $(element).tagsinput("input");
              field
                .typeahead(
                  {
                    minLength: 0,
                    hint: scope.$eval(attrs.gnTypeaheadDisableHint) ? false : true,
                    highlight: true
                  },
                  angular.extend(
                    {
                      name: "datasource",
                      displayKey: "name",
                      limit: Infinity,
                      source: data ? allOrSearchFn : engine.ttAdapter()
                    },
                    config
                  )
                )
                .on("typeahead:selected", function (event, datum) {
                  field.typeahead("val", "");
                  $(element).tagsinput("add", datum);
                  field.data("ttTypeahead").input.trigger("queryChanged");
                });

              function allOrSearchFn(q, sync) {
                if (q === "") {
                  sync(engine.all());
                  // This is the only change needed to get 'ALL'
                  // items as the defaults
                } else {
                  engine.search(q, sync);
                }
              }

              /** Binds input content to model values */
              var stringValues = [];
              var prev = stringValues.slice();

              // ui -> model
              $(element).on("itemAdded", function (event) {
                if (stringValues.indexOf(event.item.id) === -1) {
                  stringValues.push(event.item.id);
                  prev = stringValues.slice();
                  scope.gnValues = stringValues.join(" or ");
                  scope.$apply();
                }
                refreshDatum();
              });
              $(element).on("itemRemoved", function (event) {
                var idx = stringValues.indexOf(event.item.id);
                if (idx !== -1) {
                  stringValues.splice(idx, 1);
                  prev = stringValues.slice();
                  scope.gnValues = stringValues.join(" or ");
                  scope.$apply();
                }
                refreshDatum();
              });

              scope.$on("beforeSearchReset", function () {
                field.typeahead("val", "");
                stringValues = [];
                for (var i = 0; i < prev.length; i++) {
                  $(element).tagsinput("remove", prev[i]);
                }
                prev = [];
                field.data("ttTypeahead").input.trigger("queryChanged");
              });

              // model -> ui
              scope.$watch(
                "gnValues",
                function () {
                  if (angular.isDefined(scope.gnValues) && scope.gnValues != "") {
                    stringValues = scope.gnValues.split(" or ");
                  } else {
                    stringValues = [];
                  }

                  var added = stringValues.filter(function (i) {
                      return prev.indexOf(i) === -1;
                    }),
                    removed = prev.filter(function (i) {
                      return stringValues.indexOf(i) === -1;
                    }),
                    i;
                  prev = stringValues.slice();

                  // Remove tags no longer in binded model
                  for (i = 0; i < removed.length; i++) {
                    $(element).tagsinput("remove", removed[i]);
                  }

                  // Refresh remaining tags
                  $(element).tagsinput("refresh");

                  // Add new items in model as tags
                  for (i = 0; i < added.length; i++) {
                    $(element).tagsinput("add", {
                      id: added[i],
                      name: data ? findLabel(data, added[i]) : added[i]
                    });
                  }
                },
                true
              );

              /** Manage the cross to clear the input */
              var triggerElt = $(
                '<span class="close tagsinput-trigger' + ' fa fa-ellipsis-v"></span>'
              );
              field.parent().after(triggerElt);
              var resetElt = $(
                '<span class="close ' + 'tagsinput-clear">&times;</span>'
              ).on("click", function () {
                scope.gnValues = "";
                scope.$apply();
              });
              field.parent().after(resetElt);
              resetElt.hide();

              $(element).on("change", function () {
                resetElt.toggle($(element).val() != "");
              });
            };

            if (scope.options.mode == "prefetch") {
              scope.options.promise.then(doLink);
            } else if (scope.options.mode == "remote") {
              doLink(null, scope.options.remote);
            } else if (scope.options.mode == "local") {
              doLink(scope.options.data);
            }
          }
        };
      }
    ])

    .directive("usersCombo", [
      "$http",
      function ($http) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/search/formfields/" + "partials/usersCombo.html",
          scope: {
            ownerUser: "=",
            users: "="
          },

          link: function (scope, element, attrs) {
            var url = "info?_content_type=json&type=users";

            $http.get(url, { cache: true }).then(function (response) {
              var data = response.data;

              scope.users = data !== "null" ? data.users : null;

              // Select by default the first group.
              if (
                (angular.isUndefined(scope.ownerUser) || scope.ownerUser === "") &&
                data.users &&
                data.users.length > 0
              ) {
                scope.ownerUser = data.users[0]["@id"];
              }
            });
          }
        };
      }
    ])

    .directive("groupsCombo", [
      "$http",
      function ($http) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/search/formfields/" + "partials/groupsCombo.html",
          scope: {
            ownerGroup: "=",
            lang: "=",
            groups: "=",
            disabled: "=?",
            optional: "@?",
            excludeSpecialGroups: "="
          },

          link: function (scope, element, attrs) {
            var url = "../api/groups?withReservedGroup=true";
            if (attrs.profile) {
              url = "../api/groups?profile=" + attrs.profile;
            }
            var optional = scope.optional != "false" ? true : false;
            var setDefaultValue = attrs["setDefaultValue"] == "false" ? false : true;
            scope.disabled = scope.disabled ? true : false;
            scope.selectedGroup = null;
            function setSelected(group) {
              var groupId = parseInt(group);
              if (groupId != NaN) {
                scope.selectedGroup = scope.groups.find(function (v) {
                  return v.id === groupId || v["@id"] === groupId;
                });
              }
            }

            $http.get(url, { cache: true }).then(function (response) {
              var data = response.data;

              //data-ng-if is not correctly updating groups.
              //So we do the filter here
              if (scope.excludeSpecialGroups) {
                scope.groups = [];
                angular.forEach(data, function (g) {
                  if (g.id > 1) {
                    scope.groups.push(g);
                  }
                });
              } else {
                scope.groups = data;
              }

              if (optional) {
                scope.groups.unshift({
                  id: undefined,
                  name: ""
                });
              }

              setSelected(scope.ownerGroup);
              if (setDefaultValue && scope.selectedGroup === undefined) {
                scope.selectedGroup = scope.groups[0];
              } else if (scope.selectedGroup === undefined) {
                scope.selectedGroup = scope.groups[0];
              }

              scope.$watch("selectedGroup", function (n, o) {
                if (n && (n.hasOwnProperty("id") || n.hasOwnProperty("@id"))) {
                  scope.ownerGroup = scope.selectedGroup["@id"] || scope.selectedGroup.id;
                }
              });
              scope.$watch("ownerGroup", function (n, o) {
                if (n !== o) {
                  setSelected(scope.ownerGroup);
                }
              });
            });
          }
        };
      }
    ])

    .directive("protocolsCombo", [
      "$http",
      "gnSchemaManagerService",
      function ($http, gnSchemaManagerService) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/search/formfields/" +
            "partials/protocolsCombo.html",
          scope: {
            protocol: "=",
            lang: "="
          },
          controller: [
            "$scope",
            "$translate",
            function ($scope, $translate) {
              var config = "iso19139|gmd:protocol|||";
              gnSchemaManagerService.getElementInfo(config).then(function (data) {
                $scope.protocols = data.helper ? data.helper.option : null;
              });
            }
          ]
        };
      }
    ])

    .directive("sortbyCombo", [
      "$translate",
      "hotkeys",
      "gnGlobalSettings",
      function ($translate, hotkeys, gnGlobalSettings) {
        return {
          restrict: "A",
          require: "^ngSearchForm",
          templateUrl: function (elem, attrs) {
            return (
              attrs.template ||
              "../../catalog/components/search/formfields/" + "partials/sortByCombo.html"
            );
          },
          scope: {
            params: "=",
            values: "=gnSortbyValues"
          },
          link: function (scope, element, attrs, searchFormCtrl) {
            scope.lang = attrs["lang"] || gnGlobalSettings.gnCfg.langDetector.default;
            scope.sortBy = function (v) {
              angular.extend(scope.params, v);
              searchFormCtrl.triggerSearch(true);
            };
            // Replace the placeholder for the language in the sort field name
            for (var i = 0; i < scope.values.length; i++) {
              scope.values[i].sortBy = scope.values[i].sortBy.replace(
                "${searchLang}",
                "lang" + scope.lang
              );
            }

            hotkeys.bindTo(scope).add({
              combo: "s",
              description: $translate.instant("hotkeySortBy"),
              callback: function () {
                for (var i = 0; i < scope.values.length; i++) {
                  if (scope.values[i].sortBy === scope.params.sortBy) {
                    var nextOptions = i === scope.values.length - 1 ? 0 : i + 1;
                    scope.sortBy(scope.values[nextOptions]);
                    return;
                  }
                }
              }
            });
          }
        };
      }
    ])

    .directive("hitsperpageCombo", [
      function () {
        return {
          restrict: "A",
          require: "^ngSearchForm",
          templateUrl:
            "../../catalog/components/search/formfields/" +
            "partials/hitsperpageCombo.html",
          scope: {
            pagination: "=paginationCfg",
            values: "=gnHitsperpageValues"
          },
          link: function (scope, element, attrs, searchFormCtrl) {
            scope.updatePagination = function () {
              searchFormCtrl.resetPagination();
              searchFormCtrl.triggerSearch();
            };
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_formfields.directive:gnSearchSuggest
     * @restrict A
     *
     * @description
     * Add a multiselect typeahead based input for suggestion.
     * It binds a tagsinput to the input for multi select.
     * It uses typeahead to retrieve and display suggestions from the geonetwork
     * open suggestion service `suggest`, in remote mode.
     * The index fields for the suggestion is given by the `gnSearchSuggest`
     * attribute.
     * By default, the list is not shown on click even if the input value is
     * empty.
     */
    .directive("gnSearchSuggest", [
      "suggestService",
      function (suggestService) {
        return {
          restrict: "A",
          scope: {
            field: "@gnSearchSuggest",
            startswith: "@gnSearchSuggestStartswith",
            multi: "@"
          },
          link: function (scope, element, attrs) {
            var remote = {
              url: suggestService.getUrl(
                "QUERY",
                scope.field,
                scope.startswith ? "STARTSWITHFIRST" : "ALPHA"
              ),
              filter: suggestService.filterResponse,
              wildcard: "QUERY"
            };
            if (angular.isUndefined(scope.multi)) {
              element.typeahead({
                remote: remote
              });
            } else {
              element.tagsinput({});
              element
                .tagsinput("input")
                .typeahead({
                  remote: remote
                })
                .bind(
                  "typeahead:selected",
                  $.proxy(function (obj, datum) {
                    this.tagsinput("add", datum.value);
                    this.tagsinput("input").typeahead("setQuery", "");
                  }, element)
                );
            }
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_formfields.directive:gnRegionMultiselect
     * @restrict A
     *
     * @description
     * Add a multiselect typeahead based input for regions.
     * It binds a tagsinput to the input for multi select.
     * It calls the region service once on init, to feed the list, then
     * use typeahead in local mode to display region suggestions.
     * The type of regions to match is given by the `gnRegionMultiselect`
     * attribute.
     * By default, the list is shown on click even if the input value is
     * empty.
     */

    .directive("gnRegionMultiselect", [
      "gnRegionService",
      function (gnRegionService) {
        return {
          restrict: "A",
          scope: {
            field: "@gnRegionMultiselect",
            callback: "=gnCallback"
          },
          link: function (scope, element, attrs) {
            var type = {
              id: "http://geonetwork-opensource.org/regions#" + scope.field
            };
            gnRegionService.loadRegion(type, "fre").then(function (data) {
              $(element).tagsinput({
                itemValue: "id",
                itemText: "name"
              });
              var field = $(element).tagsinput("input");
              field
                .typeahead({
                  valueKey: "name",
                  local: data,
                  minLength: 0,
                  limit: 5
                })
                .on("typeahead:selected", function (event, datum) {
                  $(element).tagsinput("add", datum);
                  field.typeahead("setQuery", "");
                });

              $("input.tt-query").on("click", function () {
                var $input = $(this);

                // these are all expected to be objects
                // so falsey check is fine
                if (
                  !$input.data() ||
                  !$input.data().ttView ||
                  !$input.data().ttView.datasets ||
                  !$input.data().ttView.dropdownView ||
                  !$input.data().ttView.inputView
                ) {
                  return;
                }

                var ttView = $input.data().ttView;

                var toggleAttribute = $input.attr("data-toggled");

                if (!toggleAttribute || toggleAttribute === "off") {
                  $input.attr("data-toggled", "on");

                  $input.typeahead("setQuery", "");

                  if ($.isArray(ttView.datasets) && ttView.datasets.length > 0) {
                    // only pulling the first dataset for this hack
                    var fullSuggestionList = [];
                    // renderSuggestions expects a
                    // suggestions array not an object
                    $.each(ttView.datasets[0].itemHash, function (i, item) {
                      fullSuggestionList.push(item);
                    });

                    ttView.dropdownView.renderSuggestions(
                      ttView.datasets[0],
                      fullSuggestionList
                    );
                    ttView.inputView.setHintValue("");
                    ttView.dropdownView.open();
                  }
                } else if (toggleAttribute === "on") {
                  $input.attr("data-toggled", "off");
                  ttView.dropdownView.close();
                }
              });
            });
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_formfields.directive:schemaInfoCombo
     * @restrict A
     * @requires gnSchemaManagerService
     * @requires $http
     * @requires gnCurrentEdit
     *
     * @description
     * The `schemaInfoCombo` directive provides a combo box based on
     * a codelist (<schema>/loc/<lang>/codelist.xml) or an
     * element helper (<schema>/loc/<lang>/helper.xml) from a
     * schema plugins.
     *
     * The combo initialization is made on mouseover in order to not
     * link all combos (including hidden one) on load.
     *
     * The gn-schema-info-combo attribute can contains one of the
     * registered element (which are profile dependant ie. protocol,
     * associationType, initiativeType) or any element name with
     * namespace prefix eg. 'gmd:protocol'.
     *
     * The schema used to retrieve the element info is based on
     * the gnCurrentEdit object or 'iso19139' if not defined.
     */
    .directive("schemaInfoCombo", [
      "$http",
      "gnSchemaManagerService",
      "gnCurrentEdit",
      "$translate",
      function ($http, gnSchemaManagerService, gnCurrentEdit, $translate) {
        return {
          restrict: "A",
          replace: true,
          templateUrl:
            "../../catalog/components/search/formfields/" +
            "partials/schemainfocombo.html",
          scope: {
            selectedInfo: "=",
            lang: "=",
            extraOptions: "=",
            allowBlank: "@",
            infos: "=?schemaInfoComboValues"
          },
          link: function (scope, element, attrs) {
            var initialized = false;
            var baseList = null;
            var defaultValue;
            var allowBlank = angular.fromJson(attrs["allowBlank"]) == true;

            var addBlankValueAndSetDefault = function () {
              var blank = { label: "", code: "" },
                isCurrentValueInList = false;
              if (scope.infos != null && scope.infos.length && allowBlank) {
                scope.infos.unshift(blank);
              }
              // Search default value
              angular.forEach(scope.infos, function (h) {
                var id = h.code || h.value; // codelist or helper
                if (h.isDefault === true) {
                  defaultValue = id;
                }
                if (scope.selectedInfo != "" && id === scope.selectedInfo) {
                  isCurrentValueInList = true;
                }
              });

              // Add an option if the current value is not in the list
              if (scope.infos && !isCurrentValueInList) {
                scope.infos.push({ label: scope.selectedInfo, code: scope.selectedInfo });
              }

              // If no blank value allowed select default or first
              // If no value defined, select default or blank one
              if (!angular.isDefined(scope.selectedInfo)) {
                scope.selectedInfo =
                  defaultValue ||
                  (scope.infos && scope.infos.length > 0
                    ? scope.infos[0].code
                    : defaultValue);
              }
              // This will avoid to have undefined selected option
              // on top of the list.
            };

            scope.gnCurrentEdit = gnCurrentEdit;
            scope.$watch("gnCurrentEdit.schema", function (newValue, oldValue) {
              if (!initialized && angular.isDefined(newValue)) {
                init();
              }
            });

            scope.codelistFilter = "";
            scope.$watch("gnCurrentEdit.codelistFilter", function (n, o) {
              if (n && n !== o) {
                scope.codelistFilter = n;
                init();
              }
            });

            scope.$watch("extraOptions", function (n, o) {
              isLabelSet = false;
              appendExtraOptions();
            });
            scope.$watch("selectedInfo", function (n, o) {
              scope.infos = angular.copy(baseList);
              appendExtraOptions();
              addBlankValueAndSetDefault();
            });

            var isLabelSet = false;
            function appendExtraOptions() {
              if (baseList) {
                // Only add the header options if there are extra options defined.
                if (
                  angular.isArray(scope.extraOptions) &&
                  scope.extraOptions.length > 0
                ) {
                  if (!isLabelSet) {
                    scope.extraOptions.unshift({
                      value: "",
                      label: $translate.instant("recordFormats"),
                      disabled: true
                    });
                    scope.extraOptions.push({
                      value: "",
                      label: $translate.instant("commonProtocols"),
                      disabled: true
                    });
                    isLabelSet = true;
                  }
                  scope.infos = scope.extraOptions.concat(baseList);
                } else {
                  isLabelSet = true;
                }
              }
            }

            var init = function () {
              var schema = attrs["schema"] || gnCurrentEdit.schema || "iso19139";
              var config = schema + "|" + attrs["gnSchemaInfo"] + "|||";

              scope.type = attrs["schemaInfoCombo"];
              if (scope.type == "codelist") {
                gnSchemaManagerService
                  .getCodelist(config, scope.codelistFilter)
                  .then(function (data) {
                    scope.infos = angular.copy(data.entry);
                    baseList = angular.copy(scope.infos);
                    appendExtraOptions();
                    addBlankValueAndSetDefault();
                  });
              } else if (scope.type == "element") {
                gnSchemaManagerService
                  .getElementInfo(config, scope.codelistFilter)
                  .then(function (data) {
                    scope.infos = data.helper ? data.helper.option : null;
                    baseList = angular.copy(scope.infos);
                    appendExtraOptions();
                    addBlankValueAndSetDefault();
                  });
              }
              initialized = true;
            };
            // List is initialized only on mouseover
            // To not do it on page load eg. in associated
            // resource panel
            element.bind("mouseover", function () {
              if (!initialized) {
                init();
              }
            });
            // ... or you can force init on load
            // eg. on thesaurus admin
            if (attrs.initOnLoad) {
              if (!initialized) {
                init();
              }
            }
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_formfields.directive:gnRecordtypesCombo
     * @restrict A
     *
     * @description
     * Provide a select input for all types of record
     *  - template
     *  - metadata
     *  - subtemplate
     */
    .directive("gnRecordtypesCombo", [
      "$http",
      function ($http) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/search/formfields/" +
            "partials/recordTypesCombo.html",
          scope: {
            template: "=gnRecordtypesCombo"
          },

          link: function (scope, element, attrs) {
            scope.recordTypes = [
              { key: "METADATA", value: "METADATA" },
              { key: "TEMPLATE", value: "TEMPLATE" },
              { key: "SUB_TEMPLATE", value: "SUB_TEMPLATE" },
              { key: "TEMPLATE_OF_SUB_TEMPLATE", value: "TEMPLATE_OF_SUB_TEMPLATE" }
            ];
          }
        };
      }
    ])

    /**
     * @ngdoc directive
     * @name gn_formfields.directive:gnBboxInput
     * @restrict A
     * @requires gnMap
     * @requires olDecorateInteraction
     *
     * @description
     * The `gnBboxInput` directive provides an input widget for bounding boxes.
     */
    .directive("gnBboxInput", [
      "gnMap",
      "olDecorateInteraction",
      function (gnMap, goDecoI) {
        var extentFromValue = function (str) {
          if (str === undefined || str === "") {
            return ["", "", "", ""];
          }
          return str.split(",").map(function (val) {
            return parseFloat(val);
          });
        };

        var valueFromExtent = function (extent) {
          return extent.join(",");
        };

        return {
          restrict: "AE",
          scope: {
            crs: "=?",
            value: "=",
            required: "=",
            map: "=",
            readOnly: "<"
          },
          templateUrl:
            "../../catalog/components/search/formfields/" + "partials/bboxInput.html",

          link: function (scope, element, attrs) {
            var crs = scope.crs || "EPSG:4326";
            scope.extent = extentFromValue(scope.value);

            var style = new ol.style.Style({
              fill: new ol.style.Fill({
                color: "rgba(255,0,0,0.2)"
              }),
              stroke: new ol.style.Stroke({
                color: "#FF0000",
                width: 1.25
              })
            });

            // Create overlay to show bbox
            var layer = new ol.layer.Vector({
              source: new ol.source.Vector({
                useSpatialIndex: false
              }),
              style: style,
              updateWhileAnimating: true,
              updateWhileInteracting: true
            });

            var dragboxInteraction = new ol.interaction.DragBox({
              className: "gnbbox-dragbox"
            });
            scope.map.addInteraction(dragboxInteraction);
            layer.setMap(scope.map);

            var clearMap = function () {
              layer.getSource().clear();
            };

            dragboxInteraction.on("boxstart", clearMap);
            goDecoI(dragboxInteraction);
            dragboxInteraction.active = false;

            scope.clear = function () {
              scope.valueInternalChange = true;
              scope.value = "";
              scope.extent = extentFromValue(scope.value);
              scope.updateMap();
            };

            scope.updateMap = function () {
              layer.getSource().clear();
              if (scope.extent == ["", "", "", ""]) {
                return;
              }
              var coordinates, geom, f;
              coordinates = gnMap.getPolygonFromExtent(scope.extent);
              geom = new ol.geom.Polygon(coordinates).transform(
                crs,
                scope.map.getView().getProjection()
              );
              f = new ol.Feature();
              f.setGeometry(geom);
              layer.getSource().addFeature(f);
            };

            dragboxInteraction.on("boxend", function () {
              dragboxInteraction.active = false;
              var g = dragboxInteraction.getGeometry().clone();
              var geom = g.transform(scope.map.getView().getProjection(), crs);
              var extent = geom.getExtent();
              scope.extent = extent.map(function (coord) {
                return Math.round(coord * 10000) / 10000;
              });
              scope.onBboxChange();

              scope.$apply();
            });
            scope.dragboxInteraction = dragboxInteraction;

            scope.onBboxChange = function () {
              scope.valueInternalChange = true;
              scope.value = valueFromExtent(scope.extent);
              scope.updateMap();
            };

            element.on("$destroy", function () {
              clearMap();
              scope.map.removeLayer(layer);
            });

            // watch external change of value
            if (scope.$eval(attrs["watchValueChange"])) {
              scope.$watch("value", function (newValue) {
                if (scope.valueInternalChange) {
                  scope.valueInternalChange = false;
                } else {
                  scope.extent = extentFromValue(newValue);
                  scope.updateMap();
                }
              });
            }
          }
        };
      }
    ]);
})();
