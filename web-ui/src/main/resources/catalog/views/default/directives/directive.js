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

  goog.provide('gn_search_default_directive');

  var module = angular.module('gn_search_default_directive', []);

  module.directive('gnInfoList', ['gnMdView',
    function(gnMdView) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
            'partials/infolist.html',
        link: function linkFn(scope, element, attr) {
          scope.showMore = function(isDisplay) {
            var div = $('#gn-info-list' + this.md.getUuid());
            $(div.children()[isDisplay ? 0 : 1]).addClass('hidden');
            $(div.children()[isDisplay ? 1 : 0]).removeClass('hidden');
          };
          scope.go = function(uuid) {
            gnMdView(index, md, records);
            gnMdView.setLocationUuid(uuid);
          };
        }
      };
    }
  ]);

  module.directive('gnAttributeTableRenderer', ['gnMdView',
    function(gnMdView) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
        'partials/attributetable.html',
        scope: {
          attributeTable: '=gnAttributeTableRenderer'
        },
        link: function linkFn(scope, element, attrs) {
          if (angular.isDefined(scope.attributeTable) &&
            !angular.isArray(scope.attributeTable)) {
            scope.attributeTable = [scope.attributeTable];
          }
        }
      };
    }
  ]);

  module.directive('gnDataQualityMeasureRenderer', ['$http', '$q',
    function($http, $q) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
        'partials/measures.html',
        scope: {
          recordId: '=gnDataQualityMeasureRenderer',
          cptId: '@'
        },
        link: function linkFn(scope, element, attrs) {
          scope.components = {};

          if (angular.isDefined(scope.recordId)) {
            loadValues();
          }

          function getQe(cptId, qm) {
            return getDerivatedValues(cptId, qm, 'APE');
          };
          function getFu(cptId, qm) {
            return getDerivatedValues(cptId, qm, 'FU');
          };
          function getDerivatedValues(cptId, qm, codePrefix) {
              var qmId = qm.replace('AP', '');
            for (var i = 0; i < scope.qm.length; i++) {
              if (scope.qm[i][0].replace('#QE', '') === cptId &&
                scope.qm[i][2].indexOf(codePrefix + qmId) !== -1) {
                return {
                  name: scope.qm[i][3] + ' (' + scope.qm[i][2] + ')',
                  value: scope.qm[i][5] !== '' ?
                    scope.qm[i][5] + ' ' + scope.qm[i][6] : ''
                };
              }
            }
            return {
              name: null,
              value: null
            };
          };


          function getDpsOrTdpValues(cptId, mId, qm) {
            var tokens = cptId.split('/');
            var dpsKey = tokens[0];
            var q1 = $q.defer();
            var q2 = $q.defer();

            if (scope.isUd || scope.isTdp) {
              $http.get('q?fast=index&_content_type=json&_uuid=' + dpsKey, {
                cache: true
              }).then(function (r) {
                if (r.data.metadata && r.data.metadata.dqValues) {
                  var values = r.data.metadata.dqValues;
                  for(var i = 0; i < values.length; i ++) {
                    var v = values[i];
                    if (v.indexOf(tokens[0] + '/' + tokens[1]) === 0 &&
                        v.indexOf(mId) !== -1) {
                      var t = v.split('|');
                      qm.dps = t[5] != '' ? t[5] + ' ' + t[6] : '';
                      q1.resolve(qm);
                      break;
                    }
                  }
                }
                q1.resolve(qm);
              });
            } else {
              q1.resolve(qm);
            }


            if (scope.isUd) {
              if (tokens.length === 3) {
                var tdpKey = tokens[2];
                $http.get('q?fast=index&_content_type=json&_uuid=' + tdpKey, {
                  cache: true
                }).then(function (r) {
                  if (r.data.metadata && r.data.metadata.dqValues) {
                    var values = r.data.metadata.dqValues;
                    for(var i = 0; i < values.length; i ++) {
                      var v = values[i];
                      if (v.indexOf(cptId) === 0 && v.indexOf(mId) !== -1) {
                        var t = v.split('|');
                        qm.tdp = t[5] != '' ? t[5] + ' ' + t[6] : '';
                        q2.resolve(qm);
                        break;
                      }
                    }
                  }
                  q2.resolve(qm);
                });
              }
            } else {
              q2.resolve(qm);
            }
            return $q.all([q1.promise, q2.promise]);
          };

          function loadValues() {
            scope.isUd = false;
            scope.isTdp = false;
            $http.get('qi?_content_type=json&fast=index&_id=' +
              scope.recordId).then(
              function (r) {
                scope.isUd = r.data.metadata.standardName
                              .indexOf('Upstream Data') !== -1;
                scope.isTdp = r.data.metadata.standardName
                              .indexOf('Targeted Data Product') !== -1;
                scope.qm = r.data.metadata.dqValues;
                angular.forEach(scope.qm, function (value, idx) {
                  scope.qm[idx] = value.split('|');
                });

                scope.components = {};
                // Group by component
                // Group by QM+QE+FU
                angular.forEach(scope.qm, function (value, idx) {
                  var isQeOrFu = value[0].indexOf('#QE') !== -1,
                      cptId = value[0];

                  if (cptId.indexOf(scope.cptId) === 0) {
                    if (!isQeOrFu) {
                      if (!scope.components[cptId]) {
                        scope.components[cptId] = {
                          id: cptId,
                          name: value[1],
                          measures: []
                        };
                      }

                      var qe = getQe(cptId, value[2]);
                      var fu = getFu(cptId, value[2]);
                      getDpsOrTdpValues(cptId, value[2], {
                        qmName: value[3] + ' (' + value[2] + ')',
                        qmDefinition: value[7],
                        qmStatement: value[8],
                        qm: value[5] != '' ? value[5] + ' ' + value[6] : '',
                        qeName: qe.name,
                        qe: qe.value,
                        fuName: fu.name,
                        fu: fu.value
                      }).then(function (values) {
                        scope.components[cptId].measures.push(values[0]);
                      });
                    }
                  }
                });
              }
            )
          }
        }
      };
    }
  ]);

  module.directive('gnLinksBtn', [ 'gnTplResultlistLinksbtn',
    function(gnTplResultlistLinksbtn) {
      return {
        restrict: 'E',
        replace: true,
        scope: true,
        templateUrl: gnTplResultlistLinksbtn
      };
    }
  ]);

  module.directive('gnMdActionsMenu', ['gnMetadataActions',
    function(gnMetadataActions) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gnMdActionsMenu);

          scope.$watch(attrs.gnMdActionsMenu, function(a) {
            scope.md = a;
          });

          scope.getScope = function() {
            return scope;
          }
        }
      };
    }
  ]);

  module.directive('gnPeriodChooser', [
    function() {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
            'partials/periodchooser.html',
        scope: {
          label: '@gnPeriodChooser',
          dateFrom: '=',
          dateTo: '='
        },
        link: function linkFn(scope, element, attr) {
          var today = moment();
          scope.format = 'YYYY-MM-DD';
          scope.options = ['today', 'yesterday', 'thisWeek', 'thisMonth',
            'last3Months', 'last6Months', 'thisYear'];
          scope.setPeriod = function(option) {
            if (option === 'today') {
              var date = today.format(scope.format);
              scope.dateFrom = date;
            } else if (option === 'yesterday') {
              var date = today.clone().subtract(1, 'day')
                .format(scope.format);
              scope.dateFrom = date;
              scope.dateTo = today.format(scope.format);
              return;
            } else if (option === 'thisWeek') {
              scope.dateFrom = today.clone().startOf('week')
                .format(scope.format);
            } else if (option === 'thisMonth') {
              scope.dateFrom = today.clone().startOf('month')
                .format(scope.format);
            } else if (option === 'last3Months') {
              scope.dateFrom = today.clone().startOf('month').
                  subtract(3, 'month').format(scope.format);
            } else if (option === 'last6Months') {
              scope.dateFrom = today.clone().startOf('month').
                  subtract(6, 'month').format(scope.format);
            } else if (option === 'thisYear') {
              scope.dateFrom = today.clone().startOf('year')
                .format(scope.format);
            }
            scope.dateTo = today.add(1, 'day').format(scope.format);
          };
        }
      };
    }
  ]);
})();
