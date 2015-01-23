(function() {
  goog.provide('gn_mdactions_directive');

  goog.require('gn_mdactions_service');

  var module = angular.module('gn_mdactions_directive', []);

  module.directive('gnMetadataStatusUpdater', ['$translate', '$http',
    function($translate, $http) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'statusupdater.html',
        scope: {
          metadataId: '=gnMetadataStatusUpdater'
        },
        link: function(scope) {
          scope.lang = scope.$parent.lang;
          scope.newStatus = {value: '0'};

          function init() {
            return $http.get('md.status.list?' +
                '_content_type=json&id=' + scope.metadataId).
                success(function(data) {
                  scope.status =
                     data !== 'null' ? data.statusvalue : null;

                  angular.forEach(scope.status, function(s) {
                    if (s.on) {
                      scope.newStatus.value = s.id;
                      return;
                    }
                  });
                });
          };

          scope.updateStatus = function() {
            return $http.get('md.status.update?' +
                '_content_type=json&id=' + scope.metadataId +
                '&changeMessage=' + scope.changeMessage +
                '&status=' + scope.newStatus.value).then(
                function(data) {
                  scope.$emit('metadataStatusUpdated', true);
                  scope.$emit('StatusUpdated', {
                    msg: $translate('metadataStatusUpdatedWithNoErrors'),
                    timeout: 2,
                    type: 'success'});
                }, function(data) {
                  scope.$emit('metadataStatusUpdated', false);
                  scope.$emit('StatusUpdated', {
                    title: $translate('metadataStatusUpdatedErrors'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
          };

          init();
        }
      };
    }]);
  /**
   * @ngdoc directive
   * @name gn_mdactions_directive.directive:gnMetadataCategoryUpdater
   * @restrict A
   * @requires gnMetadataCategoryUpdater
   *
   * @description
   * The `gnMetadataCategoryUpdater` directive provides a
   * dropdown button which allows to set the metadata
   * categories.
   */
  module.directive('gnMetadataCategoryUpdater', [
    'gnMetadataActions', '$translate', '$http',
    function(gnMetadataActions, $translate, $http) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'metadatacategoryupdater.html',
        scope: {
          currentCategories: '=gnMetadataCategoryUpdater',
          metadataId: '='
        },
        link: function(scope) {
          scope.lang = scope.$parent.lang;
          scope.categories = null;
          scope.ids = [];

          var init = function() {
            return $http.get('info?type=categories&' +
                '_content_type=json', {cache: true}).
                success(function(data) {
                  scope.categories =
                     data !== 'null' ? data.metadatacategory : null;

                  angular.forEach(scope.categories, function(c) {
                    if (scope.currentCategories.indexOf(c.name) !== -1) {
                      scope.ids.push(c['@id']);
                    }
                  });
                });
          };

          scope.sortByLabel = function(c) {
            return c.label[scope.lang];
          };

          // Remove or add category to the set of ids
          scope.assign = function(c, event) {
            event.stopPropagation();
            var existIndex = scope.ids.indexOf(c['@id']);
            if (existIndex === -1) {
              scope.ids.push(c['@id']);
            } else {
              scope.ids.splice(existIndex, 1);
            }
            gnMetadataActions.assignCategories(scope.metadataId, scope.ids)
              .then(function() {
                  scope.currentCategories.push(c.name);
                }, function(error) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('changeGroupError'),
                    error: error,
                    timeout: 0,
                    type: 'danger'});
                });
            return false;
          };
          init();
        }
      };
    }]);
  /**
   * @ngdoc directive
   * @name gn_mdactions_directive.directive:gnMetadataGroupUpdater
   * @restrict A
   * @requires gnMetadataGroupUpdater
   *
   * @description
   * The `gnMetadataGroupUpdater` directive provides a
   * dropdown button which allows to update the metadata
   * group.
   */
  module.directive('gnMetadataGroupUpdater', [
    'gnMetadataActions', '$translate', '$http',
    function(gnMetadataActions, $translate, $http) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'metadatagroupupdater.html',
        scope: {
          groupOwner: '=gnMetadataGroupUpdater',
          metadataId: '='
        },
        link: function(scope) {
          scope.lang = scope.$parent.lang;
          scope.groups = null;

          scope.init = function(event) {
            return $http.get('info?_content_type=json&' +
                'type=groups&profile=Editor', {cache: true}).
                success(function(data) {
                  scope.groups = data !== 'null' ? data.group : null;
                });
          };

          scope.sortByLabel = function(group) {
            return group.label[scope.lang];
          };

          scope.assignGroup = function(g, event) {
            event.stopPropagation();
            gnMetadataActions.assignGroup(scope.metadataId, g['@id'])
              .then(function() {
                  scope.groupOwner = g['@id'];
                }, function(error) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('changeCategoryError'),
                    error: error,
                    timeout: 0,
                    type: 'danger'});
                });
            return false;
          };

        }
      };
    }]);

  module.directive('gnPermalinkInput', [
    function() {
      return {
        restrict: 'A',
        replace: false,
        templateUrl: '../../catalog/components/metadataactions/partials/' +
            'permalinkinput.html',
        link: function(scope, element, attrs) {
          scope.url = attrs['gnPermalinkInput'];
          scope.copied = false;
          setTimeout(function() {
            element.find(':input').select();
          }, 300);
        }
      };
    }]
  );

})();
