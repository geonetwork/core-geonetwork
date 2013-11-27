(function() {
  goog.provide('gn_onlinesrc_directive');

  goog.require('gn_utility');

  angular.module('gn_onlinesrc_directive', [
    'gn_utility',
    'blueimp.fileupload'
  ])
  .controller('gnOnlinesrcController', [
        '$scope',
        'gnOnlinesrc',
        'gnHttp',
        function($scope, gnOnlinesrc, gnHttp) {
          gnOnlinesrc.getAllResources();

        }
      ])

  .directive('gnOnlinesrcPanel', ['gnOnlinesrc',
        function(gnOnlinesrc) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addOnlinesrc.html',
            scope: {},
            link: function(scope, element, attrs) {
            }
          };
        }])
  .directive('gnAddOnlinesrc', ['gnOnlinesrc', 'gnHttp',
        function(gnOnlinesrc, gnHttp) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addOnlinesrc.html',
            scope: {},
            link: function(scope, element, attrs) {

              //FIXME : to move
              console.log('in gnOnlinesrcController');
              gnHttp.callService('search', {
                summaryOnly: true
              }).success(function(data) {
                console.log(data);
              });

              // mode can be 'url' or 'upload'
              scope.mode = 'url';
              scope.params = {};
              scope.onlinesrcService = gnOnlinesrc;

              /**
               * Onlinesrc uploaded with success, close the popup,
               * refresh the metadata.
               */
              var uploadOnlinesrcDone = function(data) {
                scope.clear($scope.queue);
              };

              /**
               * Onlinesrc uploaded with error, broadcast it.
               */
              var uploadOnlineSrcError = function(data) {
              };

              scope.onlinesrcUploadOptions = {
                autoUpload: false,
                //        TODO: acceptFileTypes: /(\.|\/)(xml|skos|rdf)$/i,
                done: uploadOnlinesrcDone,
                fail: uploadOnlineSrcError
              };

              /** Add online resource
               *  If it is an upload, then we submit the form with right content
               *  If it is an url, we just call a $http.get
               */
              scope.addOnlinesrc = function() {
                if (scope.mode == 'upload') {
                  scope.submit();
                }
                else {
                  scope.onlinesrcService.addOnlinesrc(scope.params);
                }
              };
            }
          };
        }])
  .directive('gnLinkParentMd', ['gnOnlinesrc',
        function(gnOnlinesrc) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linkParentMd.html',
            link: function(scope, element, attrs) {
              scope.onlinesrcService = gnOnlinesrc;
            }
          };
        }])
  .directive('gnLinkToService', ['gnOnlinesrc',
        function(gnOnlinesrc) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linkToService.html',
            link: function(scope, element, attrs) {
              scope.onlinesrcService = gnOnlinesrc;
            }
          };
        }]);

  //  .config(['gnOnlinesrc',
  //        function(gnOnlinesrc) {
  //
  //  }]);
})();
