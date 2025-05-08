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
  goog.provide("gn_saved_selections_directive");

  var module = angular.module("gn_saved_selections_directive", []);

  module.factory("gnSavedSelectionConfig", [
    "$location",
    "Metadata",
    "gnMap",
    "gnSearchSettings",
    "gnExternalViewer",
    function ($location, Metadata, gnMap, gnSearchSettings, gnExternalViewer) {
      var viewerMap = gnSearchSettings.viewerMap;

      var searchRecordsInSelection = function (uuid, records) {
        // TODO: Redirect to search app if not in a search page
        $location.path("/search").search("uuid", uuid);
      };
      return {
        // Actions defined for each type of list to
        // trigger something on this selection (eg.
        // run a search to display only this saved
        // selection content.
        actions: {
          PreferredList: {
            label: "searchSelectedRecord",
            fn: searchRecordsInSelection,
            icon: "fa-search"
          },
          AnonymousUserlist: {
            label: "searchSelectedRecord",
            fn: searchRecordsInSelection,
            icon: "fa-search"
          },
          MapLayerlist: {
            label: "addToMap",
            filterFn: function (record) {
              var md = new Metadata(record);
              const linkTypes = [
                "OGC:WMS",
                "ESRI REST: Map Service",
                "ESRI REST: Map Server"
              ];
              // returns true as soon as any type has ≥1 link
              return linkTypes.some(type => md.getLinksByType(type).length > 0);
            },
            fn: function (uuids, records) {
              for (var i = 0; i < uuids.length; i++) {
                var uuid = uuids[i],
                  record = records[uuid];

                var md = new Metadata(record);
                angular.forEach(md.getLinksByType("OGC:WMS"), function (link) {
                  if (gnExternalViewer.isEnabled()) {
                    gnExternalViewer.viewService(
                      {
                        id: md.id,
                        uuid: md.uuid
                      },
                      {
                        url: link.url,
                        type: "wms",
                        name: link.name,
                        title: link.title
                      }
                    );
                    return;
                  }

                  if (gnMap.isLayerInMap(viewerMap, link.name, link.url)) {
                    return;
                  }
                  gnMap
                    .addWmsFromScratch(viewerMap, link.url, link.name, false, md)
                    .then(function (layer) {
                      if (layer) {
                        gnMap.feedLayerWithRelated(layer, link.group);
                      }
                    });
                });

                const esriLinks = [
                  ...md.getLinksByType("ESRI REST: Map Server"),
                  ...md.getLinksByType("ESRI REST: Map Service")
                ];

                angular.forEach(esriLinks, function (link) {
                  if (gnMap.isLayerInMap(viewerMap, link.name, link.url)) {
                    return;
                  }

                  gnMap
                    .addEsriRestLayer(viewerMap, link.url, link.name, false, md)
                    .then(function (layer) {
                      if (layer) {
                        gnMap.feedLayerWithRelated(layer, link.group);
                      }
                    });

                });

              }
            },
            icon: "fa-globe"
          }
        },
        // Add user session selection types
        // * MapLayerList is a local selection used to add
        //   multiple layers in one go
        // * AnonymousUserList is a list of preferred records
        //   for anonymous user only stored in localStorage.
        localList: [
          {
            id: -10,
            name: "AnonymousUserlist",
            records: [],
            // Can be localStorage, sessionStorage or
            // null (ie. not preserved on page refresh).
            storage: "localStorage",
            isAnonymousOnly: true
          },
          {
            id: -20,
            name: "MapLayerlist",
            records: [],
            storage: null
            // }, {
            //   id: -30,
            //   name: 'DataDownloaderlist',
            //   records: [],
            //   storage: null
          }
        ]
      };
    }
  ]);

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
  module.directive("gnSavedSelections", [
    "gnSearchManagerService",
    "gnSavedSelectionConfig",
    "$http",
    "$q",
    "$rootScope",
    "$translate",
    "Metadata",
    function (
      gnSearchManagerService,
      gnSavedSelectionConfig,
      $http,
      $q,
      $rootScope,
      $translate,
      Metadata
    ) {
      // List of persistent selections
      // and user records in each selections
      var selections = {
        list: [],
        records: {},
        size: 0,
        refreshCounter: 1
      };

      var user = null;
      var storagePrefix = "basket";
      var maxSize = 200;

      function SavedSelectionController(scope) {}

      // Load metadata record. This is needed to load
      // the title to be displayed in the panel. Only the uuid
      // is stored in saved selections.
      SavedSelectionController.prototype.loadrecords = function (
        defer,
        selections,
        allRecords
      ) {
        selections.notFound = [];
        var ctrl = this;

        if (allRecords.length === 0) {
          selections.records = {};
          selections.size = 0;
          defer.resolve(selections);
          return;
        }

        // TODO: Handle case when there is
        // too many items in the saved selections
        $http
          .post(
            "../api/search/records/_search",
            {
              _source: {
                includes: ["uuid", "root", "resourceTitle*", "isTemplate", "link"]
              },
              from: 0,
              size: 2000,
              query: {
                bool: {
                  must: [{ terms: { uuid: allRecords } }]
                }
              }
            },
            { cache: true }
          )
          .then(function (r) {
            var foundRecords = [];
            angular.forEach(r.data.hits.hits, function (md) {
              var uuid = md._source.uuid;
              selections.records[uuid] = new Metadata(md);
              foundRecords.push(uuid);
            });

            // Identify records which have been deleted
            // or that current user can't see anymore.
            // This only applies to session list.
            selections.notFound = allRecords.filter(function (i) {
              return foundRecords.indexOf(i) === -1;
            });

            // Remove not found records from the selection.
            for (var i = 0; i < selections.notFound.length; i++) {
              angular.forEach(selections.list, function (sel) {
                if (sel.id < 0) {
                  ctrl.removeFromStore(sel, selections.notFound[i]);
                }
              });
            }

            selections.size = allRecords.length;
            selections.refreshCounter++;

            $rootScope.$broadcast("savedSelectionsUpdate", selections);

            defer.resolve(selections);
          });
      };

      // Load the list of db saved selection + local selection
      // and then load the content of each selections
      // from db or local/session storage.
      SavedSelectionController.prototype.init = function (user, localOnly) {
        var defer = $q.defer(),
          allRecords = [],
          ctrl = this;
        selections.list = [];

        angular.forEach(gnSavedSelectionConfig.localList, function (s) {
          if (!(user && user.id !== undefined && s.isAnonymousOnly)) {
            selections.list.push(s);
          }
        });

        // Load user data
        // List of selections does not change often. Cache them.
        $http.get("../api/userselections", { cache: true }).then(function (r) {
          if (user != undefined) {
            selections.list = selections.list.concat(r.data);
          }
          var getUserSelections = [];
          // Load records for each selection
          angular.forEach(selections.list, function (sel) {
            // Local selections have negative identifiers
            if (sel.id > -1) {
              if (user != undefined) {
                getUserSelections.push(
                  $http
                    .get("../api/userselections/" + sel.id + "/" + user)
                    .then(function (response) {
                      sel.records = response.data;
                      allRecords = allRecords.concat(response.data);
                    })
                );
              }
            } else {
              if (sel.storage !== null) {
                var key = storagePrefix + sel.name,
                  array = window[sel.storage].getItem(key);
                var records = array != "null" ? angular.fromJson(array) : [];
                sel.records = records;
                window[sel.storage].setItem(key, angular.toJson(records));
              }
              allRecords = allRecords.concat(sel.records);
            }
          });

          $q.all(getUserSelections).then(function () {
            ctrl.loadrecords(defer, selections, allRecords);
          });
        });
        return defer.promise;
      };

      SavedSelectionController.prototype.getSelections = function (user) {
        if (user && this.userId !== user.id) {
          this.userId = user.id;
          return this.init(this.userId);
        } else if (user === undefined) {
          this.userId = undefined;
          return this.init();
        } else {
          var defer = $q.defer();
          defer.resolve(selections);
          return defer.promise;
        }
      };

      SavedSelectionController.prototype.add = function (selection, user, uuid) {
        var ctrl = this;

        var tooManyItems = selection.records.length > maxSize;
        if (tooManyItems) {
          $rootScope.$broadcast("StatusUpdated", {
            msg: $translate.instant("tooManyItemsInSelection", { maxSize: maxSize }),
            timeout: 0,
            type: "danger"
          });
          return;
        }

        if (selection.id > -1) {
          if (typeof selection === "string") {
            selection = this.getSelectionId(selection);
          }

          return $http
            .put("../api/userselections/" + selection.id + "/" + this.userId, null, {
              params: {
                uuid: uuid
              }
            })
            .then(function (r) {
              ctrl.init(ctrl.userId);
            });
        } else {
          this.addToStore(this.getSelection(selection), uuid);
          ctrl.init(ctrl.userId, true);
        }
      };

      SavedSelectionController.prototype.remove = function (selection, user, uuid) {
        var ctrl = this;
        if (selection.id > -1) {
          return $http
            .delete("../api/userselections/" + selection.id + "/" + this.userId, {
              params: {
                uuid: uuid
              }
            })
            .then(function (r) {
              ctrl.init(ctrl.userId);
            });
        } else {
          this.removeFromStore(this.getSelection(selection), uuid);
        }
      };

      // For local selection, the storage is in synch with
      // the selection records property.
      SavedSelectionController.prototype.addToStore = function (selection, uuid) {
        if (selection.storage !== null) {
          var key = storagePrefix + selection.name,
            array = window[selection.storage].getItem(key);
          var records = array != "null" ? angular.fromJson(array) : [];
          records.push(uuid);
          window[selection.storage].setItem(key, angular.toJson(records));
        }
        selection.records.push(uuid);
      };

      SavedSelectionController.prototype.removeFromStore = function (selection, uuid) {
        if (selection.storage !== null) {
          var key = storagePrefix + selection.name,
            array = window[selection.storage].getItem(key);
          var records = array != "null" ? angular.fromJson(array) : [];
          var idx = records.indexOf(uuid);
          if (idx > -1) {
            records.splice(idx, 1);
            window[selection.storage].setItem(key, angular.toJson(records));
          }
        }
        if (selection.records) {
          var idx = selection.records.indexOf(uuid);
          if (idx > -1) {
            selection.records.splice(idx, 1);
            this.init(this.userId, true);
          }
        }
      };

      SavedSelectionController.prototype.getSelectionId = function (name) {
        for (var i = 0; i < selections.list.length; i++) {
          if (selections.list[i].name === name) {
            return selections.list[i].id;
          }
        }
      };

      // Return the selection object if an id is provided
      SavedSelectionController.prototype.getSelection = function (selOrId) {
        if (typeof selOrId === "number") {
          for (var i = 0; i < selections.list.length; i++) {
            if (selections.list[i].id === selOrId) {
              return selections.list[i];
            }
          }
        } else {
          return selOrId;
        }
      };

      return {
        restrict: "A",
        controller: ["$scope", SavedSelectionController]
      };
    }
  ]);

  /**
   * Panel to manage user saved selection content
   */
  module.directive("gnSavedSelectionsPanel", [
    "$translate",
    "gnLangs",
    "gnSavedSelectionConfig",
    "gnGlobalSettings",
    function ($translate, gnLangs, gnSavedSelectionConfig, gnGlobalSettings) {
      function link(scope, element, attrs, controller) {
        scope.lang = gnLangs.current;
        scope.selections = null;
        scope.actions = gnSavedSelectionConfig.actions;
        scope.isSavedSelectionEnabled =
          gnGlobalSettings.gnCfg.mods.search.savedSelection.enabled;

        if (!scope.isSavedSelectionEnabled) {
          return;
        }

        scope.$watch(
          "user",
          function (n, o) {
            if (n !== o || scope.selections === null) {
              scope.selections = null;
              controller.getSelections(scope.user).then(function (selections) {
                scope.selections = selections;
              });
            }
          },
          true
        );

        scope.remove = function (selection, uuid) {
          controller.remove(selection, scope.user, uuid);
        };

        scope.doAction = function (sel) {
          var actionFn = scope.actions[sel.name].fn;
          if (angular.isFunction(actionFn)) {
            actionFn(sel.records, scope.selections.records);
          }
          // Local selection with no storage
          // trigger a clear selection once done.
          if (sel.storage === null) {
            var nbRecords = sel.records.length;
            for (var i = 0; i < nbRecords; i++) {
              controller.remove(sel, scope.user, sel.records[0]);
            }
          }
        };
      }

      return {
        restrict: "A",
        require: "^gnSavedSelections",
        templateUrl:
          "../../catalog/components/" + "common/savedselections/partials/" + "panel.html",
        link: link,
        scope: {
          user: "=gnSavedSelectionsPanel"
        }
      };
    }
  ]);

  /**
   * Button to add or remove item from user saved selection.
   */
  module.directive("gnSavedSelectionsAction", [
    "gnSavedSelectionConfig",
    "$rootScope",
    "gnGlobalSettings",
    function (gnSavedSelectionConfig, $rootScope, gnGlobalSettings) {
      function link(scope, element, attrs, controller) {
        scope.selectionsWithRecord = [];
        scope.selections = {};
        scope.uuid = scope.record.uuid;
        scope.isSavedSelectionEnabled =
          gnGlobalSettings.gnCfg.mods.search.savedSelection.enabled;

        if (!scope.isSavedSelectionEnabled) {
          return;
        }
        $rootScope.$on("savedSelectionsUpdate", function (e, n, o) {
          scope.selections = n;
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
        });

        controller.getSelections(scope.user).then(function (selections) {
          scope.selections = selections;
        });

        scope.add = function (selection) {
          controller.add(selection, scope.user, scope.uuid);
        };

        scope.remove = function (selection) {
          controller.remove(selection, scope.user, scope.uuid);
        };

        function check(selection, canBeAdded) {
          // Authenticated user can't use local anymous selections
          if (
            scope.user &&
            scope.user.id !== undefined &&
            selection.isAnonymousOnly === true
          ) {
            return false;
          }

          // Check if selection has an advanced filter
          var selConfig = gnSavedSelectionConfig.actions[selection.name];
          var isValidRecord = false;
          if (selConfig && selConfig.filterFn && angular.isFunction(selConfig.filterFn)) {
            isValidRecord = selConfig.filterFn(scope.record);
          } else {
            isValidRecord = true;
          }

          if (angular.isArray(selection.records) && isValidRecord && canBeAdded) {
            // Check if record already in current selection
            return selection.records.indexOf(scope.uuid) === -1;
          } else if (
            angular.isArray(selection.records) &&
            isValidRecord &&
            canBeAdded === false
          ) {
            // Check if record not already in current selection
            return selection.records.indexOf(scope.uuid) !== -1;
          } else {
            return false;
          }
        }

        function checkStatus(selection, addedOrRemoved) {
          if (selection) {
            return check(selection, addedOrRemoved);
          } else {
            var result = false;
            if (scope.selections.list === undefined) {
              return false;
            }
            for (var i = 0; i < scope.selections.list.length; i++) {
              if (check(scope.selections.list[i], addedOrRemoved)) {
                result = true;
              }
            }
            return result;
          }
        }

        scope.canBeAdded = function (selection) {
          return checkStatus(selection, true);
        };
        scope.canBeRemoved = function (selection) {
          return checkStatus(selection, false);
        };
      }
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/common/" + "savedselections/partials/action.html",
        require: "^gnSavedSelections",
        link: link,
        scope: {
          selection: "@gnSavedSelectionsAction",
          record: "=",
          user: "=",
          lang: "="
        }
      };
    }
  ]);
})();
