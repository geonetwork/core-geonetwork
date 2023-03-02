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

  module.directive("gnFavouritesListPanel", [
    "gnFavouritesListService",
    "$location",
    "gnUtilityService",
    function (gnFavouritesListService, $location, gnUtilityService) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          user: "=gnUserSearchesPanel"
        },

        templateUrl:
          "../../catalog/components/favouriteslist/partials/favouritespanel.html",
        link: function (scope, element, attrs) {
          scope.isFavouritesPanelEnabled = true; // gnGlobalSettings.gnCfg.mods.search.usersearches.enabled;

          scope.favouriteLists = [];

          scope.loadFavouritesLists = function () {
            gnFavouritesListService.loadFavourites().then(
              function (favouriteLists) {
                var items = favouriteLists.data;
                items = _.sortBy(items, function (item) {
                  return item.name;
                });
                scope.favouriteLists = items;
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
                favouritesList.public = result.data.isPublic;
              });
          };

          scope.updateFavouritesList = function () {
            scope.favouriteLists = [];
            scope.loadFavouritesLists();
            scope.$emit("search", {});
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
              "updated"
            );
          };

          scope.delete = function (favouritesList) {
            gnFavouritesListService
              .deleteFavouritesList(favouritesList.id)
              .then(function (result) {
                ///
                scope.favouriteLists = scope.favouriteLists.filter(function (item) {
                  return item.id !== favouritesList.id;
                });
              });
          };

          scope.searchUrl = function (favouritesList) {
            var id = favouritesList.id;
            var params = { favouritesList: id, from: 1, to: 30 };
            $location.search(params);
          };

          //----
          scope.loadFavouritesLists();
        }
      };
    }
  ]);

  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------

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
          scope.page = { pageSize: 4, pages: 0, page: 0, from: 0, to: 0, count: 0 };
          //scope.pageSize = 4;
          // scope.pageNumber = 0;
          scope.updateSearchUrl = false;
          scope.name = scope.favouritesList.name;
          scope.hits = [];
          scope.toDelete = [];
          scope.totalHits = 0;

          scope.reloadPage = function () {
            scope.loadFavouritesPaged(
              scope.userSearch.id,
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
              .updateFavourites(scope.favouritesList.id, scope.name, scope.toDelete)
              .then(function (response) {
                scope.$emit("updated", true);
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
})();
