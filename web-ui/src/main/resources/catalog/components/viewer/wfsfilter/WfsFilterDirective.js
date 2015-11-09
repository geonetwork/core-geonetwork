(function() {
  goog.provide('gn_wfsfilter');

  var module = angular.module('gn_wfsfilter', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_wfsfilter.directive:gnWfsFilterFacets
   *
   * @description
   */
  module.directive('gnWfsFilterFacets', [
      '$http',

    function($http) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/wfsfilter/' +
            'partials/wfsfilterfacet.html',
        scope: {
          featureTypeName: '@',
          uuid: '@',
          wfsUrl: '@'
        },
        link: function(scope, element, attrs) {

          var url = 'solrfacets/' + scope.uuid + '?url=' + encodeURIComponent(scope.wfsUrl) + '&typename=' + scope.featureTypeName;

          var fields = [];

          // Describe facets configuration to build the ui
          scope.fields = fields;

          // output structure to send to filter service
          scope.output = [];

          /**
           * Call solrfacets service to retrieve the facet config from
           * applicationProfile.
           * Then build facets configuration structure for the ui.
           */
          $http.get(url).success(function(data) {
            for(var kind in data.facet_counts) {
              var facetType = getFacetType(kind);
              for(var fieldProp in data.facet_counts[kind]) {
                var field = data.facet_counts[kind][fieldProp];
                var facetField = {
                  name: fieldProp,
                  values: {},
                  type: facetType
                };

                // TODO: manage all facet type (ranges, dates)
                // facet.field
                if(angular.isArray(field) && field.length > 0) {
                  for (var i = 0; i<  field.length; i+=2) {
                    facetField.values[field[i]] = field[i+1];
                  }
                  fields.push(facetField);
                }

                // facet.interval
                else if(Object.keys(field).length > 0) {
                  facetField.values = field;
                  fields.push(facetField);
                }
              }
            }
          });

          var getFacetType = function(solrPropName) {
            var type = '';
            if(solrPropName == 'facet_ranges') {
              type = 'range';
            }
            else if(solrPropName == 'facet_intervals') {
              type = 'interval';
            }
            else if(solrPropName == 'facet_fields') {
              type = 'field';
            }
            else if(solrPropName == 'facet_dates') {
              type = 'date';
            }
            return type;
          };

          var buildSldFilter = function(key, type) {
            var res;
            if(type == 'interval' ) {
              res = {
                filter_type: 'PropertyIsBetween',
                params: key.match(/\d+(?:[.]\d+)*/g)
              }
            }
            else if(type == 'field' ) {
              res = {
                filter_type: 'PropertyIsEqualTo',
                params: [key]
              }
            }

            return res;
          };

          scope.onCheckboxClick = function(fieldName, facetKey, type) {
            var toRemove = -1;
            var output = scope.output;
            for (var i = 0; i < output.length; i++) {
              var o = output[i];
              if(o.name == fieldName && o.key == facetKey) {
                toRemove = i;
                break;
              }
            }
            if(toRemove > -1) {
              output.splice(toRemove,1);
            }
            else {
              output.push({
                name: fieldName,
                key: facetKey,
                type: type
              });
            }
          };

          scope.filter = function() {
            var output = {
              filters: []
            };

            angular.forEach(scope.output, function(f) {
              var field;
              for (var i = 0; i < output.filters.length; i++) {
                var o = output.filters[i];
                if(o.field_name == f.name) {
                  field = o;
                  break;
                }
              }
              if(!field) {
                field = {
                  field_name: f.name,
                  filter: []
                };
                output.filters.push(field);
              }
              field.filter.push(buildSldFilter(f.key, f.type));
            });
            console.log(output);
          }
        }
      };
    }]);
})();
