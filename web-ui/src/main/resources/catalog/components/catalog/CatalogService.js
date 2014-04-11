(function() {
  goog.provide('gn_catalog_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_catalog_service', [
    'gn_urlutils_service'
  ]);

  /**
   * @ngdoc service
   * @kind function
   * @name gnMetadataManager
   * @requires $http
   * @requires $location
   * @requires $timeout
   * @requires gnUrlUtils
   *
   * @description
   * The `gnMetadataManager` service provides main operations to manage
   * metadatas such as create, import, copy or delete.
   * Other operations like save are provided by another service `gnEditor`.
   */
  module.factory('gnMetadataManager', [
    '$http',
    '$location',
    '$timeout',
    'gnUrlUtils',
    function($http, $location, $timeout, gnUrlUtils) {
      return {
        //TODO: rewrite calls with gnHttp

        /**
           * @ngdoc method
           * @name gnMetadataManager#remove
           * @methodOf gnMetadataManager
           *
           * @description
           * Delete a metadata from catalog
           *
           * @param {string} id Internal id of the metadata
           * @return {HttpPromise} Future object
           */
        remove: function(id) {
          var url = gnUrlUtils.append('md.delete@json',
              gnUrlUtils.toKeyValue({
                id: id
              })
              );
          return $http.get(url);
        },

        /**
           * @ngdoc method
           * @name gnMetadataManager#copy
           * @methodOf gnMetadataManager
           *
           * @description
           * Create a copy of a metadata. The copy will belong to the same group
           * of the original metadata and will be of the same type (isTemplate,
           * isChild, fullPrivileges).
           *
           * @param {string} id Internal id of the metadata to be copied.
           * @param {string} groupId Internal id of the group of the metadata
           * @param {boolean} withFullPrivileges privileges to assign.
           * @param {boolean} isTemplate type of the metadata
           * @param {boolean} isChild is child of a parent metadata
           * @return {HttpPromise} Future object
           */
        copy: function(id, groupId, withFullPrivileges, 
            isTemplate, isChild) {
          var url = gnUrlUtils.append('md.create@json',
              gnUrlUtils.toKeyValue({
                group: groupId,
                id: id,
                template: isTemplate ? (isTemplate === 's' ? 's' : 'y') : 'n',
                child: isChild ? 'y' : 'n',
                fullPrivileges: withFullPrivileges ? 'true' : 'false'
              })
              );
          return $http.get(url);
        },

        /**
           * @ngdoc method
           * @name gnMetadataManager#import
           * @methodOf gnMetadataManager
           *
           * @description
           * Import a new from metadata from an XML snippet.
           *
           * @param {Object} data Params to send to md.insert service
           * @return {HttpPromise} Future object
           */
        importMd: function(data) {
          return $http({
            url: 'md.insert@json',
            method: 'POST',
            data: $.param(data),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          });
        },

        /**
         * @ngdoc method
         * @name gnMetadataManager#importFromDir
         * @methodOf gnMetadataManager
         *
         * @description
         * Import records from a directory on the server.
         *
         * @param {Object} data Params to send to md.import service
         * @return {HttpPromise} Future object
         */
        importFromDir: function(data) {
          return $http({
            url: 'md.import@json?' + data,
            method: 'GET'
          });
        },

        /**
           * @ngdoc method
           * @name gnMetadataManager#create
           * @methodOf gnMetadataManager
           *
           * @description
           * Create a new metadata as a copy of an existing template.
           * Will forward to `copy` method.
           *
           * @param {string} id Internal id of the metadata to be copied.
           * @param {string} groupId Internal id of the group of the metadata
           * @param {boolean} withFullPrivileges privileges to assign.
           * @param {boolean} isTemplate type of the metadata
           * @param {boolean} isChild is child of a parent metadata
           * @return {HttpPromise} Future object
           */
        create: function(id, groupId, withFullPrivileges, 
            isTemplate, isChild) {
          this.copy(id, groupId, withFullPrivileges,
              isTemplate, isChild).success(function(data) {
            $location.path('/metadata/' + data.id);
          });
          // TODO : handle creation error
        }
      };
    }
  ]);

  /**
   * @ngdoc service
   * @kind Object
   * @name gnHttpServices
   *
   * @description
   * The `gnHttpServices` service provides KVP for all geonetwork
   * services used in the UI.
   *
   *  FIXME : links are too long for JSLint.
   *
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdcreate mdCreate}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdview mdView}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdcreate mdCreate}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdinsert mdInsert}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mddelete mdDelete}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdedit mdEdit}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdeditsave mdEditSave}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-
   * mdeditsaveonly mdEditSaveonly}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-
   * mdeditsaveandclose mdEditSaveandclose}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdeditcancel mdEditCancel}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdrelations getRelations}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdsuggestion suggestionsList}
   * {@link service/config-ui-metadata#services-
   * documentation-config-ui-metadataxml_-service-mdvalidate getValidation}
   *
   * {@link service/config-service-admin-batchprocess
   * #services-documentation-config-service-admin-
   * batchprocessxml_-service-mdprocessing processMd}
   * {@link service/config-service-admin-batchprocess
   * #services-documentation-config-service-admin-
   * batchprocessxml_-service-mdprocessingbatch processAll}
   * {@link service/config-service-admin-batchprocess
   * #services-documentation-config-service-admin-
   * batchprocessxml_-service-mdprocessingbatchreport processReport}
   *
   * {@link service/config-service-admin#services-
   * documentation-config-service-adminxml_-service-info info}
   *
   * {@link service/config-service-region#services-
   * documentation-config-service-regionxml_-
   * service-regionscategory regionsList}
   * {@link service/config-service-region#services-
   * documentation-config-service-regionxml_-service-regionslist region}
   */

  module.value('gnHttpServices', {
    mdCreate: 'md.create@json',
    mdView: 'md.view@json',
    mdCreate: 'md.create@json',
    mdInsert: 'md.insert@json',
    mdDelete: 'md.delete@json',
    mdEdit: 'md.edit@json',
    mdEditSave: 'md.edit.save@json',
    mdEditSaveonly: 'md.edit.saveonly@json',
    mdEditSaveandclose: 'md.edit.save.and.close@json',
    mdEditCancel: 'md.edit.cancel@json',
    getRelations: 'md.relations@json',
    suggestionsList: 'md.suggestion@json',
    getValidation: 'md.validate@json',

    processMd: 'md.processing',
    processAll: 'md.processing.batch',
    processReport: 'md.processing.batch.report',
    processXml: 'xml.metadata.processing@json', // TODO: CHANGE

    info: 'info@json',

    country: 'regions.list@json?categoryId=' +
        'http://geonetwork-opensource.org/regions%23country',
    regionsList: 'regions.category.list@json',
    region: 'regions.list@json',


    edit: 'md.edit',
    search: 'qi@json',
    subtemplate: 'subtemplate',
    lang: 'lang@json',
    removeThumbnail: 'md.thumbnail.remove@json',
    removeOnlinesrc: 'resource.del.and.detach', // TODO: CHANGE
    geoserverNodes: 'geoserver.publisher@json' // TODO: CHANGE

  });

  /**
   * @ngdoc service
   * @kind function
   * @name gnHttp
   * @requires $http
   * @requires gnHttpServices
   * @requires $location
   * @requires gnUrlUtils

   * @description
   * The `gnHttp` service extends `$http` service
   * for geonetwork usage. It is based on `gnHttpServices` to
   * get service url.
   */
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

          /**
           * @ngdoc method
           * @name gnHttp#callService
           * @methodOf gnHttp
           *
           * @description
           * Calls a geonetwork service with given parameters
           * and an httpConfig
           * (that will be handled by `$http#get` method).
           *
           * @param {string} serviceKey key of the service to
           * get the url from `gnHttpServices`
           * @param {Object} params to add to the request
           * @param {Object} httpConfig see httpConfig of
           * $http#get method
           * @return {HttpPromise} Future object
           */
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

  /**
   * @ngdoc service
   * @kind Object
   * @name gnConfig
   *
   * @description
   * The `gnConfig` service provides KVP for all geonetwork
   * configuration settings that can be managed
   * in administration UI.
   * The `key` Object contains shortcut to full settings path.
   * The value are set in the `gnConfig` object.
   *
   * @example
     <code>
      {
        key: {
          isXLinkEnabled: 'system.xlinkResolver.enable',
          isSelfRegisterEnabled: 'system.userSelfRegistration.enable',
          isFeedbackEnabled: 'system.userFeedback.enable',
          isSearchStatEnabled: 'system.searchStats.enable',
          isHideWithHelEnabled: 'system.hidewithheldelements.enable'
        },
        isXLinkEnabled: true,
        system.server.host: 'localhost'

      }
     </code>
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
   * @ngdoc service
   * @kind function
   * @name gnConfigService
   * @requires $q
   * @requires gnHttp
   * @requires gnConfig
   *
   * @description
   * Load the catalog config and push it to gnConfig.
   */
  module.factory('gnConfigService', [
    '$q',
    'gnHttp',
    'gnConfig',
    function($q, gnHttp, gnConfig) {
      return {

        /**
         * @ngdoc method
         * @name gnConfigService#load
         * @methodOf gnConfigService
         *
         * @description
         * Get catalog configuration. The config is cached.
         * Boolean value are parsed to boolean.
         *
         * @return {HttpPromise} Future object
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
        },

        /**
         * @ngdoc method
         * @name gnConfigService#getServiceURL
         * @methodOf gnConfigService
         *
         * @description
         * Get service URL from configuration settings.
         * It is used by `gnHttp`service.
         *
         * @return {String} service url.
         */
        getServiceURL: function() {
          var url = gnConfig['system.server.protocol'] + '://' +
              gnConfig['system.server.host'] + ':' +
              gnConfig['system.server.port'] +
              gnConfig.env.baseURL + '/' +
              gnConfig.env.node + '/';
          return url;
        }
      };
    }]);

  /**
   * @ngdoc service
   * @kind function
   * @name Metadata
   *
   * @description
   * The `Metadata` service is a metadata wrapper from the jeeves
   * json output of the search service. It also provides some functions
   * on the metadata.
   */
  module.factory('Metadata', function() {
    function Metadata(k) {
      this.props = $.extend(true, {}, k);
    };

    function formatLink(sLink) {
      var linkInfos = sLink.split('|');
      return {
        name: linkInfos[1],
        url: linkInfos[2],
        desc: linkInfos[0],
        protocol: linkInfos[3],
        contentType: linkInfos[4]
      };
    }
    function parseLink(sLink) {

    };

    Metadata.prototype = {
      getUuid: function() {
        return this.props['geonet:info'].uuid;
      },
      getLinks: function() {
        return this.props.link;
      },
      getLinksByType: function(type) {
        var ret = [];
        angular.forEach(this.props.link, function(link) {
          var linkInfo = formatLink(link);
          if (linkInfo.protocol.indexOf(type) >= 0) {
            ret.push(linkInfo);
          }
        });
        return ret;
      }
    };
    return Metadata;
  });


})();
