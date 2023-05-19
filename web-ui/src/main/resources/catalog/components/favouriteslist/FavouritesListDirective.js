/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

/*
 *  This provides 2 directives;
 *
 *    a) gnFavouritesListPanel: This is the main panel on the search screen.
 *                              It shows the list of available favourites lists and has tools for them.
 *    b) gnEditFavouritesList:  This is the edit button's pop-up window.
 *                              It shows a paged list of metadata items (in the favourites list).
 */
(function () {
  goog.provide("gn_favouriteslist_directive");
  var module = angular.module("gn_favouriteslist_directive", []);

  /**
   * @ngdoc directive
   * @name gn_favourite_selections.directive:gnFavouriteSelections
   * @restrict A
   * @requires gnFavouritesListService
   * @requires $translate
   *
   * @description
   *
   */

  module.directive("gnFavouritesListPanel", [
    "gnFavouritesListService",
    "$location",
    "$rootScope",
    "gnUtilityService",
    "gnGlobalSettings",
    function (
      gnFavouritesListService,
      $location,
      $rootScope,
      gnUtilityService,
      gnGlobalSettings
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          user: "=gnFavouritesListPanel"
        },
        require: "^ngSearchForm",

        templateUrl:
          "../../catalog/components/favouriteslist/partials/favouritespanel.html",
        link: function (scope, element, attrs, ngSearchForm) {
          scope.ngSearchForm = ngSearchForm;
          scope.isFavouritesPanelEnabled =
            gnFavouritesListService.isFavouritesPanelEnabled;

          scope.favouriteLists = null;

          $rootScope.$on("favouriteSelectionsUpdate", function (e, n, o) {
            if (n != o) {
              scope.favouriteLists = n;
            }
          });

          scope.$watch(
            "user",
            function (n, o) {
              if (n !== o || scope.favouriteLists === null) {
                scope.favouriteLists = null;
                gnFavouritesListService.loadFavourites().then(function (result) {
                  var items = result.data;
                  items = _.sortBy(items, function (item) {
                    return item.name;
                  });
                  scope.favouriteLists = items;
                  $rootScope.$emit("refreshFavouriteLists", scope.favouriteLists);
                });
              }
            },
            true
          );

          scope.loadFavouritesLists = function () {
            gnFavouritesListService.loadFavourites().then(
              function (favouriteLists) {
                var items = favouriteLists.data;
                items = _.sortBy(items, function (item) {
                  return item.name;
                });
                scope.favouriteLists = items;

                $rootScope.$emit("refreshFavouriteLists", scope.favouriteLists);
              },
              function () {
                // TODO: Log error
              }
            );
          };

          scope.toggleStatus = function (favouritesList) {
            gnFavouritesListService
              .setFavouritesListStatus(favouritesList.id, !favouritesList.public)
              .then(function (result) {
                favouritesList.public = result.data.public;
              });
          };

          scope.updateFavouritesList = function () {
            scope.selections = [];
            scope.loadFavouritesLists();
          };

          scope.edit = function (favouritesList) {
            scope.currentFavouritesList = favouritesList;

            gnUtilityService.openModal(
              {
                title: "userSelectionFavourites",
                content: '<div gn-edit-favourites-list="currentFavouritesList"  ></div>',
                className: "gn-savesearch-popup",
                onCloseCallback: function () {
                  scope.updateFavouritesList();
                }
              },
              scope,
              "userSelectionFavourites.complete"
            );
          };

          scope.delete = function (favouritesList) {
            gnFavouritesListService
              .deleteFavouritesList(favouritesList.id)
              .then(function (result) {
                scope.selections = scope.favouriteLists.filter(function (item) {
                  return item.id !== favouritesList.id;
                });

                scope.updateFavouritesList();
              });
          };

          scope.searchUrl = function (favouritesList) {
            var id = favouritesList.id;
            // we need to add the resourceTemporalDateRange or the search update doen't work (it will not remove the 'favouritesList=id' from the URL)
            var params = {
              favouritesList: id,
              from: 1,
              to: 30,
              sortBy: "relevance",
              isTemplate: "n",
              resourceTemporalDateRange: {
                range: {
                  resourceTemporalDateRange: {
                    gte: null,
                    lte: null,
                    relation: "intersects"
                  }
                }
              }
            };
            scope.ngSearchForm.resetSearch(params);
          };

          scope.createNewList = function ($event) {
            gnUtilityService.openModal(
              {
                title: "userSelectionFavourites",
                content:
                  '<div gn-create-favourites-list="currentFavouritesList"  ></div>',
                className: "gn-savesearch-popup",
                onCloseCallback: function () {
                  scope.updateFavouritesList();
                }
              },
              scope,
              "updatedUserSelectionFavourites"
            );

            $event.stopPropagation();
          };
        }
      };
    }
  ]);

  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------

  module.directive("gnChooseFavouritesList", [
    "gnFavouritesListService",
    "gnConfigService",
    "gnConfig",
    "gnGlobalSettings",
    function (gnFavouritesListService, gnConfigService, gnConfig, gnGlobalSettings) {
      return {
        restrict: "A",
        replace: true,
        scope: {},
        templateUrl:
          "../../catalog/components/favouriteslist/partials/addtofavourites.html",
        link: function postLink(scope, element, attrs) {
          scope.favouritesLists = gnFavouritesListService.getCachedLists();
          scope.selectedListId = -1;

          scope.select = function (list) {
            scope.selectedListId = list.id;
          };

          scope.finish = function () {
            scope.$emit("Favourites.bulkAdd", scope.selectedListId);
          };
        }
      };
    }
  ]);

  module.directive("gnEditFavouritesList", [
    "gnFavouritesListService",
    "gnConfigService",
    "gnConfig",
    "gnGlobalSettings",

    function (gnFavouritesListService, gnConfigService, gnConfig, gnGlobalSettings) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          favouritesList: "=gnEditFavouritesList"
        },
        templateUrl:
          "../../catalog/components/favouriteslist/partials/savefavourite.html",
        link: function postLink(scope, element, attrs) {
          scope.page = {
            pageSize: 4,
            pages: 0,
            page: 0,
            from: 0,
            to: 0,
            count: 0
          };
          //scope.pageSize = 4;
          // scope.pageNumber = 0;
          scope.updateSearchUrl = false;
          scope.name = scope.favouritesList.name;
          scope.hits = [];
          scope.toDelete = [];
          scope.totalHits = 0;

          scope.reloadPage = function () {
            scope.loadFavouritesPaged(
              scope.favouritesList.id,
              scope.page.page,
              scope.page.pageSize
            );
          };

          scope.pageFirst = function () {
            scope.page.page = 0;
            scope.reloadPage();
          };

          scope.pageLast = function () {
            scope.page.page = scope.page.pages - 1;

            scope.reloadPage();
          };

          scope.pageNext = function () {
            scope.page.page++;
            if (scope.page.page >= scope.page.pages) {
              scope.page.page = scope.page.pages - 1;
            }
            scope.reloadPage();
          };

          scope.pagePrevious = function () {
            scope.page.page--;
            if (scope.page.page < 0) {
              scope.page.page = 0;
            }
            scope.reloadPage();
          };

          scope.delete = function (hit) {
            if (!scope.isDeleted(hit)) {
              scope.toDelete.push(hit._id);
            } else {
              scope.toDelete = scope.toDelete.filter(function (item) {
                return item !== hit._id;
              });
            }
          };

          scope.isDeleted = function (hit) {
            var uuid = hit._id;
            var found = scope.toDelete.find(function (val) {
              return val == uuid;
            });
            return typeof found !== "undefined";
          };

          scope.getTitle = function (hit) {
            var obj = hit._source.resourceTitleObject;
            if (obj["default"]) {
              return obj["default"];
            }
            return Object.values(obj)[0];
          };

          scope.getAbstract = function (hit) {
            var obj = hit._source.resourceAbstractObject;
            var result = "No Title";
            if (obj["default"]) {
              result = obj["default"];
            } else {
              result = Object.values(obj)[0];
            }
            if (typeof result == "undefined") {
              return "No Title";
            }
            if (result.length > 255) {
              return result.substr(0, 255) + "...";
            }
            return result;
          };

          scope.loadFavouritesPaged = function (search, pageNumber, pageSize) {
            gnFavouritesListService
              .loadFavouritesListItemsPaged(search, pageNumber, pageSize)
              .then(function (response) {
                scope.hits = response.data.hits.hits;
                scope.page.count = response.data.hits.total.value;
                //scope.page.page = 0;
                scope.page.pages = Math.ceil(scope.page.count / scope.page.pageSize);
                scope.page.from = scope.page.pageSize * scope.page.page + 1;
                scope.page.to =
                  scope.page.pageSize * scope.page.page + scope.page.pageSize;
                if (scope.page.to > scope.page.count) {
                  scope.page.to = scope.page.count;
                }
              });
          };

          scope.save = function () {
            return gnFavouritesListService
              .removeFromFavourites(scope.favouritesList.id, scope.name, scope.toDelete)
              .then(function (response) {
                scope.$emit("userSelectionFavourites.complete", true);
              });
          };

          //------------------------------------------

          scope.loadFavouritesPaged(
            scope.favouritesList.id,
            scope.page.page,
            scope.page.pageSize
          );
        }
      };
    }
  ]);

  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------

  module.directive("gnCreateFavouritesList", [
    "gnFavouritesListService",
    "gnConfigService",
    "gnConfig",
    "gnGlobalSettings",
    "$translate",

    function (
      gnFavouritesListService,
      gnConfigService,
      gnConfig,
      gnGlobalSettings,
      $translate
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: {},
        templateUrl:
          "../../catalog/components/favouriteslist/partials/createfavourite.html",
        link: function postLink(scope, element, attrs) {
          scope.updateSearchUrl = false;

          scope.save = function () {
            return gnFavouritesListService.createFavourites(scope.name).then(
              function (response) {
                scope.$emit("updatedUserSelectionFavourites", response.data);
              },
              function (_error) {
                if (_error.data && _error.data.description) {
                  alert(
                    $translate.instant("favouriteslistAddError") +
                      ": " +
                      _error.data.description
                  );
                } else {
                  alert($translate.instant("favouriteslistAddError"));
                }
              }
            );
          };
        }
      };
    }
  ]);

  /**
   * Button to add or remove item from user saved selection.
   */
  module.directive("gnFavouritesSelectionsAction", [
    "gnFavouritesListService",
    "$rootScope",
    "gnGlobalSettings",
    function (gnFavouritesListService, $rootScope, gnGlobalSettings) {
      return {
        restrict: "A",
        templateUrl: "../../catalog/components/favouriteslist/partials/action.html",
        //  require: "^gnFavouriteSelections",
        scope: {
          record: "=",
          user: "=",
          lang: "="
        },
        link: function (scope, element, attrs) {
          scope.isFavouritesPanelEnabled =
            gnFavouritesListService.isFavouritesPanelEnabled;

          scope.favouriteLists = [];

          scope.uuid = scope.record.uuid;

          $rootScope.$on("refreshFavouriteLists", function (e, n, o) {
            if (n != o) {
              scope.favouriteLists = n;
            }
          });

          $rootScope.$on("favouriteSelectionsUpdate", function (e, n, o) {
            if (n != o) {
              scope.favouriteLists = n;
            }
          });

          scope.favouriteLists = gnFavouritesListService.getCachedLists();

          scope.areEditableLists = function () {
            if (this.favouriteLists == null || this.favouriteLists.length === 0)
              return false;
            //make sure at least one list is editable
            for (var i = 0; i < scope.favouriteLists.length; i++) {
              var item = scope.favouriteLists[i];
              if (item.editable) return true;
            }
            return false;
          };

          function check(favouriteList, canBeAdded) {
            if (!favouriteList.editable) {
              return false;
            }
            if (canBeAdded) {
              return favouriteList.selections.indexOf(scope.record.uuid) === -1;
            }
            return favouriteList.selections.indexOf(scope.record.uuid) !== -1;
          }

          function checkStatus(favouriteList, addedOrRemoved) {
            if (favouriteList) {
              return check(favouriteList, addedOrRemoved);
            } else {
              var result = false;

              if (scope.favouriteLists.length === 0) {
                return false;
              }

              for (var i = 0; i < scope.favouriteLists.length; i++) {
                if (check(scope.favouriteLists[i], addedOrRemoved)) {
                  result = true;
                  break;
                }
              }
              return result;
            }
          }

          scope.add = function (favouriteList) {
            return gnFavouritesListService.addToList(favouriteList, scope.uuid);
          };

          scope.remove = function (favouriteList) {
            return gnFavouritesListService.removeFromFavourites(
              favouriteList.id,
              favouriteList.name,
              [scope.uuid]
            );
          };

          scope.canBeAdded = function (favouriteList) {
            return checkStatus(favouriteList, true);
          };
          scope.canBeRemoved = function (favouriteList) {
            return checkStatus(favouriteList, false);
          };
        }
      };
    }
  ]);
})();
