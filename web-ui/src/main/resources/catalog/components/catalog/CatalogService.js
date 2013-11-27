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
    search: 'qi@json',
    processMd : 'md.processing',
    processAll : 'md.processing.batch'
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
            console.log(originUrl);
            return $http(config);
          }
        };
      }];
  });

  module.factory('gnBatchProcessing', [
    'gnHttp',
    'gnMetadataManagerService',
    
     function(gnHttp,gnMetadataManagerService) {

        var processing = true;
        var processReport = null;
        return {

          /**
           * Run process md.processing on the edited
           * metadata after the form has been saved.
           * Then refresh the editor.
           * 
           * Return a promise, called after the form
           * refresh
           */
          runProcessMd: function(params) {
            angular.extend(params, {
              id: gnMetadataManagerService.getCurrentEdit().metadataId
            });
            return gnMetadataManagerService.save()
                .then(function() {
                  gnHttp.callService('processMd', params).then(function(data) {
                    console.log('md.processing.new success');
                    gnMetadataManagerService.refreshEditorForm($(data.data));
                  });
                });
          }

          // TODO : write batch processing service here
          // from adminTools controller
        };
      }]);
})();
