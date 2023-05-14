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
  goog.provide("gn_search_form_controller");

  goog.require("gn_catalog_service");
  goog.require("gn_search_form_results_directive");
  goog.require("gn_selection_directive");
  goog.require("gn_collection_manager_service");
  goog.require("search_filter_tags_directive");

  var module = angular.module("gn_search_form_controller", [
    "gn_catalog_service",
    "gn_selection_directive",
    "gn_collection_manager_service",
    "gn_search_form_results_directive",
    "search_filter_tags_directive"
  ]);

  /**
   * Controller to create new metadata record.
   */
  var searchFormController = function (
    $scope,
    $location,
    $parse,
    $translate,
    gnSearchManagerService,
    Metadata,
    gnSearchLocation,
    gnESClient,
    gnGlobalSettings,
    gnESService,
    gnESFacet,
    gnAlertService,
    md5
  ) {
    var defaultParams = {};
    var self = this;

    var hiddenParams = $scope.searchObj.hiddenParams;
    $scope.searchObj.configId = $scope.searchObj.configId || "search";
    $scope.searchObj.state = {
      filters: {}
    };

    /** Object where are stored result search information */
    $scope.searchResults = {
      records: [],
      count: -1,
      selectionBucket:
        $scope.searchObj.selectionBucket || (Math.random() + "").replace(".", "")
    };
    $scope.finalParams = {};

    $scope.searching = 0;
    $scope.paginationInfo = $scope.paginationInfo || {};

    /**
     * Return the current search parameters.
     **/
    this.getSearchParams = function () {
      return $scope.searchObj.params;
    };

    this.getFinalParams = function () {
      var p = angular.copy($scope.finalParams, {});
      p.query_string = JSON.stringify($scope.searchObj.state.filters);
      return p;
    };

    this.getSearchResults = function () {
      return $scope.searchResults;
    };

    /**
     * Tells if there is a pagination directive nested to this one.
     * Mainly activated by pagination directive link function.
     */
    $scope.hasPagination = false;
    this.activatePagination = function () {
      $scope.hasPagination = true;
      if (
        !$scope.searchObj.permalink ||
        (angular.isUndefined($scope.searchObj.params.from) &&
          angular.isUndefined($scope.searchObj.params.to))
      ) {
        self.resetPagination();
      }
    };

    /**
     * Reset pagination 'from' and 'to' params and merge them
     * to $scope.params
     */
    this.resetPagination = function (customPagination) {
      if ($scope.hasPagination) {
        $scope.paginationInfo.currentPage = 1;
        this.updateSearchParams(this.getPaginationParams(customPagination));
      }
    };

    var cleanSearchParams = function (params) {
      for (v in params) {
        if (v !== "sortOrder" && params[v] == "") {
          delete params[v];
        }
      }
    };

    /**
     * triggerSearch
     *
     * Run a search with the actual $scope.params
     * Update the paginationInfo object with the total
     * count of metadata found.
     *
     * @param {boolean} resetPagination If true, then
     * don't reset pagination info.
     */

    this.triggerSearchFn = function (keepPagination) {
      $scope.searching++;
      $scope.searchObj.params = angular.extend(
        {},
        $scope.searchObj.defaultParams || defaultParams,
        $scope.searchObj.params,
        defaultParams
      );

      // Set default pagination if not set
      if (
        (!keepPagination && !$scope.searchObj.permalink) ||
        angular.isUndefined($scope.searchObj.params.from) ||
        angular.isUndefined($scope.searchObj.params.to)
      ) {
        self.resetPagination();
      }

      // Set default sortBy
      if (angular.isUndefined($scope.searchObj.params.sortBy)) {
        angular.extend($scope.searchObj.params, $scope.searchObj.sortbyDefault);
      }

      var params = angular.copy($scope.searchObj.params);
      var finalParams = angular.extend(params, hiddenParams);
      $scope.finalParams = finalParams;
      var esParams = gnESService.generateEsRequest(
        finalParams,
        $scope.searchObj.state,
        $scope.searchObj.configId,
        $scope.searchObj.filters
      );

      function buildSearchKey(esParams) {
        var param = angular.copy(esParams, {});
        ["from", "size", "sort"].forEach(function (k) {
          delete param[k];
        });
        return md5.createHash(JSON.stringify(param));
      }

      // Compute facet only if search is reset or new filters apply
      // When moving in pages or changing sort,
      // no need to compute aggregations again and again
      var searchKey = buildSearchKey(esParams),
        dontComputeAggs = $scope.searchResults.searchKey === searchKey,
        lastSearchAggs = undefined;
      if (dontComputeAggs) {
        lastSearchAggs = $scope.searchResults.facets;
        delete esParams.aggregations;
      }
      // console.log(dontComputeAggs, $scope.searchResults.searchKey, searchKey);
      $scope.searchResults.searchKey = searchKey;

      // For now, only the list result template renders related records
      // based on UI config.
      var template =
          $scope.resultTemplate && $scope.resultTemplate.endsWith("list.html")
            ? "grid"
            : "",
        templateConfig = gnGlobalSettings.gnCfg.mods.search[template],
        types = templateConfig && templateConfig.related ? templateConfig.related : [];

      gnESClient
        .search(
          esParams,
          $scope.searchResults.selectionBucket || "metadata",
          $scope.searchObj.configId,
          types
        )
        .then(
          function (data) {
            // data is not an object: this is an error
            if (typeof data !== "object") {
              console.warn(
                "An error occurred while searching. Response is not an object.",
                esParams,
                data.data
              );
              if ($scope.showHealthIndexError !== true) {
                // Index status already displayed
                gnAlertService.addAlert({
                  id: "searchError",
                  msg: $translate.instant("searchInvalidResponse"),
                  type: "danger"
                });
              }
              return;
            }

            // make sure we have a hits object if ES did not give back anything
            data.hits = data.hits || { hits: [], total: 0 };
            var records = data.hits.hits.map(function (r) {
              return new Metadata(r);
            });
            $scope.searchResults.records = records;
            $scope.searchResults.count = data.hits.total.value;
            $scope.searchResults.facets = dontComputeAggs
              ? lastSearchAggs
              : data.facets || {};

            // compute page number for pagination
            if ($scope.hasPagination) {
              var paging = $scope.paginationInfo;

              // Means the `from` and `to` params come from permalink
              if ((paging.currentPage - 1) * paging.hitsPerPage + 1 != params.from) {
                paging.currentPage = (params.from - 1) / paging.hitsPerPage + 1;
              }

              paging.resultsCount = $scope.searchResults.count;
              paging.to = Math.min(
                $scope.searchResults.count,
                paging.currentPage * paging.hitsPerPage
              );
              paging.pages = Math.ceil(
                $scope.searchResults.count / paging.hitsPerPage,
                0
              );
              paging.from = (paging.currentPage - 1) * paging.hitsPerPage + 1;
            }

            $scope.$emit("searchFinished", { count: $scope.searchResults.count });
          },
          function (data) {
            console.warn(
              "An error occurred while searching. Bad request.",
              esParams,
              data.data
            );
            gnAlertService.addAlert({
              id: "searchError",
              msg: $translate.instant("searchBadRequest"),
              type: "danger"
            });
          }
        )
        .then(function () {
          $scope.searching--;
        });
    };

    /**
     * If we use permalink, the triggerSearch call will in fact just update
     * the url with the params, then the event $locationChangeSuccess will call
     * the geonetwork search from url params.
     */
    if ($scope.searchObj.permalink) {
      var triggerSearchFn = self.triggerSearchFn;

      self.triggerSearch = function (keepPagination) {
        if (!keepPagination) {
          self.resetPagination();
        }

        $scope.$broadcast("beforesearch");

        var params = angular.copy($scope.searchObj.params);
        cleanSearchParams(params);

        // Synch query_string and state filter.
        var filters = $scope.searchObj.state.filters;
        if (angular.isObject(filters)) {
          var query_string = JSON.stringify(filters);
          if (Object.keys(filters).length) {
            params.query_string = query_string;
          } else {
            delete params.query_string;
          }
        } else {
          if (filters != "") {
            params.query_string = filters;
            $scope.searchObj.state.filters = JSON.parse(filters);
          } else {
            delete params.query_string;
          }
        }

        if (angular.equals(params, gnSearchLocation.getParams())) {
          triggerSearchFn(false);
        } else {
          gnSearchLocation.setSearch(params);
        }
      };

      $scope.$on("$locationChangeSuccess", function (e, newUrl, oldUrl) {
        // We are not in a url search so leave
        if (!gnSearchLocation.isSearch()) return;

        // We are getting back to the search, no need to reload it
        if (newUrl == gnSearchLocation.lastSearchUrl) return;

        var params = gnSearchLocation.getParams();
        if (params.query_string) {
          $scope.searchObj.state.filters = JSON.parse(params.query_string);
        } else {
          $scope.searchObj.state.filters = {};
        }

        $scope.searchObj.params = params;
        triggerSearchFn();
      });
    } else {
      this.triggerSearch = this.triggerSearchFn;
    }

    /**
     * update $scope.params by merging it with given params
     * @param {!Object} params
     */
    this.updateSearchParams = function (params) {
      angular.extend($scope.searchObj.params, params);
    };

    this.resetSearch = function (searchParams, preserveGeometrySearch) {
      $scope.$broadcast("beforeSearchReset", preserveGeometrySearch);

      if (searchParams) {
        $scope.searchObj.params = searchParams;
      } else {
        $scope.searchObj.params = {};
      }
      if ($scope.searchObj.sortbyDefault) {
        angular.extend($scope.searchObj.params, $scope.searchObj.sortbyDefault);
      }

      var customPagination = searchParams;

      self.resetPagination(customPagination);
      $scope.searchObj.state = {
        filters: {},
        exactMatch: false,
        titleOnly: false,
        languageStrategy: "searchInAllLanguages",
        forcedLanguage: undefined,
        languageWhiteList: undefined,
        detectedLanguage: undefined
      };
      $scope.triggerSearch();
      $scope.$broadcast("resetSelection");
    };
    $scope.$on("resetSearch", function (evt, searchParams, preserveGeometrySearch) {
      $scope.controller.resetSearch(searchParams, preserveGeometrySearch);
    });

    $scope.$on("search", function () {
      $scope.triggerSearch();
    });

    $scope.$on("clearResults", function () {
      $scope.searchResults = {
        records: [],
        count: 0,
        selectionBucket: $scope.searchObj.selectionBucket
      };
    });

    $scope.triggerSearch = this.triggerSearch;

    /*
     * Implement AngularJS $parse without the restriction of expressions
     */
    var parse = function (path) {
      var fn = function (obj) {
        var paths = path.split("^^^"),
          current = obj,
          i;

        for (i = 0; i < paths.length; ++i) {
          if (current[paths[i]] == undefined) {
            return undefined;
          } else {
            current = current[paths[i]];
          }
        }
        return current;
      };
      fn.assign = function (obj, value) {
        var paths = path.split("^^^"),
          current = obj,
          i;

        for (i = 0; i < paths.length - 1; ++i) {
          if (current[paths[i]] == undefined) {
            current[paths[i]] = {};
          }
          current = current[paths[i]];
        }
        current[paths[paths.length - 1]] = value;
      };
      return fn;
    };

    var removeKey = function (obj, keys) {
      var head = keys[0];
      var tail = keys.slice(1);
      for (var prop in obj) {
        obj.hasOwnProperty(prop) &&
          (head.toString() === prop && tail.length === 0
            ? delete obj[prop]
            : "object" === typeof obj[prop] &&
              (removeKey(obj[prop], tail),
              0 === Object.keys(obj[prop]).length && delete obj[prop]));
      }
    };

    this.updateState = function (path, value, doNotRemove) {
      if (path[0] === "any" || path[0] === "uuid" || path[0] === "geometry") {
        delete $scope.searchObj.params[path[0]];
        if (path[0] === "geometry") {
          $scope.$broadcast("beforeSearchReset", false);
        }
      } else if (
        path[0] === "resourceTemporalDateRange" &&
        value === true
        // // Remove range see SearchFilterTagsDirective
      ) {
        delete $scope.searchObj.state.filters[path[0]];
      } else {
        var filters = $scope.searchObj.state.filters;
        var getter = parse(path.join("^^^"));
        var existingValue = getter(filters);
        if (angular.isUndefined(existingValue) || doNotRemove) {
          var setter = getter.assign;
          setter(filters, value);
        } else {
          if (existingValue !== value) {
            var setter = getter.assign;
            setter(filters, value);
          } else {
            removeKey(filters, path);
          }
        }
      }
      this.triggerSearch();
    };

    this.isInSearch = function (path) {
      if (!path) return;
      var filters = $scope.searchObj.state.filters;
      var getter = parse(path.join("^^^"));
      var res = getter(filters);
      return angular.isDefined(res);
    };

    this.hasChildInSearch = function (path) {
      if (!path) return;
      var filters = $scope.searchObj.state.filters;
      if (filters[path[0]]) {
        return Object.keys(filters[path[0]]).some(function (key) {
          return key.indexOf(path[1]) === 0 && key != path[1];
        });
      } else {
        return false;
      }
    };

    this.isNegativeSearch = function (path) {
      if (!path) return;
      var filters = $scope.searchObj.state.filters;
      var getter = parse(path.join("^^^"));
      var res = getter(filters);
      if (angular.isString(res)) {
        return res.indexOf("-(") === 0;
      } else {
        return res === false;
      }
    };

    this.getMissingLabel = function (key) {
      if (!key) return;
      var facet = $scope.searchResults.facets.filter(function (facet) {
        return facet.key === key;
      });
      return (
        (facet.length &&
          facet[0].config &&
          facet[0].config.terms &&
          facet[0].config.terms.missing) ||
        "missing"
      );
    };

    this.hasFiltersForKey = function (key) {
      return !!$scope.searchObj.state.filters[key];
    };

    this.loadMoreTerms = function (facet, moreItemsNumber) {
      var request = gnESService.generateEsRequest(
        $scope.finalParams,
        $scope.searchObj.state,
        $scope.searchObj.configId,
        $scope.searchObj.filters
      );
      return gnESClient.getTermsParamsWithNewSizeOrFilter(
        request.query,
        facet.key,
        facet.config,
        facet.items.length + (moreItemsNumber || 20),
        facet.include || undefined,
        facet.exclude || undefined
      );
    };

    this.filterTerms = function (facet) {
      var request = gnESService.generateEsRequest(
        $scope.finalParams,
        $scope.searchObj.state,
        $scope.searchObj.configId,
        $scope.searchObj.filters
      );
      return gnESClient.getTermsParamsWithNewSizeOrFilter(
        request.query,
        facet.key,
        facet.config,
        undefined,
        facet.include,
        facet.exclude
      );
    };

    /**
     * @param {boolean} value
     */
    this.setExactMatch = function (value) {
      this.updateSearchParams({ exactMatch: value });
      $scope.searchObj.state.exactMatch = value;
    };

    /**
     * @return {boolean}
     */
    this.getExactMatch = function () {
      return $scope.searchObj.state.exactMatch;
    };

    /**
     * @param {boolean} value
     */
    this.setTitleOnly = function (value) {
      this.updateSearchParams({ titleOnly: value });
      $scope.searchObj.state.titleOnly = value;
    };

    /**
     * @return {boolean}
     */
    this.getTitleOnly = function () {
      return $scope.searchObj.state.titleOnly;
    };

    /**
     * @param {string} value
     */
    this.setLanguageStrategy = function (value) {
      this.updateSearchParams({ languageStrategy: value });
      $scope.searchObj.state.languageStrategy = value;
    };

    /**
     * @return {string}
     */
    this.getLanguageStrategy = function () {
      return $scope.searchObj.state.languageStrategy;
    };

    /**
     * @param {string} value
     */
    this.setForcedLanguage = function (value) {
      this.updateSearchParams({ forcedLanguage: value });
      $scope.searchObj.state.forcedLanguage = value;
    };

    /**
     * @return {string}
     */
    this.getForcedLanguage = function () {
      return $scope.searchObj.state.forcedLanguage;
    };

    /**
     * @param {array<string>} value
     */
    this.setLanguageWhiteList = function (value) {
      $scope.searchObj.state.languageWhiteList = value;
    };

    /**
     * @return {array<string>}
     */
    this.getLanguageWhiteList = function () {
      return $scope.searchObj.state.languageWhiteList;
    };

    this.getDetectedLanguage = function () {
      return $scope.searchObj.state.detectedLanguage;
    };

    /**
     * @param {boolean} value
     */
    this.setOnlyMyRecord = function (value) {
      if (value) {
        $scope.searchObj.params["owner"] = $scope.user.id;
      } else {
        delete $scope.searchObj.params["owner"];
      }
    };

    /**
     * @return {boolean}
     */
    this.getOnlyMyRecord = function () {
      $scope.value;
      if ($scope.searchObj.params["owner"]) {
        $scope.value = true;
      } else {
        $scope.value = false;
      }
      return $scope.value;
    };
  };

  searchFormController["$inject"] = [
    "$scope",
    "$location",
    "$parse",
    "$translate",
    "gnSearchManagerService",
    "Metadata",
    "gnSearchLocation",
    "gnESClient",
    "gnGlobalSettings",
    "gnESService",
    "gnESFacet",
    "gnAlertService",
    "md5"
  ];

  /**
   * Possible attributes:
   *  * runSearch: run search inmediately after the  directive is loaded.
   *  * waitForUser: wait until a user id is available to trigger the search.
   */
  module.directive("ngSearchForm", [
    "gnSearchLocation",
    "gnESService",
    function (gnSearchLocation, gnESService) {
      return {
        restrict: "A",
        scope: true,
        controller: searchFormController,
        controllerAs: "controller",
        link: function (scope, element, attrs) {
          scope.resetSearch = function (
            htmlElementOrDefaultSearch,
            preserveGeometrySearch
          ) {
            if (angular.isObject(htmlElementOrDefaultSearch)) {
              scope.controller.resetSearch(
                htmlElementOrDefaultSearch,
                preserveGeometrySearch
              );
            } else {
              scope.controller.resetSearch();
              $(htmlElementOrDefaultSearch).focus();
            }
          };

          var waitForPagination = function () {
            // wait for pagination to be set before triggering search
            if (element.find("[data-gn-pagination]").length > 0) {
              var unregisterFn = scope.$watch("hasPagination", function () {
                if (scope.hasPagination) {
                  scope.triggerSearch(true);
                  unregisterFn();
                }
              });
            } else {
              scope.triggerSearch(false);
            }
          };

          // Run a first search on directive rendering if attr is specified
          // Don't run it on page load if the permalink is 'on' and the
          // $location is not set to 'search'
          if (
            attrs.runsearch &&
            (!scope.searchObj.permalink || gnSearchLocation.isSearch())
          ) {
            // get permalink params on page load
            if (scope.searchObj.permalink) {
              scope.searchObj.params = gnSearchLocation.getParams();

              if (scope.searchObj.params.query_string) {
                scope.searchObj.state.filters = scope.searchObj.params.query_string;
              }
            }

            if (attrs.waitForUser === "true") {
              var userUnwatch = scope.$watch("user.id", function (userNewVal) {
                // Don't trigger the search until the user id has been loaded
                // Unregister the watch once we have the user id.
                if (angular.isDefined(userNewVal)) {
                  waitForPagination();
                  userUnwatch();
                }
              });
            } else {
              waitForPagination();
            }
          }
        }
      };
    }
  ]);
})();
