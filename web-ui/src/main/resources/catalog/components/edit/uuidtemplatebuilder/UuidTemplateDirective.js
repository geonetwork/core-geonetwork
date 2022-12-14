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
  goog.provide("gn_uuid_template_builder_directive");

  var module = angular.module("gn_uuid_template_builder_directive", []);

  module.directive("gnUuidTemplateBuilder", [
    "$http",
    "gnUtilityService",
    function ($http, gnUtilityService) {
      return {
        restrict: "A",
        scope: {
          uuidDetails: "=gnUuidTemplateBuilder",
          userDefinedOnly: "@userDefinedOnly",
          cb: "&"
        },
        templateUrl:
          "../../catalog/components/edit/uuidtemplatebuilder/partials/uuidtemplate.html",
        link: function (scope, element, attrs) {
          scope.mdIdentifierTemplates = {};
          scope.selectedTemplate = undefined;
          scope.mdIdentifierTemplateTokens = {};
          scope.mdIdentifierFieldsFilled = false;

          function loadMetadataIdentifierTemplates() {
            $http
              .get(
                "../api/identifiers" +
                  (scope.userDefinedOnly === "true" ? "?userDefinedOnly=true" : "")
              )
              .success(function (data) {
                scope.mdIdentifierTemplates = data;
                if (data.length > 0) {
                  scope.selectedTemplate = scope.mdIdentifierTemplates[0];
                } else {
                  console.warn(
                    "No template identifier defined. Check Admin Console > Metadata > Metadata identifier configuration."
                  );
                }
              });
          }

          String.prototype.replaceAll = function (find, replace) {
            var str = this;
            return str.replace(
              new RegExp(find.replace(/[-\/\\^$*+?.()|[\]{}]/g, "\\$&"), "g"),
              replace
            );
          };

          /**
           * Executed when the metadata identifier template is changed.
           * Creates the model with the tokens of the template,
           * to fill from the template fields in the form.
           *
           */
          loadTemplateToken = function (n, o) {
            if (n == o) return;

            scope.mdIdentifierTemplateTokens = {};

            if (scope.selectedTemplate.template != "") {
              var tokens = scope.selectedTemplate.template.match(/\{(.+?)\}/g);

              if (tokens) {
                for (var i = 0; i < tokens.length; i++) {
                  var labelValue = tokens[i].replace("{", "").replace("}", ""),
                    isUuid = labelValue === "UUID";

                  scope.mdIdentifierTemplateTokens[i] = {
                    label: labelValue,
                    value: isUuid ? gnUtilityService.randomUuid() : "",
                    readonly: isUuid
                  };
                }
              }
            }

            scope.buildIdentifier();
          };

          scope.$watch("selectedTemplate", loadTemplateToken);

          /**
           * Updates the metadata identifier template label
           * with the values filled by the user.
           *
           */
          scope.buildIdentifier = function () {
            scope.mdIdSelectedTemplateForLabel = scope.selectedTemplate.template;
            var isValid = true;

            for (key in scope.mdIdentifierTemplateTokens) {
              if (scope.mdIdentifierTemplateTokens[key].value) {
                var labelKey = scope.mdIdentifierTemplateTokens[key].label;

                scope.mdIdSelectedTemplateForLabel =
                  scope.mdIdSelectedTemplateForLabel.replace(
                    "{" + labelKey + "}",
                    scope.mdIdentifierTemplateTokens[key].value
                  );
              } else {
                isValid = false;
              }
            }

            scope.mdIdSelectedTemplateForLabel = scope.mdIdSelectedTemplateForLabel
              .replaceAll("{", " ")
              .replaceAll("}", " ");

            scope.uuidDetails.uuid = scope.mdIdSelectedTemplateForLabel;
            scope.uuidDetails.isValid = isValid;
            scope.uuidDetails.template = scope.selectedTemplate;
          };

          scope.$watchCollection("uuidDetails", function (n, o) {
            if (n !== o && scope.cb) {
              scope.cb(n);
            }
          });

          loadMetadataIdentifierTemplates();
        }
      };
    }
  ]);
})();
