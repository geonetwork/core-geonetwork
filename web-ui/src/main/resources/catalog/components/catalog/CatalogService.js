(function() {
  goog.provide('gn_catalog_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_catalog_service', [
    'gn_urlutils_service'
  ]);

  // TODO: move to metadatamanger
  // TODO: rename this is more than new metadata
  module.provider('gnNewMetadata', function() {
    this.$get = ['$http', '$location', '$timeout', 'gnUrlUtils',
                 function($http, $location, $timeout, gnUrlUtils) {
        return {
          deleteMetadata: function(id) {
            var url = gnUrlUtils.append('md.delete@json',
                gnUrlUtils.toKeyValue({
                  id: id
                })
                );
            return $http.get(url);
          },
          copyMetadata: function(id, groupId, withFullPrivileges, 
              isTemplate, tab, isChild) {
            var url = gnUrlUtils.append('md.create@json',
                gnUrlUtils.toKeyValue({
                  group: groupId,
                  id: id,
                  template: isTemplate ? 'y' : 'n',
                  child: isChild ? 'y' : 'n',
                  fullPrivileges: withFullPrivileges ? 'true' : 'false'
                })
                );
            return $http.get(url);
          },
          importMetadata: function(data) {
            return $http({
              url: 'md.insert@json',
              method: 'POST',
              data: $.param(data),
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            });
          },
          createNewMetadata: function(id, groupId, withFullPrivileges, 
              isTemplate, tab, isChild) {
            //            $http.get(url).success(function(data) {
            //              // TODO: If using NRT in Lucene, the record
            //              // will not be indexed straight away. Add a
            //              // timeout for the index to be reopened
            //              //
            //              // A better approach could be to be able
            //              // to force a reopen on the editor search
            //              // which check if the record exist
            //              // or
            //              // do not use timeout if NRT is not used
            //              // in config-lucene.xml
            //              $timeout(function () {
            //                $location.path('/metadata/' + data.id);
            //              }, 1000)
            //
            //            });
            // NRT is turned off by default.
            this.copyMetadata(id, groupId, withFullPrivileges,
                isTemplate, tab, isChild).success(function(data) {
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
    info: 'info@json',
    search: 'qi@json',
    processMd: 'md.processing',
    processAll: 'md.processing.batch',
    processXml: 'xml.metadata.processing@json', // TODO: CHANGE
    getRelations: 'md.relations@json',
    getValidation: 'md.validate@json',
    subtemplate: 'subtemplate',
    lang: 'lang@json',
    country: 'regions.list@json?categoryId=' +
        'http://geonetwork-opensource.org/regions%23country',
    regionsList: 'regions.category.list@json',
    region: 'regions.list@json',
    removeThumbnail: 'md.thumbnail.remove@json',
    removeOnlinesrc: 'resource.del.and.detach', // TODO: CHANGE
    suggestionsList: 'md.suggestion@json',
    geoserverNodes: 'geoserver.publisher@json' // TODO: CHANGE

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
    'gnCurrentEdit',
    '$q',
    function(gnHttp, gnEditor, gnCurrentEdit, $q) {

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
          if (!params.id && !params.uuid) {
            angular.extend(params, {
              id: gnCurrentEdit.id
            });
          }
          var defer = $q.defer();
          gnEditor.save(false, true)
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


  /**
   * Store the catalog config loaded by the gnConfigService.
   */
  module.value('gnConfig', {
    key: {
      isXLinkEnabled: 'system.xlinkResolver.enable',
      isSelfRegisterEnabled: 'system.userSelfRegistration.enable',
      isFeedbackEnabled: 'system.userFeedback.enable',
      isSearchStatEnabled: 'system.searchStats.enable',
      isHideWithHelEnabled: 'system.hidewithheldelements.enable'
    }
  });

  /**
   * Load the catalog config and push it to gnConfig.
   */
  module.factory('gnConfigService', [
    '$q',
    'gnHttp',
    'gnConfig',
    function($q, gnHttp, gnConfig) {
      return {
        /**
          * Get catalog configuration. The config is cached.
          * Boolean value are parsed to boolean.
          */
        load: function() {
          var defer = $q.defer();
          gnHttp.callService('info',
              {type: 'config'},
              {cache: true}).then(function(response) {
            angular.forEach(response.data, function(value, key) {
              if (value == 'true' || value == 'false') {
                response.data[key] = value === 'true';
              }
            });
            angular.extend(gnConfig, response.data);
            defer.resolve(gnConfig);
          });
          return defer.promise;
        }
      };
    }]);
})();
