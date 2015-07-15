(function() {

  goog.provide('sxt_linksbtn');

  var module = angular.module('sxt_linksbtn', []);


  module.directive('sxtLinksBtn', [ 'gnSearchSettings', 'gnViewerSettings',
    function(searchSettings, viewerSettings) {
      return {
        restrict: 'E',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/linksbtn.html',
        link: function(scope) {
          scope.isMap = searchSettings.mainTabs.map;
          scope.isPanier = searchSettings.mainTabs.panier;
        }
      };
    }
  ]);
})();
