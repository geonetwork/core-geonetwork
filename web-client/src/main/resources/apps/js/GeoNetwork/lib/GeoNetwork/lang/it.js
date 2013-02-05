/*
 * Copyright (C) 2009 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace('GeoNetwork', 'GeoNetwork.Lang');

GeoNetwork.Lang.it = {

    'createDateYear' : 'anni',
    'createDateYears' : 'aAnno',
    'denominator' : 'risoluzione spaziale',
    'denominators' : 'Risoluzione spaziale',
    'orgName' : 'organizzazione',
    'orgNames' : 'Organizzazioni',
    'serviceTypes' : 'Tipi di servizio',
    'facetMore' : '+ Mostra di più',
    'facetLess' : '- Mostra meno',
    'view': 'View',
    'zoomTo': 'Zoom to',
    'saveXml': 'Save as XML',
    'saveXmlIso19139': 'Save as ISO19139 XML',
    'saveGM03': 'Save as GM03',
    'saveRdf': 'Save as RDF',
    'exportCsv': 'Export (CSV)',
    'exportZip': 'Export (ZIP)',
    'printSel': 'Export (PDF)',
    'getMEF': 'Export (ZIP)',
    'hitsPerPage': 'Hits per page',
    'sortBy': 'Sort by',
    'otherActions': 'Other actions',
    'onSelection': 'On selection',
    'none': 'none',
    'all': 'all',
    'allInPage': 'all in page',
    'select': 'Select ',
    'resultBy': ' result(s) / '
};

OpenLayers.Util.extend(OpenLayers.Lang.it, GeoNetwork.Lang.it);
