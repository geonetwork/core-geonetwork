(function() {
  goog.provide('inspire_multilingual_text_directive');

  var module = angular.module('inspire_multilingual_text_directive', []);

  module.directive('inspireMultilingualText', function() {
    return {
      scope: {
        title: '@',
        rows: '@',
        disabled: '@',
        placeholder: '@',
        validationClass: '@',
        languages: '=',
        mainLang: '=',
        field: '='
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      link: function($scope) {
        $scope.$watch('languages', function(){
          if (!$scope.editLang) {
            $scope.setEditLang($scope.languages[0]);
          }
        }); $scope.$watch('mainLang', function(newVal){
          if (newVal) {
            $scope.editLang = newVal;
          } else if ($scope.langsForUI && $scope.langsForUI.length > 0) {
            $scope.editLang = $scope.languages[0];
          }
        });

        $scope.setEditLang = function(lang) {
          if (!lang) {
            if ($scope.languages.length > 0) {
              lang = $scope.languages[0];
            } else {
              lang = $scope.lang;
            }
          }
          $scope.editLang = lang;
        };
        $scope.cls = undefined;
        $scope.validate = function() {

          var i, invalid;
          var isInvalid = function(model) {
            return !model || model.length == 0
          };

          if (!$scope.validationClass) {
            return '';
          }

          invalid = true;
          for (i = 0; i < $scope.languages.length; i++) {
            invalid = invalid && isInvalid($scope.field[$scope.languages[i]]);
          }

          $scope.cls = invalid ? $scope.validationClass : '' ;
        };

        $scope.validate();
      },
      template: '<div class="form-group">' +
        '<label data-ng-show="title" for="title" class="col-xs-3 control-label" ><span data-translate="">{{title}}</span>: </label>' +
        '<div data-ng-class="title ? [cls, \'col-xs-9\'] : [cls, \'col-xs-12\']">' +
        '<textarea data-ng-disabled="disabled" rows="{{rows}}" id="title" class="form-control col-xs-12" ' +
        '          data-ng-repeat="lang in languages" data-ng-model="field[lang]" ' +
        '          data-ng-change="validate()" '+
        '          data-ng-show="editLang === lang || editLang === \'all\'" ' +
        '          placeholder="{{placeholder ? placeholder + \' -- \' : \'\'}}{{lang | translate}}" />' +
        '<ul class="nav nav-pills">' +
        '<li data-ng-class="lang === editLang ? \'active\' : \'\'" data-ng-repeat="lang in languages" data-ng-hide="editLang === \'all\'"> ' +
        '<a data-ng-click="setEditLang(lang)">{{lang | translate}}</a>' +
        '</li>' +
        '<li>' +
        '<a data-ng-click="editLang === \'all\' ? setEditLang(mainLang) : setEditLang(\'all\')">' +
        '{{editLang === \'all\' ? \'collapse\' : \'all\' | translate}}</a>' +
        '</li>' +
        '</ul>' +
        '</div></div>'
    };
  });

})();
