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
  goog.provide('gn_saved_selections_directive');

  var module = angular.module('gn_saved_selections_directive',
      []);

  module.factory('gnSavedSelectionConfig', ['$location', function($location) {
    return {
      actions: {
        'PreferredList': {
          label: 'searchSelectedRecord',
          fn: function(uuids) {
            // TODO: Redirect to search app if in admin
            $location.path('/search').search('_uuid', uuids.join(' or '));
          },
          icon: 'fa-search'
        }
      }
    };
  }]);

  /**
   * @ngdoc directive
   * @name gn_saved_selections.directive:gnSavedSelections
   * @restrict A
   * @requires gnSavedSelectionsService
   * @requires $translate
   *
   * @description
   *
   */
  module.directive('gnSavedSelections', [
    'gnSearchManagerService', '$http', '$q',
    function(gnSearchManagerService, $http, $q) {

      // List of persistent selections
      // and user records in each selections
      var selections = {
        list: [],
        labels: {},
        size: 0,
        refreshCounter: 1
      };

      var user = null;

      function SavedSelectionController(scope) {
      };

      SavedSelectionController.prototype.init =
          function(user) {
        var defer = $q.defer(), allRecords = [];
        selections.list = [];
        // Load user data
        $http.get('../api/userselections').then(function(r) {
          selections.list = r.data;

          var getUserSelections = [];
          // Load records for each selection
          angular.forEach(selections.list, function(sel) {
            getUserSelections.push(
                $http.get('../api/userselections/' +
                sel.id + '/' + user).then(
                function(response) {
                  sel.records = response.data;
                  allRecords = allRecords.concat(response.data);
                }));
          });

          $q.all(getUserSelections).then(function() {
            // Load record labels
            gnSearchManagerService.search(
                'q?_content_type=json&buildSummary=false&' +
                'fast=index&_uuid=' +
                allRecords.join(' or ')).then(
                function(r) {
                  angular.forEach(r.metadata, function(md) {
                    if (md) {
                      selections.labels[md['geonet:info'].uuid] =
                          md.title || md.defaultTitle;
                    }
                  });
                  selections.size = allRecords.length;
                  selections.refreshCounter++;
                  defer.resolve(selections);
                });

          });
        });
        return defer.promise;
      };

      SavedSelectionController.prototype.getSelections =
          function(user) {
        if (user && this.userId !== user.id) {
          this.userId = user.id;
          return this.init(this.userId);
        } else {
          var defer = $q.defer();
          defer.resolve(selections);
          return defer.promise;
        }
      };

      SavedSelectionController.prototype.add =
          function(selection, user, uuids) {
        var ctrl = this;
        if (typeof selection === 'string') {
          selection = this.getSelectionId(selection);
        }
        return $http.put('../api/userselections/' +
            selection + '/' + this.userId, null, {
              params: {
                uuid: uuids
              }
            }).then(function(r) {
          ctrl.init(ctrl.userId);
        });
      };

      SavedSelectionController.prototype.remove =
          function(selection, user, uuids) {
        var ctrl = this;
        return $http.delete('../api/userselections/' +
            selection + '/' + this.userId, {
              params: {
                uuid: uuids
              }
            }).then(function(r) {
          ctrl.init(ctrl.userId);
        });
      };
      SavedSelectionController.prototype.getSelectionId =
          function(selection) {
        for (var i = 0; i < selections.list.length; i++) {
          if (selections.list[i].name === selection) {
            return selections.list[i].id;
          }
        }
      };

      return {
        restrict: 'A',
        controller: ['$scope', SavedSelectionController]
        //scope: {}
      };
    }]);


  /**
   *
   */
  module.directive('gnSavedSelectionsPanel', [
    '$translate', 'gnLangs', 'gnSavedSelectionConfig',
    function($translate, gnLangs, gnSavedSelectionConfig) {
      function link(scope, element, attrs, controller) {
        scope.lang = gnLangs.current;
        scope.selections = null;
        scope.actions = gnSavedSelectionConfig.actions;

        scope.$watch('user', function(n, o) {
          if ((n !== o && n && n.id) || scope.selections === null) {
            scope.selections = null;
            controller.getSelections(scope.user).then(function(selections) {
              scope.selections = selections;
            });
          }
        });

        scope.remove = function(selection, uuid) {
          controller.remove(selection, scope.user, uuid);
        };

      }

      return {
        restrict: 'A',
        require: '^gnSavedSelections',
        templateUrl: '../../catalog/components/' +
            'common/savedselections/partials/' +
            'panel.html',
        link: link,
        scope: {
          user: '=gnSavedSelectionsPanel'
        }
      };
    }]);


  /**
   *
   */
  module.directive('gnSavedSelectionsAction', function() {
    function link(scope, element, attrs, controller) {
      scope.selectionsWithRecord = [];
      scope.selections = {};
      scope.$watchCollection('selections', function(n, o) {
        if (n.refreshCounter !== o.refreshCounter) {
          // Check in which selection this record is in
          scope.selectionsWithRecord = [];
          for (var i = 0; i < scope.selections.list.length; i++) {
            var s = scope.selections.list[i];
            if (s.records) {
              for (var j = 0; j < s.records.length; j++) {
                if (s.records[j] === scope.uuid) {
                  scope.selectionsWithRecord.push(s.id);
                  break;
                }
              }
            }
          }
        }
      });

      controller.getSelections(scope.user).then(function(selections) {
        scope.selections = selections;
      });

      scope.add = function(selection) {
        controller.add(selection, scope.user, scope.uuid);
      };
      scope.remove = function(selection) {
        controller.remove(selection, scope.user, scope.uuid);
      };
    }
    return {
      restrict: 'A',
      templateUrl: '../../catalog/components/common/savedselections/partials/' +
          'action.html',
      require: '^gnSavedSelections',
      link: link,
      scope: {
        selection: '@gnSavedSelectionsAction',
        uuid: '=',
        user: '=',
        lang: '='
      }
    };
  });
})();
