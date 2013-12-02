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
        function($scope, gnOnlinesrc) {
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
  .directive('gnAddOnlinesrc', ['gnOnlinesrc',
    'gnOwsCapabilities',
        function(gnOnlinesrc, gnOwsCapabilities) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addOnlinesrc.html',
            scope: {},
            link: function(scope, element, attrs) {

              // mode can be 'url' or 'upload'
              scope.mode = 'url';
              
              // the form parms that will be submited
              scope.params = {};
              
              // Tells if we need to display layer grid and send
              // layers to the submit
              scope.isWMSProtocol = false;
              
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

              /** 
               *  Add online resource
               *  If it is an upload, then we submit the form with right content
               *  If it is an URL, we just call a $http.get
               */
              scope.addOnlinesrc = function() {
                if (scope.mode == 'upload') {
                  scope.submit();
                }
                else {
                  scope.onlinesrcService.addOnlinesrc(scope.params);
                }
              };
             
              /**
               * loadWMSCapabilities
               * 
               * Call WMS capabilities request with params.url.
               * Update params.layers scope value, that will be also
               * passed to the layers grid directive.
               */
              scope.loadWMSCapabilities = function() {
                if(scope.isWMSProtocol) {
                  gnOwsCapabilities.getCapabilities(scope.params.url)
                  .then(function(layers) {
                    scope.layers = layers;
                  });
                }
              };
              
              /**
               * On protocol combo Change.
               * Update isWMSProtocol values to display or hide
               * layer grid and call or not a getCapabilities.
               */
              scope.$watch('params.protocol', function(){
                if(!angular.isUndefined(scope.params.protocol)) {
                  scope.isWMSProtocol = (scope.params.protocol.
                      indexOf('OGC:WMS') >= 0);
                  scope.loadWMSCapabilities();
                }
              });
              
              /**
               * On URL change, reload WMS capabilities
               * if the protocol is WMS
               */
              scope.$watch('params.url', function(){
                if(!angular.isUndefined(scope.params.url)) {
                  scope.loadWMSCapabilities();
                }
              });

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
  .directive('gnLinkToService', ['gnOnlinesrc', 'Metadata',
        function(gnOnlinesrc, Metadata) {
          return {
            restrict: 'A',
            scope: {},
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linkToService.html',
            link: function(scope, element, attrs) {
              scope.params = {
//                  type: 'service'
              };
              
              // This object is used to share value between this 
              // directive and the FormController scope that is contained
              // by the directive
              scope.stateObj = {};
              
              scope.$watchCollection('stateObj.selectRecords', function(){
                if(!angular.isUndefined(scope.stateObj.selectRecords) &&
                    scope.stateObj.selectRecords.length > 0) {
                  var md = new Metadata(scope.stateObj.selectRecords[0]);
                  console.log(md.getLinksByType('OGC:WMS'));
                }
              });

              scope.onlinesrcService = gnOnlinesrc;
            }
          };
        }]);

  //  .config(['gnOnlinesrc',
  //        function(gnOnlinesrc) {
  //
  //  }]);
})();
