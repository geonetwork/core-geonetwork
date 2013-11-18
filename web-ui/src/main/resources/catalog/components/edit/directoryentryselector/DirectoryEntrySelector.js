(function() {
  goog.provide('gn_directory_entry_selector');

  var module = angular.module('gn_directory_entry_selector', []);

  /**
   *
   *
   */
  module.directive('gnDirectoryEntrySelector',
      ['$rootScope', '$timeout',
        'gnMetadataManagerService',
        function($rootScope, $timeout,
            gnMetadataManagerService) {

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
             scope.snippet = null;
             scope.snippetRef = gnMetadataManagerService.
             buildXMLFieldName(scope.elementRef, scope.elementName);

             scope.add = function() {
               gnMetadataManagerService.add(scope.metadataId,
                   scope.elementRef, scope.elementName, scope.domId, 'before');
               return false;
             };

             // <request><codelist schema="iso19139"
             // name="gmd:CI_RoleCode" /></request>
             scope.addContact = function(contact, usingXlink) {
               var id = contact['geonet:info'].id;
               gnMetadataManagerService.getRecord(id).then(function(xml) {
                 // TODO: contact role
                 scope.snippet = gnMetadataManagerService.
                 buildXML(scope.elementName, xml);

                 scope.clearResults();

                 $timeout(function() {
                   // Save the metadata and refresh the form
                   gnMetadataManagerService.save(scope.metadataId, true);
                 });
               });

               return false;
             };
             scope.openSelector = function() {
               console.log('open');
             };
           }
         };
       }]);
})();
