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
        languages: '=',
        mainLang: '=',
        field: '='
      },
      transclude: true,
      restrict: 'A',
      replace: 'true',
      link: function($scope) {
        $scope.$watch('mainLang', function(newVal){
          if (newVal) {
            $scope.editLang = newVal;
          } else if ($scope.languages && $scope.languages.length > 0) {
            $scope.editLang = $scope.languages[0];
          }
        });

        $scope.setEditLang = function(lang) {
          $scope.editLang = lang;
        }
      },
      template: '<div class="form-group">' +
        '<label data-ng-show="title" for="title" class="col-xs-3 control-label" ><span data-translate="">{{title}}</span>: </label>' +
        '<div class="col-xs-9">' +
        '<textarea data-ng-disabled="disabled" rows="{{rows}}" id="title" class="form-control col-xs-12" ' +
        '          data-ng-repeat="lang in languages" data-ng-model="field[lang]" ' +
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
