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
  goog.provide('gn_system_settings_controller');

  goog.require('gn_ui_config');


  var module = angular.module('gn_system_settings_controller',
      ['gn_ui_config']);

  module.filter('hideLanguages', function() {
    return function(input) {
      var filtered = [];
      angular.forEach(input, function(el) {
        if (el.name.indexOf('system/site/labels/') === -1) {
          filtered.push(el);
        }
      });
      return filtered;
    }
  });

  /**
   * Filters internal settings used by GeoNetwork,
   * not intended to be configured by the user.
   */
  module.filter('hideGeoNetworkInternalSettings', function() {
    return function(input) {
      var filtered = [];
      var internal = ['system/userFeedback/lastNotificationDate'];
      angular.forEach(input, function(el) {
        if (internal.indexOf(el.name) === -1) {
          filtered.push(el);
        }
      });
      return filtered;
    }
  });

  module.filter('orderObjectBy', function() {
    return function(input, attribute) {
      if (!angular.isObject(input)) return input;

      var array = [];
      for (var objectKey in input) {
        array.push(input[objectKey]);
      }

      array.sort(function(a, b) {
        a = parseInt(a[attribute]);
        b = parseInt(b[attribute]);
        return a - b;
      });
      return array;
    }
  });
  /**
   * GnSystemSettingsController provides management interface
   * for catalog settings.
   *
   * TODO:
   *  * Add custom forms for some settings (eg. contact for CSW,
   *  Metadata views > default views, Search only in requested language)
   */
  module.controller('GnSystemSettingsController', [
    '$scope', '$http', '$rootScope', '$translate', '$location',
    'gnUtilityService', '$timeout',
    function($scope, $http, $rootScope, $translate, $location,
        gnUtilityService, $timeout) {

      $scope.settings = [];
      $scope.initalSettings = [];
      $scope.sectionsLevel1 = {};
      $scope.systemUsers = null;
      $scope.processTitle = '';
      $scope.orderProperty = 'position';
      $scope.reverse = false;
      $scope.systemInfo = {
        'stagingProfile': 'production'
      };
      $scope.stagingProfiles = ['production', 'development', 'testing'];
      $scope.updateProfile = function() {

        $http.put('../api/site/info/staging/' +
            $scope.systemInfo.stagingProfile)
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('profileUpdated'),
                timeout: 2,
                type: 'success'});
            }).error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('profileUpdatedFailed'),
                timeout: 2,
                type: 'danger'});
            });
      };

      $scope.loadTplReport = null;
      $scope.atomFeedType = '';

      /**
         * Load catalog settings as a flat list and
         * extract firs and second level sections.
         *
         * Form field name is also based on settings
         * key replacing "/" by "." (to not create invalid
         * element name in XML Jeeves request element).
         */
      function loadSettings() {

        $http.get('../api/site/info/build')
            .success(function(data) {
              $scope.systemInfo = data;
            });
        // load log files
        $http.get('../api/site/logging')
            .success(function(data) {
              $scope.logfiles = data;
            });
        $http.get('../api/site/settings/details')
            .success(function(data) {

              var sectionsLevel1 = [];
              var sectionsLevel2 = [];
              var uiConfigFound = false;

              // Stringify JSON for editing in text area
              angular.forEach(data, function(s) {
                if (s.name === 'ui/config') {
                  uiConfigFound = true;
                }
                if (s.dataType === 'JSON') {
                  s.value = angular.toJson(s.value);
                }
              });

              // Init empty settings for the UI configuration if none
              // eg. an old database.
              if (!uiConfigFound) {
                data.push({name: 'ui/config', value: {}});
              }

              $scope.settings = data;
              angular.copy(data, $scope.initalSettings);


              for (var i = 0; i < $scope.settings.length; i++) {
                var tokens = $scope.settings[i].name.split('/');
                // Extract level 1 and 2 sections
                if (tokens && $scope.settings[i].name != 'ui/config') {
                  var level1name = tokens[0];
                  if (sectionsLevel1.indexOf(level1name) === -1) {
                    sectionsLevel1.push(level1name);
                    $scope.sectionsLevel1[level1name] = {
                      'name': level1name,
                      'position': $scope.settings[i].position,
                      children: []
                    };
                  }
                  var level2name = level1name + '/' + tokens[1];
                  if (sectionsLevel2.indexOf(level2name) === -1) {
                    sectionsLevel2.push(level2name);
                    $scope.sectionsLevel1[level1name].children.push({
                      'name': level2name,
                      'position': $scope.settings[i].position,
                      'children': filterBySection($scope.settings, level2name)
                    });
                  }
                }

                var target = $location.search()['scrollTo'];
                if (target) {
                  $timeout(function () {
                    gnUtilityService.scrollTo(target);
                  }, 300);
                }
              }
            }).error(function(data) {
              // TODO
            });
      }

      function loadUsers() {
        $http.get('../api/users').success(function(data) {
          $scope.systemUsers = data;
        });
      }

      /**
         * Filter all settings for a section
         */
      var filterBySection = function(elements, section) {
        var settings = [];
        var regexp = new RegExp('^' + section + '/.*|^' + section + '$');
        for (var i = 0; i < elements.length; i++) {
          var s = elements[i];
          if (regexp.test(s.name)) {
            settings.push(s);
          }
        }
        return settings;
      };

      /**
         * Save the form containing all settings. When saved,
         * broadcast success status and reload catalog info.
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

              $scope.loadCatalogInfo();
            })
            .error(function(data) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('settingsUpdateError'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
      };
      $scope.processName = null;
      $scope.processRecommended = function(processName) {
        $scope.processName = processName;
        $scope.processTitle =
            $translate.instant('processRecommendedOnHostChange-help', {
              old: buildUrl($scope.initalSettings),
              by: buildUrl($scope.settings)
            });
      };
      $scope.resourceIdProcessName = null;
      $scope.processRecommendedForId = function(processName) {
        $scope.resourceIdProcessName = processName;
        $scope.processResourceTitle =
            $translate.instant('processRecommendedOnHostChange-help', {
              old: buildUrl($scope.initalSettings),
              by: buildUrl($scope.settings)
            });
      };
      $scope.filterForm = function(e,formId) {

        var filterValue = e.target.value.toLowerCase();

        $(formId + " .form-group").filter(function() {

          var filterText = $(this).find('label').text().toLowerCase();
          var matchStart = filterText.indexOf("" + filterValue.toLowerCase() + "");

          if (matchStart > -1) {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      };
      $scope.resetFilter = function(formId) {

        $(formId + " .form-group").each(function() {
          // clear filter
          $('#filter-settings').val('');
          // show the element
          $(this).show();
          // show the fieldsets
          $(formId + ' fieldset').show();
        });

      };

      $scope.testMailConfiguration = function() {
        $http.get('../api/0.1/tools/mail/test')
            .then(function(response) {
              $rootScope.$broadcast('StatusUpdated', {
                title: response.data});
            }, function(response) {
              $rootScope.$broadcast('StatusUpdated', {
                title: response.data,
                timeout: 0,
                type: 'danger'});
            });
      };
      var buildUrl = function(settings) {
        var port = filterBySection(settings, 'system/server/port')[0].value;
        var host = filterBySection(settings, 'system/server/host')[0].value;
        var protocol = filterBySection(settings,
            'system/server/protocol')[0].value;

        return protocol + '://' + host +
                (isPortRequired(procotol, port) ? ':' + port : '');
      };

      var isPortRequired = function(protocol, port) {
        if (protocol == 'http' && port == '80') {
          return false;
        } else if (protocol == 'https' && port == '443') {
          return false;
        } else {
          return true;
        }
      }

      /**
       * Save settings and move to the batch process page
       *
       * TODO: set the process to use and select all
       */
      $scope.saveAndProcessSettings = function(formId, process) {
        $scope.saveSettings(formId);

        $location.path('/tools/batch/select/all/process/' + process)
            .search(
            'urlPrefix=' + buildUrl($scope.initalSettings) +
            '&newUrlPrefix=' + buildUrl($scope.settings));
      };


      /**
       * Execute Atom feed harvester
       */
      $scope.executeAtomHarvester = function() {
        $http.get('atomharvester?_content_type=json').success(function(data) {
          $scope.loadTplReport = data;

          $('#atomHarvesterModal').modal();

        }).error(function(data) {
          $scope.loadTplReport = data;

          $('#atomHarvesterModal').modal();
        });
      };



      /**
         * Scroll to an element.
         */
      $scope.scrollTo = gnUtilityService.scrollTo;

      loadUsers();
      loadSettings();
    }]);

})();
