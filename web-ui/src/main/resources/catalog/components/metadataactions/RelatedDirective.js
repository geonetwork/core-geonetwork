(function() {

  goog.provide('gn_related_directive');
  goog.require('gn_map_service');

  var module = angular.module('gn_related_directive', [ 'gn_map_service' ]);

  /**
   * Shows a list of related records given an uuid with the actions defined in
   * config.js
   */
  module
      .directive(
          'gnRelated',
          [
              '$http',
              'gnMap',
              'gnSearchSettings',
              function($http, gnMap, gnSearchSettings) {
                return {
                  restrict : 'A',
                  templateUrl : function(elem, attrs) {
                    return attrs.template
                        || '../../catalog/components/metadataactions/partials/related.html';
                  },
                  scope : {
                    uuid : '@gnRelated',
                    template : '@',
                    types : '@',
                    list : '@'
                  },
                  link : function(scope, element, attrs, controller) {
                    scope.mapService = gnMap;
                    scope.map = gnSearchSettings.searchMap;
                    scope.actions = gnSearchSettings.mdSettings.actions;

                    scope.exec = function(a) {
                      eval(a);
                    };

                    scope.updateRelations = function() {
                      scope.relations = [];
                      if (scope.uuid) {
                        if (!scope.list) {
                          $http
                              .get(
                                  'md.relations?_content_type=json&uuid='
                                      + scope.uuid
                                      + (scope.types ? '&type=' + scope.types
                                          : ''))
                              .success(function(data, status, headers, config) {

                                if (data && data != 'null' && data.relation) {
                                  if (!angular.isArray(data.relation))
                                    data.relation = [ data.relation ];
                                  for (i = 0; i < data.relation.length; i++) {
                                    scope.relations.push(data.relation[i]);
                                  }
                                }
                              });
                        } else {
                          scope.relations = scope.list;
                        }
                      }
                    };

                    scope.$watch('uuid', function() {
                      scope.updateRelations();
                    });

                    scope.updateRelations();
                  }
                };
              } ]);

  module.directive('relatedTooltip', function() {
    return function(scope, element, attrs) {
      for (var i = 0; i < element.length; i++) {
        element[i].title = scope.$parent.md['@type'];
      }
      element.tooltip();
    };
  });
})();
