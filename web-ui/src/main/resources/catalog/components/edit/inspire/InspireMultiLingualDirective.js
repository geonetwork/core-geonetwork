(function() {
  'use strict';
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
        $scope.$watchCollection('languages', function(newVal){
          if (newVal.indexOf($scope.editLang) < 0) {
            $scope.editLang = newVal[0];
          }
        });
        $scope.$watch('mainLang', function(newVal){
          if (newVal) {
            $scope.editLang = newVal;
          } else if ($scope.langsForUI && $scope.langsForUI.length > 0) {
            $scope.editLang = $scope.languages[0];
          }
        });
        $scope.$watch('field', function(){
          $scope.validate();
        }, true);
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
        $scope.pillClass = function(lang) {
          var text = $scope.field[lang];
          if (lang === $scope.editLang) {
            return 'active';
          }

          if (!text || text.length === 0) {
            return 'warning';
          }
        };
        $scope.cls = undefined;
        $scope.validate = function() {

          var i, invalid;
          var isInvalid = function(model) {
            return !model || model.length === 0;
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
      templateUrl: '../../catalog/components/edit/inspire/partials/multilingual.html'
    };
  });

}());
