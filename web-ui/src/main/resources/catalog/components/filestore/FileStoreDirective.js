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
   * Given the flat list of a record's attachments (each resource's `filename` may contain
   * "/"-separated folders) and the folder currently being browsed, compute a single level of the
   * folder tree: the immediate subfolders of currentFolder, and the files directly inside it.
   * No HTTP call is needed for this - the full flat list is already loaded.
   *
   * Mutates each matched file resource in place to add a `_displayName` (its filename relative
   * to currentFolder, with no further "/"), mirroring how the directive already stashes
   * transient UI state (eg. `filename_edit`) directly on resource objects.
   *
   * @param {Array} resources The flat list of resources, each with a `.filename`.
   * @param {string} currentFolder The folder being browsed ('' for the root).
   * @return {{folders: Array<string>, files: Array}} Sorted immediate subfolder names, and the
   *     resource objects that are files directly inside currentFolder.
   */
  function gnFileStoreGroupByFolder(resources, currentFolder) {
    var prefix = currentFolder ? currentFolder + "/" : "";
    var folderNames = [];
    var seenFolders = {};
    var files = [];

    angular.forEach(resources || [], function (r) {
      var relativePath = r.filename || "";
      if (prefix && relativePath.indexOf(prefix) !== 0) {
        return;
      }
      var remainder = relativePath.substring(prefix.length);
      var slashIndex = remainder.indexOf("/");
      if (slashIndex === -1) {
        r._displayName = remainder;
        files.push(r);
      } else {
        var folderName = remainder.substring(0, slashIndex);
        if (!seenFolders[folderName]) {
          seenFolders[folderName] = true;
          folderNames.push(folderName);
        }
      }
    });

    folderNames.sort();

    return {
      folders: folderNames,
      files: files
    };
  }

  /**
   * Build the "/"-separated URL path segment for a destination folder, matching the
   * POST/PUT .../attachments/{folder:.+} mapping on AttachmentsApi. Each path segment is
   * encoded individually so the "/" separators of a multi-level folder (eg. "a/b") aren't
   * escaped to %2F - mirrors how FilesystemStoreResource already builds nested-path URLs with
   * Guava's urlFragmentEscaper, which also leaves "/" unescaped.
   *
   * @param {string} folder The destination folder ('' or falsy for the root).
   * @return {string} '' at the root, otherwise "/" followed by the encoded folder path.
   */
  function gnFileStoreFolderUrlSegment(folder) {
    if (!folder) {
      return "";
    }
    return (
      "/" +
      folder
        .split("/")
        .map(function (segment) {
          return encodeURIComponent(segment);
        })
        .join("/")
    );
  }

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
            folder: "@?",
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

            var droparea = $(".file-drop-area");

            // highlight drag area
            input.on("dragenter focus click", function () {
              droparea.addClass("is-active");
            });

            // back to normal state
            input.on("dragleave blur drop", function () {
              droparea.removeClass("is-active");
            });

            var uploadFile = function () {
              scope.queue = [];
              scope.filestoreUploadOptions = angular.extend(
                {
                  singleUpload: scope.singleUpload,
                  autoUpload: scope.autoUpload,
                  url:
                    "../api/records/" +
                    gnCurrentEdit.uuid +
                    "/attachments" +
                    gnFileStoreFolderUrlSegment(scope.folder) +
                    "?visibility=" +
                    (scope.visibility || "public"),
                  dropZone: $("#gn-upload-" + scope.id),
                  pasteZone: null,
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

            // Rebuild the upload URL whenever the destination folder changes (eg. the user
            // navigates to a different folder in gnFileStore while this widget stays mounted).
            // Reassigning scope.filestoreUploadOptions to a new object (inside uploadFile) is
            // what the fileUpload directive's own $watch (jquery.fileupload-angular.js) picks up
            // to re-apply the option live to the underlying jQuery File Upload widget.
            scope.$watch("folder", function (newValue, oldValue) {
              if (newValue !== oldValue && angular.isDefined(scope.uuid)) {
                uploadFile();
              }
            });

            var humanizeDataSize = function (bytes) {
              if (bytes === 0) return "0 Bytes";
              var sizes = ["Bytes", "KB", "MB", "GB", "TB"];
              var i = Math.floor(Math.log(bytes) / Math.log(1024)); // Determine the index for sizes
              return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + " " + sizes[i]; // Format size
            };

            // Function to remove files from scope.queue that match data.files by $$hashKey
            var removeUploadedFilesFromQueue = function (data) {
              data.files.forEach(function (file) {
                for (var i = 0; i < scope.queue.length; i++) {
                  if (scope.queue[i].$$hashKey === file.$$hashKey) {
                    scope.queue.splice(i, 1);
                    break;
                  }
                }
              });
            };

            var uploadResourceSuccess = function (e, data) {
              $rootScope.$broadcast("gnFileStoreUploadDone");
              if (scope.afterUploadCb && angular.isFunction(scope.afterUploadCb())) {
                scope.afterUploadCb()(data.response().jqXHR.responseJSON);
              }
              removeUploadedFilesFromQueue(data);
            };

            var uploadResourceFailed = function (e, data) {
              var jqXHR = angular.isDefined(data.response().jqXHR)
                ? data.response().jqXHR
                : null;
              var message =
                jqXHR &&
                angular.isDefined(jqXHR.responseJSON) &&
                angular.isDefined(jqXHR.responseJSON.message)
                  ? jqXHR.responseJSON.message
                  : "";
              if (message === "" && jqXHR) {
                if (jqXHR.status === 0) {
                  // Catch 0 which is generally a network error
                  message = "uploadNetworkErrorException";
                } else if (jqXHR.status === 413) {
                  // Catch 413 which may come from a proxy server with no messages.
                  message = "uploadedResourceSizeExceededException";
                }
              }
              if (message === "" && typeof data.errorThrown === "string") {
                message = data.errorThrown;
              }

              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("resourceUploadError"),
                error: {
                  message: (function () {
                    switch (message) {
                      case "uploadNetworkErrorException":
                        return $translate.instant("uploadNetworkErrorException", {
                          file: data.files[0].name
                        });
                      case "ResourceAlreadyExistException":
                        return $translate.instant(
                          "uploadedResourceAlreadyExistException",
                          {
                            file: data.files[0].name
                          }
                        );
                      case "uploadedResourceSizeExceededException":
                        console.error(
                          "File " +
                            data.files[0].name +
                            " too large (" +
                            data.files[0].size +
                            " bytes)."
                        );
                        return $translate.instant(
                          "uploadedResourceSizeExceededException",
                          {
                            file: data.files[0].name,
                            humanizedSize: humanizeDataSize(data.files[0].size)
                          }
                        );
                      default:
                        return message;
                    }
                  })()
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
              removeUploadedFilesFromQueue(data);
            };
          }
        };
      }
    ])
    .directive("gnDataUploaderFromUrl", [
      "gnCurrentEdit",
      "$rootScope",
      "$http",
      "$translate",
      "gnUrlUtils",
      function (gnCurrentEdit, $rootScope, $http, $translate, gnUrlUtils) {
        return {
          restrict: "A",
          templateUrl:
            "../../catalog/components/filestore/partials/dataUploaderFromUrl.html",
          scope: {
            btnLabel: "=?gnDataUploaderButton",
            isOverview: "=?isOverview",
            visibility: "@?",
            folder: "@?",
            afterUploadCb: "&?",
            afterUploadErrorCb: "&?"
          },
          link: function (scope, element, attrs) {
            scope.uuid = undefined;
            scope.gnCurrentEdit = gnCurrentEdit;
            scope.lang = scope.$parent.lang;
            scope.id = Math.random();
            scope.fileTypes = scope.fileTypes || "*.*";
            scope.visibility = scope.visibility || "public";

            scope.uploadFromUrl = function () {
              var params = gnUrlUtils.toKeyValue({
                url: scope.url,
                visibility: scope.visibility
              });
              $http
                .put(
                  "../api/records/" +
                    gnCurrentEdit.uuid +
                    "/attachments" +
                    gnFileStoreFolderUrlSegment(scope.folder) +
                    "?" +
                    params
                )
                .then(
                  function (response) {
                    $rootScope.$broadcast("gnFileStoreUploadDone");
                    if (
                      scope.afterUploadCb &&
                      angular.isFunction(scope.afterUploadCb())
                    ) {
                      scope.afterUploadCb()(response.data);
                    }
                  },
                  function (response) {
                    var message = (response.data && response.data.message) || "";
                    var errorMessage =
                      {
                        uploadNetworkErrorException: $translate.instant(
                          "uploadNetworkErrorException",
                          { file: scope.url }
                        ),
                        ResourceAlreadyExistException: $translate.instant(
                          "uploadedResourceAlreadyExistException",
                          { file: scope.url }
                        ),
                        uploadedResourceSizeExceededException: $translate.instant(
                          "uploadedResourceSizeExceededException",
                          { file: scope.url }
                        )
                      }[message] || message;

                    $rootScope.$broadcast("StatusUpdated", {
                      title: $translate.instant("resourceUploadError"),
                      error: { message: errorMessage },
                      timeout: 0,
                      type: "danger"
                    });
                    if (
                      scope.afterUploadErrorCb &&
                      angular.isFunction(scope.afterUploadErrorCb())
                    ) {
                      scope.afterUploadErrorCb()(message);
                    }
                  }
                );
            };

            var unregisterWatch = scope.$watch("gnCurrentEdit.uuid", function (n, o) {
              if ((n && angular.isUndefined(scope.uuid)) || (n && n != o)) {
                scope.uuid = n;
                unregisterWatch();
              }
            });
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
          templateUrl: "../../catalog/components/filestore/partials/filestore.html",
          scope: {
            uuid: "=gnFileStore",
            selectCallback: "&",
            filter: "=",
            layout: "@"
          },
          link: function (scope, element, attrs, controller) {
            scope.autoUpload =
              angular.isUndefined(attrs["autoUpload"]) || attrs["autoUpload"] == "true";

            scope.filestoreUploadOptions = {
              autoUpload: scope.autoUpload,
              singleUpload: false
            };

            scope.filestoreUploadOptionsSetResource = {
              autoUpload: scope.autoUpload,
              singleUpload: true
            };

            var defaultStatus = angular.isUndefined(attrs["defaultStatus"])
              ? "public"
              : attrs["defaultStatus"];
            scope.onlinesrcService = gnOnlinesrc;
            scope.gnCurrentEdit = gnCurrentEdit;
            scope.selectOptions = { current: undefined };
            scope.metadataResources = [];
            scope.editingResource = false;
            scope.currentFolder = "";
            scope.currentFolders = [];
            scope.currentFiles = [];
            scope.isFiltered = false;

            // While a filter is active, gnfilestoreService.get() already searches the whole
            // tree server-side (matched against each file's leaf name), so folder-grouping the
            // result would hide matches outside the currently browsed folder. Show a flat list
            // of every match instead, with its full relative path as the display name so folder
            // context isn't lost now that a single currentFolder no longer applies; clearing the
            // filter goes back to browsing currentFolder exactly where it was left, since it's
            // never touched while filtered.
            function updateFolderView() {
              scope.isFiltered = !!scope.filter;

              if (scope.isFiltered) {
                var matches = [];
                angular.forEach(scope.metadataResources || [], function (r) {
                  r._displayName = r.filename;
                  matches.push(r);
                });
                scope.currentFolders = [];
                scope.currentFiles = matches;
                return;
              }

              var grouped = gnFileStoreGroupByFolder(
                scope.metadataResources,
                scope.currentFolder
              );
              scope.currentFolders = grouped.folders;
              scope.currentFiles = grouped.files;
            }

            scope.openFolder = function (folderName) {
              scope.currentFolder = scope.currentFolder
                ? scope.currentFolder + "/" + folderName
                : folderName;
              updateFolderView();
            };

            scope.openParentFolder = function () {
              var lastSlash = scope.currentFolder.lastIndexOf("/");
              scope.currentFolder =
                lastSlash === -1 ? "" : scope.currentFolder.substring(0, lastSlash);
              updateFolderView();
            };

            // Folders aren't persisted objects (see the analysis report, design decision #1) -
            // "creating" one just means browsing into it, so the next upload targets that
            // destination; it only starts showing up for real once something's uploaded there.
            //
            // Kept as properties of one object (not bare scope properties) because the
            // ng-model'd input lives inside an ng-if (layout !== 'select'), which creates a
            // child scope: ng-model="newFolder.name" writes to the shared object found via the
            // prototype chain, whereas ng-model="newFolderName" would instead create a new own
            // property shadowing this scope's, invisible to createFolder() below.
            scope.newFolder = { name: "", invalid: false };

            // Mirrors the validation AbstractStore.checkResourceId applies server-side once the
            // folder is combined with a filename, so the user gets immediate feedback instead of
            // a failed upload.
            function isValidFolderSegment(name) {
              return (
                !!name &&
                name.indexOf("..") === -1 &&
                name.indexOf("//") === -1 &&
                name.charAt(0) !== "/" &&
                name.charAt(name.length - 1) !== "/"
              );
            }

            scope.createFolder = function () {
              var name = (scope.newFolder.name || "").trim();
              if (!isValidFolderSegment(name)) {
                scope.newFolder.invalid = true;
                return;
              }
              scope.newFolder.invalid = false;
              scope.newFolder.name = "";
              scope.openFolder(name);
            };

            function updateVisibilityEditingPanel(index, editing) {
              if (editing) {
                $("#resource_" + index).addClass("hidden");
                $("#resource_edit_" + index).removeClass("hidden");
              } else {
                $("#resource_" + index).removeClass("hidden");
                $("#resource_edit_" + index).addClass("hidden");
              }
            }
            // A file's own folder is derived from its filename (everything before the last "/"),
            // not from scope.currentFolder - rename works the same whether the file is reached
            // by browsing into its folder or by a whole-tree filter match (see updateFolderView).
            function folderPrefixOf(filename) {
              var lastSlash = (filename || "").lastIndexOf("/");
              return lastSlash === -1 ? "" : filename.substring(0, lastSlash);
            }

            function isValidLeafFilename(name) {
              return !!name && name.indexOf("/") === -1;
            }

            scope.editResource = function (r, index) {
              // The rename box only ever edits the leaf filename - the folder prefix (if any) is
              // kept alongside it and silently re-attached on save, so nested files aren't
              // renamed by having the user retype their whole path.
              r._renameFolderPrefix = folderPrefixOf(r.filename);
              r.filename_edit = r._renameFolderPrefix
                ? r.filename.substring(r._renameFolderPrefix.length + 1)
                : r.filename;
              r._renameOriginalLeaf = r.filename_edit;
              scope.editingResource = true;
              scope.duplicatedFilename = false;
              scope.invalidFilename = false;

              updateVisibilityEditingPanel(index, true);
            };

            scope.cancelEditResource = function (r, index) {
              delete r.filename_edit;
              delete r._renameFolderPrefix;
              delete r._renameOriginalLeaf;
              scope.duplicatedFilename = false;
              scope.invalidFilename = false;
              scope.editingResource = false;
              updateVisibilityEditingPanel(index, false);
            };

            scope.saveEditResource = function (r, index) {
              // TODO: check if the resource is already in the list and update it in the backend

              var newLeafName = (r.filename_edit || "").trim();
              if (!isValidLeafFilename(newLeafName)) {
                scope.invalidFilename = true;
                return;
              }
              scope.invalidFilename = false;

              var newFullName = r._renameFolderPrefix
                ? r._renameFolderPrefix + "/" + newLeafName
                : newLeafName;

              gnfilestoreService.get(scope.gnCurrentEdit.uuid, "").then(function (data) {
                var files = data.data;
                var fileNameExists = false;
                for (var i = 0; i < files.length; i++) {
                  // Comparing full paths (folder prefix included) is what scopes this check to
                  // files in the same folder as the one being renamed - a file with the same leaf
                  // name in a different folder has a different full path and isn't a duplicate.
                  if (
                    files[i].filename == newFullName &&
                    files[i].filename != r.filename
                  ) {
                    fileNameExists = true;
                    break;
                  }
                }

                if (!fileNameExists) {
                  scope.duplicatedFilename = false;

                  gnfilestoreService
                    .updateResourceName(scope.gnCurrentEdit.uuid, r, newFullName)
                    .then(function (response) {
                      scope.editingResource = false;
                      updateVisibilityEditingPanel(index, false);
                      scope.loadMetadataResources();

                      // Refresh the onlinesrc service to update the resource list and the metadata form
                      // with the new resource name.
                      gnOnlinesrc.refresh();
                    });
                } else {
                  scope.duplicatedFilename = true;
                }
              });
            };
            scope.setResource = function (r) {
              scope.selectCallback({ selected: r });
            };

            scope.loadMetadataResources = function () {
              return gnfilestoreService
                .get(scope.uuid, scope.filter)
                .then(function (response) {
                  scope.metadataResources = response.data;
                  updateFolderView();
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

            scope.$on("gnFileStoreUploadDone", function (evt, data) {
              if (data) {
                // Select the provided resource by the url value.
                scope.loadMetadataResources().then(function () {
                  for (var i = 0; i < scope.metadataResources.length; i++) {
                    if (scope.metadataResources[i].url === data) {
                      scope.setResource(scope.metadataResources[i]);
                      break;
                    }
                  }
                });
              } else {
                scope.loadMetadataResources();
              }
            });

            scope.$watch("filter", function (newValue, oldValue) {
              if (angular.isDefined(scope.uuid) && newValue != oldValue) {
                scope.loadMetadataResources();
              }
            });
            scope.$watch("selectOptions.current", function (newValue, oldValue) {
              if (newValue != oldValue) {
                scope.setResource(scope.selectOptions.current);
              }
            });
            scope.$watch("uuid", function (newValue, oldValue) {
              if (angular.isDefined(scope.uuid) && newValue != oldValue) {
                scope.currentFolder = "";
                scope.loadMetadataResources();

                scope.queue = [];
              }
            });
            if (angular.isDefined(scope.uuid)) {
              scope.loadMetadataResources();
            }
          }
        };
      }
    ]);
})();
