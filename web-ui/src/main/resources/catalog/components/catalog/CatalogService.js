(function() {
  goog.provide('gn_catalog_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_catalog_service', [
    'gn_urlutils_service'
  ]);

  module.provider('gnNewMetadata', function() {
    this.$get = ['$http', '$location', 'gnUrlUtils',
                 function($http, $location, gnUrlUtils) {
        return {
          // TODO: move to metadatamanger
          createNewMetadata: function(id, groupId, fullPrivileges, 
              template, tab) {
            var url = gnUrlUtils.append('md.create@json',
                gnUrlUtils.toKeyValue({
                  group: groupId,
                  id: id,
                  isTemplate: template || 'n',
                  fullPrivileges: fullPrivileges || true
                })
                );

            $http.get(url).success(function(data) {
              $location.path('/metadata/' + data.id);
            });
            // TODO : handle creation error
          }
        };
      }];
  });

  module.value('gnHttpServices', {
    mdCreate: 'md.create@json',
    edit: 'md.edit',
    search: 'qi@json',
    processMd: 'md.processing',
    processAll: 'md.processing.batch',
    processXml: 'xml.metadata.processing@json', // TODO: CHANGE
    getRelations: 'md.relations.get@json',
    removeThumbnail: 'md.thumbnail.remove@json',
    removeOnlinesrc: 'resource.del.and.detach' // TODO: CHANGE
  });

  module.provider('gnHttp', function() {

    this.$get = ['$http', 'gnHttpServices' , '$location', 'gnUrlUtils',
      function($http, gnHttpServices, $location, gnUrlUtils) {

        var originUrl = this.originUrl = gnUrlUtils.urlResolve(
            window.location.href, true);

        var defaults = this.defaults = {
          host: originUrl.host,
          pathname: originUrl.pathname,
          protocol: originUrl.protocol
        };

        var urlSplit = originUrl.pathname.split('/');
        if (urlSplit.lenght < 3) {
          //TODO manage error
        }
        else {
          angular.extend(defaults, {
            webapp: urlSplit[1],
            srv: urlSplit[2],
            lang: urlSplit[3]
          });
        }
        return {
          callService: function(serviceKey, params, httpConfig) {

            var config = {
              url: gnHttpServices[serviceKey],
              params: params,
              method: 'GET'
            };
            angular.extend(config, httpConfig);
            return $http(config);
          }
        };
      }];
  });

  // TODO: Move to editor module (because it's not needed
  // for search apps for example)
  module.factory('gnBatchProcessing', [
    'gnHttp',
    'gnEditor',
    '$q',
    function(gnHttp, gnEditor, $q) {

      var processing = true;
      var processReport = null;
      return {

        /**
         * Run process md.processing on the edited
         * metadata after the form has been saved.
         *
         * Return a promise called the batch has been
         * processed
         */
        runProcessMd: function(params) {
          angular.extend(params, {
            id: gnEditor.getCurrentEdit().metadataId
          });
          var defer = $q.defer();
          gnEditor.save()
                .then(function() {
                gnHttp.callService('processMd', params).then(function(data) {
                  defer.resolve(data);
                });
              });
          return defer.promise;
        },

        runProcessMdXml: function(params) {
          return gnHttp.callService('processXml', params);
        }


        // TODO : write batch processing service here
        // from adminTools controller
      };
    }]);
})();
