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
  goog.provide('gn_editor_xml_service');

  goog.require('gn_schema_manager_service');

  var module = angular.module('gn_editor_xml_service',
      ['gn_schema_manager_service']);

  module.value('gnXmlTemplates', {
    CRS: {iso19139: '<gmd:referenceSystemInfo ' +
          "xmlns:gmd='http://www.isotc211.org/2005/gmd' " +
          "xmlns:gmx='http://www.isotc211.org/2005/gmx' " +
          "xmlns:xlink='http://www.w3.org/1999/xlink'>" +
          '<gmd:MD_ReferenceSystem>' +
          '<gmd:referenceSystemIdentifier>' +
          '<gmd:RS_Identifier>' +
          '<gmd:code>' +
          '<gmx:Anchor xlink:title="{{description}}" xlink:href="http://www.opengis.net/def/crs/EPSG/0/{{code}}">' +
          'EPSG:{{code}}' +
          '</gmx:Anchor>' +
          '</gmd:code>' +
          '</gmd:RS_Identifier>' +
          '</gmd:referenceSystemIdentifier>' +
          '</gmd:MD_ReferenceSystem>' +
          '</gmd:referenceSystemInfo>',
      'iso19115-3':
          '  <mrs:referenceSystemIdentifier ' +
          "xmlns:mrs='http://standards.iso.org/iso/19115/-3/mrs/1.0' " +
          "xmlns:mcc='http://standards.iso.org/iso/19115/-3/mcc/1.0' " +
          "xmlns:gcx='http://standards.iso.org/iso/19115/-3/gcx/1.0' " +
          "xmlns:gco='http://standards.iso.org/iso/19115/-3/gco/1.0' " +
          "xmlns:xlink='http://www.w3.org/1999/xlink'>" +
          '    <mcc:MD_Identifier>' +
          '     <mcc:code>' +
          '       <gcx:Anchor xlink:href="http://www.opengis.net/def/crs/EPSG/0/{{code}}">' +
          'EPSG:{{code}}' +
          '</gcx:Anchor>' +
          '     </mcc:code>' +
          '     <mcc:description>' +
          '       <gco:CharacterString>{{description}}</gco:CharacterString>' +
          '     </mcc:description>' +
          '   </mcc:MD_Identifier>' +
          ' </mrs:referenceSystemIdentifier>'
    }});

  module.factory('gnEditorXMLService',
      ['gnSchemaManagerService',
       'gnXmlTemplates',
       function(
       gnSchemaManagerService, gnXmlTemplates) {
         var getNamespacesForElement = function(schema, elementName) {
           var nsDeclaration = [];
           var ns = elementName.split(':');
           if (ns.length === 2) {
             nsDeclaration = ['xmlns:', ns[0], "='",
               gnSchemaManagerService.findNamespaceUri(ns[0], schema), "'"];
           }
           return nsDeclaration.join('');
         };
         return {
           /**
           * Create a referenceSystemInfo XML snippet replacing
           * description, codeSpace and version properties of
           * the CRS.
           */
           buildCRSXML: function(crs, schema, xmlSnippet) {
             var replacement = ['description', 'codeSpace',
               'authority', 'code', 'version'];
             var xml = undefined;
             if (xmlSnippet) {
               xml = xmlSnippet
             } else {
               xml = gnXmlTemplates.CRS[schema]
             }
             // Profiles
             if (xml === undefined) {
               xml = gnXmlTemplates.CRS[schema.split('.')[0]];
             }
             if (xml === undefined) {
               xml = gnXmlTemplates.CRS['iso19139'];
             }
             angular.forEach(replacement, function(key) {
               xml = xml.replace(new RegExp('{{' + key + '}}', 'g'), crs[key]);
             });
             return xml;
           },
           /**
           * Create an XML snippet to be inserted in a form field.
           * The element name will be the parent element of the
           * snippet provided.
           *
           * The element namespace should be defined
           * in the list of namespaces returned by getNamespacesForElement.
           */
           buildXML: function(schema, elementName, snippet) {
             if (snippet.match(/^<\?xml/g)) {
               var xmlDeclaration =
               '<?xml version="1.0" encoding="UTF-8"?>';
               snippet = snippet.replace(xmlDeclaration, '');
             }

             var nsDeclaration = getNamespacesForElement(schema, elementName);

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
           buildXMLForXlink: function(schema, elementName,
                                      xlink, extraAttributeMap) {
             var nsDeclaration = getNamespacesForElement(schema, elementName);

             // Escape & in XLink url
             xlink = xlink.replace(/&/g, '&amp;');

             var tokens = [
               '<', elementName,
               ' ', nsDeclaration,
               ' xmlns:xlink="',
               gnSchemaManagerService.findNamespaceUri('xlink'), '"',
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
