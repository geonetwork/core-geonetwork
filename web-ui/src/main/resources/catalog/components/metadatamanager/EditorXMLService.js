(function() {
  goog.provide('gn_editor_xml_service');

  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_editor_xml_service',
      ['gn_schema_manager_service']);

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

  module.factory('gnEditorXMLService',
      ['gnNamespaces',
       'gnXmlTemplates',
       function(
       gnNamespaces, gnXmlTemplates) {
         var getNamespacesForElement = function(elementName) {
           var ns = elementName.split(':');
           var nsDeclaration = [];
           if (ns.length === 2) {
             nsDeclaration = ['xmlns:', ns[0], "='",
                              gnNamespaces[ns[0]], "'"];
           }
           return nsDeclaration.join('');
         };
         return {
           /**
            * Create a referenceSystemInfo XML snippet replacing
            * description, codeSpace and version properties of
            * the CRS.
            */
           buildCRSXML: function(crs) {
             var replacement = ['description', 'codeSpace', 'version'];
             var xml = gnXmlTemplates.CRS;
             angular.forEach(replacement, function(key) {
               xml = xml.replace('{{' + key + '}}', crs[key]);
             });
             return xml;
           },
           /**
            * Create an XML snippet to be inserted in a form field.
            * The element name will be the parent element of the
            * snippet provided.
            *
            * The element namespace should be defined
            * in the list of gnNamespaces.
            */
           buildXML: function(elementName, snippet) {
             if (snippet.match(/^<\?xml/g)) {
               var xmlDeclaration =
                   '<?xml version="1.0" encoding="UTF-8"?>';
               snippet = snippet.replace(xmlDeclaration, '');
             }

             var nsDeclaration = getNamespacesForElement(elementName);

             var tokens = [
               '<', elementName,
               ' ', nsDeclaration, '>',
               snippet, '</', elementName, '>'];
             return tokens.join('');
           },
           /**
            * Build an XML snippet for the element name
            * and xlink provided.
            */
           buildXMLForXlink: function(elementName, xlink) {
             var nsDeclaration = getNamespacesForElement(elementName);

             // Escape & in XLink url
             xlink = xlink.replace('&', '&amp;');

             var tokens = [
               '<', elementName,
               ' ', nsDeclaration,
               ' xmlns:xlink="', gnNamespaces.xlink, '"',
               ' xlink:href="',
               xlink, '"/>'];
             return tokens.join('');
           }
         };
       }]);
})();
