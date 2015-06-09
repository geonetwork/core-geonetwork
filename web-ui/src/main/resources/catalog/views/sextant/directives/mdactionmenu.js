(function() {

  goog.provide('sxt_mdactionmenu');

  var module = angular.module('sxt_mdactionmenu', []);


  module.directive('sxtMdActionsMenu', ['gnMetadataActions', '$http',
    '$location', 'Metadata', 'sxtService',
    function(gnMetadataActions, $http, $location, Metadata, sxtService) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.sxtMdActionsMenu);
          if (!scope.md) {
            var url = $location.url();
            var uuid = url.substring(url.lastIndexOf('/') + 1);
            $http.get('q?_uuid=' + uuid + '&fast=index&_content_type=json&buildSummary=false').success (function (resp) {
              scope.md = new Metadata(resp.metadata);
              sxtService.feedMd(scope);
            });
          }
        }
      };
    }
  ]);
})();
