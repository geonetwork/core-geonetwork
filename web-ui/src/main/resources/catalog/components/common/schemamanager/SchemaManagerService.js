(function() {
  goog.provide('gn_schema_manager_service');

  var module = angular.module('gn_schema_manager_service', []);

  module.factory('gnSchemaManagerService',
      ['$q', '$http', '$cacheFactory', 'gnUrlUtils',
       function($q, $http, $cacheFactory, gnUrlUtils) {
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

         var extractNamespaces = function(data) {
           var result = {};
           var len = data['schemas'].length;
            for (var i = 0; i < len; i++) {
              var sc = data['schemas'][i];
              var name = sc['name'];
              var nss = sc['namespaces'];
              var modNs = {};
              if (typeof nss == 'string') {
                var nssArray = nss.split(' ');
                for (var j = 0; j < nssArray.length; j++) {
                  var nsPair = nssArray[j].split('=');
                  var prefix = nsPair[0].substring(6);
                  var namespaceUri = nsPair[1].
                 substring(1, nsPair[1].length - 1);
                  modNs[prefix] = namespaceUri;
                }
              }
              result[name] = modNs;
            }
            return result;
         };

         return {
           /**
            * Find namespace uri for prefix in namespaces, optionally restricted
            * to schema specified. Schema namespaces are assumed to have
            * been loaded into the cache via getNamespaces when metadata
            * record was edited.
            */
           findNamespaceUri: function(prefix, schema) {
             var namespaces = infoCache.get('schemas');
             var nsUri = ''; // return empty string by default (what else?)
             if (schema != undefined) {
                nsUri = namespaces[schema][prefix];
              } else {
                for (var sc in namespaces) {
                  nsUri = namespaces[sc][prefix];
                  if (nsUri != undefined) break;
                }
              }
              return nsUri;
            },

           /**
            * Load schema namespaces into infoCache. This should be done
            * when a metadata record was edited.
            */
           getNamespaces: function() {
             var defer = $q.defer();
             var fromCache = infoCache.get('schemas');
             if (fromCache) {
               defer.resolve(fromCache);
             } else {
                var url = gnUrlUtils.append('info?_content_type=json',
                   gnUrlUtils.toKeyValue({
                 type: 'schemas'
                   })
               );
               $http.get(url, { cache: false }).
               success(function(data) {
                 var nss = extractNamespaces(data);
                 infoCache.put('schemas', nss);
                 defer.resolve(nss);
               });
             }
             return defer.promise;
           },

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

               $http.post('md.element.info?_content_type=json',
               getPostRequestBody(), {
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
                 $http.post('md.element.info?_content_type=json', requestBody, {
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
