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
  goog.provide("gn_onlinesrc_service");

  var module = angular.module("gn_onlinesrc_service", []);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_onlinesrc.service:gnOnlinesrc
   * @requires gnBatchProcessing
   * @requires gnHttp
   * @requires gnEditor
   * @requires gnCurrentEdit
   * @requires $q
   * @requires Metadata
   *
   * @description
   * The `gnOnlinesrc` service provides all tools required to manage
   * online resources like method to link or remove all kind of resources.
   */
  module.factory("gnOnlinesrc", [
    "gnBatchProcessing",
    "gnHttp",
    "gnEditor",
    "gnCurrentEdit",
    "$q",
    "$http",
    "$window",
    "$rootScope",
    "$translate",
    "$filter",
    "Metadata",
    "gnUrlUtils",
    "gnGlobalSettings",
    function (
      gnBatchProcessing,
      gnHttp,
      gnEditor,
      gnCurrentEdit,
      $q,
      $http,
      $window,
      $rootScope,
      $translate,
      $filter,
      Metadata,
      gnUrlUtils,
      gnGlobalSettings
    ) {
      var reload = false;
      var openCb = {};

      /**
       * To match an icon to a protocol
       * TODO: Should be the same as in related resource directive
       */
      var protocolIcons = [
        ["dq-report", "fa-certificate"],
        ["legend", "fa-paint-brush"],
        ["fcats", "fa-table"],
        ["FILE:", "fa-database"],
        ["OGC:OWS", "fa-map"],
        ["OGC:WMC", "fa-map"],
        ["OGC:WM", "fa-map"],
        ["OGC:WFS", "fa-download"],
        ["OGC:", "fa-globe"],
        ["KML:", "fa-globe"],
        ["ESRI", "fa-globe"],
        ["WWW:LINK", "fa-link"],
        ["DB:", "fa-columns"],
        ["WWW:DOWNLOAD", "fa-download"]
      ];
      var defaultIcon = "fa-link";
      /**
       * Prepare batch process request parameters.
       *   - get parameters from onlinesrc form
       *   - add process name
       *   - encode URL
       *   - update name and desc if we add layers
       */
      var setParams = function (processName, formParams) {
        var params = angular.copy(formParams);
        angular.extend(params, {
          process: processName
        });
        //        if (!angular.isUndefined(params.url)) {
        //          params.url = encodeURIComponent(params.url);
        //        }
        return setLayersParams(params);
      };

      /**
       * Prepare name and url parameters
       * if we are adding resource with layers.
       *
       * Parse all selected layers, extract name
       * and title to build name param like
       *   name : name1,name2,name3
       */
      var setLayersParams = function (params) {
        if (angular.isArray(params.selectedLayers) && params.selectedLayers.length > 0) {
          var names = [];

          angular.forEach(params.selectedLayers, function (layer) {
            names.push(layer.Name || layer.name);
          });

          var addLayersInUrl = params.addLayersInUrl;

          if (addLayersInUrl != "" && params.protocol.indexOf("OGC:WMS") >= 0) {
            params.url = gnUrlUtils.remove(params.url, [addLayersInUrl], true);
            params.url = gnUrlUtils.append(
              params.url,
              addLayersInUrl + "=" + names.join(",")
            );
          }

          if (params.wmsResources.addLayerNamesMode == "resourcename") {
            angular.extend(params, {
              name: names.join(",")
            });
          }
        }

        delete params.layers;
        delete params.selectedLayers;
        delete params.wmsResources;
        delete params.addLayersInUrl;
        return params;
      };

      var refreshForm = function (scope, data) {
        gnEditor.refreshEditorForm(data);
        scope.reload = true;
      };

      var closePopup = function (id) {
        if (id) {
          $(id).modal("hide");
        }
      };

      /**
       * Run batch process, then refresh form with process
       * response and reload the updated online resources list.
       * The first save is done in 'runProcessMd'
       */
      var runProcess = function (scope, params) {
        return gnBatchProcessing.runProcessMd(params).then(
          function (data) {
            refreshForm(scope, $(data.data));
          },
          function (error) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("runProcessError"),
              error: error,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };

      /**
       * Run a service (not a batch) to add or remove
       *
       * an onlinesrc.
       * Save the form, launch the service, then refresh
       * the form and reload the onlinesrc list.
       * The save is silent, in order not to reload the
       * onlinesrc list on save and on batch success.
       */
      var runService = function (service, params, scope) {
        return gnEditor.save(false, true).then(function () {
          gnHttp.callService(service, params).then(
            function (response) {
              refreshForm(scope);
            },
            function (response) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("runServiceError"),
                error: response.data,
                timeout: 0,
                type: "danger"
              });
            }
          );
        });
      };

      /**
       * gnOnlinesrc service PUBLIC METHODS
       * - getAllResources
       * - addOnlinesrc
       * - linkToParent
       * - linkToDataset
       * - linkToService
       * - removeThumbnail
       * - removeOnlinesrc
       *******************************************
       */
      return {
        /**
         * This value is watched from gnOnlinesrcList directive
         * to reload online resources list when it is true
         */
        reload: reload,

        /**
         * @ngdoc method
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         * @name gnOnlinesrc#getAllResources
         *
         * @description
         * Get all online resources for the current edited
         * metadata.
         *
         * @return {HttpPromise} Future object
         */
        getAllResources: function (types) {
          var linksAndRelatedPromises = [],
            apiPrefix = "../api/records/" + gnCurrentEdit.uuid,
            isArray = angular.isArray(types),
            defaultRelatedTypes = ["thumbnails", "onlines"],
            relatedTypes = [],
            associatedTypes = [],
            isApproved = gnCurrentEdit.metadata.draft !== "y";

          if (isArray) {
            var relatedTypeFilterFn = function (t) {
              return defaultRelatedTypes.indexOf(t) !== -1;
            };
            relatedTypes = types.filter(relatedTypeFilterFn);
            associatedTypes = types.filter(function (t) {
              return !relatedTypeFilterFn(t);
            });
          } else {
            relatedTypes = defaultRelatedTypes;
          }

          if (relatedTypes.length > 0) {
            linksAndRelatedPromises.push(
              $http.get(
                apiPrefix +
                  "/related?type=" +
                  relatedTypes.join("&type=") +
                  (!isApproved ? "&approved=false" : ""),
                {
                  headers: {
                    Accept: "application/json"
                  }
                }
              )
            );
          }

          if (associatedTypes.length > 0) {
            linksAndRelatedPromises.push(
              $http.get(
                apiPrefix +
                  "/associated?type=" +
                  associatedTypes.join(",") +
                  (!isApproved ? "&approved=false" : ""),
                {
                  headers: {
                    Accept: "application/json"
                  }
                }
              )
            );
          }

          var all = $q.all(linksAndRelatedPromises).then(function (result) {
            var relations = {};
            for (var i = 0; i < result.length; i++) {
              angular.extend(relations, result[i].data);
            }
            Object.keys(relations).forEach(function (key) {
              if (defaultRelatedTypes.indexOf(key) === -1) {
                if (angular.isArray(relations[key])) {
                  relations[key] = relations[key].map(function (r) {
                    return new Metadata(r);
                  });
                }
              }
            });
            return relations;
          });
          return all;
        },

        /**
         * @ngdoc method
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         * @name gnOnlinesrc#formatResources
         *
         * @description
         * If multilingual, get current lang url to
         * diplay the resource in the list (img, link)
         * lUrl means localize Url
         *
         * @return {Object} Containing relations and siblingTypes properties
         */
        formatResources: function (data, lang, mdLanguage) {
          var siblingTypes = [];
          // If multilingual, get current lang url to
          // diplay the resource in the list (img, link)
          // lUrl means localize Url
          angular.forEach(data.onlines, function (src) {
            src.lUrl =
              src.url[lang] || src.url[mdLanguage] || src.url[Object.keys(src.url)[0]];
          });
          angular.forEach(data.thumbnails, function (img) {
            img.lUrl =
              img.url[lang] || img.url[mdLanguage] || img.url[Object.keys(img.url)[0]];
          });
          if (data.siblings) {
            for (var i = 0; i < data.siblings.length; i++) {
              var type = data.siblings[i].associationType;
              if ($.inArray(type, siblingTypes) == -1) {
                siblingTypes.push(type);
              }
            }
          }
          return {
            relations: data,
            siblingTypes: siblingTypes
          };
        },
        /**
         * @ngdoc method
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         * @name gnOnlinesrc#onOpenPopup
         *
         * @description
         * Open onlinesrc popup and call registered
         * function (from the directive).
         *
         * @param {string} type of the directive that calls it.
         */
        onOpenPopup: function (type, additionalParams) {
          var fn = openCb[type];
          if (angular.isFunction(fn)) {
            openCb[type](additionalParams);
          } else {
            console.warn(
              "No callback functions available for '" + type + "'. Check the type value."
            );
          }
        },

        /**
         * @ngdoc method
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         * @name gnOnlinesrc#openExternalResourceManagement
         *
         * @description
         * Open open external resource management popup
         * function (from the directive).
         *
         * @param {r} MetadataResource
         * @param {$window} window object
         */
        openExternalResourceManagement: function (r, $window) {
          try {
            var url = r.metadataResourceExternalManagementProperties.url;
          } catch (e) {
            console.log("external management url not defined");
            return;
          }

          var modal = gnCurrentEdit.resourceManagementExternalProperties.modal;
          var externalManagementWindowsParameters =
            gnCurrentEdit.resourceManagementExternalProperties.windowParameters;

          var win = window.open(url, "_blank", externalManagementWindowsParameters);

          if (modal) {
            var ZIndex = $(".modal").css("z-index");
            $(".modal").css("z-index", 0);
            var timer = setInterval(function () {
              if (win.closed) {
                clearInterval(timer);
                $(".modal").css("z-index", ZIndex);
                $rootScope.$broadcast("gnFileStoreUploadDone");
              } else {
                // whenever user comes back to the browser window give them focus on the popup.
                // This will simulat a modal
                if (document.hasFocus()) {
                  win.focus();
                }
              }
            }, 250);
          }
        },

        /**
         * @ngdoc method
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         * @name gnOnlinesrc#register
         *
         * @description
         * onlinesrc directive can register a function
         * on the open window event.
         *
         * @param {string} type of the directive that calls it.
         * @param {function} fn callback to call on `onOpenPopup`
         */
        register: function (type, fn) {
          openCb[type] = fn;
        },

        /**
         * @ngdoc method
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         * @name gnOnlinesrc#add
         *
         * @description
         * The `add` method call a batch process to add a new online
         * resource to the current metadata.
         * It prepares the parameters and call batch
         * request from the `gnBatchProcessing` service.
         *
         * @param {string} params to send to the batch process
         * @param {string} popupid id of the popup to close after process.
         */
        add: function (params, popupid) {
          return runProcess(this, setParams(params.process, params)).then(function () {
            closePopup(popupid);
          });
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#linkToMd
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `linkToMd` method links to another metadata, could be
         * - parent
         * - source dataset
         * - feature catalog
         *
         * @param {string} mode type of the metadata to link
         * @param {Object} record metadata to link.
         * @param {string} popupid id of the popup to close after process.
         */
        linkToMd: function (mode, record, popupid) {
          var md = new Metadata(record);
          var params = {
            process: mode + "-add"
          };
          if (mode == "fcats") {
            params.uuidref = md.uuid;
          } else {
            params[mode + "Uuid"] = md.uuid;
          }
          params[mode + "Url"] = md.remoteUrl || "";
          params[mode + "Title"] =
            md.title || // Remote
            md.resourceTitle || // not multilingual eg. 19110
            md.resourceTitleObject.default;
          return runProcess(this, params).then(function () {
            closePopup(popupid);
          });
        },

        getApprovedUrl: function (url) {
          if (
            gnCurrentEdit.metadata.draft === "y" &&
            url.match(".*/api/records/" + gnCurrentEdit.uuid + "/attachments/.*") != null
          ) {
            if (url.match(".*(&?)((approved=.*)(&?))+")) {
              // Remove approved parameter if already exists.
              url = gnUrlUtils.remove(url, ["approved"], true);
            }
            url += (url.indexOf("?") > 0 ? "&" : "?") + "approved=false";
          }
          return url;
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#linkToService
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `linkToService` links a service to the current metadata
         *
         * @param {Object} params for the batch
         * @param {string} popupid id of the popup to close after process.
         */
        linkToService: function (params, popupid, addOnlineSrcInDataset) {
          var scope = this;

          // Add link of the service in the dataset (optional)
          var addDatasetToServiceFn = function (r) {
            var qParams = setParams("dataset-add", params);
            if (addOnlineSrcInDataset) {
              return runProcess(scope, {
                scopedName: qParams.remote ? "" : qParams.name || "",
                uuidref: qParams.uuidSrv,
                uuid: qParams.uuidDS,
                url: qParams.url,
                title: qParams.name,
                source: qParams.identifier || "",
                protocol: qParams.protocol,
                process: qParams.process
              }).then(function () {
                closePopup(popupid);
              });
            } else {
              refreshForm(this);
              closePopup(popupid);
            }
          };

          // Add link to the dataset in the service
          var qParams = setParams("service-add", params);
          if (qParams.remote) {
            // We can't update a remote record.
            // Only make link in current dataset.
            addDatasetToServiceFn();
          } else {
            return gnBatchProcessing
              .runProcessMd(
                {
                  scopedName: qParams.remote ? "" : qParams.name || "",
                  uuidref: qParams.uuidDS,
                  title: qParams.datasetTitle,
                  uuid: qParams.uuidSrv,
                  source: qParams.identifier || "",
                  process: qParams.process
                },
                true
              )
              .then(addDatasetToServiceFn, function (error) {
                // Current user may not be able to edit
                // the targeted dataset. Notify user in this case
                // that only the service will be updated.
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("linkToServiceError"),
                  msg: $translate.instant("cantAddLinkToDataset"),
                  timeout: 0,
                  type: "danger"
                });
                addDatasetToServiceFn();
              });
          }
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#linkToDataset
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `linkToDataset` calls md.processing. in mode 'parent-add'
         * to link a service to the edited metadata
         *
         * @param {Object} params for the batch
         * @param {string} popupid id of the popup to close after process.
         */
        linkToDataset: function (params, popupid, addOnlineSrcToDataset) {
          // Define if when linking a service with a dataset
          // a online source element should be added to the dataset
          // first or not.
          var scope = this;

          var addDatasetToServiceFn = function () {
            var qParams = setParams("dataset-add", params);

            return runProcess(scope, {
              // names are equal if no selected layers
              // See #setLayersParams
              // In this case, dataset-add.xsl MUST not add coupledResource
              // So setting it to empty
              scopedName: params.remote
                ? ""
                : params.name === qParams.name
                ? ""
                : qParams.name,
              uuidref: qParams.uuidDS,
              uuid: qParams.uuidSrv,
              url: qParams.remote ? qParams.url : "",
              title: qParams.remote ? qParams.name : "",
              source: qParams.identifier || "",
              process: qParams.process
            }).then(function () {
              closePopup(popupid);
            });
          };

          // We can't update a remote record.
          // Only make link in current dataset.
          if (!params.remote && addOnlineSrcToDataset) {
            var qParams = setParams("onlinesrc-add", params);
            return gnBatchProcessing
              .runProcessMd(
                {
                  name: qParams.name,
                  desc: qParams.desc,
                  url: qParams.url,
                  uuidref: qParams.uuidSrv,
                  uuid: qParams.uuidDS,
                  source: qParams.source,
                  protocol: qParams.protocol,
                  process: qParams.process
                },
                true
              )
              .then(addDatasetToServiceFn, function (error) {
                // Current user may not be able to edit
                // the targeted dataset. Notify user in this case
                // that only the service will be updated.
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("linkToServiceError"),
                  msg: $translate.instant("cantAddLinkToDataset"),
                  timeout: 0,
                  type: "danger"
                });
                addDatasetToServiceFn();
              });
          } else {
            return addDatasetToServiceFn();
          }
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#linkToSibling
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `linkToSibling` runs a the process sibling with given parameters
         *
         * @param {Object} params for the batch
         * @param {string} popupid id of the popup to close after process.
         */
        linkToSibling: function (params, popupid) {
          return runProcess(this, setParams("sibling-add", params)).then(function () {
            closePopup(popupid);
          });
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeThumbnail
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeThumbnail` removes a thumbnail from metadata.
         * Type large or small is specified in parameter.
         * The onlinesrc panel is reloaded after removal.
         *
         * @param {Object} thumb the online resource to remove
         */
        removeThumbnail: function (thumb) {
          var scope = this;

          // It is a url thumbnail
          if (thumb.id.indexOf("resources.get") < 0) {
            return runProcess(
              this,
              setParams("thumbnail-remove", {
                id: gnCurrentEdit.id,
                thumbnail_url: thumb.id
              })
            );
          }
          // It is an uploaded tumbnail
          else {
            return runService(
              "removeThumbnail",
              {
                type: thumb.title === "thumbnail" ? "small" : "large",
                id: gnCurrentEdit.id,
                version: gnCurrentEdit.version
              },
              this
            );
          }
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeOnlinesrc
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeOnlinesrc` removes an online resource from metadata.
         * Depending if the online resource has been uploaded or not, we call
         * a batch processing or a simple service to remove it.
         *
         * @param {Object} onlinesrc the online resource to remove
         */
        removeOnlinesrc: function (onlinesrc) {
          var url = onlinesrc.lUrl || onlinesrc.url;
          if (
            url.match(".*/api/records/' + gnCurrentEdit.uuid + '/attachments/.*") != null
          ) {
            url = gnUrlUtils.remove(url, ["approved"], true);
          }

          return runProcess(
            this,
            setParams("onlinesrc-remove", {
              id: gnCurrentEdit.id,
              url: url,
              name: $filter("gnLocalized")(onlinesrc.title)
            })
          );
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeService
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeService` removes a service from a metadata.
         *
         * @param {Object} record the metadata to remove
         */
        removeService: function (record) {
          var params = {
              uuid: record.uuid,
              uuidref: gnCurrentEdit.uuid
            },
            service = this;

          gnBatchProcessing.runProcessMd(setParams("services-remove", params)).then(
            function (data) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("serviceDetachedToCurrentRecord"),
                timeout: 3
              });
              service.reload = true;
            },
            function (error) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("removeServiceError"),
                error: error,
                timeout: 0,
                type: "danger"
              });
            }
          );
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeDataset
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeDataset` removes a dataset from a metadata of
         * service.
         *
         * @param {Object} record the record resource to remove
         */
        removeDataset: function (record) {
          var params = {
            uuid: gnCurrentEdit.uuid,
            uuidref: record.uuid
          };
          runProcess(this, setParams("datasets-remove", params));
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeMdLink
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeMdLink` removes a linked metadata by calling a process.
         *
         * @param {string} mode can be 'source', 'parent'
         * @param {Object} record the record to remove
         */
        removeMdLink: function (mode, record) {
          var params = {};
          params[mode + "Uuid"] = record.uuid;
          runProcess(this, setParams(mode + "-remove", params));
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeFeatureCatalog
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeFeatureCatalog` removes a feature catalog link from the
         * current metadata.
         *
         * @param {Object} record the record to remove
         */
        removeFeatureCatalog: function (record) {
          var params = {
            uuid: gnCurrentEdit.uuid,
            uuidref: record["@subtype"] ? record.url : record.uuid
          };
          runProcess(this, setParams("fcats-remove", params));
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#removeSibling
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `removeSibling` removes a sibling link from the
         * current metadata.
         *
         * @param {Object} record the record to remove
         */
        removeSibling: function (record) {
          var params = {
            uuid: gnCurrentEdit.uuid,
            uuidref: record.uuid
          };
          runProcess(this, setParams("sibling-remove", params));
        },

        /**
         * @ngdoc method
         * @name gnOnlinesrc#buildOnLineResource
         * @methodOf gn_onlinesrc.service:gnOnlinesrc
         *
         * @description
         * The `buildOnLineResource` create an ISO19139
         * XML snippet based on the url, protocol, name
         * and description provided.
         *
         * @param {String} url the URL
         * @param {String} protocol the URL protocol
         * @param {String} name the name
         * @param {String} description the description
         */
        buildOnLineResource: function (url, protocol, name, description) {
          return (
            '<gmd:onLine xmlns:gmd="http://www.isotc211.org/2005/gmd" ' +
            '            xmlns:gco="http://www.isotc211.org/2005/gco">' +
            "  <gmd:CI_OnlineResource>" +
            "    <gmd:linkage><gmd:URL>" +
            url +
            "    </gmd:URL></gmd:linkage>" +
            "    <gmd:protocol><gco:CharacterString>" +
            protocol +
            "    </gco:CharacterString></gmd:protocol>" +
            "    <gmd:name><gco:CharacterString>" +
            name +
            "    </gco:CharacterString></gmd:name>" +
            "    <gmd:description><gco:CharacterString>" +
            description +
            "    </gco:CharacterString></gmd:description>" +
            "  </gmd:CI_OnlineResource>" +
            "</gmd:onLine>"
          );
        },
        /**
         * Specific method used by the geopublisher.
         * Compute online resource XML for the given protocols.
         *
         * return the XML snippet to include to the form.
         */
        addFromGeoPublisher: function (layerName, title, node, protocols) {
          var xml = "";

          for (var p in protocols) {
            if (protocols.hasOwnProperty(p) && protocols[p].checked === true) {
              // TODO : define default description
              var key = p + "url";
              xml +=
                this.buildOnLineResource(
                  node[key],
                  protocols[p].label,
                  layerName,
                  title + " (" + protocols[p].label + ")"
                ) + "&&&";
            }
          }
          return xml;
        }
      };
    }
  ]);

  /**
   * Service to query a DOI service and return the results.
   */
  module.service("gnDoiSearchService", [
    "$http",
    function ($http) {
      return {
        search: function (url, prefix, query) {
          return $http.get(url + "?prefix=" + prefix + "&query=" + query);
        }
      };
    }
  ]);
})();
