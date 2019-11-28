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
  goog.require('gn_atom_service');

  var module = angular.module('gn_atom_directive', [
    'gn_atom_service'
  ]);

  goog.provide('gn_atom_directive');

  /**

   Parse atom feed, according to spec
   http://inspire.ec.europa.eu/documents/Network_Services/
   Technical_Guidance_Download_Services_3.0.pdf
   a metadata should contain a link to a service feed, however in practice a lot
   of providers link to a dataset feed directly this script parses the feed and
   detects if it is a service feed or dataset feed in case it is a service feed,
   it is checked if there is a dataset with a matching identification in that
   case the file downloads are shown if not, then the user can select one
   of the available datasets

   service feeds

   <entry>
     <title xml:lang="nl">Stads- en Dorpsgezichten</title>
     <inspire_dls:spatial_dataset_identifier_code>
        e39bd6e0-7651-11e0-a1f0-0800200c9a62
      </inspire_dls:spatial_dataset_identifier_code>
     <inspire_dls:spatial_dataset_identifier_namespace>
        http://www.cultureelerfgoed.nl
      </inspire_dls:spatial_dataset_identifier_namespace>
     <link href="http://www.nationaalgeoregister.nl/geonetwork/srv/nl/csw?
                Service=CSW&amp;Request=GetRecordById&amp;Version=2.0.2&amp;
                id=4e2ef670-cddd-11dd-ad8b-0800200c9a66&amp;
                outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full"
            rel="describedby" type="application/xml" />
     <link rel="alternate"
            href="http://services.rce.geovoorziening.nl/www/download/data/
                  Stads_en_Dorpsgezichten_nl.xml"
            type="application/atom+xml" hreflang="nl"
            title="Feed bevattende de Stads- en Dorpsgezichten dataset" />
     <link rel="alternate"
            href="http://services.rce.geovoorziening.nl/www/download/data/
                  Stads_en_Dorpsgezichten_nl.xml"
            type="text/html" hreflang="nl"
            title="Feed bevattende de Stads- en Dorpsgezichten dataset" />
     <id>
      http://services.rce.geovoorziening.nl/www/download/data/Stads_en_Dorpsgezichten_nl.xml
     </id>
     <rights>Geen beperking</rights>
     <updated>2012-06-25T10:45:03</updated>
     <summary>
      Deze dataset bevat de begrenzingen van alle gebieden waarvoor de
      procedure is gestart om het gebied aan te wijzen als rijksbeschermd
      stads- of dorpsgezicht (ex artikel 35 van de Monumentenwet 1988).
     </summary>
     <georss:polygon>
      50.74 3.25 53.48 3.25 53.48 7.22 50.74 7.22 50.74 3.25
      </georss:polygon>
     <category term="http://www.opengis.net/def/crs/EPSG/0/28992"
                label="Amersfoort / RD New" />
     <category term="http://www.opengis.net/def/crs/EPSG/0/4258"
                label="ETRS89" />
   </entry>

   dataset feeds

   <entry>
   <title xml:lang="nl">
   Archeologische Monumenten in CRS EPSG:4258 (ShapeFile)
   </title>
   <link rel="alternate"
          href="http://services.rce.geovoorziening.nl/www/download/data/
                Archeologische_Monumenten_4258.zip"
          type="application/x-shapefile" hreflang="nl" length="7810253"
          title="Archeologische Monumenten data als shapefile in
                  ETRS89(http://www.opengis.net/def/crs/EPSG/0/4258)"/>
   <link rel="enclosure"
          href="http://services.rce.geovoorziening.nl/www/download/data/
                Archeologische_Monumenten_4258.zip"
          type="application/x-shapefile" hreflang="nl" length="7810253"
          title="Archeologische Monumenten data als shapefile in
                  ETRS89(http://www.opengis.net/def/crs/EPSG/0/4258)"/>
   <id>
   http://services.rce.geovoorziening.nl/www/download/data/
   Archeologische_Monumenten_4258.zip
   </id>
   <updated>2016-09-10T21:03:56</updated>
   <category term="http://www.opengis.net/def/crs/EPSG/0/4258"
              label="ETRS89"/>
   </entry>
   */
  module.directive('gnAtomDownload', ['gnAtomService', 'gnGlobalSettings',
    function(gnAtomService, gnGlobalSettings) {
      return {
        restrict: 'A',
        scope: {
          layer: '=gnAtomDownload',
          map: '=',
          md: '='
        },
        templateUrl: '../../catalog/components/' +
            'viewer/atom/partials/atomDownload.html',
        link: function(scope, element, attrs, ctrls) {
          scope.isMapViewerEnabled = gnGlobalSettings.isMapViewerEnabled;
          scope.atomLinks = null; // file links from dataset feed
          scope.datasetLinks = null; // datasetfeed links in service feed
          scope.atom = null; //feed content
          scope.layerSelected = null; // layer selected from service
          scope.isAtomAvailable = false; // if $http.get fails
          scope.isLayerInAtom = false; // dataset is found in service feed
          scope.atomChecked = false; // request to atom is running
          var init = function() {
            try {
              // Get WMS URL from attrs or try by getting the url layer property
              scope.url = attrs.url || scope.layer.get('url');
              scope.layerName = attrs.layerName;
              scope.checkAtom(attrs.url).then(scope.setLinks, function() {
              }).finally(function() {
                scope.atomChecked = true;
              });
            } catch (e) {
              scope.problemContactingServer = true;
              scope.atomChecked = true;
            }
          };


          scope.update = function(atom) {
            scope.layerSelected = atom;
            if (atom && atom.url) {
              scope.atomChecked = false;
              scope.checkAtom(atom.url).then(scope.setLinks, function() {
              }).finally(function() {
                scope.atomChecked = true;
              });
            } else {
              scope.atomLinks = [];
            }
          };

          scope.checkAtom = function(url) {
            return gnAtomService.parseFeed(url)
                .then(function(atom) {
                  scope.atom = atom;
                });
          };

          scope.setLinks = function() {
            var atomLinks = [];
            var datasetLinks = [];
            var isService = false;

            // Pre-defined Dataset Download Service implementations shall publish separate
            // datasets as individual entries within an Atom feed
            scope.atom.find('entry').each(function() {
              var atomLink = {};
              try {
                atomLink = {
                  id: $(this).find('id').text(),
                  title: $(this).find('title').first().text(),
                  //check if entry has inspire extension
                  uuid: $(this).find('spatial_dataset_identifier_code').text(),
                  namespace: $(this).find('spatial_dataset_identifier_namespace').text(),
                  links: []
                };

                // The Download Service Feed shall contain an Atom ‗link‘ element that
                // contains an HTTP URI for the Download Service Feed document. The value
                // of the ‗rel‘ attribute of this element shall be ―self,
                // the ‗hreflang‘ attribute shall use the appropriate language code
                // and the value of the ‗type‘ attribute shall be application/atom+xml.
                $(this).find('link').each(function() {
                  if ($(this).attr('type') === 'application/atom+xml') {
                    atomLink.url = $(this).attr('href');
                    isService = true;
                  }
                });

                if (angular.isUndefined(atomLink.uuid)) {
                  isService = true;
                }
              } catch (e) {
                console.warn('Error while parsing ATOM entry.');
              }


              if (isService) {
                datasetLinks.push(atomLink);
              } else {
                try {
                  var defaultCrs = $(this).find('category').attr('label'),
                    defaultGeom = $(this).find('georss:polygon').text();
                  $(this).find('link').each(function() {
                    var link = {
                      title: $(this).attr('title') || atomLink.title,
                      url: $(this).attr('href'),
                      type: $(this).attr('type'),
                      length: Math.round(($(this).attr('length') || 0) / 10485.76) / 100,
                      crs: defaultCrs,
                      geom: $(this).attr('bbox') || defaultGeom
                    }
                    atomLink.links.push(link);
                    atomLinks.push(link);
                  });
                } catch (e) {
                  console.warn('Error while parsing ATOM entry link.');
                }
              }
            });


            if (isService) {
              //check if layer in feed (by name=id or uuid=uuid)
              scope.layerSelected = null;
              $(datasetLinks).each(function() {
                if (scope.layerName === $(this).id ||
                    ($(this).uuid && scope.md.source === $(this).uuid)) {
                  scope.layerSelected = $(this);
                  scope.isLayerInAtom = true;
                }
                //todo: get dataset-feed for this layer
                scope.isAtomAvailable = true;
              });
              //list dataset links
              scope.atomDatasets = datasetLinks;
            } else {
              //else dataset feed
              //list dataset links
              scope.atomLinks = atomLinks;
              scope.isAtomAvailable = true;
              scope.isLayerInAtom = true;
            }
          };

          init();
        }
      };
    }
  ]);
})();
