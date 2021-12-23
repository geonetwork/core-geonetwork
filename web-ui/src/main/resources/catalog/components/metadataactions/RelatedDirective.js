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
  goog.provide('gn_related_directive');

  goog.require('gn_atom');
  goog.require('gn_related_observer_directive');
  goog.require('gn_relatedresources_service');
  goog.require('gn_wms');
  goog.require('gn_wmts');
  goog.require('gn_external_viewer');

  var module = angular.module('gn_related_directive', [
    'gn_relatedresources_service', 'gn_related_observer_directive', 'gn_wms',
    'gn_wmts', 'gn_atom', 'gn_external_viewer'
  ]);

  /**
   * Shows a list of related records given an uuid with the actions defined in
   * config.js
   */
  module.service('gnRelatedService', ['$http', '$q',
    function($http, $q) {
    this.get = function(uuidOrId, types) {
      var canceller = $q.defer();
      var request = $http({
        method: 'get',
        url: '../api/records/' + uuidOrId + '/related?' +
            (types ?
            'type=' + types.split('|').join('&type=') :
            ''),
        timeout: canceller.promise,
        cache: true
      });

      var promise = request.then(
          function(response) {
            return (response.data);
          },
          function() {
            return ($q.reject('Something went wrong loading ' +
            'related records of type ' + types));
          }
          );

      promise.abort = function() {
        canceller.resolve();
      };

      promise.finally(
          function() {
            promise.abort = angular.noop;
            canceller = request = promise = null;
          }
      );
      return (promise);
    };

    this.getMdsRelated = function(mds, types) {
      var uuids = mds.map(function (md) {
        return md.uuid;
      });
      var url = '../api/related';
      return $http.get(url, {
        params: {
          type: types,
          uuid: uuids
        }
      });
    };
    this.getMdsRelatedWithMultipleSearch = function(mds, types) {
      var uuids = mds.map(function(md) {
        return md.uuid;
      });
      // type:children > Is a children: If record.parentUuid then uuid: record.parentUuid
      // Is a service: If record.operatesOn then uuid: record.operatesOn
      // Is a sibling?: agg_associated: record.uuid

      var promise = $q.defer();
      var body = '';
      var searchFields = {
        'children': 'parentUuid',
        'services': 'recordOperateOn',
        'hassources': 'hassources',
        'associated': 'agg_associated',
        'hasfeaturecats': 'hasfeaturecats'
      };
      for (var j = 0; j < mds.length; j++) {
        for (var i = 0; i < types.length; i++) {
          body += '{"index": "records"}\n';
          switch (types[i]) {
            case 'services':
              body += '{' +
                '"query": {"terms": {' +
                '"' + (searchFields[types[i]] || 'uuid') + '": ["' + mds[j].uuid + '"]}}, ' +
                '"_source":["resourceTitle*", "id"]}\n';
              break;
            case 'children':
              body += '{' +
                '"query": {"terms": {' +
                '"' + (searchFields[types[i]] || 'uuid') + '": ["' + mds[j].uuid + '"]}}, ' +
                '"_source":["resourceTitle*", "id"]}\n';
              break;
            default:
              body += '{"query": {"match_all": {}}, "from": 0, "size": 0}\n'
          }
        }
      }
      $http.post('../api/search/records/_msearch', body).then(function (r) {
        var related = {};
        for (var j = 0; j < mds.length; j++) {
          var uuid = mds[j].uuid;
          related[uuid] = {};
          for (var i = 0; i < types.length; i++) {
            var t = types[i];
            var values = [];
            var results = r.data.responses[i + j];
            if (results.hits.total.value > 0) {
              for (var k = 0; k < results.hits.hits.length; k ++) {
                var record = results.hits.hits[k];
                values.push({
                  id: record._source.id,
                  title: {eng: record._source.resourceTitleObject.default}
                });
              }
            }
            related[uuid][t] = values.length > 0 ? values : undefined;
          }
        }
        promise.resolve({data: related});
      });

      return promise.promise;
    };
  }]);

  module
      .directive('gnRelated', [
        'gnRelatedService',
        'gnGlobalSettings',
        'gnSearchSettings',
        'gnRelatedResources',
        'gnExternalViewer',
        function(gnRelatedService, gnGlobalSettings,
                 gnSearchSettings, gnRelatedResources,
                 gnExternalViewer) {
          return {
            restrict: 'A',
            templateUrl: function(elem, attrs) {
              return attrs.template ||
                      '../../catalog/components/metadataactions/partials/related.html';
            },
            scope: {
              md: '=gnRelated',
              template: '@',
              types: '@',
              title: '@',
              list: '@',
              filter: '@',
              container: '@',
              user: '=',
              hasResults: '=?'
            },
            require: '?^gnRelatedObserver',
            link: function(scope, element, attrs, controller) {
              var promise;
              var elem = element[0];
              scope.lang = scope.lang || scope.$parent.lang;
              element.on('$destroy', function() {
                // Unregister the directive in the observer if it is defined
                if (controller) {
                  controller.unregisterGnRelated(elem);
                }
              });

              if (controller) {
                // Register the directive in the observer
                controller.registerGnRelated(elem);
              }

              scope.loadRelations = function(relation) {
                angular.forEach(relation, function(value, idx) {
                  if (!value) { return; }

                  // init object if required
                  scope.relations = scope.relations || {};
                  scope.relationFound = true;
                  scope.hasResults = true;

                  if (!scope.relations[idx]) {
                    scope.relations[idx] = [];
                  }
                  if (scope.filter && angular.isArray(value)) {
                    var tokens = scope.filter.split(':'),
                      field = tokens[0],
                      filter = tokens[1];
                    scope.relations[idx] = [];
                    for (var i = 0; i < value.length; i++) {
                      if (filter.indexOf(value[i][field]) !== -1) {
                        scope.relations[idx].push(value[i]);
                      }
                    }
                  } else {
                    scope.relations[idx] = value;
                  }

                  if (scope.relations.siblings && scope.relations.associated) {
                    for (var i = 0; i < scope.relations.associated.length; i++) {
                      if (scope.relations.siblings.filter(function (e) {
                        return e.id === scope.relations.associated[i].id;
                      }).length > 0) {
                        /* siblings object contains associated element */
                      } else {
                        scope.relations.siblings.push(scope.relations.associated[i])
                      }
                    }
                    scope.relations.associated = {};
                  }
                });
              };

              scope.updateRelations = function() {
                scope.relations = null;
                if (scope.id) {
                  scope.relationFound = false;
                  if (controller) {
                    controller.startGnRelatedRequest(elem);
                  }
                  (promise = gnRelatedService.get(
                     scope.id, scope.types)
                  ).then(function(data) {
                       scope.loadRelations(data);
                       if (angular.isDefined(scope.container)
                           && scope.relations == null) {
                         $(scope.container).hide();
                       }
                       if (controller) {
                         controller.finishRequest(elem, scope.relationFound);
                       }
                     } , function() {
                      if (controller) {
                        controller.finishRequest(elem, false);
                      }
                  });
                }
              };

              scope.getTitle = function(link) {
                return link.title['#text'] || link.title;
              };

              scope.externalViewerAction = function(mainType, link, md) {
                gnExternalViewer.viewService(md, link);
              };
              scope.hasAction = gnRelatedResources.hasAction;
              scope.getBadgeLabel = gnRelatedResources.getBadgeLabel;
              scope.isLayerProtocol = gnRelatedResources.isLayerProtocol;
              scope.externalViewerActionEnabled = gnExternalViewer.isEnabledViewAction();

              scope.config = gnRelatedResources;

              scope.$watchCollection('md', function(n, o) {
                if (n && n !== o || angular.isUndefined(scope.id)) {
                  if (promise && angular.isFunction(promise.abort)) {
                    promise.abort();
                  }
                  if (scope.md != null) {
                    if (scope.md.relatedRecords) {
                      var relations = {};
                      scope.types.split('|').map(function(t) {
                        relations[t] = scope.md.relatedRecords[t];
                      })
                      scope.loadRelations(relations);
                    } else {
                      scope.id = scope.md.id;
                      scope.updateRelations();
                    }
                  }
                }
              });
            }
          };
        }]);



  module
    .directive('gnRelatedSeriesByCriteria', [
      function() {
        return {
          restrict: 'A',
          templateUrl: function(elem, attrs) {
            return attrs.template ||
              '../../catalog/components/metadataactions/partials/relatedSeriesByCriteria.html';
          },
          scope: {
            children: '=gnRelatedSeriesByCriteria',
            agg: '=',
            filters: '=',
            sortBy: '@',
            title: '@'
          },
          link: function(scope, element, attrs, controller) {
            scope.lang = scope.lang || scope.$parent.lang;

            scope.criteria = {p: {}};

            function reset() {
              scope.displayedRecords = scope.children;
              scope.current = undefined;
            }

            reset();

            scope.filterRecordsBy = function(key, value) {
              var newKey = key + '-' + value;
              if (newKey === scope.current) {
                reset();
                return;
              }
              scope.current = key + '-' + value;
              scope.displayedRecords = [];
              var b = scope.agg[key].buckets;
              b.forEach(function (k) {
                if (k.key === value) {
                  k.docs.hits.hits.forEach(function (r) {
                    scope.displayedRecords =
                      scope.displayedRecords.concat(_.filter(scope.children, {id: r._id}));
                  });

                  if (scope.sortBy) {
                    scope.displayedRecords.sort(function(a, b) {
                      return a.record[scope.sortBy].localeCompare(b.record[scope.sortBy])
                    });
                  }
                }
              });
            };
          }
        };
      }]);

  module.directive('relatedTooltip', function() {
    return function(scope, element, attrs) {
      for (var i = 0; i < element.length; i++) {
        element[i].title = scope.$parent.md['@type'];
      }
      element.tooltip();
    };
  });


  module
    .directive('gnRecordLinksButton', ['gnRelatedResources',
      function(gnRelatedResources) {
        return {
          restrict: 'A',
          replace: true,
          transclude: true,
          templateUrl: function(elem, attrs) {
            return attrs.template ||
              '../../catalog/components/metadataactions/partials/recordLinksButton.html';
          },
          scope: {
            links: '=gnRecordLinksButton',
            // empty or dropdown or dropdownOrButton (if one link)
            btn: '@',
            btnClass: '@',
            btnDisabled: '=',
            type: '=',
            title: '@',
            altTitle: '@',
            // none, dropdownOnly
            iconMode: '@',
            iconClass: '@',
            record: '='
          },
          link: function(scope, element, attrs, controller) {
            if (scope.links && scope.links.length > 0) {
              scope.mainType = gnRelatedResources.getType(scope.links[0], scope.type || 'onlines');
              scope.icon = scope.iconClass || gnRelatedResources.getClassIcon(scope.mainType);
            }
          }
        }
      }]);

  /**
   * Can support a link returned by the related API
   * or a link in a metadata record.
   *
   * Related API provides multilingual links and takes care of
   * user privileges. For metadata link, check download/dynamic properties.
   */
  module
    .directive('gnRecordLinkButton', ['gnRelatedResources',
      function(gnRelatedResources) {
        return {
          restrict: 'A',
          templateUrl: function(elem, attrs) {
            return attrs.template ||
              '../../catalog/components/metadataactions/partials/recordLinkButton.html';
          },
          scope: {
            link: '=gnRecordLinkButton',
            btn: '=',
            btnClass: '=',
            btnDisabled: '=',
            // none, only
            iconMode: '=',
            iconClass: '=',
            type: '=',
            record: '='
          },
          link: function(scope, element, attrs, controller) {
            if (scope.link) {
              scope.mainType = gnRelatedResources.getType(scope.link, scope.type || 'onlines');
              scope.badge = gnRelatedResources.getBadgeLabel(scope.mainType, scope.link);
              scope.icon = scope.iconClass || gnRelatedResources.getClassIcon(scope.mainType);
              scope.hasAction = gnRelatedResources.hasAction(scope.mainType);
              scope.service = gnRelatedResources;
              scope.isDropDown = scope.btn && scope.btn.indexOf('dropdown') === 0;
              scope.isSibling = (scope.mainType == 'MDSIBLING'
                && scope.link.associationType
                && scope.link.associationType != '');
            }
          }
        }
      }]);

  module
    .directive('gnRecordsTable', [
      'Metadata',
      function(Metadata) {
        return {
          restrict: 'A',
          templateUrl: function(elem, attrs) {
            return attrs.template ||
              '../../catalog/components/metadataactions/partials/recordsTable.html';
          },
          scope: {
            records: '=gnRecordsTable',
            // Comma separated values. Supported
            // * properties eg. resourceTitle
            // * object path eg. cl_status.key
            // * links by type eg. link:OGC
            columns: '@',
            labels: '@'
          },
          link: function(scope, element, attrs, controller) {
            var initialized = false;
            scope.columnsConfig = scope.columns.split(',');
            scope.data = [];
            scope.headers = [];
            scope.isArray = angular.isArray;

            if (scope.labels) {
              scope.headers = scope.labels.split(',');
            } else {
              scope.columnsConfig.map(function(c) {
                scope.headers.push(c.startsWith('link/') ? c.split('/')[1] : c);
              });
            }

            function loadData() {
              scope.data = [];
              scope.records.map(function(r) {
                r = new Metadata(r);
                var recordData = {};
                scope.columnsConfig.map(function(c) {
                  recordData[c] = c.startsWith('link/')
                    ? r.getLinksByType(c.split('/')[1])
                    : (c.indexOf('.') != -1 ? _.at(r, c) : r[c]);
                });
                recordData.md = r;
                scope.data.push(recordData);
              });
              scope.data.sort(function(a, b) {
                var sortBy = scope.columnsConfig[0];
                return a[sortBy].localeCompare(b[sortBy])
              });
            }

            scope.$watchCollection('records', function(n, o) {
              if (n && (n !== o || !initialized)) {
                loadData();
                initialized = true;
              }
            });
          }
        }
    }]);
})();
