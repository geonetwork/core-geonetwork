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
  goog.provide('gn_csw_settings_controller');


  var module = angular.module('gn_csw_settings_controller',
      []);

  /**
   * GnCSWSettingsController provides management interface
   * for CSW settings.
   *
   */
  module.controller('GnCSWSettingsController', [
    '$scope', '$http', '$rootScope', '$translate',
    'gnUtilityService', 'gnESClient', 'Metadata',
    function($scope, $http, $rootScope, $translate, gnUtilityService,
             gnESClient, Metadata) {
      /**
       * CSW properties
       */
      $scope.cswSettings = {};
      $scope.cswServiceRecord = null;

      $scope.serviceRecordSearchObj = {
        internal: true,
        any: '',
        defaultParams: {
          any: '',
          from: 1,
          to: 50,
          type: 'service',
          sortBy: 'resourceTitleObject.default.keyword',
          sortOrder: 'asc'
        }
      };

      /**
       * CSW element set name (an array of xpath).
       */
      $scope.cswElementSetName = [];

      /**
       * The filter for field column
       */
      $scope.cswFieldFilterValue = '';

      /**
       * The filter value for language
       */
      $scope.cswLanguageFilterValue = $scope.lang;

      /**
       * List of languages populated when settings are loaded.
       */

      $scope.cswLanguages = {};
      /**
       * List of field populated when settings are loaded.
       */
      $scope.cswFields = {};


      /**
         * Load catalog settings and extract CSW settings
         */
      function loadSettings() {
        $http.get('../api/site/settings?set=CSW')
            .success(function(data) {
              $scope.cswSettings = data;
              loadServiceRecords();
            }).error(function(data) {
              // TODO
            });
      }

      function loadServiceRecords() {
        var id = $scope.cswSettings['system/csw/capabilityRecordUuid'];
        if (angular.isDefined(id) && id != -1){
          var query =
            {"query": {
                "term": {
                  "uuid": {
                    "value": id
                  }
                }
              }, "from": 0, "size": 1};

          gnESClient.search(query).then(function(data) {
            angular.forEach(data.hits.hits, function(record) {
              var md = new Metadata(record);
              $scope.cswServiceRecord = md;
            });
          });
        }
      }
      $scope.$watchCollection('cswSettings', function(n, o){
        if (n != o) {
          loadServiceRecords();
        }
      });


      function loadCSWElementSetName() {
        $http.get('admin.config.csw.customelementset?_content_type=json&')
            .success(function(data) {
              if (data) {
                $scope.cswElementSetName =
                    $.isArray(data.xpaths) ? data.xpaths : [data.xpaths];
              } else {
                $scope.cswElementSetName = [];
              }
            });
      }
      $scope.addCSWElementSetName = function() {
        $scope.cswElementSetName.push(['']);
      };
      $scope.deleteElementSetName = function(e) {
        var index = $.inArray(e, $scope.cswElementSetName);
        $scope.cswElementSetName.splice(index, 1);
      };
      $scope.saveCSWElementSetName = function(formId) {
        $http({
          method: 'POST',
          url: 'admin.config.csw.customelementset.save',
          data: '_content_type=json&' + $(formId).serialize(),
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        })
            .success(function(data) {
              loadCSWElementSetName();
            });
      };

      /**
       * Save the form containing all settings. When saved,
       * broadcast success or failure status.
       */
      $scope.saveSettings = function(formId) {
        $http.post('../api/site/settings',
          gnUtilityService.serialize(formId), {
            headers: {'Content-Type':
                'application/x-www-form-urlencoded'}
          })
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('settingsUpdated'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('settingsUpdateError'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
      };

      /**
       * Filter CSW settings by language and/or field
       */
      $scope.cswFilter = function(items) {
        var result = [];
        var field = $scope.cswFieldFilterValue;
        var lang = $scope.cswLanguageFilterValue;

        if (items) {
          angular.forEach(items.capabilitiesInfoFields, function(value, key) {
            var selected = false;
            // Filter only by lang
            if (lang !== '' &&
                field === '' &&
                items.capabilitiesInfoFields[key].langId === lang) {
              selected = true;
            }
            //Filter only by field
            if (field !== '' &&
                lang === '' &&
                items.capabilitiesInfoFields[key].fieldName === field) {
              selected = true;
            }
            // Filter by both
            if (field !== '' &&
                lang !== '' &&
                items.capabilitiesInfoFields[key].langId === lang &&
                items.capabilitiesInfoFields[key].fieldName === field) {
              selected = true;
            }
            // All
            if (field === '' &&
                lang === '') {
              selected = true;
            }
            if (selected) {
              result.push(items.capabilitiesInfoFields[key]);
            }
          });
        }
        return result;
      };

      loadSettings();
      loadCSWElementSetName();

    }]);

})();
