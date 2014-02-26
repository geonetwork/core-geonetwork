(function() {
  goog.provide('gn_thesaurus_type_directive');

  var module = angular.module('gn_thesaurus_type_directive', []);

  module.directive('gnThesaurusType', [function() {

    return {
      restrict: 'A',
      replace: true,
      transclude: true,
      scope: {
        typeList: '=gnThesaurusType',
        model: '=gnModel',
        disabled: '=gnDisabled'
      },
      templateUrl: '../../catalog/components/thesaurus/type/partials/' +
          'thesaurus-type.html',
      link: function(scope, element, attrs) {
        scope.types = scope.typeList ||
            ['theme', 'discipline', 'place', 'stratum', 'temporal'];
      }
    };
  }]);
})();
