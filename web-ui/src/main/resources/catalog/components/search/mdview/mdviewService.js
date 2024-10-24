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
  goog.provide("gn_mdview_service");

  var module = angular.module("gn_mdview_service", []);

  module.value("gnMdViewObj", {
    previousRecords: [],
    current: {
      record: null,
      index: null
    }
  });

  module.service("gnMdView", [
    "gnSearchLocation",
    "$rootScope",
    "gnMdFormatter",
    "Metadata",
    "gnMdViewObj",
    "gnSearchManagerService",
    "gnSearchSettings",
    "gnUrlUtils",
    "gnUtilityService",
    "gnESService",
    "gnESClient",
    "gnESFacet",
    "gnGlobalSettings",
    "gnMetadataActions",
    "$http",
    "$filter",
    function (
      gnSearchLocation,
      $rootScope,
      gnMdFormatter,
      Metadata,
      gnMdViewObj,
      gnSearchManagerService,
      gnSearchSettings,
      gnUrlUtils,
      gnUtilityService,
      gnESService,
      gnESClient,
      gnESFacet,
      gnGlobalSettings,
      gnMetadataActions,
      $http,
      $filter
    ) {
      // Keep where the metadataview come from to get back on close
      var initFromConfig = function () {
        if (!gnSearchLocation.isMdView()) {
          gnMdViewObj.from = gnSearchLocation.path();
        }
      };
      $rootScope.$on("$locationChangeStart", initFromConfig);
      initFromConfig();

      this.feedMd = function (index, md, records) {
        // Set the index for previous/next record
        if (gnMdViewObj.records) {
          for (var i = 0; i < gnMdViewObj.records.length; i++) {
            if (gnMdViewObj.records[i].id == md.id) {
              gnMdViewObj.current.index = i;
              break;
            }
          }
        }

        // Set the route only if not same as before
        var formatter = gnSearchLocation.getFormatter();

        gnUtilityService.scrollTo();

        gnMdViewObj.current.record = md;

        // Record with associations
        if (md.resourceType && md.related) {
          var relatedRecords = md.related;
          var recordsMap = {};
          // Collect stats using search service
          if (relatedRecords) {
            // Build metadata as the API response already contains an index document
            Object.keys(relatedRecords).map(function (k) {
              relatedRecords[k] &&
                relatedRecords[k].map &&
                relatedRecords[k].map(function (l) {
                  recordsMap[l._id] = new Metadata(l);
                });
            });

            // Configuration to retrieve the results for the aggregations
            var relatedFacetConfig =
              gnGlobalSettings.gnCfg.mods.recordview.relatedFacetConfig;
            Object.keys(relatedFacetConfig).map(function (k) {
              relatedFacetConfig[k].aggs = {
                docs: {
                  top_hits: {
                    // associated stats with UUIDs
                    size: 100,
                    _source: {
                      includes: ["uuid"]
                    }
                  }
                }
              };
            });

            // Build multiquery to get aggregations for each
            // set of associated records
            var body = "";
            var relatedRecordKeysWithValues = []; // keep track of the relations with values

            Object.keys(relatedRecords).forEach(function (k) {
              if (relatedRecords[k] && relatedRecords[k].length > 0) {
                relatedRecordKeysWithValues.push(k);

                body += '{"index": "records"}\n';
                body +=
                  "{" +
                  '  "query": {' +
                  '    "bool": {' +
                  '      "must": [' +
                  '        { "terms": { "uuid": ["' +
                  relatedRecords[k]
                    .map(function (md) {
                      return md._id;
                    })
                    .join('","') +
                  '"]} },' +
                  '        { "terms": { "isTemplate": ["n"] } }' +
                  "      ]" +
                  "    }" +
                  "  }," +
                  '  "aggs":' +
                  JSON.stringify(relatedFacetConfig) +
                  "," +
                  '  "from": 0,' +
                  '  "size": 100,' +
                  '  "_source": ["uuid"]' +
                  "}";
              }
            });

            // Collect stats in main portal as some records may not be visible in subportal
            if (Object.entries(recordsMap).length !== 0) {
              $http
                .post("../../srv/api/search/records/_msearch", body)
                .then(function (data) {
                  gnMdViewObj.current.record.related = [];

                  Object.keys(relatedRecords).map(function (k) {
                    relatedRecords[k] &&
                      relatedRecords[k].map(function (l, i) {
                        var md = recordsMap[l._id];
                        relatedRecords[k][i] = md;
                      });
                  });

                  gnMdViewObj.current.record.related = relatedRecords;
                  gnMdViewObj.current.record.related.all = Object.values(recordsMap);
                  gnMdViewObj.current.record.related.uuids = Object.keys(recordsMap);

                  relatedRecordKeysWithValues.forEach(function (key, index) {
                    gnMdViewObj.current.record.related["aggregations_" + key] =
                      data.data.responses[index].aggregations;
                  });
                });
            }
          }
        }

        // TODO: do not add duplicates
        gnMdViewObj.previousRecords.push(md);

        // Don't increase popularity for working copies
        if (!gnMdViewObj.usingFormatter && md.draft !== "y") {
          $http.post("../api/records/" + md.uuid + "/popularity");
        }
        this.setLocationUuid(md.uuid, formatter);
        gnMdViewObj.recordsLoaded = true;
      };

      /**
       * Update location to be /metadata/uuid.
       * Remove the search path and attributes from location too.
       * @param {string} uuid
       */
      this.setLocationUuid = function (uuid, formatter) {
        gnSearchLocation.setUuid(uuid, formatter);
      };

      // The service needs to keep a reference to the metadata item scope
      var currentMdScope;
      this.setCurrentMdScope = function (scope, index, records) {
        currentMdScope = scope;
        // gnMdViewObj.records = records;
        // gnMdViewObj.current.index = index;
      };
      this.getCurrentMdScope = function () {
        return currentMdScope;
      };

      /**
       * Called when you want to pass from mdview uuid url back to search.
       * It change path back to search and inject the last parameters saved
       * at last search.
       */
      this.removeLocationUuid = function () {
        if (gnMdViewObj.from && gnMdViewObj.from != gnSearchLocation.SEARCH) {
          gnSearchLocation.path(gnMdViewObj.from);
        } else {
          gnSearchLocation.restoreSearch();
        }
      };

      this.buildRelatedTypesQueryParameter = function (types) {
        types =
          types ||
          "parent|children|sources|hassources|" +
            "brothersAndSisters|services|datasets|" +
            "siblings|associated|fcats|hasfeaturecats|related";
        return "relatedType=" + types.split("|").join("&relatedType=");
      };

      /**
       * Init the mdview behavior linked on $location.
       * At start and $location change, the uuid is extracted
       * from the url and the md is loaded. If the md was already loaded
       * by a previous search, we use this object, otherwise we launch
       * a new search to retrieve this md.
       */
      this.initMdView = function () {
        var that = this;
        var loadMdView = function (event, newUrl, oldUrl) {
          gnMdViewObj.loadDetailsFinished = false;
          gnMdViewObj.recordsLoaded = false;
          var uuid = gnSearchLocation.getUuid();
          if (uuid) {
            if (
              !gnMdViewObj.current.record ||
              gnMdViewObj.current.record.uuid !== uuid ||
              newUrl !== oldUrl
            ) {
              //Check if we want the draft version
              var getDraft = window.location.hash.indexOf("/metadraf/") > 0;
              var foundMd = false;

              // Check if the md is in current search
              // With ES, we always reload the document
              // because includes are limited in the main search response.
              // if (angular.isArray(gnMdViewObj.records)
              //           && !getDraft) {
              //   for (var i = 0; i < gnMdViewObj.records.length; i++) {
              //     var md = gnMdViewObj.records[i];
              //     if (md.uuid === uuid) {
              //       foundMd = true;
              //       that.feedMd(i, md, gnMdViewObj.records);
              //     }
              //   }
              // }

              if (!foundMd) {
                // get a new search to pick the md
                gnMdViewObj.current.record = null;
                $http
                  .post(
                    "../api/search/records/_search?" +
                      that.buildRelatedTypesQueryParameter(),
                    {
                      query: {
                        bool: {
                          must: [
                            {
                              multi_match: {
                                query: uuid,
                                fields: ["id", "uuid"]
                              }
                            },
                            { terms: { isTemplate: ["n", "y"] } },
                            { terms: { draft: ["n", "y", "e"] } }
                          ]
                        }
                      }
                    },
                    { cache: true }
                  )
                  .then(
                    function (r) {
                      if (r.data.hits.total.value > 0) {
                        //If trying to show a draft that is not a draft, correct url:
                        if (
                          r.data.hits.total.value == 1 &&
                          window.location.hash.indexOf("/metadraf/") > 0
                        ) {
                          window.location.hash = window.location.hash.replace(
                            "/metadraf/",
                            "/metadata/"
                          );
                          //Now the location change event handles this
                          return;
                        }

                        //If returned more than one, maybe we are looking for the draft
                        var i = 0;

                        r.data.hits.hits.forEach(function (md, index) {
                          if (getDraft && md._source.draft == "y") {
                            //This will only happen if the draft exists
                            //and the user can see it
                            i = index;
                          } else if (!getDraft && md._source.draft != "y") {
                            // This use the non-draft version when the  results include the
                            // approved and the working copy (draft) versions
                            i = index;
                          }
                        });

                        var metadata = [];
                        metadata.push(new Metadata(r.data.hits.hits[i]));

                        var data = { metadata: metadata };
                        //Keep the search results (gnMdViewObj.records)
                        // that.feedMd(0, undefined, data.metadata);
                        //and the trace of where in the search result we are
                        // TODOES: Review
                        that.feedMd(
                          gnMdViewObj.current.index,
                          data.metadata[0],
                          gnMdViewObj.records
                        );
                        gnMdViewObj.loadDetailsFinished = true;
                      } else {
                        gnMdViewObj.loadDetailsFinished = true;
                      }
                    },
                    function (error) {
                      gnMdViewObj.loadDetailsFinished = true;
                    }
                  );
              }
            } else {
              gnMdViewObj.loadDetailsFinished = true;
            }
          } else {
            gnMdViewObj.loadDetailsFinished = true;
            gnMdViewObj.current.record = null;
          }
        };

        loadMdView();
        // To manage uuid on page loading
        $rootScope.$on("$locationChangeSuccess", loadMdView);
      };

      /**
       * Open a metadata just from info in the layer. If the metadata comes
       * from the catalog, then the layer 'md' property contains the gn md
       * object. If not, we search the md in the catalog to open it.
       * @param {ol.layer} layer
       */
      this.openMdFromLayer = function (layer) {
        var md = layer.get("md");
        if (!md && layer.get("metadataUrl")) {
          var mdUrl = gnUrlUtils.urlResolve(layer.get("metadataUrl"));
          if (mdUrl.host == gnSearchLocation.host()) {
            gnSearchLocation.setUuid(layer.get("metadataUuid"));
          } else {
            window.open(layer.get("metadataUrl"), "_blank");
          }
        } else {
          this.feedMd(0, md, [md]);
        }
      };
    }
  ]);

  module.service("gnMdFormatter", [
    "$rootScope",
    "$http",
    "$compile",
    "$translate",
    "$sce",
    "gnAlertService",
    "gnSearchSettings",
    "$q",
    "gnMetadataManager",
    "gnUtilityService",
    function (
      $rootScope,
      $http,
      $compile,
      $translate,
      $sce,
      gnAlertService,
      gnSearchSettings,
      $q,
      gnMetadataManager,
      gnUtilityService
    ) {
      /**
       * First matching view for each formatter is returned.
       *
       * @param record
       * @returns {*[]}
       */
      this.getFormatterForRecord = function (record) {
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

            function addView(f, v) {
              list.push({ label: f.label, url: v.url });
              isViewSet = true;
            }
            function evaluateView(v) {
              gnUtilityService.checkConfigurationPropertyCondition(
                record,
                v,
                function () {
                  if (!isViewSet) {
                    addView(f, v);
                  }
                  return;
                }
              );
            }
            for (var j = 0; j < f.views.length; j++) {
              var v = f.views[j];
              evaluateView(v);
            }

            if (f.url !== undefined && !isViewSet) {
              list.push(f);
            }
          }
        }
        return list;
      };

      this.getFormatterUrl = function (fUrl, scope, uuid, opt_url) {
        var url;
        var promiseMd;
        var gnMetadataFormatter = this;
        if (scope && scope.md) {
          var deferMd = $q.defer();
          deferMd.resolve(scope.md);
          promiseMd = deferMd.promise;
        } else {
          promiseMd = gnMetadataManager.getMdObjByUuid(uuid);
        }

        return promiseMd.then(function (md) {
          if (angular.isString(fUrl)) {
            url = fUrl.replace("{{uuid}}", md.uuid);
          } else if (angular.isFunction(fUrl)) {
            url = fUrl(md);
          }

          // Attach the md to the grid element scope
          if (!scope.md) {
            scope.$parent.md = md;
            scope.md = md;
          }
          return (
            url ||
            "../api/records/" +
              uuid +
              gnMetadataFormatter.getFormatterForRecord(md)[0].url
          );
        });
      };

      this.load = function (uuid, selector, scope, opt_url) {
        $rootScope.$broadcast("mdLoadingStart");
        var newscope = scope ? scope.$new() : angular.element($(selector)).scope().$new();

        this.getFormatterUrl(
          opt_url || gnSearchSettings.formatter.defaultUrl,
          newscope,
          uuid,
          opt_url
        ).then(function (url) {
          $http
            .get(url, {
              headers: {
                Accept: "text/html"
              }
            })
            .then(
              function (response) {
                $rootScope.$broadcast("mdLoadingEnd");

                var newscope = scope
                  ? scope.$new()
                  : angular.element($(selector)).scope().$new();

                newscope.fragment = $compile(angular.element(response.data))(newscope);

                var el = document.createElement("div");
                el.setAttribute("gn-metadata-display", "");
                $(selector).append(el);
                $compile(el)(newscope);
              },
              function () {
                $rootScope.$broadcast("mdLoadingEnd");
                gnAlertService.addAlert({
                  msg: $translate.instant("metadataViewLoadError"),
                  type: "danger"
                });
              }
            );
        });
      };
    }
  ]);
})();
