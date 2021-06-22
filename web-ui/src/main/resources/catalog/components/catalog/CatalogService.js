/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
    'Metadata',
    function($http, $location, $timeout, gnUrlUtils, Metadata) {
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
          return $http.delete('../api/records/' + id);
        },

        /**
         * @ngdoc method
         * @name gnMetadataManager#validate
         * @methodOf gnMetadataManager
         *
         * @description
         * Validate a metadata from catalog
         *
         * @param {string} id Internal id of the metadata
         * @return {HttpPromise} Future object
         */
        validate: function(id) {
          return $http.put('../api/records/' + id + '/validate/internal');
        },

        /**
         * @ngdoc method
         * @name gnMetadataManager#validateDirectoryEntry
         * @methodOf gnMetadataManager
         *
         * @description
         * Validate a directory entry (shared object) from catalog
         *
         * @param {string} id Internal id of the directory entry
         * @param {bool} newState true is validated, false is rejected
         * @return {HttpPromise} Future object
         */
        validateDirectoryEntry: function(id, newState) {
          var param = '?isvalid=' + (newState ? 'true' : 'false');
          return $http.put('../api/records/' + id + '/validate/internal' + param);
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
           * @param {boolean|string} isTemplate type of the metadata (bool is
           *  for TEMPLATE, other values are SUB_TEMPLATE and
           *  TEMPLATE_OF_SUB_TEMPLATE)
           * @param {boolean} isChild is child of a parent metadata
           * @param {string} metadataUuid , the uuid of the metadata to create
           *                 (when metadata uuid is set to manual)
           * @param {boolean} hasCategoryOfSource copy categories from source
           * @return {HttpPromise} Future object
           */
        copy: function(id, groupId, withFullPrivileges,
            isTemplate, isChild, metadataUuid, hasCategoryOfSource) {
          // new md type determination
          var mdType;
          switch (isTemplate) {
            case 'TEMPLATE_OF_SUB_TEMPLATE':
              mdType = 'TEMPLATE_OF_SUB_TEMPLATE';
              break;

            case 'SUB_TEMPLATE':
              mdType = 'SUB_TEMPLATE';
              break;

            case 'TEMPLATE':
            case true:
              mdType = 'TEMPLATE';
              break;

            default: mdType = 'METADATA';
          }

          var url = gnUrlUtils.toKeyValue({
            metadataType: mdType,
            sourceUuid: id,
            isChildOfSource: isChild ? 'true' : 'false',
            group: groupId,
            isVisibleByAllGroupMembers: withFullPrivileges ? 'true' : 'false',
            targetUuid: metadataUuid || '',
            hasCategoryOfSource: hasCategoryOfSource ? 'true' : 'false'
          });
          return $http.put('../api/records/duplicate?' + url, {
            headers: {
              'Accept': 'application/json'
            }
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
        importFromXml: function(urlParams, xml) {
          return $http.put('../api/records?' + urlParams, xml, {
            headers: {
              'Content-Type': 'application/xml'
            }
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
           * @param {string} tab is the metadata editor tab to open
           * @param {string} metadataUuid , the uuid of the metadata to create
           *                 (when metadata uuid is set to manual)
           * @param {boolean} hasCategoryOfSource copy categories from source
           * @return {HttpPromise} Future object
           */
        create: function(id, groupId, withFullPrivileges,
            isTemplate, isChild, tab, metadataUuid, hasCategoryOfSource) {

          return this.copy(id, groupId, withFullPrivileges,
              isTemplate, isChild, metadataUuid, hasCategoryOfSource)
              .success(function(id) {
                var path = '/metadata/' + id;
                if (tab) {
                  path += '/tab/' + tab;
                }
                $location.path(path)
                .search('justcreated')
                .search('redirectUrl', 'catalog.edit');
              });
        },

        /**
         * @ngdoc method
         * @name gnMetadataManager#getMdObjByUuid
         * @methodOf gnMetadataManager
         *
         * @description
         * Get the metadata js object from catalog. Trigger a search and
         * return a promise.
         * @param {string} uuid or id of the metadata
         * @param {array} isTemplate optional isTemplate value (y, n, s, t...)
         * @return {HttpPromise} of the $http post
         */
        getMdObjByUuid: function(uuid, isTemplate) {
          return $http.post('../api/search/records/_search', {"query": {
              "bool" : {
                "must": [
                  {"multi_match": {
                      "query": uuid,
                      "fields": ['id', 'uuid']}},
                  {"terms": {"isTemplate": isTemplate !== undefined ? isTemplate : ['n']}},
                  {"terms": {"draft": ["n", "y", "e"]}}
                ]
              }
            }}).then(function(r) {
              if (r.data.hits.total.value > 0) {
                return new Metadata(r.data.hits.hits[0]);
              } else {
                console.warn("Record with UUID/ID " + uuid + " not found.")
              }
              });
        },

        /**
         * @ngdoc method
         * @name gnMetadataManager#getMdObjById
         * @methodOf gnMetadataManager
         *
         * @description
         * Get the metadata js object from catalog. Trigger a search and
         * return a promise.
         * @param {string} id of the metadata
         * @param {array} isTemplate optional isTemplate value (y, n, s, t...)
         * @return {HttpPromise} of the $http post
         */
        getMdObjById: function(id, isTemplate) {
          return this.getMdObjByUuid(id, isTemplate);
        },

        /**
         * @ngdoc method
         * @name gnMetadataManager#updateMdObj
         * @methodOf gnMetadataManager
         *
         * @description
         * Update the metadata object
         *
         * @param {object} md to reload
         * @return {HttpPromise} of the $http get
         */
        updateMdObj: function(md) {
          return this.getMdObjByUuid(md.uuid).then(
              function(md_) {
                angular.extend(md, md_);
                return md;
              }
          );
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
   */

  module.value('gnHttpServices', {
    mdGetXML19139: 'xml_iso19139',

    publish: 'md.publish',
    unpublish: 'md.unpublish',

    processAll: 'md.processing.batch',
    processReport: 'md.processing.batch.report',
    processXml: 'xml.metadata.processing',

    suggest: 'suggest',

    search: 'q',
    internalSearch: 'qi',
    subtemplate: 'subtemplate',
    lang: 'lang?_content_type=json&',
    removeThumbnail: 'md.thumbnail.remove?_content_type=json&',
    removeOnlinesrc: 'resource.del.and.detach', // TODO: CHANGE
    suggest: 'suggest',
    selectionLayers: 'selection.layers',

    featureindexproxy: '../../index/features',
    indexproxy: '../../index/records'
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
              url: gnHttpServices[serviceKey] || serviceKey,
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
          isInspireEnabled: 'system.inspireValidation.enable',
          isRatingUserFeedbackEnabled: 'system.localratinguserfeedback.enable',
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
      isInspireEnabled: 'system.inspire.enable',
      isRatingUserFeedbackEnabled: 'system.localrating.enable',
      isSearchStatEnabled: 'system.searchStats.enable',
      isHideWithHelEnabled: 'system.hidewithheldelements.enable'
    },
    'map.is3DModeAllowed': window.location.search.indexOf('with3d') !== -1
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
    '$http', '$q',
    'gnConfig',
    function($http, $q, gnConfig) {
      var defer = $q.defer();
      var loadPromise = defer.promise;
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
          return $http.get('../api/site/settings', {cache: true})
              .then(function(response) {
                angular.extend(gnConfig, response.data);
                // Replace / by . in settings name
                angular.forEach(gnConfig, function(value, key) {
                  if (key.indexOf('/') !== -1) {
                    gnConfig[key.replace(/\//g, '.')] = value;
                    delete gnConfig[key];
                  }
                });
                // Override parameter if set in URL
                if (window.location.search.indexOf('with3d') !== -1) {
                  gnConfig['map.is3DModeAllowed'] = true;
                }
                defer.resolve(gnConfig);
              }, function() {
                defer.reject();
              });
        },
        loadPromise: loadPromise,

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
        getServiceURL: function(useDefaultNode) {
          var port = '';
          if (gnConfig['system.server.protocol'] === 'http' &&
             gnConfig['system.server.port'] &&
             gnConfig['system.server.port'] != null &&
             gnConfig['system.server.port'] != 80) {

            port = ':' + gnConfig['system.server.port'];

          } else if (gnConfig['system.server.protocol'] === 'https' &&
             gnConfig['system.server.securePort'] &&
             gnConfig['system.server.securePort'] != null &&
             gnConfig['system.server.securePort'] != 443) {

            port = ':' + gnConfig['system.server.securePort'];

          }

          var node = (!useDefaultNode?
            gnConfig.env.node:gnConfig.env.defaultNode);

          var url = gnConfig['system.server.protocol'] + '://' +
              gnConfig['system.server.host'] + port +
              gnConfig.env.baseURL + '/' +
              node + '/';
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
  module.factory('Metadata', ['gnLangs', '$translate',
    function(gnLangs, $translate) {
    function Metadata(k) {
      // Move _source properties to the root.
      var source = k._source;
      delete k._source;
      $.extend(true, this, k, source);

      var record = this;

      // See EsSearchManager#documentToJson to define fields as array.
      // var listOfArrayFields = ; // Except for geom
      if (angular.isDefined(record.geom) &&
        !angular.isArray(record.geom)) {
        record.geom = [record.geom];
      }

      // Multilingual fields
      $.each(this, function(key, value) {
        var fieldName = key;
        // Object fields and codelist are storing translations.
        // Create a field with the UI translation or fallback to default.
        if (key.endsWith('Object') || key.indexOf('cl_') === 0) {
          record.translate(fieldName);
        }
      });

      // See below; probably not necessary
      this.linksCache = [];

      this.getAllContacts();
    };


    Metadata.prototype = {
      // For codelist, default property is replaced
      // For Object, a new field is created without the Object suffix.
      translate: function(fieldName) {
        var fieldValues = this[fieldName],
          isCodelist = fieldName.indexOf('cl_') === 0;

        // In object lang prop, in translations, default prop.
        function getCodelistTranslation(o) {
          if (o['lang' + gnLangs.current]) {
            return o['lang' + gnLangs.current];
          } else if ($translate.instant(o.key) != o.key) {
            return $translate.instant(o.key);
          }
          return o.default;
        }

        if (angular.isArray(fieldValues)) {
          var translatedValues = [];
          angular.forEach(fieldValues, function(o) {
            if (isCodelist) {
              o.default = getCodelistTranslation(o);
            } else {
              translatedValues.push(o['lang' + gnLangs.current] || o.default);
            }
          });
          if (!isCodelist) {
            this[fieldName.slice(0, -6)] = translatedValues;
          }
        } else if (angular.isObject(fieldValues)) {
          if(isCodelist) {
            o.default = getCodelistTranslation(fieldValues)
          } else {
            this[fieldName.slice(0, -6)] =
              fieldValues['lang' + gnLangs.current] || fieldValues.default;
          }
        } else {
          console.warn(fieldName + ' is not defined in this record.');
        }
      },
      isPublished: function() {
        return JSON.parse(this.isPublishedToAll) === true;
      },
      isValid: function() {
        return this.valid === '1';
      },
      hasValidation: function() {
        return (this.valid > -1);
      },
      isOwned: function() {
        return this.owner === 'true';
      },
      getOwnerId: function() {
        return this.ownerId;
      },
      getGroupOwner: function() {
        return this.owner;
      },
      getSchema: function() {
        return this.schema;
      },
      publish: function() {
        this.isPublishedToAll = this.isPublished() ?
            false : true;
      },
      getFields: function(filter) {
        var values = {}, props = this, keys = Object.keys(this)
          .filter(function(name) {return new RegExp(filter).test(name)});
        keys.forEach(function (k) {
          values[k] = props[k];
        });
        return values;
      },
      getLinks: function() {
        return this.link;
      },
      getLinkGroup: function(layer) {
        var links = this.getLinksByType('OGC');
        for (var i = 0; i < links.length; ++i) {
          var link = links[i];
          if (link.name == layer.getSource().getParams().LAYERS) {
            return link.group;
          }
        }
      },
      /**
       * Get all links of the metadata of the given types.
       * The types are strings in arguments.
       * You can give the exact matching with # ('#OG:WMS') or just find an
       * occurence for the match ('OGC').
       * You can passe several types to find ('OGC','WFS', '#getCapabilities')
       *
       * If the first argument is a number, you do the search within the link
       * group (search only onlinesrc in the given transferOptions).
       *
       * @return {*} an Array of links
       */
      getLinksByType: function() {
        var ret = [];

        var types = Array.prototype.splice.call(arguments, 0);
        var groupId;

        var key = types.join('|');
        if (angular.isNumber(types[0])) {
          groupId = types[0];
          types.splice(0, 1);
        }
        if (this.linksCache[key] && groupId === undefined) {
          return this.linksCache[key];
        }
        angular.forEach(this.link, function(link) {
          if (types.length > 0) {
            types.forEach(function(type) {
              if (type.substr(0, 1) == '#') {
                var protocolMatch = link.protocol == type.substr(1, type.length - 1);
                if ((protocolMatch && groupId === undefined) ||
                    (protocolMatch && groupId != undefined && groupId == link.group)) {
                  ret.push(link);
                }
              }
              else {
                if (link.protocol.toLowerCase().indexOf(
                    type.toLowerCase()) >= 0 &&
                    (!groupId || groupId == link.group)) {
                  ret.push(link);
                }
              }
            });
          } else {
            ret.push(link);
          }
        });
        this.linksCache[key] = ret;
        return ret;
      },
      /**
       * Return an object containing metadata contacts
       * as an array and resource contacts as array
       *
       * @return {{metadata: Array, resource: Array, distribution: Array}}
       */
      getAllContacts: function() {
        this.allContacts = {metadata:[], resource:[]};
        if (this.contact && this.contact.length > 0){
          this.allContacts.metadata = this.contact;
        }
        if (this.contactForResource && this.contactForResource.length > 0){
          this.allContacts.resource = this.contactForResource;
        }
        if (this.contactForDistribution && this.contactForDistribution.length > 0){
          this.allContacts.distribution = this.contactForDistribution;
        }
        return this.allContacts;
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
      },
      isWorkflowEnabled: function() {
        var st = this.mdStatus;
        var res = st &&
            //Status is unknown
            (!isNaN(st) && st != '0');

        //What if it is an array: gmd:MD_ProgressCode
        if (!res && Array.isArray(st)) {
          angular.forEach(st, function(s) {
            if (!isNaN(s) && s != '0') {
              res = true;
            }
          });
        }
        return res;
      }
    };
    return Metadata;
  }]);


})();
