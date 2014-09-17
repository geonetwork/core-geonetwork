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
      'gnMap',
    function (goDecorateLayer, gnNcWms, gnMap) {
      return {
        restrict: 'A',
        replace: true,
        scope:true,
        templateUrl: '../../catalog/components/viewer/' +
          'partials/mainviewer.html',
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              scope.map = scope.$eval(iAttrs['map']);

              /** Define object to receive measure info */
              scope.measureObj = {};

              /** Define vector layer used for drawing */
              scope.drawVector;

              /** print definition */
              scope.activeTools = {};

              scope.zoom = function(map, delta) {
                gnMap.zoom(map,delta);
              };
              scope.zoomToMaxExtent = function(map) {
                map.getView().setResolution(gnMapConfig.maxResolution);
              };

              var div = document.createElement('div');
              div.className = 'overlay';
              var overlay = new ol.Overlay({
                element: div,
                positioning: 'bottom-left'
              });
              scope.map.addOverlay(overlay);

            },
            post: function postLink(scope, iElement, iAttrs, controller) {
              //TODO: find another solution to render the map
              setTimeout(function() {
                scope.map.updateSize();
              }, 100);
            }
          }
        }
      };
    }]);

  // TODO : to remove those directives when ngeo allow null class
  module.directive('giBtnGroup', function() {
    return {
      restrict: 'A',
      controller: ['$scope', function($scope) {
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
      }]
    };
  })
      .directive('giBtn', ['$parse', function($parse) {
        return {
          require: ['?^giBtnGroup','ngModel'],
          restrict: 'A',
          //replace: true,
          scope: true,
          link: function (scope, element, attrs, ctrls) {
            var buttonsCtrl = ctrls[0], ngModelCtrl = ctrls[1];
            var ngModelGet = $parse(attrs['ngModel']);
            var cls = attrs['giBtn'];
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
      }]);

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
  module.directive('gnvLayermanagerBtn', [
    function () {
      return {
        restrict: 'A',
        link: function (scope, element, attrs) {
          element.find('.btn-group button').bind('click', function() {
            element.find('.btn-group button').removeClass('active');
            element.addClass('active');
            $(this).addClass('active');
            var carousel = $(this).parents('.panel-body').first().find('.panel-carousel-window');
            element.find('.layers').addClass('collapsed');
            carousel.find('.panel-carousel').removeClass('collapsed');
            element.find('.unfold').css('opacity',1);
            carousel.find('.panel-carousel-container').css('left',
                '-' + ($(this).index()*100) + '%');
          });
          element.find('.nav li').bind('click', function(e) {
            e.preventDefault();
            $(this).parents('.nav').first().find('li').removeClass('active');
            $(this).addClass('active');
            var carousel = $(this).parents('.layers').first().find('.panel-carousel-window');
            carousel.find('.panel-carousel-container').css('left',
                '-' + ($(this).index()*100) + '%');
            $(':focus').blur();
          });

          element.find('.unfold').click(function(){
            var el = $(this).parent('.panel-body');
            el.find('.btn-group button').removeClass('active');
            element.find('.layers').removeClass('collapsed');
            el.find('.panel-carousel').addClass('collapsed');
            el.find('.unfold').css('opacity',0);
          });
        }
      };
    }]);

  module.directive('gnvClosePanel', [
    function () {
      return {
        restrict: 'A',
        require: 'giBtnGroup',
        scope: true,
        link: function (scope, element, attrs, btngroupCtrl) {
          $('.close').click(function(){
            var t = $(this).parents('.panel-tools').first();
            t.addClass('force-hide');
            $('[rel=#'+t.attr('id')+']').removeClass('active');
            scope.$apply(function() {
              btngroupCtrl.activate();
            });
          });
        }
      };
    }]);
})();
