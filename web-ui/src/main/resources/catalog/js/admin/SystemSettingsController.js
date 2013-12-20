(function() {
  goog.provide('gn_system_settings_controller');


  var module = angular.module('gn_system_settings_controller',
      []);


  /**
   * GnSystemSettingsController provides management interface
   * for catalog settings.
   *
   * TODO:
   *  * Add custom forms for some settings (eg. contact for CSW,
   *  Metadata views > default views, Search only in requested language)
   */
  module.controller('GnSystemSettingsController', [
    '$scope', '$http', '$rootScope', '$translate', 'gnUtilityService',
    function($scope, $http, $rootScope, $translate, gnUtilityService) {

      $scope.settings = [];
      var sectionsLevel1 = [];
      var sectionsLevel2 = [];
      $scope.sectionsLevel1 = [];
      $scope.sectionsLevel2 = [];
      $scope.systemUsers = null;

      /**
         * Load catalog settings as a flat list and
         * extract firs and second level sections.
         *
         * Form field name is also based on settings
         * key replacing "/" by "." (to not create invalid
         * element name in XML Jeeves request element).
         */
      function loadSettings() {
        $http.get('xml.config.get@json?asTree=false').success(function(data) {

          gnUtilityService.parseBoolean(data);
          $scope.settings = data;

          for (var i = 0; i < $scope.settings.length; i++) {
            var tokens = $scope.settings[i]['@name'].split('/');
            $scope.settings[i].formName =
                $scope.settings[i]['@name'].replace(/\//g, '.');
            // Extract level 1 and 2 sections
            if (tokens) {
              if (sectionsLevel1.indexOf(tokens[0]) === -1) {
                sectionsLevel1.push(tokens[0]);
                $scope.sectionsLevel1.push({
                  'name': tokens[0],
                  '@position': $scope.settings[i]['@position']});
              }
              var level2name = tokens[0] + '/' + tokens[1];
              if (sectionsLevel2.indexOf(level2name) === -1) {
                sectionsLevel2.push(level2name);
                $scope.sectionsLevel2.push({
                  'name': level2name,
                  '@position': $scope.settings[i]['@position']});
              }
            }
          }
        }).error(function(data) {
          // TODO
        });
      }

      function loadUsers() {
        $http.get('admin.user.list@json').success(function(data) {
          $scope.systemUsers = data;
        });
      }
      /**
         * Filter all settings for a section
         */
      $scope.filterBySection = function(section) {
        var settings = [];

        for (var i = 0; i < $scope.settings.length; i++) {
          var s = $scope.settings[i];
          if (s['@name'].indexOf(section) !== -1) {
            settings.push(s);
          }
        }
        return settings;
      };

      /**
         * Order by position
         */
      $scope.getOrderBy = function(s) {
        return s['@position'];
      };

      /**
         * Save the form containing all settings. When saved,
         * broadcast success status and reload catalog info.
         */
      $scope.saveSettings = function(formId) {

        $http.get($scope.url + 'admin.config.save?' +
                gnUtilityService.serialize(formId))
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('settingsUpdated'),
                timeout: 2,
                type: 'success'});

              $scope.loadCatalogInfo();
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
         * Scroll to an element.
         */
      $scope.scrollTo = gnUtilityService.scrollTo;

      loadUsers();
      loadSettings();
    }]);

})();
