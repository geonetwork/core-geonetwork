(function() {
  goog.provide('gn_editor_directive');

  var module = angular.module('gn_editor_directive', ['gn_editor']);

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

          var init = function() {
            $http.get('info@json?type=groups&profile=Editor', {cache: true}).
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

          init();
        }
      };
    }]);
})();
