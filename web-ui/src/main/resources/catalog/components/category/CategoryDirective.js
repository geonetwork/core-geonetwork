(function() {
  goog.provide('gn_category_directive');

  var module = angular.module('gn_category_directive', []);

  /**
     * Provide a list of categories if at least one
     * exist in the catalog
     *
     */
  module.directive('gnCategory', ['$http', '$translate',
    function($http, $translate) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          element: '=gnCategory',
          lang: '@lang',
          label: '@label'
        },
        templateUrl: '../../catalog/components/category/partials/' +
            'category.html',
        link: function(scope, element, attrs) {
          $http.get('info?_content_type=json&type=categories', {cache: true}).
              success(function(data) {
                scope.categories = data.metadatacategory;
              }).error(function(data) {
                // TODO
              });
        }
      };
    }]);

  module.directive('gnBatchCategories', [
    '$http', '$translate', '$q',
    function($http, $translate, $q) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        templateUrl: '../../catalog/components/category/partials/' +
            'batchcategory.html',
        link: function(scope, element, attrs) {
          scope.report = null;

          $http.get('info?_content_type=json&type=categories', {cache: true}).
              success(function(data) {
                scope.categories = data.metadatacategory;
              }).error(function(data) {
                // TODO
              });

          scope.save = function(replace) {
            scope.report = null;
            var defer = $q.defer();
            var params = {};
            var url = 'md.category.batch.update?_content_type=json';

            if (replace) {
              url += '&mode=add';
            }

            angular.forEach(scope.categories, function(c) {
              if (c.checked === true) {
                params['_' + c['@id']] = 'on';
              }
            });
            $http.get(url, {params: params})
              .success(function(data) {
                  scope.report = data;
                  defer.resolve(data);
                }).error(function(data) {
                  scope.report = data;
                  defer.reject(data);
                });
            return defer.promise;
          };
        }
      };
    }]);
})();
