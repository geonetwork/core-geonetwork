(function() {
  goog.provide('gn_metadata_manager_service');

  var module = angular.module('gn_metadata_manager_service', []);

  module.provider('gnMetadataManagerService',
      function() {
        this.$get = [
          '$q',
          '$rootScope',
          '$http',
          '$translate',
          '$compile',
          'gnUrlUtils',
          function($q, $rootScope, $http, $translate, $compile, gnUrlUtils) {
            var _select = function(uuid, andClearSelection, action) {
              var defer = $q.defer();
              $http.get(
                  'metadata.select@json?' + (uuid ? 'id=' + uuid : '') +
                  (andClearSelection ? '' : '&selected=' + action))
                    .success(function(data, status) {
                    defer.resolve(data);
                  }).error(function(data, status) {
                    defer.reject(error);
                  });
              return defer.promise;
            };
            var NAMESPACES = {
                gmd: 'http://www.isotc211.org/2005/gmd',
                gfc: 'http://www.isotc211.org/2005/gfc'
              };
            return {
              NAMESPACES: NAMESPACES,
              // TODO : move select to SearchManagerService
              select: function(uuid, andClearSelection) {
                return _select(uuid, andClearSelection, 'add');
              },
              unselect: function(uuid) {
                return _select(uuid, false, 'remove');
              },
              selectAll: function() {
                return _select(null, false, 'add-all');
              },
              selectNone: function() {
                return _select(null, false, 'remove-all');
              },
              view: function(md) {
                window.open('../../?uuid=' + md['geonet:info'].uuid,
                    'gn-view');
              },
              edit: function(md) {
                location.href = 'catalog.edit?#/metadata/' +
                    md['geonet:info'].id;
              },
              getRecord: function(id) {
                var defer = $q.defer();
                var url = gnUrlUtils.append('xml.metadata.get',
                    gnUrlUtils.toKeyValue({
                      id: id
                    })
                    );
                $http.get(url).
                    success(function(data, status) {
                      defer.resolve(data);
                    }).
                    error(function(data, status) {
                      //                TODO handle error
                      //                defer.reject(error);
                    });
                return defer.promise;
              },

              /**
               * Build a field name for an XML field
               * TODO: move to editor service
               */
              buildXMLFieldName: function(elementRef, elementName) {
                var t = ['_X', elementRef,
                         '_', elementName.replace(':', 'COLON')];
                return t.join('');
              },

              /**
               * Create an XML fragment to be inserted in a form field.
               * The element name will be the parent element of the
               * snippet provided. It has to be in the gmd: namespace.
               *
               * TODO : could be nice to have namespaces as global constant
               */
              buildXML: function(elementName, snippet) {
                if (snippet.match(/^<\?xml/g)) {
                  var xmlDeclaration =
                      '<?xml version="1.0" encoding="UTF-8"?>';
                  snippet = snippet.replace(xmlDeclaration, '');
                }
                
                var ns = elementName.split(':');
                var nsDeclaration = [];
                if (ns.length === 2) {
                  nsDeclaration = ['xmlns:', ns[0], "='", 
                                   NAMESPACES[ns[0]], "'"];
                }
                
                var tokens = [
                  '<', elementName,
                  " ", nsDeclaration.join(''), '>',
                  snippet, '</', elementName, '>'];
                return tokens.join('');
              }
            };
          }];
      });
})();
