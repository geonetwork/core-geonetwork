(function () {
  goog.provide('gn_localisation');

  var module = angular.module('gn_localisation', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_localisation.directive:gnLocalisationInput
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive('gnLocalisationInput', ['$timeout',
    function ($timeout) {
    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/viewer/localisation/' +
        'partials/localisation.html',
      scope: {
        map: '='
      },
      controllerAs: 'locCtrl',
      controller: ['$scope', 'gnHttp', function($scope, gnHttp){
        this.zoomTo = function(extent, map) {
          map.getView().fitExtent(extent, map.getSize());
        };

        this.search = function(query) {
          gnHttp.callService('suggestion', {
            field: 'keyword',
            q: query
          }).success(function(response) {
            $scope.results = [];
            for(var i=0;i<response[1].length;i++) {
              if($.inArray(response[1][i],$scope.results) === -1) {
                $scope.results.push(response[1][i]);
              }
            }
          });
        };
      }],
      link: function (scope, element, attrs) {

        /** localisation text query */
        scope.query = '';

        /** default localisation */
        scope.localisations = [{
          name: 'United States',
          extent: [-13884991, 2870341, -7455066, 6338219]
        }, {
          name: 'France',
          extent: [-13884991, 2870341, -7455066, 6338219]
        }];

        /** Clear input and search results */
        scope.clearInput = function() {
          scope.query = '';
          scope.results = [];

        };

        // Bind events to display the dropdown menu
        element.find('input').bind('focus', function(evt) {
          element.addClass('open');
        }).bind('blur', function(evt) {
          $timeout(function() {
            element.removeClass('open');
          }, 1000, false);
        });

      }
    };
  }]);
})();
