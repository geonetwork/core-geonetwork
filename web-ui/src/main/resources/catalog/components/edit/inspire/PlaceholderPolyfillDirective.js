(function() {
  'use strict';
  goog.provide('placeholder_polyfill_directive');

  var module = angular.module('placeholder_polyfill_directive', []);

  module.directive('placeholder', ['$timeout', function ($timeout) {
    var update = function (element) {
      var val = element.val();
      var placeholder = val === element.attr('placeholder');
      if (!placeholder) {
        element.removeClass('placeholder');
      }
      if (element.is(":focus")) {
        element.removeClass('placeholder');
        if (placeholder) {
          element.val('');
        }
      } else {
        if (val === "") {
          element.val(element.attr('placeholder'));
          element.addClass('placeholder');
        }
      }
    };

    return {
      link: function (scope, elm, attrs) {
        if (attrs.type === 'password') {
          return;
        }
        angular.forEach(elm.get(), function(e){
          var nodeName = e.nodeName.toUpperCase();
          if (nodeName === 'INPUT' || nodeName === 'TEXTAREA') {
            $timeout(function () {
              update(elm);
              elm.focus(function () {
                update($(this));
              }).blur(function () {
                update($(this));
              });
            });

            if (attrs.ngModel) {
              scope.$watch(attrs.ngModel, function() {
                update(elm);
              });
            }
          }
        });
      }
    };
  }]);


}());
