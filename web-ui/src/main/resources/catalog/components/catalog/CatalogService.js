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
          var url = gnUrlUtils.append('md.create',
              gnUrlUtils.toKeyValue({
                _content_type: 'json',
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
            url: 'md.insert?_content_type=json',
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
         * @name gnMetadataManager#importFromXml
         * @methodOf gnMetadataManager
         *
         * @description
         * Import records from a xml string.
         *
         * @param {Object} data Params to send to md.insert service
         * @return {HttpPromise} Future object
         */
        importFromXml: function(data) {
          return $http.post('md.insert?_content_type=json', data, {
            headers: {'Content-Type':
                  'application/x-www-form-urlencoded'}
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
            isTemplate, isChild, tab) {
          return this.copy(id, groupId, withFullPrivileges,
              isTemplate, isChild).success(function(data) {
            var path = '/metadata/' + data.id;
            if (tab) {
              path += '/tab/' + tab;
            }
            $location.path(path);
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
    mdInsert: 'md.insert@json',
    mdDelete: 'md.delete@json',
    mdDeleteBatch: 'md.delete.batch',
    mdEdit: 'md.edit@json',
    mdEditSave: 'md.edit.save@json',
    mdEditSaveonly: 'md.edit.saveonly@json',
    mdEditSaveandclose: 'md.edit.save.and.close@json',
    mdEditCancel: 'md.edit.cancel@json',
    getRelations: 'md.relations@json',
    suggestionsList: 'md.suggestion@json',
    getValidation: 'md.validate@json',
    mdSelect: 'metadata.select?_content_type=json', // TODO: CHANGE

    mdGetPDF: 'pdf',
    mdGetPDFSelection: 'pdf.selection.search', // TODO: CHANGE
    mdGetRDF: 'rdf.metadata.get',
    mdGetMEF: 'mef.export',
    mdGetXML19139: 'xml_iso19139',
    csv: 'csv.search',

    mdPrivileges: 'md.privileges.update@json',
    mdPrivilegesBatch: 'md.privileges.batch.update@json',
    publish: 'md.publish',
    unpublish: 'md.unpublish',

    processMd: 'md.processing',
    processAll: 'md.processing.batch',
    processReport: 'md.processing.batch.report',
    processXml: 'xml.metadata.processing',

    info: 'info@json',

    country: 'regions.list@json?categoryId=' +
        'http://geonetwork-opensource.org/regions%23country',
    regionsList: 'regions.category.list@json',
    region: 'regions.list@json',

    suggest: 'suggest',

    edit: 'md.edit',
    search: 'q',
    internalSearch: 'qi',
    subtemplate: 'subtemplate',
    lang: 'lang@json',
    removeThumbnail: 'md.thumbnail.remove@json',
    removeOnlinesrc: 'resource.del.and.detach', // TODO: CHANGE
    geoserverNodes: 'geoserver.publisher@json', // TODO: CHANGE
    suggest: 'suggest',
    facetConfig: 'search/facet/config'
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
          },

          /**
           * Return service url for a given key
           * @param {string} serviceKey
           * @return {*}
           */
          getService: function(serviceKey) {
            return gnHttpServices[serviceKey];
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
      isXLinkLocal: 'system.xlinkResolver.localXlinkEnable',
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
      $.extend(true, this, k);
      var listOfArrayFields = ['topicCat', 'category'];
      var record = this;
      $.each(listOfArrayFields, function (idx) {
        var field = listOfArrayFields[idx];
        if (angular.isDefined(record[field]) &&
            !angular.isArray(record[field])) {
          record[field] = [record[field]];
        }
      });
    };

    function formatLink(sLink) {
      var linkInfos = sLink.split('|');
      return {
        name: linkInfos[0],
        url: linkInfos[2],
        desc: linkInfos[1],
        protocol: linkInfos[3],
        contentType: linkInfos[4]
      };
    }
    function parseLink(sLink) {

    };

    Metadata.prototype = {
      getUuid: function() {
        return this['geonet:info'].uuid;
      },
      getId: function() {
        return this['geonet:info'].id;
      },
      isPublished: function() {
        return this['geonet:info'].isPublishedToAll === 'true';
      },
      publish: function() {
        this['geonet:info'].isPublishedToAll = this.isPublished() ?
            'false' : 'true';
      },
      getLinks: function() {
        return this.link;
      },
      getLinksByType: function() {
        var ret = [];
        var types = Array.prototype.splice.call(arguments, 0);
        angular.forEach(this.link, function(link) {
          var linkInfo = formatLink(link);
          types.forEach(function(type) {
            if (type.substr(0, 1) == '#') {
              if (linkInfo.protocol == type.substr(1, type.length - 1)) {
                ret.push(linkInfo);
              }
            }
            else {
              if (linkInfo.protocol.indexOf(type) >= 0) {
                ret.push(linkInfo);
              }
            }
          });
        });
        return ret;
      },
      getThumbnails: function() {
        if (angular.isArray(this.image)) {
          var images = {list: []};
          for (var i = 0; i < this.image.length; i++) {
            var s = this.image[i].split('|');
            var insertFn = 'push';
            if (s[0] === 'thumbnail') {
              images.small = s[1];
              var insertFn = 'unshift';
            } else if (s[0] === 'overview') {
              images.big = s[1];
            }
            images.list[insertFn]({url: s[1], label: s[2]});
          }
        }
        return images;
      },
      getContacts: function() {
        if (angular.isArray(this.responsibleParty)) {
          var ret = {};
          for (var i = 0; i < this.responsibleParty.length; i++) {
            var s = this.responsibleParty[i].split('|');
            if (s[1] === 'resource') {
              ret.resource = s[2];
            } else if (s[1] === 'metadata') {
              ret.metadata = s[2];
            }
          }
        }
        return ret;
      },
      getBoxAsPolygon: function() {
        // Polygon((4.6810%2045.9170,5.0670%2045.9170,5.0670%2045.5500,4.6810%2045.5500,4.6810%2045.9170))
        if (this.geoBox) {
          var coords = this.geoBox.split('|');
          return 'Polygon((' +
              coords[0] + ' ' +
              coords[1] + ',' +
              coords[2] + ' ' +
              coords[1] + ',' +
              coords[2] + ' ' +
              coords[3] + ',' +
              coords[0] + ' ' +
              coords[3] + ',' +
              coords[0] + ' ' +
              coords[1] + '))';
        } else {
          return null;
        }
      },
      getOwnername: function() {
        if (this.userinfo) {
          var userinfo = this.userinfo.split('|');
          try {
            if (userinfo[2] !== userinfo[1]) {
              return userinfo[2] + ' ' + userinfo[1];
            } else {
              return userinfo[1];
            }
          } catch (e) {
            return '';
          }
        } else {
          return '';
        }
      }
    };
    return Metadata;
  });


})();
