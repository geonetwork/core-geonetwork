(function() {
  goog.provide('gn_editor_directive');

  var module = angular.module('gn_editor_directive', ['gn_editor']);

  /**
   * @ngdoc directive
   * @name gn_editor_directive.directive:gnMetadataCategoryUpdater
   * @restrict A
   * @requires gnMetadataCategoryUpdater
   *
   * @description
   * The `gnMetadataCategoryUpdater` directive provides a
   * dropdown button which allows to set the metadata
   * categories.
   */
  module.directive('gnMetadataCategoryUpdater', [
    'gnEditor', '$rootScope', '$translate', '$http',
    function(gnEditor, $rootScope, $translate, $http) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadatamanager/partials/' +
            'metadatacategoryupdater.html',
        scope: {
          currentCategories: '=gnMetadataCategoryUpdater'
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
            gnEditor.assignCategories(scope.ids)
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
   * @name gn_editor_directive.directive:gnMetadataGroupUpdater
   * @restrict A
   * @requires gnMetadataGroupUpdater
   *
   * @description
   * The `gnMetadataGroupUpdater` directive provides a
   * dropdown button which allows to update the metadata
   * group.
   */
  module.directive('gnMetadataGroupUpdater', [
    'gnEditor', '$rootScope', '$translate', '$http',
    function(gnEditor, $rootScope, $translate, $http) {

      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/metadatamanager/partials/' +
            'metadatagroupupdater.html',
        scope: {
          groupOwner: '=gnMetadataGroupUpdater'
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

          scope.assignGroup = function(g) {
            gnEditor.assignGroup(g['@id'])
              .then(function() {
                  scope.groupOwner = g['@id'];
                }, function(error) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate('changeGroupError'),
                    error: error,
                    timeout: 0,
                    type: 'danger'});
                });
            return false;
          };

        }
      };
    }]);
})();
