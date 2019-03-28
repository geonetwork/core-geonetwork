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
  goog.provide('gn_usersearches_directive');

  var module = angular.module('gn_usersearches_directive', []);


  /**
   * Directive to display featured user searches in the home page.
   *
   */
  module.directive('gnFeaturedUserSearchesHome', [
    'gnUserSearchesService', 'gnConfigService', 'gnConfig', 'gnLangs',
    '$http', '$translate', '$location',
    function(gnUserSearchesService, gnConfigService, gnConfig, gnLangs,
             $http, $translate, $location) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl:
        '../../catalog/components/usersearches/partials/featuredusersearcheshome.html',
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;

          gnUserSearchesService.loadFeaturedUserSearches().then(
            function(featuredSearchesCollection) {
              scope.featuredSearches = featuredSearchesCollection.data;
            }, function() {
              // TODO: Log error
            }
          );

          scope.search = function(url) {
            $location.path('/search').search(url);
          };
        }
      };
    }]);


  /**
   * Directive to display the user searches panel in the search page.
   *
   */
  module.directive('gnUserSearchesPanel', [
    'gnUserSearchesService', 'gnConfigService', 'gnConfig',
    'gnUtilityService', 'gnAlertService', 'gnLangs',
    '$http', '$translate', '$location',
    function(gnUserSearchesService, gnConfigService, gnConfig,
             gnUtilityService, gnAlertService, gnLangs,
             $http, $translate, $location) {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          user: '=gnUserSearchesPanel'
        },
        templateUrl:
          '../../catalog/components/usersearches/partials/usersearchespanel.html',
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;
          scope.isUserSearchesEnabled = gnConfig['system.usersearches.enabled'];

          scope.userSearches = null;
          scope.currentSearch = null;

          scope.$watch('user', function(n, o) {
            if (n !== o || scope.userSearches === null) {
              scope.userSearches = null;

              scope.loadUserSearches();
            }
          });

          scope.loadUserSearches = function() {
            gnUserSearchesService.loadUserSearches().then(
              function(featuredSearchesCollection) {
                scope.userSearches = featuredSearchesCollection.data;
              }, function() {
                // TODO: Log error
              }
            );
          };

          scope.isUserSearchPanelEnabled = function() {
            return scope.isUserSearchesEnabled &&
              (scope.user) && (scope.user.id !== undefined);
          };

          scope.canManageUserSearches = function() {
            return (scope.user) && (scope.user.isAdministratorOrMore());
          };

          scope.search = function(url) {
            $location.path('/search').search(url);
          };

          scope.editUserSearch = function(search) {
            console.log("editUserSearch: " + search);

            scope.openSaveUserSearchPanel(search);
          };

          scope.removeUserSearch = function(search) {
            return gnUserSearchesService.removeUserSearch(search).then(
              function() {
                gnAlertService.addAlert({
                  msg: $translate.instant('userSearchremoved'),
                  type: 'success'
                });

                scope.loadUserSearches();
              }, function(reason) {
                gnAlertService.addAlert({
                  msg: reason.data,
                  type: 'danger'
                });
              });
          };

          scope.openSaveUserSearchPanel = function(search) {
            scope.currentSearch = search;

            gnUtilityService.openModal({
              title: 'savesearch',
              content: '<div gn-save-user-search="currentSearch" data-user="user"></div>',
              className: 'gn-savesearch-popup',
              onCloseCallback: function() {
                scope.loadUserSearches()
              }
            }, scope, 'UserSearchUpdated');
          };


          scope.openAdminUserSearchPanel = function() {

            gnUtilityService.openModal({
              title: 'savesearch',
              content: '<div gn-user-search-manager=""></div>',
              className: 'gn-searchmanager-popup',
              onCloseCallback: function() {
                scope.loadUserSearches()
              }
            }, scope, 'UserSearchUpdated');
          };


        }
      };
    }]);


  /**
   * Directive for the user search create/update panel.
   *
   */
  module.directive('gnSaveUserSearch', [
    'gnUserSearchesService', 'gnConfigService', 'gnConfig', 'gnLangs', 'gnGlobalSettings',
    '$http', '$translate', '$location', '$httpParamSerializer',
    function(gnUserSearchesService, gnConfigService, gnConfig, gnLangs, gnGlobalSettings,
             $http, $translate, $location, $httpParamSerializer) {
      return {
        restrict: 'A',
        replace: true,
        scope: {
          userSearch: '=gnSaveUserSearch',
          user: '='
        },
        templateUrl:
          '../../catalog/components/usersearches/partials/saveusersearch.html',
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;
          scope.updateSearchUrl = false;

          scope.availableLangs = gnGlobalSettings.gnCfg.mods.header.languages;
          scope.langList = angular.copy(scope.availableLangs);
          angular.forEach(scope.langList, function(lang2, lang3) {
            scope.langList[lang3] = '#' + lang2;
          });
          scope.currentLangShown = scope.lang;

          var retrieveSearchParameters = function() {
            var searchParams = angular.copy($location.search());

            delete searchParams.from;
            delete searchParams.to;
            delete searchParams.fast;
            delete searchParams._content_type;

            return $httpParamSerializer(searchParams);;
          };


          if (!scope.userSearch) {
            scope.userSearch = {
              url: retrieveSearchParameters(),
              id: 0,
              creatorId: scope.user.id,
              names: {}
            };

            scope.editMode = false;
          } else {
            scope.editMode = true;
          }


          scope.isAdministratorUser = function() {
            return (scope.user) && (scope.user.isAdministratorOrMore());
          };


          scope.updateUrl = function() {
            scope.userSearch.url = retrieveSearchParameters();
          };


          scope.saveUserSearch = function() {
            var userSearch = angular.copy(scope.userSearch);
            delete userSearch.title;

            return gnUserSearchesService.saveUserSearch(userSearch).then(
              function(response) {
                scope.$emit('UserSearchUpdated', true);
                scope.$emit('StatusUpdated', {
                  msg: 'UserSearchUpdated',
                  timeout: 0,
                  type: 'success'});

              }, function(response) {
                scope.$emit('StatusUpdated', {
                  title: 'UserSearchUpdatedError',
                  error: response.data,
                  timeout: 0,
                  type: 'danger'});
              });
          };

        }
      };
    }]);


  /**
   * Directive for the user searches manager.
   *
   */
  module.directive('gnUserSearchManager', [
    'gnUserSearchesService', 'gnConfigService', 'gnConfig',
    'gnUtilityService', 'gnAlertService', 'gnLangs',
    '$http', '$translate',
    function(gnUserSearchesService, gnConfigService, gnConfig,
             gnUtilityService, gnAlertService, gnLangs,
             $http, $translate) {
      return {
        restrict: 'A',
        replace: true,
        scope: {

        },
        templateUrl:
          '../../catalog/components/usersearches/partials/usersearchesmanager.html',
        link: function postLink(scope, element, attrs) {
          scope.lang = gnLangs.current;

          scope.loadAllUserSearches = function() {
            gnUserSearchesService.loadAllUserSearches().then(
              function(featuredSearchesCollection) {
                scope.userSearches = featuredSearchesCollection.data;

                scope.userSearches = _.sortBy(scope.userSearches, 'creator');
              }, function() {
                // TODO: Log error
              }
            );
          };

          scope.removeUserSearch = function(search) {
            return gnUserSearchesService.removeUserSearch(search).then(
              function() {
                gnAlertService.addAlert({
                  msg: $translate.instant('userSearchremoved'),
                  type: 'success'
                });

                scope.loadAllUserSearches();
              }, function(reason) {
                gnAlertService.addAlert({
                  msg: reason.data,
                  type: 'danger'
                });
              });
          };

          scope.editUserSearch = function(search) {
            scope.currentSearch = search;

            gnUtilityService.openModal({
              title: 'savesearch',
              content: '<div gn-save-user-search="currentSearch" data-user="user"></div>',
              className: 'gn-savesearch-popup',
              onCloseCallback: function() {
                scope.loadAllUserSearches()
              }
            }, scope, 'UserSearchUpdated');
          };


          scope.loadAllUserSearches();
        }
      };
    }]);
})();
