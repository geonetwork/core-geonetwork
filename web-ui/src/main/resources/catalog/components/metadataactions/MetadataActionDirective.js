(function() {
  goog.provide('gn_mdactions_directive');

  var module = angular.module('gn_mdactions_directive', []);

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
              setTimeout(function(){
                element.find(':input').select();
              }, 300);
            }
          };
        }]
  );
})();
