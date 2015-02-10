(function() {
  goog.provide('gn_harvester_directive');

  var module = angular.module('gn_harvester_directive', []);


  /**
     * Display harvester identification section with
     * name, group and icon
     */
  module.directive('gnHarvesterIdentification', ['$http', '$rootScope',
    function($http, $rootScope) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          harvester: '=gnHarvesterIdentification'
          //               lang: '@lang'
        },
        templateUrl: '../../catalog/components/admin/harvester/partials/' +
            'identification.html',
        link: function(scope, element, attrs) {
          scope.lang = 'eng'; // FIXME
          scope.openTranslationModal = function() {
            var translations = scope.harvester.site.translations;
            if (translations === undefined || angular.isArray(translations)) {
              translations = {};
              scope.harvester.site.translations = translations;
            }

            for (var i = 0; i < scope.languages.length; i++) {
              if (translations[scope.languages[i].id] === undefined) {
                translations[scope.languages[i].id] = scope.harvester.site.name;
              }
            }
            $('#translationModal').modal('show');
          };
          $http.get('admin.harvester.info?type=icons&_content_type=json',
              {cache: true})
          .success(function(data) {
                scope.icons = data[0];
              });
          // $http.get('admin.usergroups.list@json?id=' + 1)
          //          .success(function(data) {
          $http.get('info?_content_type=json&type=languages', {cache: true})
            .success(function(data) {
                scope.languages = data.language;
              });
          $http.get('admin.group.list@json', {cache: true})
            .success(function(data) {
                scope.groups = data !== 'null' ? data : null;
              });
          scope.setIcon = function(i) {
            scope.harvester.site.icon = i;
          };
        }
      };
    }]);

  /**
     * Display account when remote login is used.
     */
  module.directive('gnHarvesterAccount', [
    function() {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          harvester: '=gnHarvesterAccount'
        },
        templateUrl: '../../catalog/components/admin/harvester/partials/' +
            'account.html',
        link: function(scope, element, attrs) {

        }
      };
    }]);
  /**
     * Display harvester schedule configuration.
     */
  module.directive('gnHarvesterSchedule', ['$translate',
    function($translate) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          harvester: '=gnHarvesterSchedule'
        },
        templateUrl: '../../catalog/components/admin/harvester/partials/' +
            'schedule.html',
        link: function(scope, element, attrs) {
          scope.cronExp = ['0 0 12 * * ?',
                           '0 15 10 * * ?',
                           '0 0/5 14 * * ?',
                           '0 15 10 ? * MON-FRI',
                           '0 15 10 15 * ?'];
          scope.setSchedule = function(exp) {
            scope.harvester.options.every = exp;
          };
        }
      };
    }]);


  /**
     * Display harvester privileges configuration with
     * publish to all or by group.
     *
     * This direcive does not provide definition of
     * download and interactive privileges. TODO
     * see if this is necessary. Download maybe.
     * interactive may be better handled by using withheld
     * attribute.
     *
     * TODO: this directive could also be used
     * for metadata privileges. To be improved.
     */
  module.directive('gnHarvesterPrivileges',
      ['$http', '$translate', '$rootScope',
       function($http, $translate, $rootScope) {

         return {
           restrict: 'A',
           replace: true,
           scope: {
             harvester: '=gnHarvesterPrivileges',
             lang: '@'
           },
           templateUrl: '../../catalog/components/admin/harvester/partials/' +
           'privileges.html',
           link: function(scope, element, attrs) {
             scope.selectedPrivileges = {};

             var getPrivilege = function(groupId) {
               return {
                 '@id' : groupId,
                 'operation' : [{
                   '@name' : 'view'
                 }, {
                   '@name' : 'dynamic'
                 }, {
                   '@name' : 'download'
                 }]};
             };
             var defaultPrivileges = [getPrivilege(1)];

             scope.visibleTo = function(who) {
               scope.custom = false;
               scope.selectedPrivileges = {};
               if (who == 'all') {
                 scope.allGroup = false;
                 scope.selectedPrivileges = {1: true};
               } else if (who == 'none') {
                 scope.allGroup = false;
                 scope.selectedPrivileges = {1: false};
               } else if (who == 'allGroup') {
                 scope.allGroup = !scope.allGroup;
                 angular.forEach(scope.groups, function(g) {
                   scope.selectedPrivileges[g['@id']] = scope.allGroup;
                 });
               }
             };
             function loadGroups() {
               $http.get('info?_content_type=json&' +
               'type=groupsIncludingSystemGroups',
               {cache: true})
                 .success(function(data) {
                 scope.groups = data !== 'null' ? data.group : null;
               });
             }

             var initHarvesterPrivileges = function() {

               angular.forEach(scope.harvester.privileges, function(g) {
                 scope.selectedPrivileges[g['@id']] = true;
               });
             };

             var init = function() {
               scope.selectedPrivileges = {};

               loadGroups();

               // If only one privilege config
               // and group name is equal to Internet (ie. 1)
               if (scope.harvester.privileges &&
               scope.harvester.privileges.length === 1 &&
               scope.harvester.privileges[0]['@id'] == '1') {
                 $('#gn-harvester-visible-all').button('toggle');
               }

               initHarvesterPrivileges();

               scope.$watchCollection('selectedPrivileges', function() {
                 scope.harvester.privileges = [];
                 angular.forEach(scope.selectedPrivileges,
                 function(value, key) {
                   if (value) {
                     scope.harvester.privileges.push(
                     getPrivilege(key)
                     );
                   }
                 });
               });
             };


             scope.$watch('harvester', init);
           }
         };
       }]);
})();
