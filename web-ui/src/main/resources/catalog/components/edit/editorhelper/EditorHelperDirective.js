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
  goog.provide("gn_editor_helper");

  var module = angular.module("gn_editor_helper", []);

  /**
   * @ngdoc directive
   * @name gn_fields.directive:gnFieldSuggestions
   * @function
   *
   * @description
   * Create a list of values based on index field name
   */
  module.directive("gnFieldSuggestions", [
    "$http",
    function ($http) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/edit/" +
          "editorhelper/partials/fieldsuggestions.html",
        scope: {
          ref: "@",
          field: "@",
          fq: "@"
        },
        link: function (scope, element, attrs) {
          scope.suggestions = [];
          if (scope.field != "") {
            var url =
              "suggest?sortBy=ALPHA&maxNumberOfTerms=1000&" +
              "origin=INDEX_TERM_VALUES&field=" +
              scope.field;
            if (scope.fq != "") {
              url += "&q=" + scope.fq;
            }
            $http.get(url, { cache: true }).then(function (r) {
              scope.suggestions = r.data[1];
            });
          }

          var field = document.gnEditor[scope.ref] || $("#" + scope.ref).get(0);
          $(field).removeClass("hidden");

          var populateField = function (field, value) {
            if (field && value !== undefined) {
              // Checkpoint - Remove first token corresponding to challenge
              if (value.indexOf("|") !== -1) {
                value = value.split("|").slice(1).join("|");
              }
              field.value = field.type === "number" ? parseFloat(value) : value;
              $(field).change();
              $(field).keyup();
            }
          };

          scope.$watch("selected", function (n, o) {
            if (n && n !== o) {
              populateField(field, n);
            }
          });
        }
      };
    }
  ]);
  /**
   * @ngdoc directive
   * @name gn_editor_helper.directive:gnEditorHelper
   * @restrict A
   *
   * @description
   * Create a widget to handle a list of suggestion to help editor
   * to populate a field. Suggestions are list of values defined
   * in labels.xml for each schema.
   *
   */
  module.directive("gnEditorHelper", [
    "$timeout",
    "$translate",
    "gnCurrentEdit",
    function ($timeout, $translate, gnCurrentEdit) {
      return {
        restrict: "A",
        replace: false,
        transclude: false,
        scope: {
          mode: "@gnEditorHelper",
          ref: "@",
          type: "@",
          relatedElement: "@",
          relatedAttr: "@",
          tooltip: "@",
          multilingualField: "@"
        },
        templateUrl:
          "../../catalog/components/edit/editorhelper/partials/" + "editorhelper.html",
        link: function (scope, element, attrs) {
          // Retrieve the target field by name (general case)
          // or by id (template mode field).
          var field = document.gnEditor[scope.ref] || $("#" + scope.ref).get(0),
            relatedAttributeField = document.gnEditor[scope.relatedAttr],
            relatedElementField = document.gnEditor[scope.relatedElement],
            initialValue = field.value;

          // Function to properly set the target field value
          var populateField = function (field, value) {
            if (field && value !== undefined) {
              field.value = field.type === "number" ? parseFloat(value) : value;
              $(field).change();
              // gn-check add class on keyup event.
              $(field).keyup();
            }
          };

          // Load the config from the textarea containing the helpers
          scope.config = angular.fromJson($("#" + scope.ref + "_config")[0].value);
          if (scope.mode == "") {
            scope.config.defaultSelected = {
              "@value": "",
              "#text": $translate.instant("recommendedValues"),
              disabled: true
            };
          } else {
            scope.config.defaultSelected = {};
          }

          // If only one option, convert to an array
          if (!$.isArray(scope.config.option)) {
            scope.config.option = [scope.config.option];
          }
          if (angular.isArray(scope.config)) {
            scope.config.option = scope.config;
          }

          if (scope.mode == "") {
            // Add on top the recommended values option
            scope.config.option.unshift(scope.config.defaultSelected);
          }

          // Add record formats if any
          scope.isProtocol =
            attrs.tooltip.indexOf && attrs.tooltip.indexOf("protocol|") !== -1;
          if (scope.isProtocol && gnCurrentEdit.dataFormats.length > 0) {
            var labelPrefix = $translate.instant("recordFormatDownload");
            var formats = [
              { "#text": $translate.instant("recordFormats"), disabled: true }
            ];
            for (var j = 0; j < gnCurrentEdit.dataFormats.length; j++) {
              var f = gnCurrentEdit.dataFormats[j];
              var f = { "@value": f.value, "#text": labelPrefix + f.label };
              formats.push(f);
            }
            formats.push({
              "#text": $translate.instant("commonProtocols"),
              disabled: true
            });
            scope.config.option = formats.concat(scope.config.option);
          }

          // Check if current value is one of the suggestion
          var isInList = false;
          angular.forEach(scope.config.option, function (opt) {
            if (opt !== undefined && opt["@value"] === initialValue) {
              isInList = true;
            }
          });
          if (!isInList) {
            scope.otherValue = { "@value": initialValue };
          } else {
            scope.otherValue = { "@value": "" };
          }

          // Set the initial value
          scope.config.selected = scope.config.defaultSelected;

          scope.config.value =
            field.type === "number" ? parseFloat(field.value) : field.value;
          scope.config.layout =
            scope.mode && scope.mode.indexOf("radio") !== -1 ? "radio" : scope.mode;

          scope.selectOther = function () {
            $("#otherValue_" + scope.ref).focus();
          };
          scope.selectOtherRadio = function () {
            $("#otherValueRadio_" + scope.ref).prop("checked", true);
          };
          scope.updateWithOtherValue = function () {
            field.value = scope.otherValue["@value"];
            $(field).change();
          };
          scope.select = function (value) {
            field.value = value["@value"];
            $(field).change();
          };

          // On change event update the related element(s)
          // which is sent by the form
          scope.$watch("config.selected", function () {
            var option = scope.config.selected;
            if (option && option["@value"]) {
              // Set the current value to the selected option if not empty
              scope.config.value =
                field.type === "number" ? parseFloat(option["@value"]) : option["@value"];
              populateField(relatedAttributeField, option["@title"] || "");
              populateField(relatedElementField, option["@title"] || "");
            }
          });

          // When field value change, update the main element
          // value
          scope.$watch("config.value", function () {
            populateField(field, scope.config.value);
          });

          // In suggestion mode, existing record value
          // are preserved but user can not enter a value
          // which is not in the helper list.
          if (scope.mode === "suggestion") {
            // Init typeahead and tag input
            var initTagsInput = function () {
              var id = "#tagsinput_" + scope.ref;
              $timeout(function () {
                try {
                  $(id).tagsinput({
                    itemValue: "@value",
                    itemText: "@value"
                  });

                  // Add current value
                  var found = false;
                  for (var i = 0; i < scope.config.option.length; i++) {
                    var h = scope.config.option[i];
                    if (h["@value"] == scope.config.value) {
                      found = true;
                      $(id).tagsinput("add", h);
                      break;
                    }
                  }
                  // Add the value from the record in case it is not
                  // in the helper.
                  if (!found) {
                    $(id).tagsinput("add", { "@value": scope.config.value });
                  }

                  var field = $(id).tagsinput("input");

                  var helperAutocompleter = new Bloodhound({
                    datumTokenizer: Bloodhound.tokenizers.obj.whitespace("@value"),
                    queryTokenizer: Bloodhound.tokenizers.whitespace,
                    local: scope.config.option
                  });
                  helperAutocompleter.initialize();

                  function allOrSearchFn(q, sync) {
                    if (q === "") {
                      sync(helperAutocompleter.all());
                    } else {
                      helperAutocompleter.search(q, sync);
                    }
                  }

                  field
                    .typeahead(
                      {
                        minLength: 0,
                        highlight: true
                      },
                      {
                        name: "helper",
                        displayKey: "@value",
                        limit: scope.config.option.length,
                        source: allOrSearchFn
                      }
                    )
                    .bind(
                      "typeahead:selected",
                      $.proxy(function (obj, h) {
                        // Add to tags
                        if (this.tagsinput("items").length > 0) {
                          this.tagsinput("removeAll");
                        }
                        this.tagsinput("add", h);

                        // Update selection and snippet
                        angular.copy(this.tagsinput("items"), scope.selected);
                        scope.config.selected = h;
                        scope.$apply();

                        // Clear typeahead
                        this.tagsinput("input").typeahead("val", "");

                        // helperAutocompleter.initialize(true);
                      }, $(id))
                    );
                } catch (e) {
                  console.warn("No tagsinput for " + id + ", error: " + e.message);
                }
              });
            };

            initTagsInput();
          }
        }
      };
    }
  ]);
})();
