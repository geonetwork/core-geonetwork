(function() {
  goog.provide('gn_directory_entry_selector');

  var module = angular.module('gn_directory_entry_selector', []);

  /**
   *
   *
   */
  module.directive('gnDirectoryEntrySelector',
      ['$rootScope', '$timeout', '$q', '$http',
        'gnMetadataManagerService', 'gnUrlUtils',
        function($rootScope, $timeout, $q, $http, 
            gnMetadataManagerService, gnUrlUtils) {

         return {
           restrict: 'A',
           replace: true,
           transclude: true,
           scope: {
             mode: '@gnDirectoryEntrySelector',
             metadataId: '@',
             elementName: '@',
             elementRef: '@',
             domId: '@'
           },
           templateUrl: '../../catalog/components/edit/' +
           'directoryentryselector/partials/' +
           'directoryentryselector.html',
           link: function(scope, element, attrs) {
             var separator = '&&&';

             scope.snippet = null;
             scope.snippetRef = gnMetadataManagerService.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             scope.add = function() {
               gnMetadataManagerService.add(scope.metadataId,
                   scope.elementRef, scope.elementName,
                   scope.domId, 'before');
               return false;
             };

             // <request><codelist schema="iso19139"
             // name="gmd:CI_RoleCode" /></request>
             scope.addContact = function(contact, role, usingXlink) {
               if (!(contact instanceof Array)) {
                 contact = [contact];
               }

               scope.snippet = '';
               var snippets = [];

               var checkState = function() {
                 if (snippets.length === contact.length) {
                   scope.snippet = snippets.join(separator);
                   scope.clearResults();

                    $timeout(function() {
                      // Save the metadata and refresh the form
                      gnMetadataManagerService.save(scope.metadataId, true);
                    });
                 }
               };

               angular.forEach(contact, function(c) {
                 var id = c['geonet:info'].id,
                 uuid = c['geonet:info'].uuid;
                 var params = {uuid: uuid};
                 if (role) {
                   params.process =
                   'gmd:role/gmd:CI_RoleCode/@codeListValue~' + role;
                 }
                 var url = gnUrlUtils.append(
                     // TODO: Get URL from gnHttp
                     'http://localhost:8080/geonetwork/srv/eng/' +
                     'subtemplate',
                     gnUrlUtils.toKeyValue(params));

                 // FIXME: this call is useless when using XLink
                 $http.get(url).success(function(xml) {
                   if (usingXlink) {
                     snippets.push(gnMetadataManagerService.
                     buildXMLForXlink(scope.elementName, url));
                   } else {
                     snippets.push(gnMetadataManagerService.
                     buildXML(scope.elementName, xml));
                   }
                   checkState();
                 });

                 //gnMetadataManagerService.getRecord(id).then(function(xml) {
                 //  // TODO: contact role
                 //  if (usingXlink) {
                 //    // TODO: handle other types
                 //    // TODO: catalog base URL
                 //    var xlink = 'http://localhost:8080/geonetwork' +
                 //   '/srv/eng/subtemplate?uuid=' + uuid;
                 //
                 //    snippets.push(gnMetadataManagerService.
                 //   buildXMLForXlink(scope.elementName, xlink));
                 //  } else {
                 //    snippets.push(gnMetadataManagerService.
                 //   buildXML(scope.elementName, xml));
                 //  }
                 //  checkState();
                 //                 });

               });

               return false;
             };
             scope.openSelector = function() {
               // TODO: Schema should be a parameter
               gnMetadataManagerService
               .getCodelist('iso19139|gmd:CI_RoleCode')
               .then(function(data) {
                 scope.roles = data[0].entry;
               });
               // FIXME: add dependency on popup
               $('#gn-directory-entry-popup').toggle();
             };
           }
         };
       }]);

  module.directive('gnDirectoryEntryMultiSelector',
      ['$rootScope', '$timeout',
        'gnMetadataManagerService',
        function($rootScope, $timeout,
            gnMetadataManagerService) {

         return {
           restrict: 'A',
           replace: true,
           templateUrl: '../../catalog/components/edit/' +
           'directoryentryselector/partials/' +
           'directoryentrymultiselector.html',
           link: function(scope, element, attrs) {
             scope.selected = [];
           }
         };
       }]);

})();
