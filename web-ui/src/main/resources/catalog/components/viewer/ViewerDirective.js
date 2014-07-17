(function () {
  goog.provide('gn_viewer_directive');

  var module = angular.module('gn_viewer_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive('gnMainViewer', [
    function () {
      return {
        restrict: 'A',
        replace: true,
        templateUrl: '../../catalog/components/viewer/' +
          'partials/mainviewer.html',
        link: function (scope, element, attrs) {
        }
      };
    }]);
  module.directive('goBtnGroup', function() {
    return {
      restrict: 'A',
      controller: function($scope) {
        var buttonScopes = [];

        this.activate = function(btnScope) {
          angular.forEach(buttonScopes, function(b) {
            if (b != btnScope) {
              b.ngModelSet(b, false);
            }
          });
        };

        this.addButton = function(btnScope) {
          buttonScopes.push(btnScope);
        };
      }
    };
  })
      .directive('goBtn', function($parse) {
        return {
          require: ['?^goBtnGroup','ngModel'],
          restrict: 'A',
          //replace: true,
          scope: true,
          link: function (scope, element, attrs, ctrls) {
            var buttonsCtrl = ctrls[0], ngModelCtrl = ctrls[1];
            var ngModelGet = $parse(attrs['ngModel']);
            var cls = attrs['goBtn'];
            scope.ngModelSet = ngModelGet.assign;

            if(buttonsCtrl) buttonsCtrl.addButton(scope);

            //ui->model
            element.bind('click', function () {
              scope.$apply(function () {
                ngModelCtrl.$setViewValue(!ngModelCtrl.$viewValue);
                ngModelCtrl.$render();
              });
            });

            //model -> UI
            ngModelCtrl.$render = function () {
              if (buttonsCtrl && ngModelCtrl.$viewValue) {
                buttonsCtrl.activate(scope);
              }
              if(cls != '') {
                element.toggleClass(cls, ngModelCtrl.$viewValue);
              }
            };
          }
        };
      });

  module.directive('gnvToolsBtn', [
    function () {
      return {
        restrict: 'A',
        link: function (scope, element, attrs) {
          element.bind('click', function() {
            if (element.hasClass('active')) {
              element.removeClass('active');
              $(element.attr('rel')).addClass('force-hide')
            } else {
              $('.btn').removeClass('active');
              element.addClass('active');
              $('.panel-tools').addClass('force-hide');
              $(element.attr('rel')).removeClass('force-hide')
            }
          });
        }
      };
    }]);
  module.directive('gnvLayermanagerBtn', ['gnHttp',
    function (gnHttp) {
      return {
        restrict: 'A',
        link: function (scope, element, attrs) {
          element.find('.btn-group button').bind('click', function() {
            element.find('.btn-group button').removeClass('active');
            element.addClass('active');
            element.find('.layers').addClass('collapsed');
            element.find('.layerTree').removeClass('collapsed');
            element.find('.unfold').css('opacity',1);
          });

          element.find('.unfold').click(function(){
            element.find('.btn-group button').removeClass('active');
            element.find('.layers').removeClass('collapsed');
            element.find('.layerTree').addClass('collapsed');
            element.find('.unfold').css('opacity',0);
          });
        }
      };
    }]);
})();
