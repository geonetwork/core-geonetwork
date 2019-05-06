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

(function() {
  goog.provide('gn_wfsfilter_directive');

  // Custom feature and facet configuration example
  // var TMP_PROFILE =
  // { 'extendOnly': false,
  // 'fields': [
  // {
  // "name":"WATER_KM",
  // "aggs": {
  //
  // "histogram": {
  // "interval": 5000,
  // "extended_bounds" : {
  //   "min" : 2000,
  //   "max" : 100000
  // }
  // }
  // ,"range" : {
  // "ranges" : [
  //   { "to" : 7500 },
  //   { "from" : 7500, "to" : 50000 },
  //   { "from" : 50000 }
  // ]
  // }
  // }
  // },
  // {
  // "name": "CARPOOL"
  // }
  //
  // {'name': 'param_group_liste'},
  // {'name': 'ent_prog_cd'},
  // {'name': 'param_liste'},
  // {
  //   'name': 'CUSTOM_POS',
  //   'aggs': {
  //     'filters': {
  //       'filters': {
  //         '48 - 50': {'query_string':
  //               {'query':
  //                 '+ft_ent_longitude_s:<0.03 +ft_ent_latitude_s:<44'}},
  //         '49 - 53': {'query_string':
  //               {'query':
  //                 '+ft_ent_longitude_s:>0.03 +ft_ent_latitude_s:>45'}}
  //       }
  //     }
  //   }
  // }, {
  //   'name': 'range_Date',
  //   'type': 'rangeDate',
  //   'minField': 'date_min',
  //   'maxField': 'date_max',
  //   'display': 'graph'
  // },
  // {
  //   'name': 'date_min',
  //   'display': 'graph'
  // },
  // {
  //   'name': 'date_max',
  //   'display': 'form'
  // },
  // {
  //   'name': 'date_min',
  //   'display': 'graph'
  // }
  // ],
  // 'treeFields': ['CD_REGION'],
  // 'tokenizedFields': {
  // 'CGENELIN': '-'
  // }
  // };


  var module = angular.module('gn_wfsfilter_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacets
   *
   * @description
   */
  module.directive('gnWfsFilterFacets', [
    '$http',
    'wfsFilterService',
    '$q',
    '$rootScope',
    'gnIndexRequestManager',
    'gnIndexService',
    'gnGlobalSettings',
    'gnSearchSettings',
    'gnFeaturesTableManager',
    function($http, wfsFilterService, $q, $rootScope,
             gnIndexRequestManager, gnIndexService, gnGlobalSettings,
             gnSearchSettings, gnFeaturesTableManager) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacet.html',
        scope: {
          featureTypeName: '@',
          wfsUrl: '@',
          displayCount: '@',
          layer: '='
        },
        controller: function() {},
        link: function(scope, element, attrs, ctrl) {

          var indexUrl, uuid, ftName, appProfile, appProfilePromise;
          scope.map = scope.$parent.map;
          var map = scope.map;

          // Only admin can index the features
          scope.user = $rootScope.user;

          // Display or not the results count
          scope.showCount = angular.isDefined(attrs['showcount']);

          scope.ctrl = {
            searchGeometry: undefined
          };

          scope.output = {};
          // initialize object as it is not supposed to be undefined

          // if true, the "apply filters" button will be available
          scope.filtersChanged = false;
          scope.previousFilterState = {
            params: {},
            geometry: ''
          };

          // this will hold filters entered by the user for each facet
          // keys are facet names
          scope.facetFilters = {};

          // Get an instance of index object
          var indexObject, extentFilter;
          scope.filterGeometry = undefined;

          // Extent of current features matching the filter.
          scope.featureExtent = undefined;

          /**
           * Init the directive when the scope.layer has changed.
           * If the layer is given through the isolate scope object, the init
           * is called only once. Otherwise, the same directive is used for
           * all different feature types.
           */
          function init() {

            var source = scope.layer.getSource();
            if (!source || !(source instanceof ol.source.ImageWMS ||
                source instanceof ol.source.TileWMS)) {
              return;
            }

            angular.extend(scope, {
              fields: [],
              isWfsAvailable: undefined,
              isFeaturesIndexed: false,
              status: null,
              // FIXME: On page reload the md is undefined and the filter does not work
              md: scope.layer.get('md'),
              mdUrl: scope.layer.get('url'),
              url: gnGlobalSettings.getNonProxifiedUrl(
                scope.wfsUrl || scope.layer.get('url').replace(/wms/i, 'wfs'))
            });

            uuid = scope.md && scope.md.getUuid();
            // FIXME ? This comes from Sextant probably and
            // does not work here when current layer change
            // the previous featureTypeName is still used.
            // ftName = scope.featureTypeName ||             ftName = scope.featureTypeName ||
;
            ftName = scope.layer.getSource().getParams().LAYERS;
            scope.featureTypeName = ftName;

            appProfile = null;
            appProfilePromise = wfsFilterService.getApplicationProfile(uuid,
                ftName,
                gnGlobalSettings.getNonProxifiedUrl(scope.wfsUrl ? scope.url : scope.mdUrl),
                // A WFS URL is in the metadata or we're guessing WFS has
                // same URL as WMS
                scope.wfsUrl ? 'WFS' : 'WFS').then(
                function(response) {
                  if (response.status == 200) {
                    appProfile = angular.fromJson(response.data['0']);
                    return appProfile;
                  }
                }).catch(function() {});

            indexObject = wfsFilterService.registerEsObject(scope.url, ftName);
            scope.indexObject = indexObject;
            scope.layer.set('indexObject', indexObject);


            scope.checkWFSServerUrl();
            scope.initIndexRequest();
          };

          /**
           * Check if the provided WFS server url return a response.
           * @return {HttpPromise} promise
           */
          scope.checkWFSServerUrl = function() {
            return $http.get(scope.url)
                .then(function() {
                  scope.isWfsAvailable = true;
                }, function() {
                  scope.isWfsAvailable = false;
                });
          };

          /**
           * Init the index Request Object, either from meta index or from
           * application profile.
           */
          function loadFields() {

            // If an app profile is defined, then we update s
            // `olrObject.initialParams` with external config
            // appProfile = TMP_PROFILE;
            if (appProfile && appProfile.fields) {

              indexObject.indexFields =
                  wfsFilterService.indexMergeApplicationProfile(
                  indexObject.filteredDocTypeFieldsInfo, appProfile);
              indexObject.initBaseParams();
              if (!appProfile.extendOnly) {
                indexObject.setFielsdOrder();
              }
            }
            scope.hmActive = appProfile ? appProfile.heatmap : true;

            scope.resetFacets().then(scope.restoreInitialFilters);
          }
          function getDataModelLabel(fieldId) {
            for (var j = 0; j < scope.md.attributeTable.length; j++) {
              if (fieldId ==
                  scope.md.attributeTable[j].name) {
                return scope.md.attributeTable[j].definition;
              }
            }
            return null;
          }

          scope.initIndexRequest = function() {
            var config = {
              wfsUrl: scope.url,
              featureTypeName: ftName
            };

            scope.status = null;
            scope.isFeaturesIndexed = false;
            var docFields = [];
            scope.countTotal = null;

            indexObject.getDocTypeInfo(config).then(function() {
              scope.isFeaturesIndexed = true;
              scope.status = null;
              var docFields = indexObject.filteredDocTypeFieldsInfo;
              scope.countTotal = indexObject.totalCount;

              if (scope.md && scope.md.attributeTable) {
                for (var i = 0; i < docFields.length; i++) {
                  var label = getDataModelLabel(docFields[i].label);
                  if (label) {
                    // TODO: Multilingual
                    docFields[i].label = label;
                  }
                }
              }
              appProfilePromise.then(loadFields);
            }, function(error) {
              scope.status = error.data ? 'indexAccessError' : error.statusText;
              scope.statusTitle = error.statusText;
            });
          };
          scope.dropFeatures = function() {
            return $http.delete(
                '../api/workers/data/wfs/actions?serviceUrl=' +
                encodeURIComponent(scope.url) +
                '&typeName=' + encodeURIComponent(ftName)).then(function() {
              scope.initIndexRequest();
            }, function() {
              console.warn('Failed to remove features for type ' + id);
            });
          };
          /**
           * Update the state of the facet search.
           * The `scope.output` structure represent the state of the facet
           * checkboxes form.
           *
           * @param {string} fieldName index field name
           * @param {string} facetKey facet key for this field
           * @param {string} type facet type
           */
          scope.onCheckboxClick = function(field, facet) {

            var fieldName = field.name;
            var facetKey = facet.value;
            var output = scope.output;
            scope.lastClickedField = null;

            if (output[fieldName]) {
              if (output[fieldName].values[facetKey]) {
                delete output[fieldName].values[facetKey];
                if (Object.keys(output[fieldName].values).length == 0) {
                  delete output[fieldName];
                }
              }
              else {
                scope.lastClickedField = fieldName;
                output[fieldName].values[facetKey] = true;
              }
            }
            else {
              scope.lastClickedField = fieldName;
              output[fieldName] = {
                type: field.type,
                query: facet.query,
                values: {}
              };
              output[fieldName].values[facetKey] = true;
            }
            scope.filterFacets();
          };
          ctrl.onCheckboxClick = scope.onCheckboxClick;

          scope.isFacetSelected = function(fName, value) {
            try {
              var iS = scope.output[fName].values[value];
              return iS;
            }
            catch (e) {
              return false;
            }
          };

          // this returns a callback with the fieldname & type available
          // the callback is called when the date range is updated, with
          // 'from' and 'to' properties as arguments
          scope.onUpdateDateRange = function(field, dateFrom, dateTo) {
            scope.output[field.name] = {
              type: field.type || 'date',
              values: {
                from: dateFrom,
                to: dateTo
              }
            };

            scope.filterFacets();
          };

          /**
           * Send a new filtered request to index to update the facet ui
           * structure.
           * This method is called each time the user check or uncheck a box
           * from the ui, or when he updates the filter input.
           * @param {boolean} formInput the filter comes from input change
           */
          scope.filterFacets = function(formInput) {
            scope.$broadcast('FiltersChanged');

            // Update the facet UI
            var expandedFields = [];
            angular.forEach(scope.fields, function(f) {
              if (f.expanded) {
                expandedFields.push(f.name);
              }
            });

            // use value filters for facets
            var aggs = {};
            Object.keys(scope.facetFilters).forEach(function(facetName) {
              var filter = scope.facetFilters[facetName];
              if (!filter) { return; }

              // Regex filter can only be apply to string type.
              if (facetName.match(/^ft_.*_s$/)) {
                // make the filter case insensitive, ie : abc => [aA][bB][cC]
                // only alpha regex
                var lettersRegexOnly = /^[A-Za-z\u00C0-\u017F]+$/;
                filter = filter.replace(/./g, function (match) {
                  var upperMatch = scope.accentify(match).toUpperCase();
                  var lowerMatch = scope.accentify(match).toLowerCase();
                  return lettersRegexOnly.test(match) ? '[' + lowerMatch + upperMatch + ']': match;
                });

                aggs[facetName] = {
                  terms: {
                    include: '.*' + filter + '.*'
                  }
                };
              }
            });

            addBboxAggregation(aggs);

            //indexObject is only available if Elastic is configured
            if (indexObject) {
              indexObject.searchWithFacets({
                params: scope.output,
                geometry: scope.filterGeometry
              }, aggs).
                  then(function(resp) {
                    searchResponseHandler(resp);
                    angular.forEach(scope.fields, function(f) {
                      if (expandedFields.indexOf(f.name) >= 0) {
                        f.expanded = true;
                      }
                    });
                  });
            }
          };


          // Compute bbox of returned object
          // At some point we may be able to use geo_bounds aggregation
          // when it is supported for geo_shape type
          // See https://github.com/elastic/elasticsearch/issues/7574
          // Eg.
          // "viewport" : {
          //   "geo_bounds" : {
          //     "field" : "location",
          //     "wrap_longitude" : true
          //   }
          // }
          function addBboxAggregation(aggs) {
            aggs['bbox_xmin'] = {'min': {'field': 'bbox_xmin'}};
            aggs['bbox_ymin'] = {'min': {'field': 'bbox_ymin'}};
            aggs['bbox_xmax'] = {'max': {'field': 'bbox_xmax'}};
            aggs['bbox_ymax'] = {'max': {'field': 'bbox_ymax'}};
          };

          function setFeatureExtent(agg) {
            scope.autoZoomToExtent = true;
            if (scope.autoZoomToExtent
              && agg.bbox_xmin.value && agg.bbox_ymin.value
              && agg.bbox_xmax.value && agg.bbox_ymax.value) {
              var isPoint = agg.bbox_xmin.value === agg.bbox_xmax.value
                            && agg.bbox_ymin.value === agg.bbox_ymax.value,
                  radius = .05,
                  extent = [agg.bbox_xmin.value, agg.bbox_ymin.value,
                            agg.bbox_xmax.value, agg.bbox_ymax.value];

              if (isPoint) {
                var point = new ol.geom.Point([agg.bbox_xmin.value, agg.bbox_ymin.value]);
                extent = new ol.extent.buffer(point.getExtent(), radius);
              }
              scope.featureExtent = ol.extent.applyTransform(extent,
                ol.proj.getTransform("EPSG:4326", scope.map.getView().getProjection()));
            }
          };

          scope.zoomToResults = function () {
            scope.map.getView().fit(scope.featureExtent, scope.map.getSize());
          };

          scope.$watch('featureExtent', function(n, o) {
            if (n && n !== o) {
              scope.zoomToResults();
            }
          });


          scope.accentify = function(str) {
            var searchStr = str.toLocaleLowerCase()
            var accents = {
                a: 'àáâãäåæa',
                c: 'çc',
                e: 'èéêëæe',
                i: 'ìíîïi',
                n: 'ñn',
                o: 'òóôõöøo',
                s: 'ßs',
                u: 'ùúûüu',
                y: 'ÿy'
              }
            return accents.hasOwnProperty(searchStr) ? accents[searchStr] : str
          }

          scope.getMore = function(field) {
            indexObject.getFacetMoreResults(field).then(function(response) {
              field.values = response.facets[0].values;
            });
          };

          /**
           * reset and init the facet structure.
           * call the index service to get info on all facet fields and bind it
           * to the output structure to generate the ui.
           */
          scope.resetFacets = function() {
            scope.output = {};
            scope.lastClickedField = null;

            scope.resetSLDFilters();

            var boxElt = element.find('.gn-bbox-input');
            if (boxElt.length) {
              angular.element(boxElt).scope().clear();
            }

            scope.layer.set('esConfig', null);
            scope.$broadcast('FiltersChanged');

            // reset text search in facets
            scope.facetFilters = {};

            var aggs = {};
            addBboxAggregation(aggs);

            // load all facet and fill ui structure for the list
            return indexObject.searchWithFacets({}, aggs).
                then(function(resp) {
              searchResponseHandler(resp);
            });
          };

          function searchResponseHandler(resp) {
            indexObject.pushState();
            scope.count = resp.count;

            // if a facet was clicked, keep the previous facet object
            var lastClickedFacet = scope.fields.filter(function(e){
              return e.name === scope.lastClickedField;
            })[0];
            scope.fields = resp.facets.map(function(e){
              return lastClickedFacet && lastClickedFacet.name === e.name ?
                lastClickedFacet : e;
            });

            scope.sortAggregation();
            resp.indexData.aggregations &&
            setFeatureExtent(resp.indexData.aggregations);
          };

          /**
           * Each aggregations are sorted based as defined in the application profil config
           * and the query is ordered based on this config.
           *
           * The values of each aggregations are sorted, checked first.
           */
          scope.sortAggregation = function() {
            // Disable sorting of aggregations by alpha order and based on expansion
            // Order comes from application profile
            // scope.fields.sort(function (a, b) {
            //   var aChecked = !!scope.output[a.name];
            //   var bChecked = !!scope.output[b.name];
            //   var aLabel = a.label;
            //   var bLabel = b.label;
            //   if ((aChecked && bChecked) || (!aChecked && !bChecked)) {
            //     return aLabel.localeCompare(bLabel);
            //   }
            //   return (aChecked === bChecked) ? 0 : aChecked ? -1 : 1;
            // });

            scope.fields.forEach(function (facette) {
              facette.values.sort(function (a, b) {
                var aChecked = scope.isFacetSelected(facette.name, a.value);
                var bChecked = scope.isFacetSelected(facette.name, b.value);
                if ((aChecked && bChecked) || (!aChecked && !bChecked)) {
                  if (gnSearchSettings.facetOrdering === 'alphabetical') {
                    return a.value.localeCompare(b.value);
                  }
                  return b.count - a.count;
                }
                return (aChecked === bChecked) ? 0 : aChecked ? -1 : 1;
              })
            })
          }

          /**
           * alter form values & resend a search in case there are initial
           * filters loaded from the context. This must only happen once
           */
          scope.restoreInitialFilters = function() {
            // no initial filter: leave
            if (!indexObject.initialFilters) {
              return;
            }

            var initialFilters = indexObject.initialFilters;

            // apply filters to form
            scope.output = initialFilters.qParams || {};
            if (initialFilters.geometry) {
              scope.ctrl.searchGeometry =
                  initialFilters.geometry[0][0] + ',' +
                  initialFilters.geometry[1][1] + ',' +
                  initialFilters.geometry[1][0] + ',' +
                  initialFilters.geometry[0][1];
            }

            var aggs = {};
            addBboxAggregation(aggs);

            // resend a search with initial filters to alter the facets
            return indexObject.searchWithFacets({
              params: initialFilters.qParams,
              geometry: initialFilters.geometry
            }, aggs).then(function(resp) {
              indexObject.pushState();
              scope.fields = resp.facets;
              scope.sortAggregation();
              scope.count = resp.count;

              // look for date graph fields; call onUpdateDate to refresh them
              angular.forEach(scope.fields, function(field) {
                if (field.display == 'graph') {
                  // scope.onUpdateDate(field);
                }
              });

              scope.$broadcast('FiltersChanged');
            });
          };

          scope.resetSLDFilters = function() {
            scope.layer.getSource().updateParams({
              SLD: null
            });
            scope.layer.setExtent();
          };

          /**
           * On filter click, build from the UI the SLD rules config object
           * that will be send to generateSLD service.
           */
          scope.filterWMS = function() {
            // save this filter state for future comparison
            scope.previousFilterState.params = angular.merge({}, scope.output);
            scope.previousFilterState.geometry = scope.ctrl.searchGeometry;

            scope.zoomToResults();

            var defer = $q.defer();
            var sldConfig = wfsFilterService.createSLDConfig(scope.output);
            var layer = scope.layer;

            indexObject.pushState();
            layer.set('esConfig', indexObject);
            if (!extentFilter) {
              layer.setExtent();
            }
            else {
              layer.setExtent(
                  ol.proj.transformExtent(extentFilter, 'EPSG:4326',
                      scope.map.getView().getProjection()));

            }
            if (sldConfig.filters.length > 0) {
              wfsFilterService.getSldUrl(sldConfig, layer.get('url'),
                  ftName).success(function(sldURL) {
                // Do not activate it
                // Usually return 414 Request-URI Too Large
                var useSldBody = false;
                if (useSldBody) {
                  $http.get(sldURL).then(function(response) {
                    layer.getSource().updateParams({
                      SLD_BODY: response.data
                    });
                  });
                } else {
                  layer.getSource().updateParams({
                    SLD: sldURL
                  });
                }
              }).finally(function() {
                scope.filtersChanged = false;   // reset 'apply filters' button
                defer.resolve();
              });
            } else {
              layer.getSource().updateParams({
                SLD: null
              });
              scope.filtersChanged = false;
              defer.resolve();
            }
            return defer.promise;
          };

          /**
           * Trigger the indexation of the feature type.
           * Only available for administrators.
           */
          scope.indexWFSFeatures = function(version) {
            appProfilePromise.then(function() {
              wfsFilterService.indexWFSFeatures(
                  scope.url,
                  ftName,
                  appProfile ? appProfile.tokenizedFields : null,
                  appProfile ? appProfile.treeFields : null,
                  uuid,
                  version);
            });
          };

          // Init the directive
          if (scope.layer) {
            init();
          }
          else {
            scope.$watch('layer', function(n, o) {
              if (n !== o) {
                init();
              }
            });
          }

          //Manage geographic search
          scope.$watch('ctrl.searchGeometry', function(geom, old) {
            extentFilter = undefined;
            scope.filterGeometry = undefined;
            if (geom && geom != ',,,') {
              extentFilter = geom.split(',').map(function(val) {
                return parseFloat(val);
              });
              scope.filterGeometry = [
                [extentFilter[0], extentFilter[3]],
                [extentFilter[2], extentFilter[1]]
              ];
              scope.filterFacets();
            }
            // when reset from gnBbox directive
            else {
              scope.filterFacets();
            }
            // reset from wfsFilter directive: only signal change of filter
            scope.$broadcast('FiltersChanged');
          });

          scope.showTable = function() {
            gnFeaturesTableManager.clear();
            gnFeaturesTableManager.addTable({
              name: scope.layer.get('label') || scope.layer.get('name'),
              type: 'index'
            }, {
              map: scope.map,
              indexObject: indexObject,
              layer: scope.layer
            });
          };

          element.on('$destroy', function() {
            scope.$destroy();
          });

          // triggered when the filter state is changed
          // (compares with previous state)
          scope.$on('FiltersChanged', function(event, args) {
            // this handles the cases where bbox string value is undefined
            // or equal to ',,,' or '', which all amount to the same thing
            function normalize(s) { return (s || '').replace(',,,', ''); }

            var geomChanged = normalize(scope.ctrl.searchGeometry) !==
                normalize(scope.previousFilterState.geometry);

            // only compare params object if necessary
            var paramsChanged = false;
            if (!geomChanged) {
              paramsChanged = !angular.equals(
                  scope.previousFilterState.params, scope.output);
            }

            scope.filtersChanged = paramsChanged || geomChanged;
          });

          // returns true if there is an active filter for this field
          // field object is optional
          scope.isFilterActive = function(facetName, field) {
            // special case for geometry
            if (facetName == 'geometry') {
              return !!scope.filterGeometry;
            }

            // no available value: return false
            if (!scope.output[facetName]) {
              return false;
            }

            // special case for dates
            if (scope.output[facetName].type == 'date' ||
                scope.output[facetName].type == 'rangeDate') {
              var values = scope.output[facetName].values;

              // no dates defined: leave
              if (!field || !values) {
                return false;
              }

              // check if there is a valid "higher than" or "lower than" filter
              var lowerBound = field.dates && field.dates[0];
              var upperBound = field.dates && field.dates[field.dates.length - 1];
              var lowerActive = values.from &&
                  moment(values.from, 'DD-MM-YYYY').startOf('day').valueOf() >
                  lowerBound;
              var upperActive = values.to &&
                  moment(values.to, 'DD-MM-YYYY').endOf('day').valueOf() <
                  upperBound;
              return lowerActive || upperActive;
            }

            // other fields: the filter must be active
            return true;
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacetsTree
   *
   * @description
   * The global markup for the facet tree. Each node of the tree is a
   * sub directive `gnWfsFilterFacetsTreeItem`.
   *
   * This directive is used to be the controller of all the tree.
   */
  module.directive('gnWfsFilterFacetsTree', [
    function() {
      return {
        restrict: 'A',
        scope: {
          field: '<gnWfsFilterFacetsTree',
          isSelectedFn: '&gnWfsFilterFacetsTreeIsselected'
        },
        require: {
          wfsFilterCrl: '^^gnWfsFilterFacets'
        },
        template: '<div gn-wfs-filter-facets-tree-item=' +
            '"treeCtrl.field.tree"></div>',
        bindToController: true,
        controllerAs: 'treeCtrl',
        controller: function() {
          this.$onInit = function() {
            this.onCheckboxTreeClick = function(value) {

              this.wfsFilterCrl.onCheckboxClick(this.field, {
                value: value
              });
            };
            this.isSelected = function(value) {
              return this.isSelectedFn({
                name: this.field.name,
                value: value
              });
            };
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacetsTreeItem
   *
   * @description
   * The tree node structure of the wfs filter facet tree.
   */
  module.directive('gnWfsFilterFacetsTreeItem', [
    function() {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacetTreeItem.html',
        scope: {
          node: '<gnWfsFilterFacetsTreeItem'
        },
        require: {
          treeCtrl: '^^gnWfsFilterFacetsTree'
        },
        bindToController: true,
        controllerAs: 'ctrl',
        controller: ['$attrs', function($attrs) {
          this.isRoot = $attrs['gnWfsFilterFacetsTreeItemNotroot'] ===
              undefined;

          this.$onInit = function() {

            this.onCheckboxTreeClick = function() {
              this.treeCtrl.onCheckboxTreeClick(this.node.key);
            };
            this.isSelected = function() {
              return this.treeCtrl.isSelected(this.node.key);
            };
          };
        }],
        link: function(scope, el, attrs, ctrls) {
          scope.toggleNode = function(evt) {
            el.find('.fa').first().toggleClass('fa-minus-square')
                .toggleClass('fa-plus-square');
            el.children('.list-group').toggle();
            !evt || evt.preventDefault();
            evt.stopPropagation();
            return false;
          };
        }
      };
    }]);

})();
