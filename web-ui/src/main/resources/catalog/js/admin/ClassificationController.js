(function() {
  goog.provide('gn_classification_controller');


  goog.require('gn_categories_controller');
  goog.require('gn_thesaurus_controller');

  var module = angular.module('gn_classification_controller',
      ['gn_thesaurus_controller', 'gn_categories_controller']);


  /**
   *
   */
  module.controller('GnClassificationController',
      ['$scope', '$http',
       function($scope, $http) {

          $scope.pageMenu = {
           folder: 'classification/',
           defaultTab: 'thesaurus',
           tabs:
           [{
             type: 'thesaurus',
             label: 'manageThesaurus',
             icon: 'icon-archive',
             href: '#/classification/thesaurus'
           },{
                  type: 'directory',
                  label: 'manageDirectory',
                  icon: 'icon-list-ul',
                  href: 'subtemplate.admin' // TODO
                },{
             type: 'categories',
             label: 'manageCategory',
             icon: 'icon-tags',
             href: '#/classification/categories'
           }]
          };

       }]);

})();
