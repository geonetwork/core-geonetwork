(function() {
  goog.provide('gn_metadata_manager_service');

  var module = angular.module('gn_metadata_manager_service', []);

  module.value('gnNamespaces', {
    gmd: 'http://www.isotc211.org/2005/gmd',
    gfc: 'http://www.isotc211.org/2005/gfc'
  });
  module.value('gnXmlTemplates', {
    CRS: '<gmd:referenceSystemInfo ' +
        "xmlns:gmd='http://www.isotc211.org/2005/gmd' " +
        "xmlns:gco='http://www.isotc211.org/2005/gco'>" +
        '<gmd:MD_ReferenceSystem>' +
        '<gmd:referenceSystemIdentifier>' +
        '<gmd:RS_Identifier>' +
        '<gmd:code>' +
        '<gco:CharacterString>{{description}}' +
        '</gco:CharacterString>' +
        '</gmd:code>' +
        '<gmd:codeSpace>' +
        '<gco:CharacterString>{{codeSpace}}</gco:CharacterString>' +
        '</gmd:codeSpace>' +
        '<gmd:version>' +
        '<gco:CharacterString>{{version}}</gco:CharacterString>' +
        '</gmd:version>' +
        '</gmd:RS_Identifier>' +
        '</gmd:referenceSystemIdentifier>' +
        '</gmd:MD_ReferenceSystem>' +
        '</gmd:referenceSystemInfo>'
  });

  module.factory('gnMetadataManagerService',
      ['$q',
       '$rootScope',
       '$http',
       '$translate',
       '$compile',
       '$cacheFactory',
       'gnUrlUtils',
       'gnNamespaces',
       'gnXmlTemplates',
       function($q, $rootScope, $http, $translate, $compile, 
           $cacheFactory,
       gnUrlUtils, gnNamespaces, gnXmlTemplates) {
         /**
         * Contains a list of metadata records currently edited
         * with the editor configuration.
         */
         var metadataIdsConfig = {};

         /**
         * Animation duration for slide up/down
         */
         var duration = 300;

         var tooltipCache = $cacheFactory('tooltipCache');
         var _select = function(uuid, andClearSelection, action) {
           var defer = $q.defer();
           $http.get('metadata.select@json?' +
               (uuid ? 'id=' + uuid : '') +
                       (andClearSelection ? '' : '&selected=' + action)).
               success(function(data, status) {
                 defer.resolve(data);
               }).
                   error(function(data, status) {
                     defer.reject(error);
                   });
           return defer.promise;
         };
         return {
           startEditing: function(metadataId, config) {
             metadataIdsConfig[metadataId] = config;
           },
           getCurrentEdit: function() {
             return metadataIdsConfig;
           },
           buildEditUrlPrefix: function(metadataId, service) {
             var params = [service, '?id=', metadataId];
             var config = metadataIdsConfig[metadataId];
             config.tab && params.push('&currTab=', config.tab);
             config.displayAttributes &&
             params.push('&displayAttributes=', config.displayAttributes);
             return params.join('');
           },
           /**
            * Save the metadata record currently in editing session.
            *
            * If refreshForm is true, then will also update the current form.
            * This is required while switching tab for example. Update the tab
            * value in the form and trigger save to update the view.
            */
           save: function(metadataId, refreshForm) {
             var defer = $q.defer();
             var config = metadataIdsConfig[metadataId];
             if (config.saving) {
               return;
             } else {
               config.savedStatus = $translate('saving');
               config.saving = true;
             }

             $http.post(
                 refreshForm ? 'md.edit.save' : 'md.edit.saveonly',
                 $(config.formId).serialize(),
                 {
                   headers: {'Content-Type':
                     'application/x-www-form-urlencoded'}
                 }).success(function(data) {
               if (refreshForm) {
                 var snippet = $(data);
                 $(config.formId).replaceWith(snippet);

                 // Compiling
                 if (config.compileScope) {
                   $compile(snippet)(config.compileScope);
                 }
               }

               config.savedStatus = $translate('allChangesSaved');
               config.savedTime = moment();


               // FIXME : This should go somewhere else ?
               //          console.log($('.gn-tooltip'));
               //          $('#gn-tooltip').tooltip();

               config.saving = false;

               defer.resolve(snippet);
             }).error(function(error) {
               config.saving = false;
               config.savedTime = moment();
               config.saveStatus = $translate('saveMetadataError');
               defer.reject(error);
             });
             return defer.promise;
           },
           /**
           * Add another element or attribute
           * of the same type to the metadata record.
           *
           * Position could be: after (default) or before
           *
           * When attribute is expanded, the returned element contains the field
           * and the element is replaced by the new one with the attribute
           * requested.
           */
           add: function(metadataId, ref, name, 
               insertRef, position, attribute) {
             // for element: md.elem.add?id=1250&ref=41&
             //   name=gmd:presentationForm
             // for attribute md.elem.add?id=19&ref=42&name=gco:nilReason
             //                  &child=geonet:attribute

             var config = metadataIdsConfig[metadataId];
             var attributeAction = attribute ? '&child=geonet:attribute' : '';
             var defer = $q.defer();
             $http.get(this.buildEditUrlPrefix(metadataId, 'md.element.add') +
             '&ref=' + ref + '&name=' + name + attributeAction)
                    .success(function(data) {
               // Append HTML snippet after current element - compile Angular
               var target = $('#gn-el-' + insertRef);
               var snippet = $(data);

               if (attribute) {
                 target.replaceWith(snippet);
               } else {
                 snippet.css('display', 'none');   // Hide
                 target[position || 'after'](snippet); // Insert
                 snippet.slideDown(duration, function() {});   // Slide

                 // Remove the Add control from the current element
                 var addControl = $('#gn-el-' + insertRef + ' .gn-add');
               }
               $compile(snippet)(config.compileScope);
               defer.resolve(snippet);

             }).error(function(data) {
               defer.reject(data);
             });

             return defer.promise;
           },
           addChoice: function(metadataId, ref, parent, name, 
               insertRef, position) {
             var defer = $q.defer();
             var config = metadataIdsConfig[metadataId];
             // md.elem.add?id=1250&ref=41&name=gmd:presentationForm
             $http.get(this.buildEditUrlPrefix(metadataId, 'md.element.add') +
                      '&ref=' + ref +
                      '&name=' + parent +
                      '&child=' + name).success(function(data) {
               // Append HTML snippet after current element - compile Angular
               var target = $('#gn-el-' + insertRef);
               var snippet = $(data);

               target[position || 'before'](snippet);

               $compile(snippet)(config.compileScope);
               defer.resolve(snippet);
             }).error(function(data) {
               defer.reject(data);
             });
             return defer.promise;
           },
           remove: function(metadataId, ref, parent) {
             // md.element.remove?id=<metadata_id>&ref=50&parent=41
             // Call service to remove element from metadata record in session
             var defer = $q.defer();
             $http.get('md.element.remove@json?id=' + metadataId +
                     '&ref=' + ref + '&parent=' + parent)
                     .success(function(data) {
               // Remove element from the DOM
               var target = $('#gn-el-' + ref);
               target.slideUp(duration, function() { $(this).remove();});

               // TODO: Take care of moving the + sign
               defer.resolve(data);
             }).error(function(data) {
               defer.reject(data);
             });
             return defer.promise;
           },
           /**
            * Retrieve field information (ie. name, description, helpers).
            * Information are cached in the tooltipCache.
            *
            * Return a promise.
            */
           getTooltip: function(config) {
             //<request>
             //  <element schema="iso19139"
             //   name="gmd:geometricObjectType"
             //   context="gmd:MD_GeometricObjects"
             //   fullContext="xpath"
             //   isoType="" /></request>
             var defer = $q.defer();
             var fromCache = tooltipCache.get(config);
             if (fromCache) {
               defer.resolve(fromCache);
             } else {
               var getPostRequestBody = function() {
                 var info = config.split('|'),
                 requestBody = '<request><element schema="' + info[0] +
                 '" name="' + info[1] +
                 '" context="' + info[2] +
                 '" fullContext="' + info[3] +
                 '" isoType="' + info[4] + '" /></request>';
                 return requestBody;
               };

               $http.post('md.element.info@json', getPostRequestBody(), {
                 headers: {'Content-type': 'application/xml'}
               }).
               success(function(data) {
                 tooltipCache.put(config, data);
                 defer.resolve(data);
               });
             }
             return defer.promise;
           },
           // TODO: move selection to search service
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
             // TODO : replace to use new services
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
           buildCRSXML: function(crs) {
             var replacement = ['description', 'codeSpace', 'version'];
             var xml = gnXmlTemplates.CRS;
             angular.forEach(replacement, function(key) {
               xml = xml.replace('{{' + key + '}}', crs[key]);
             });
             return xml;
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
                                gnNamespaces[ns[0]], "'"];
             }

             var tokens = [
               '<', elementName,
               ' ', nsDeclaration.join(''), '>',
               snippet, '</', elementName, '>'];
             return tokens.join('');
           }
         };
       }]);
})();
