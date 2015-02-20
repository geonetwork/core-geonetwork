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
    '$scope', '$http', '$rootScope', '$translate', 'gnUtilityService',
    function($scope, $http, $rootScope, $translate, gnUtilityService) {
      var cswSettings = ['system/csw/contactId'];
      var cswBooleanSettings = ['system/csw/enable',
        'system/csw/metadataPublic',
        'system/csw/transactionUpdateCreateXPath'];

      /**
       * CSW properties
       */
      $scope.cswSettings = {};

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
        $http.get('admin.config.list?_content_type=json&asTree=false')
          .success(function(data) {
              for (var i = 0; i < data.length; i++) {
                var setting = data[i];
                if (cswBooleanSettings.indexOf(setting['@name']) !== -1) {
                  var value = setting['#text'].toLowerCase();
                  $scope.cswSettings[setting['@name']] =
                      (value == 'true' || value == 'on');
                } else if (cswSettings.indexOf(setting['@name']) !== -1) {
                  $scope.cswSettings[setting['@name']] = setting['#text'];
                }
              }
            }).error(function(data) {
              // TODO
            });
      }

      function loadUsers() {
        $http.get('admin.user.list?_content_type=json&').
            success(function(data) {
              $scope.users = data;
              loadSettings();
            }).error(function(data) {
              // TODO
            });
      }


      function loadCSWConfig() {
        $http.get('admin.config.csw?_content_type=json&').
            success(function(data) {
              $scope.cswConfig = data;
              angular.forEach($scope.cswConfig.capabilitiesInfoFields,
                  function(value, key) {
                    $scope.cswLanguages[$scope.cswConfig.
                      capabilitiesInfoFields[key].langId] = true;
                    $scope.cswFields[$scope.cswConfig.
                      capabilitiesInfoFields[key].fieldName] = true;
                  });
              loadSettings();
            }).error(function(data) {
              // TODO
            });
      }

      function loadCSWElementSetName() {
        $http.get('admin.config.csw.customelementset?_content_type=json&')
        .success(function(data) {
              if (data) {
                $scope.cswElementSetName =
                    $.isArray(data.xpath) ? data.xpath : [data.xpath];
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
        $http.get('admin.config.csw.customelementset.save?_content_type=json&' +
                $(formId).serialize())
          .success(function(data) {
              loadCSWElementSetName();
            });
      };

      /**
       * Save the form containing all settings. When saved,
       * broadcast success or failure status.
       */
      $scope.saveSettings = function(formId) {
        saveSettings(formId, 'admin.config.save');
      };
      /**
       * Save the form containing all capabilities properties. When saved,
       * broadcast success or failure status.
       */
      $scope.saveProperties = function(formId) {
        saveSettings(formId, 'admin.config.csw.save');
      };
      var saveSettings = function(formId, service) {

        $http.get(service + '?' +
                gnUtilityService.serialize(formId))
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('settingsUpdated'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('settingsUpdateError'),
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

      loadUsers();  // Which then load settings
      loadCSWConfig();
      loadCSWElementSetName();

    }]);

})();
