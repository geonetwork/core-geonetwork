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

              /**
               * Calls service 'relations.get' to load
               * all online resources of the current
               * metadata into the list
               */
              var loadRelations = function() {
                gnOnlinesrc.getAllResources()
                .then(function(data) {
                      scope.relations = data;
                    });
              };

              // Load all relations on form init
              loadRelations();

              // Reload relations when a directive requires it
              scope.$watch('onlinesrcService.reload', function() {
                if (scope.onlinesrcService.reload) {
                  loadRelations();
                  scope.onlinesrcService.reload = false;
                  console.log('## reload relations');
                }
              });
            }
          };
        }])
   .directive('gnAddThumbnail', [
        'gnOnlinesrc',
        'gnEditor',
        'gnCurrentEdit',
        'gnOwsCapabilities',
        function(gnOnlinesrc, gnEditor, gnCurrentEdit, gnOwsCapabilities) {
          return {
            restrict: 'A',
            templateUrl: '../../catalog/components/edit/onlinesrc/' +
                'partials/addThumbnail.html',
            scope: {},
            link: function(scope, element, attrs) {
              scope.metadataId = gnCurrentEdit.id;

              // mode can be 'url' or 'upload'
              scope.mode = 'url';

              // the form params that will be submited
              scope.params = {};

              // TODO: should be in gnEditor ?
              var getVersion = function() {
                return scope.params.version = gnCurrentEdit.version;
              };

              /**
               * Onlinesrc uploaded with success, close the popup,
               * refresh the metadata form.
               * Callback of the submit().
               */
              var uploadOnlinesrcDone = function(evt, data) {
                gnEditor.refreshEditorForm();
                gnOnlinesrc.reload = true;
              };

              /**
               * Onlinesrc uploaded with error, broadcast it.
               */
              var uploadOnlineSrcError = function(data) {
              };

              // upload directive options
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
                  getVersion();
                  gnEditor.save()
                  .then(function(data) {
                        scope.submit();
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
        'gnEditor',
        function(gnOnlinesrc, gnOwsCapabilities, gnEditor) {
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
                scope.clear(scope.queue);
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

              scope.onAddSuccess = function() {
                gnEditor.refreshEditorForm();
                scope.onlinesrcService.reload = true;
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
