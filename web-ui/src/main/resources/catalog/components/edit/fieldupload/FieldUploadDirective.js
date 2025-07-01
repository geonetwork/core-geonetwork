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
  goog.provide("gn_field_upload_directive");

  var module = angular.module("gn_field_upload_directive", []);

  /**
   *  Create a widget to handle editing of Anchor fields, that require to
   *  upload files to the metadata and link them.
   *
   *  Files are stored in the metadata store, the same way as
   *  when adding online resources.
   *
   *  Format of the xml snippet created:
   *
   *  <gmd:supplementalInformation>
   *    <gmx:Anchor xlink:href="http://localhost:8080/geonetwork/srv/api/
   *    records/cde2f47f-4a3f-4da1-8918-9923e7d372f1/attachments/report2019.docx">
   *        Report 2019</gmx:Anchor>
   *  </gmd:supplementalInformation>
   *
   *  The directive doesn't support multilingual fields.
   */
  module.directive("gnFieldUploadDiv", [
    "$http",
    "gnFileStoreService",
    "gnCurrentEdit",
    "gnSchemaManagerService",
    "$rootScope",
    "$translate",
    function (
      $http,
      gnFileStoreService,
      gnCurrentEdit,
      gnSchemaManagerService,
      $rootScope,
      $translate
    ) {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        scope: {
          value: "@gnFieldUploadDiv",
          label: "@label",
          elementName: "@",
          ref: "@",
          parentRef: "@"
        },
        templateUrl:
          "../../catalog/components/edit/fieldupload/partials/" + "fieldupload.html",
        link: function (scope, element, attrs) {
          // Has a reference to a file in the metadata store?
          scope.fileAvailable = false;
          scope.gnCurrentEdit = gnCurrentEdit;
          // Can modify the xlink (for external links)?
          scope.allowModifyXlink = attrs.allowModifyXlink == "true";
          scope.linkHref = attrs.linkHref;

          scope.xmlSnippetRef = scope.parentRef.replace("_", "_X") + "_replace";

          var buildXmlSnippet = function () {
            var anchorElement = "gmx:Anchor";
            var anchorElementNs = anchorElement.split(":")[0];
            var elementNs = scope.elementName.split(":")[0];
            var elementXlinkNs = "xlink";

            // Use underscore _.escape to encode html entities in the xml snippet
            scope.xmlSnippet =
              "<" +
              scope.elementName +
              " xmlns:" +
              elementNs +
              '="' +
              gnSchemaManagerService.findNamespaceUri(elementNs, gnCurrentEdit.schema) +
              '" xmlns:' +
              elementXlinkNs +
              '="' +
              gnSchemaManagerService.findNamespaceUri(
                elementXlinkNs,
                gnCurrentEdit.schema
              ) +
              '" xmlns:' +
              anchorElementNs +
              '="' +
              gnSchemaManagerService.findNamespaceUri(
                anchorElementNs,
                gnCurrentEdit.schema
              ) +
              '">' +
              "<" +
              anchorElement +
              ' xlink:href="' +
              _.escape(scope.linkHref) +
              '">' +
              _.escape(scope.value) +
              "</" +
              anchorElement +
              ">" +
              "</" +
              scope.elementName +
              ">";
          };

          var linkFileToField = function (link) {
            var fileName = link.id.split("/").splice(2).join("/");

            scope.fileAvailable = true;
            scope.link = link;
            scope.link.name = fileName;
            scope.linkHref = link.url;

            if (scope.value === "") {
              scope.value = decodeURIComponent(fileName);
            }

            buildXmlSnippet();
          };

          var uploadResourceSuccess = function (e, data) {
            $rootScope.$broadcast("gnFileStoreUploadDone");
            scope.clear(scope.queue);
            linkFileToField(data.response().jqXHR.responseJSON);
          };

          var uploadResourceFailed = function (e, data) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("resourceUploadError"),
              error: {
                message:
                  data.errorThrown +
                  angular.isDefined(data.response().jqXHR.responseJSON.message)
                    ? data.response().jqXHR.responseJSON.message
                    : ""
              },
              timeout: 0,
              type: "danger"
            });
            scope.clear(scope.queue);
          };

          // Upload file once dropped or selected
          var uploadFile = function () {
            scope.queue = [];
            scope.filestoreUploadOptions = {
              autoUpload: true,
              url:
                "../api/records/" +
                encodeURIComponent(gnCurrentEdit.uuid) +
                "/attachments?visibility=public",
              dropZone: $("#gn-overview-dropzone"),
              singleUpload: true,
              // TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
              done: uploadResourceSuccess,
              fail: uploadResourceFailed,
              headers: { "X-XSRF-TOKEN": $rootScope.csrf }
            };
          };

          var isFileFromMetataStore = function (url) {
            return url.match(".*/api/records/(.*)/attachments/.*") != null;
          };

          scope.hasToDisplayFileChooser = function () {
            return !(
              scope.fileAvailable ||
              (!scope.fileAvailable && !scope.allowModifyXlink && scope.linkHref != "")
            );
          };

          scope.removeFile = function (file) {
            var url = file.url;

            if (isFileFromMetataStore(url)) {
              // A thumbnail from the filestore
              gnFileStoreService.delete({ url: url }).then(function () {
                // then remove from record
                scope.linkHref = "";
                scope.fileAvailable = false;
                buildXmlSnippet();
              });
            } else {
              scope.linkHref = "";
              scope.fileAvailable = false;
              buildXmlSnippet();

              $scope.$apply();
            }
          };

          scope.$watch("gnCurrentEdit.uuid", function (newValue, oldValue) {
            scope.uuid = newValue;
            uploadFile();

            if (isFileFromMetataStore(scope.linkHref)) {
              // Retrieve the file if it's a file stored in the metadata store
              gnFileStoreService.get(scope.uuid, "").then(function (data) {
                var files = data.data;
                scope.fileAvailable = false;

                for (var i = 0; i < files.length; i++) {
                  if (files[i].url == scope.linkHref) {
                    scope.link = files[i];
                    scope.link.name = files[i].id.split("/").splice(2).join("/");
                    scope.fileAvailable = true;
                    break;
                  }
                }
              });
            } else {
              scope.link = {
                url: scope.linkHref,
                name: scope.linkHref
              };
            }
          });

          scope.$watch("value", function (newValue, oldValue) {
            if (newValue !== oldValue) {
              buildXmlSnippet();
            }
          });

          scope.$watch("linkHref", function (newValue, oldValue) {
            if (newValue !== oldValue) {
              buildXmlSnippet();
            }
          });

          buildXmlSnippet();
        }
      };
    }
  ]);
})();
