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
              md: '=gnRelated',
              template: '@',
              types: '@',
              title: '@',
              list: '@'
            },
            link: function(scope, element, attrs, controller) {
              scope.updateRelations = function() {
                if (scope.md) {
                  scope.uuid = scope.md.getUuid();
                }
                scope.relations = [];
                if (scope.uuid) {
                  $http.get(
                     'md.relations?_content_type=json&uuid=' +
                     scope.uuid + (scope.types ? '&type=' +
                     scope.types : ''), {cache: true})
                            .success(function(data, status, headers, config) {
                       if (data && data != 'null' && data.relation) {
                         if (!angular.isArray(data.relation))
                           scope.relations = [
                             data.relation
                           ];
                         for (var i = 0; i < data.relation.length; i++) {
                           scope.relations.push(data.relation[i]);
                         }
                       }
                     });
                }
              };

              scope.getTitle = function(link) {
                return link.title['#text'] || link.title;
              };

              scope.hasAction = function(mainType) {
                return angular.isFunction(
                   gnRelatedResources.map[mainType].action);
              };
              scope.config = gnRelatedResources;

              scope.$watchCollection('md', function() {
                scope.updateRelations();
              });

              /**
               * Return an array of all relations of the given types
               * @return {Array}
               */
              scope.getByTypes = function() {
                var res = [];
                var types = Array.prototype.splice.call(arguments, 0);
                angular.forEach(scope.relations, function(rel) {
                  if (types.indexOf(rel['@type']) >= 0) {
                    res.push(rel);
                  }
                });
                return res;
              };
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
