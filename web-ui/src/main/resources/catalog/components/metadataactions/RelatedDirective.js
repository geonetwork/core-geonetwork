(function() {

  goog.provide('gn_related_directive');
  goog.require('gn_relatedresources_service');

  var module = angular.module('gn_related_directive', [
    'gn_relatedresources_service'
  ]);

  /**
   * Shows a list of related records given an uuid with the actions defined in
   * config.js
   */
  module
      .directive(
          'gnRelated',
          [
        '$http',
        'gnRelatedResources',
        function($http, gnRelatedResources) {
          return {
            restrict: 'A',
            templateUrl: function(elem, attrs) {
              return attrs.template ||
                      '../../catalog/components/metadataactions/partials/related.html';
            },
            scope: {
              uuid: '@gnRelated',
              template: '@',
              types: '@',
              title: '@',
              list: '@'
            },
            link: function(scope, element, attrs, controller) {

              scope.updateRelations = function() {
                scope.relations = [];
                if (scope.uuid) {
                  if (!scope.list) {
                    $http.get(
                       'md.relations?_content_type=json&uuid=' +
                       scope.uuid + (scope.types ? '&type=' +
                       scope.types : ''), {cache: true})
                              .success(function(data, status, headers, config) {
                         if (data && data != 'null' && data.relation) {
                           if (!angular.isArray(data.relation))
                             scope.relation = [
                               data.relation
                             ];
                           for (var i = 0; i < data.relation.length; i++) {
                             scope.relations.push(data.relation[i]);
                           }
                         }
                       });
                  } else {
                    scope.relations = scope.list;
                  }
                }
              };

              scope.getTitle = function(link) {
                return link.title['#text'] || link.title;
              };

              scope.config = gnRelatedResources;

              scope.$watch('uuid', function() {
                scope.updateRelations();
              });
            }
          };
        }]);

  module.directive('relatedTooltip', function() {
    return function(scope, element, attrs) {
      for (var i = 0; i < element.length; i++) {
        element[i].title = scope.$parent.md['@type'];
      }
      element.tooltip();
    };
  });

})();
