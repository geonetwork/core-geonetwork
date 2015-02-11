(function() {

  goog.provide('gn_toptoolbar');

  var module = angular.module('gnTopToolbar', []);

  module.directive('gnTopToolbarElement', ['gnSearchLocation',
    function(gnSearchLocation) {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/views/default/directives/' +
            'partials/toolbarelement.html',
        scope: {},
        link: function linkFn(scope, element, attrs) {
          scope.originalPath = attrs.gnPath;
          scope.path = attrs.gnPath;
          scope.cssIcon = attrs.gnCssicon;
          scope.label = attrs.gnTopToolbarElement;
          scope.title = attrs.gnTitle;
          if(!scope.title) {
            scope.title = scope.label;
          }
          scope.onclick = attrs.gnClick;
          
          scope.checkActive = function() {
            //Check if we are on the same service:
            var current = window.location.pathname;
            current = current.substring(current.lastIndexOf("/") + 1);
            if(current.indexOf("?") > 0) {
              current = current.substring(0, current.indexOf("?"));
            }
            var cleanPath = scope.originalPath;
            if(cleanPath.indexOf("#") > 0) {
              cleanPath = cleanPath.substring(0, cleanPath.indexOf("#"))
            }
  
            var currentHash = window.location.hash;
            if(currentHash.indexOf("?") > 0) {
              currentHash = currentHash.substring(0, currentHash.indexOf("?"));
            }
            var cleanHash = scope.originalPath;
            if(cleanHash.indexOf("#") > 0) {
              cleanHash = cleanHash.substring(cleanHash.indexOf("#"))
            } else {
              cleanHash = "";
            }
            scope.active = (current == cleanPath &&
                 currentHash == cleanHash);
           };
          
          scope.$on('$locationChangeSuccess', scope.checkActive);
          scope.$watch(scope.active, function(){
            if(scope.active) {
              scope.path = scope.originalPath;
              if(scope.path.indexOf("#") > 0) {
                scope.path = scope.path.substring(scope.path.indexOf("#"))
              }
            }
            else {
              scope.path = scope.originalPath;
            }
          });
          
          scope.checkActive();
        }
      };
    }
  ]);

})();
