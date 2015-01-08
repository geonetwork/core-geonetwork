(function() {

  goog.provide('gn_search_default_directive');

  var module = angular.module('gn_search_default_directive', []);

  module.directive('gnInfoList', [
    function () {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/js/custom/default/' +
        'partials/infolist.html',
        link: function linkFn(scope, element, attr) {
          scope.showMore = function (isDisplay) {
            var div = $('#gn-info-list' + this.md.getUuid());
            $(div.children()[isDisplay ? 0 : 1]).addClass('hidden');
            $(div.children()[isDisplay ? 1 : 0]).removeClass('hidden');
          };
        }
      };
    }
  ]);
})();
