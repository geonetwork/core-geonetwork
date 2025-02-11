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
  goog.provide("gn_filestore_directive");

  /**
   */
  angular
    .module("gn_filestore_directive", ["blueimp.fileupload"])
    /**
     * Upload a file in the filestore
     */
    .directive("gnDataUploaderButton", [
      "gnCurrentEdit",
      "$rootScope",
      "$translate",
      function (gnCurrentEdit, $rootScope, $translate) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/filestore/" + "partials/dataUploaderButton.html",
          scope: {
            btnLabel: "=?gnDataUploaderButton",
            isOverview: "=?isOverview",
            uploadOptions: "=?",
            fileTypes: "=?",
            visibility: "@?",
            afterUploadCb: "&?",
            afterUploadErrorCb: "&?"
          },
          link: function (scope, element, attrs) {
            scope.uuid = undefined;
            scope.gnCurrentEdit = gnCurrentEdit;
            scope.lang = scope.$parent.lang;
            scope.id = Math.random();
            scope.fileTypes = scope.fileTypes || "*.*";
            scope.autoUpload =
              angular.isUndefined(attrs["autoUpload"]) || attrs["autoUpload"] == "true";
            scope.visibility = angular.isUndefined(attrs["visibility"])
              ? "public"
              : attrs["visibility"];
            scope.queue = [];
            scope.singleUpload = true;

            var input = element.find("input");
            if (
              angular.isDefined(scope.uploadOptions) &&
              scope.uploadOptions.singleUpload === false
            ) {
              input.attr("multiple", "multiple");
            }

            var uploadFile = function () {
              scope.queue = [];
              scope.filestoreUploadOptions = angular.extend(
                {
                  singleUpload: scope.singleUpload,
                  autoUpload: scope.autoUpload,
                  url:
                    "../api/records/" +
                    gnCurrentEdit.uuid +
                    "/attachments?visibility=" +
                    (scope.visibility || "public"),
                  dropZone: $("#gn-upload-" + scope.id),
                  // TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
                  done: uploadResourceSuccess,
                  fail: uploadResourceFailed,
                  headers: { "X-XSRF-TOKEN": $rootScope.csrf }
                },
                scope.uploadOptions || {}
              );
            };

            var unregisterWatch = scope.$watch("gnCurrentEdit.uuid", function (n, o) {
              if ((n && angular.isUndefined(scope.uuid)) || (n && n != o)) {
                scope.uuid = n;
                uploadFile();
                unregisterWatch();
              }
            });

            var uploadResourceSuccess = function (e, data) {
              $rootScope.$broadcast("gnFileStoreUploadDone");
              if (scope.afterUploadCb && angular.isFunction(scope.afterUploadCb())) {
                scope.afterUploadCb()(data.response().jqXHR.responseJSON);
              }
              scope.clear(scope.queue);
            };

            var uploadResourceFailed = function (e, data) {
              var message =
                data.errorThrown +
                angular.isDefined(data.response().jqXHR.responseJSON.message)
                  ? data.response().jqXHR.responseJSON.message
                  : "";

              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("resourceUploadError"),
                error: {
                  message:
                    message === "ResourceAlreadyExistException"
                      ? $translate.instant("uploadedResourceAlreadyExistException", {
                          file: data.files[0].name
                        })
                      : message
                },
                timeout: 0,
                type: "danger"
              });
              if (
                scope.afterUploadErrorCb &&
                angular.isFunction(scope.afterUploadErrorCb())
              ) {
                scope.afterUploadErrorCb()(message);
              }
            };
          }
        };
      }
    ])
    .directive("gnFileStore", [
      "gnFileStoreService",
      "gnOnlinesrc",
      "gnCurrentEdit",
      "$translate",
      "$rootScope",
      "$parse",
      function (
        gnfilestoreService,
        gnOnlinesrc,
        gnCurrentEdit,
        $translate,
        $rootScope,
        $parse
      ) {
        return {
          restrict: "A",
          templateUrl: "../../catalog/components/filestore/" + "partials/filestore.html",
          scope: {
            uuid: "=gnFileStore",
            selectCallback: "&",
            filter: "="
          },
          link: function (scope, element, attrs, controller) {
            scope.autoUpload =
              angular.isUndefined(attrs["autoUpload"]) || attrs["autoUpload"] == "true";

            scope.filestoreUploadOptions = {
              autoUpload: scope.autoUpload,
              singleUpload: false
            };

            var defaultStatus = angular.isUndefined(attrs["defaultStatus"])
              ? "public"
              : attrs["defaultStatus"];
            scope.onlinesrcService = gnOnlinesrc;
            scope.gnCurrentEdit = gnCurrentEdit;

            scope.setResource = function (r) {
              scope.selectCallback({ selected: r });
            };
            scope.metadataResources = [];

            scope.loadMetadataResources = function () {
              return gnfilestoreService
                .get(scope.uuid, scope.filter)
                .then(function (response) {
                  scope.metadataResources = response.data;
                });
            };
            scope.setResourceStatus = function (r) {
              gnfilestoreService.updateStatus(r).then(
                function () {
                  scope.loadMetadataResources();
                },
                function (data) {
                  $rootScope.$broadcast("StatusUpdated", {
                    title: $translate.instant("resourceUploadError"),
                    error: {
                      message:
                        (data.errorThrown || data.statusText) +
                        (angular.isFunction(data.response)
                          ? data.response().jqXHR.responseJSON.message
                          : "")
                    },
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
            };
            scope.deleteResource = function (r) {
              gnfilestoreService.delete(r).then(scope.loadMetadataResources);
            };
            scope.$on("gnFileStoreUploadDone", scope.loadMetadataResources);

            scope.$watch("filter", function (newValue, oldValue) {
              if (angular.isDefined(scope.uuid) && newValue != oldValue) {
                scope.loadMetadataResources();
              }
            });
            scope.$watch("uuid", function (newValue, oldValue) {
              if (angular.isDefined(scope.uuid) && newValue != oldValue) {
                scope.loadMetadataResources();

                scope.queue = [];
              }
            });
          }
        };
      }
    ]);
})();
