/*
 * angular-ui-bootstrap
 * http://angular-ui.github.io/bootstrap/

 * Version: 0.12.1 - 2015-02-20
 * License: MIT
 */
angular.module("ui.bootstrap", ["ui.bootstrap.tpls","ui.bootstrap.rating"]);
angular.module("template/rating/rating.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("template/rating/rating.html",
    "<span ng-mouseleave=\"reset()\" ng-keydown=\"onKeydown($event)\" tabindex=\"0\" role=\"slider\" aria-valuemin=\"0\" aria-valuemax=\"{{range.length}}\" aria-valuenow=\"{{value}}\">\n" +
    "    <i ng-repeat=\"r in range track by $index\" ng-mouseenter=\"enter($index + 1)\" ng-click=\"rate($index + 1)\" class=\"glyphicon\" ng-class=\"$index < value && (r.stateOn || 'glyphicon-star') || (r.stateOff || 'glyphicon-star-empty')\">\n" +
    "        <span class=\"sr-only\">({{ $index < value ? '*' : ' ' }})</span>\n" +
    "    </i>\n" +
    "</span>");
}]);
angular.module("ui.bootstrap.tpls", ["template/rating/rating.html"]);
angular.module('ui.bootstrap.rating', [])

  .constant('ratingConfig', {
    max: 5,
    stateOn: null,
    stateOff: null
  })

  .controller('RatingController', ['$scope', '$attrs', 'ratingConfig', function($scope, $attrs, ratingConfig) {
    var ngModelCtrl  = { $setViewValue: angular.noop };

    this.init = function(ngModelCtrl_) {
      ngModelCtrl = ngModelCtrl_;
      ngModelCtrl.$render = this.render;

      this.stateOn = angular.isDefined($attrs.stateOn) ? $scope.$parent.$eval($attrs.stateOn) : ratingConfig.stateOn;
      this.stateOff = angular.isDefined($attrs.stateOff) ? $scope.$parent.$eval($attrs.stateOff) : ratingConfig.stateOff;

      var ratingStates = angular.isDefined($attrs.ratingStates) ? $scope.$parent.$eval($attrs.ratingStates) :
        new Array( angular.isDefined($attrs.max) ? $scope.$parent.$eval($attrs.max) : ratingConfig.max );
      $scope.range = this.buildTemplateObjects(ratingStates);
    };

    this.buildTemplateObjects = function(states) {
      for (var i = 0, n = states.length; i < n; i++) {
        states[i] = angular.extend({ index: i }, { stateOn: this.stateOn, stateOff: this.stateOff }, states[i]);
      }
      return states;
    };

    $scope.rate = function(value) {
      if ( !$scope.readonly && value >= 0 && value <= $scope.range.length ) {
        ngModelCtrl.$setViewValue(value);
        ngModelCtrl.$render();
      }
    };

    $scope.enter = function(value) {
      if ( !$scope.readonly ) {
        $scope.value = value;
      }
      $scope.onHover({value: value});
    };

    $scope.reset = function() {
      $scope.value = ngModelCtrl.$viewValue;
      $scope.onLeave();
    };

    $scope.onKeydown = function(evt) {
      if (/(37|38|39|40)/.test(evt.which)) {
        evt.preventDefault();
        evt.stopPropagation();
        $scope.rate( $scope.value + (evt.which === 38 || evt.which === 39 ? 1 : -1) );
      }
    };

    this.render = function() {
      $scope.value = ngModelCtrl.$viewValue;
    };
  }])

  .directive('rating', function() {
    return {
      restrict: 'EA',
      require: ['rating', 'ngModel'],
      scope: {
        readonly: '=?',
        onHover: '&',
        onLeave: '&'
      },
      controller: 'RatingController',
      template:  "<span ng-mouseleave=\"reset()\" ng-keydown=\"onKeydown($event)\" tabindex=\"0\" role=\"slider\" aria-valuemin=\"0\" aria-valuemax=\"{{range.length}}\" aria-valuenow=\"{{value}}\">\n" +
        "    <i ng-repeat=\"r in range track by $index\" ng-mouseenter=\"enter($index + 1)\" ng-click=\"rate($index + 1)\" class=\"glyphicon\" ng-class=\"$index < value && (r.stateOn || 'glyphicon-star') || (r.stateOff || 'glyphicon-star-empty')\">\n" +
        "        <span class=\"sr-only\">({{ $index < value ? '*' : ' ' }})</span>\n" +
        "    </i>\n" +
        "</span>",
      // templateUrl: 'template/rating/rating.html',
      replace: true,
      link: function(scope, element, attrs, ctrls) {
        var ratingCtrl = ctrls[0], ngModelCtrl = ctrls[1];

        if ( ngModelCtrl ) {
          ratingCtrl.init( ngModelCtrl );
        }
      }
    };
  });
