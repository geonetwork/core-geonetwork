(function() {

  goog.provide('gn_search_geocat_mdactionmenu');

  var module = angular.module('gn_search_geocat_mdactionmenu', []);


  module.directive('gcMdActionsMenu', ['gnMetadataActions', '$http', '$location', 'Metadata',
    function(gnMetadataActions, $http, $location, Metadata) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/geocat/directives/' +
            'partials/mdactionmenu.html',
        link: function linkFn(scope, element, attrs) {
          scope.mdService = gnMetadataActions;
          scope.md = scope.$eval(attrs.gcMdActionsMenu);
          if (!scope.md) {
            var url = $location.url();
            var uuid = url.substring(url.lastIndexOf('/') + 1);
            $http.get('q?_uuid=' + uuid + '&fast=index&_content_type=json&buildSummary=false').success (function (resp) {
              scope.md = new Metadata(resp.metadata);
            });
          }
        }
      };
    }
  ]);
})();
