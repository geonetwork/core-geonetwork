(function() {
  goog.provide('gn_editor_xml_service');

  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_editor_xml_service',
      ['gn_schema_manager_service']);

  module.value('gnXmlTemplates', {
    CRS: {iso19139: '<gmd:referenceSystemInfo ' +
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
          '</gmd:referenceSystemInfo>',
      'iso19115-3': '<mdb:referenceSystemInfo ' +
          "xmlns:mrs='http://standards.iso.org/19115/-3/mrs/1.0' " +
          "xmlns:mcc='http://standards.iso.org/19115/-3/mcc/1.0' " +
          "xmlns:mdb='http://standards.iso.org/19115/-3/mdb/1.0' " +
          "xmlns:cit='http://standards.iso.org/19115/-3/cit/1.0' " +
          "xmlns:gco='http://standards.iso.org/19115/-3/gco/1.0'>" +
          '<mrs:MD_ReferenceSystem>' +
          '  <mrs:referenceSystemIdentifier>' +
          '    <mcc:MD_Identifier>' +
          '      <mcc:authority>' +
          '       <cit:CI_Citation>' +
          '         <cit:title>' +
          '  <gco:CharacterString>{{authority}}</gco:CharacterString>' +
          '         </cit:title>' +
          '       </cit:CI_Citation>' +
          '     </mcc:authority>' +
          '     <mcc:code>' +
          '       <gco:CharacterString>{{code}}</gco:CharacterString>' +
          '     </mcc:code>' +
          '     <mcc:codeSpace>' +
          '  <gco:CharacterString>{{description}}</gco:CharacterString>' +
          '     </mcc:codeSpace>' +
          '     <mcc:version>' +
          '       <gco:CharacterString>{{version}}</gco:CharacterString>' +
          '     </mcc:version>' +
          '   </mcc:MD_Identifier>' +
          ' </mrs:referenceSystemIdentifier>' +
          '</mrs:MD_ReferenceSystem>' +
          '</mdb:referenceSystemInfo>'
    }});

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
           buildCRSXML: function(crs, schema) {
             var replacement = ['description', 'codeSpace',
               'authority', 'code', 'version'];
             var xml = gnXmlTemplates.CRS[schema] ||
             gnXmlTemplates.CRS['iso19139'];
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
            *
            * extraAttributeMap is other attributes to add to the element.
            * For example xlink:title
           */
           buildXMLForXlink: function(elementName, xlink, extraAttributeMap) {
             var nsDeclaration = getNamespacesForElement(elementName);

             // Escape & in XLink url
             xlink = xlink.replace(/&/g, '&amp;');

             var tokens = [
               '<', elementName,
               ' ', nsDeclaration,
               ' xmlns:xlink="', gnNamespaces.xlink, '"',
               ' xlink:href="',
               xlink, '"'];


             angular.forEach(extraAttributeMap, function(value, attName) {
               tokens.push(' ');
               tokens.push(attName);
               tokens.push('="');
               tokens.push(value);
               tokens.push('"');
             });

             tokens.push(' />');
             return tokens.join('');
           }
         };
       }]);
})();
