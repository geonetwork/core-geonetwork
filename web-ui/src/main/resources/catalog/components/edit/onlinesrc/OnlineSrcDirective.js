(function() {
  goog.provide('gn_onlinesrc_directive');

  goog.require('gn_utility');

  /**
   * Provide directives for online resources
   *
   * - gnOnlinesrcList
   * - gnAddThumbnail
   * - gnAddOnlinesrc
   * - gnLinkParentMd
   * - gnLinkServiceToDataset
   */
  angular.module('gn_onlinesrc_directive', [
    'gn_utility',
    'blueimp.fileupload'
  ])
  .directive('gnOnlinesrcList', ['gnOnlinesrc',
        function(gnOnlinesrc) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/onlinesrcList.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.onlinesrcService = gnOnlinesrc;
              gnOnlinesrc.getAllResources()
                .then(function(data) {
                    scope.relations = data;
                  });
            }
          };
        }])
   .directive('gnAddThumbnail', [
        'gnOnlinesrc',
        'gnMetadataManagerService',
        'gnOwsCapabilities',
        function(gnOnlinesrc, gnMetadataManagerService, gnOwsCapabilities) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addThumbnail.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.metadataId = gnMetadataManagerService.
                  getCurrentEdit().metadataId;
              
              // mode can be 'url' or 'upload'
              scope.mode = 'url';

              // the form params that will be submited
              scope.params = {};

              /**
               * If we send an upload via form submit, the form field
               * 'version' has to be set.
               */
              scope.$watch('mode', function() {
                if (angular.isUndefined(scope.params.version)) {
                  getVersion();
                }
              });

              // TODO: should be in gnMetadataManagerService ?
              var getVersion = function() {
                return scope.params.version = $(gnMetadataManagerService.
                    getCurrentEdit().formId).
                    find('input[id="version"]').val();
              };

              /**
               * Onlinesrc uploaded with success, close the popup,
               * refresh the metadata.
               */
              var uploadOnlinesrcDone = function(evt,data) {
                $(gnMetadataManagerService.
                    getCurrentEdit().formId).
                    find('input[id="version"]').val(data.result.version);
                
                  gnMetadataManagerService.refreshEditorForm();
              };

              /**
               * Onlinesrc uploaded with error, broadcast it.
               */
              var uploadOnlineSrcError = function(data) {
              };

              scope.onlinesrcUploadOptions = {
                autoUpload: false,
                done: uploadOnlinesrcDone,
                fail: uploadOnlineSrcError
              };

              /**
               *  Add thumbnail
               *  If it is an upload, then we submit the form with right content
               *  If it is an URL, we just call a $http.get
               */
              scope.addThumbnail = function() {
                if (scope.mode == 'upload') {
                  gnMetadataManagerService.save()
                  .then(function(data) {
                        getVersion();
                        scope.submit();
                        // TODO:
                        // option1: Get version number from response
                        // and update the editor form
                        // option2: Reload the editor for this record
                        // (option2 in widget/option1 avoid editor reload)
                      });
                }
                else {
                  gnOnlinesrc.addThumbnailByURL(scope.params);
                }
              };
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
                  gnOnlinesrc.addOnlinesrc(scope.params);
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
                if (scope.isWMSProtocol) {
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
              scope.$watch('params.protocol', function() {
                if (!angular.isUndefined(scope.params.protocol)) {
                  scope.isWMSProtocol = (scope.params.protocol.
                      indexOf('OGC:WMS') >= 0);
                  scope.loadWMSCapabilities();
                }
              });

              /**
               * On URL change, reload WMS capabilities
               * if the protocol is WMS
               */
              scope.$watch('params.url', function() {
                if (!angular.isUndefined(scope.params.url)) {
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
                'partials/linkToParent.html',
            link: function(scope, element, attrs) {

              // sent to the SearchFormController
              scope.autoSearch = true;

              scope.onlinesrcService = gnOnlinesrc;
            }
          };
        }])
  .directive('gnLinkServiceToDataset', [
        'gnOnlinesrc',
        'Metadata',
        'gnOwsCapabilities',
        function(gnOnlinesrc, Metadata, gnOwsCapabilities) {
          return {
            restrict: 'A',
            scope: {},
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/linkServiceToDataset.html',
            link: function(scope, element, attrs) {

              scope.mode = attrs['gnLinkServiceToDataset'];

              // sent to the SearchFormController
              scope.autoSearch = true;

              // parameters of the search form contained in the directive
              scope.params = {
                type: scope.mode == 'attachService' ?
                    'service' : 'dataset'
              };

              // parameters of the online resource form
              scope.srcParams = {};

              // This object is used to share value between this
              // directive and the SearchFormController scope that is contained
              // by the directive
              scope.stateObj = {};

              /**
               * loadWMSCapabilities
               *
               * Call WMS capabilities on the service metadata URL.
               * Update params.layers scope value, that will be also
               * passed to the layers grid directive.
               */
              scope.loadWMSCapabilities = function(url) {
                gnOwsCapabilities.getCapabilities(url)
                .then(function(layers) {
                      scope.layers = layers;
                    });
              };

              /**
               * Watch the result metadata selection change.
               * selectRecords is a value of the SearchFormController scope.
               * On service metadata selection, check if the service has
               * a WMS URL and send request if yes (then display layers grid).
               */
              scope.$watchCollection('stateObj.selectRecords', function() {
                if (!angular.isUndefined(scope.stateObj.selectRecords) &&
                    scope.stateObj.selectRecords.length > 0) {
                  var md = new Metadata(scope.stateObj.selectRecords[0]);
                  var links = md.getLinksByType('OGC:WMS');

                  if (scope.mode == 'attachService') {

                    if (angular.isArray(links) && links.length == 1) {
                      scope.loadWMSCapabilities(links[0].url);
                      scope.srcParams.uuid = md.getUuid();
                    }
                  }
                  else {
                    scope.layers = links;
                    scope.srcParams.refuuid = md.getUuid();
                  }
                }
              });

              /**
               * Call 2 services:
               *  - link a dataset to a service
               *  - link a service to a dataset
               * Hide modal on success.
               */
              scope.linkTo = function() {
                if (scope.srcParams.uuid) {
                  gnOnlinesrc.linkToDataset(scope.srcParams)
                    .then(function() {
                        gnOnlinesrc.linkToService(scope.srcParams)
                        .then(function() {
                              $('#linktoservice-popup').modal('hide');
                            });
                      });
                }
              };
            }
          };
        }]);
})();
