(function() {
  goog.provide('gn_schema_manager_service');

  var module = angular.module('gn_schema_manager_service', []);

  module.value('gnNamespaces', {
    gmd: 'http://www.isotc211.org/2005/gmd',
    gco: 'http://www.isotc211.org/2005/gco',
    gfc: 'http://www.isotc211.org/2005/gfc',
    gml: 'http://www.opengis.net/gml',
    gmx: 'http://www.isotc211.org/2005/gmx',
    gsr: 'http://www.isotc211.org/2005/gsr',
    gss: 'http://www.isotc211.org/2005/gss',
    gts: 'http://www.isotc211.org/2005/gts',
    srv: 'http://www.isotc211.org/2005/srv',
    xlink: 'http://www.w3.org/1999/xlink',
    mri: 'http://www.isotc211.org/2005/mri/1.0/2013-06-24'
  });

  /**
   * Map of elements used when retrieving codelist
   * according to the metadata schema.
   */
  module.value('gnElementsMap', {
    protocol: {
      'iso19139': 'gmd:protocol',
      'iso19115-3': 'cit:protocol'
    },
    roleCode: {
      'iso19139': 'gmd:CI_RoleCode',
      'iso19115-3': 'cit:CI_RoleCode'
    },
    associationType: {
      'iso19139': 'gmd:DS_AssociationTypeCode',
      'iso19115-3': 'mri:DS_AssociationTypeCode'
    },
    initiativeType: {
      'iso19139': 'gmd:DS_InitiativeTypeCode',
      'iso19115-3': 'mri:DS_InitiativeTypeCode'
    }
  });

  module.factory('gnSchemaManagerService',
      ['$q', '$http', '$cacheFactory',
       function($q, $http, $cacheFactory) {
         /**
          * Cache field info and codelist info
          *
          * TODO: Maybe we could improve caching ?
          * On page load, many codelist are retrieved
          * and the first one is not returned before
          * others are requested and as such are not
          * yet populated in the cache. Not sure how
          * this could be improved ?
          */
         var infoCache = $cacheFactory('infoCache');

         return {
           getCodelist: function(config) {
             //<request><codelist schema="iso19139" name="gmd:CI_RoleCode"/>
             var defer = $q.defer();
             var fromCache = infoCache.get(config);
             if (fromCache) {
               defer.resolve(fromCache);
             } else {
               var getPostRequestBody = function() {
                 var info = config.split('|'),
                 requestBody = '<request><codelist schema="' + info[0] +
                 '" name="' + info[1] +
                 '" /></request>';
                 return requestBody;
               };

               $http.post('md.element.info@json', getPostRequestBody(), {
                 headers: {'Content-type': 'application/xml'}
               }).
               success(function(data) {
                 infoCache.put(config, data);
                 defer.resolve(data);
               });
             }
             return defer.promise;
           },
           /**
            * Retrieve field information (ie. name, description, helpers).
            * Information are cached in the infoCache.
            *
            * Return a promise.
            */
           getElementInfo: function(config) {
             //<request>
             //  <element schema="iso19139"
             //   name="gmd:geometricObjectType"
             //   context="gmd:MD_GeometricObjects"
             //   fullContext="xpath"
             //   isoType="" /></request>
             var defer = $q.defer();
             var fromCache = infoCache.get(config);
             if (fromCache) {
               defer.resolve(fromCache);
             } else {
               var getPostRequestBody = function() {
                 var info = config.split('|');
                 var requestBody = null;

                 // Check at least element name is defined
                 // to get information about that element.
                 if (info[1] !== '') {
                   requestBody = '<request><element schema="' + info[0] +
                   '" name="' + info[1] +
                   '" context="' + (info[2] || '') +
                   '" fullContext="' + (info[3] || '') +
                   '" isoType="' + (info[4] || '') + '" /></request>';
                 }
                 return requestBody;
               };

               var requestBody = getPostRequestBody();
               if (requestBody === null) {
                 defer.reject({error: 'Invalid config.', config: config});
               } else {
                 $http.post('md.element.info@json', requestBody, {
                   headers: {'Content-type': 'application/xml'}
                 }).
                 success(function(data) {
                   infoCache.put(config, data);
                   defer.resolve(data);
                 });
               }
             }
             return defer.promise;
           }
         };
       }]);
})();
