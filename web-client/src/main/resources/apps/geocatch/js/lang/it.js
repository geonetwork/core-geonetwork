/*
/*
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
Ext.namespace('GeoNetwork', 'GeoNetwork.GeoCatCh.Lang');

GeoNetwork.GeoCatCh.Lang.it = {
	'any': '- Qualunque -',
	'dataset': 'Data',
	'basicgeodata': 'Basic Geodata',
	'basicgeodata-federal': ' + Basic Geodata - federal',
	'basicgeodata-cantonal': ' + Basic Geodata - cantonal',
	'basicgeodata-communal': ' + Basic Geodata - communal',
	'basicgeodata-other': ' + Basic Geodata - other',
	'service': 'Services',
	'service-OGC:WMS': ' + WMS Services',
	'service-OGC:WFS': ' + WFS Services',

	'withinGeo': 'Completely within or equal',
	'intersectGeo': 'Intersects',
	'containsGeo': 'Contains at least',
    'Login.error.message': 'Login failed. Check your username and password.',

    'kantone': 'Cantone',
    'any2': 'Qualunque',
    'showAdvancedOptions' : 'Ricerca avanzata',
    'hideAdvancedOptions' : 'Nascondere opzioni avanzati',

    'what': 'What?',
    'searchText': 'Ricerca testuale',
    'rtitle': 'Titolo',
    'Abstract': 'Abstract',
    'keyword': 'Parola chiave',
    'theme': 'Temi',
    'contact': 'Contatto',
    'organisationName': 'Organizzazione',

    'template': 'Template',
    'identifier': 'Identifier',
    'formatTxt': 'Formato',
    'toEdit': 'Da adattare',
    'toPublish': 'Da pubblicare',

    'where': 'Where?',
    'wherenone': 'Dappertutto',
    'bbox': 'BBOX',
    'adminUnit': 'Unità amministrativa',
    'drawOnMap': 'Disegna sulla carta',
    'when': 'When?',

    'source': 'Provenienza?',
    'catalog': 'Catalogo',

    'startNewPolygonHelp' : 'Disegna un nuovo poligono (doppio clic per terminare)',
    'deletePolygonHelp' : 'Annulare o ricominciare un nuovo poligono',

    'changeDate': 'Metadata revision',

    'Search': 'Ricerca',
    'refineSearch': 'Affinare la ricerca',
    
    'removeLayers' : 'Rimuovere tutti i livelli e ripristinare la mappa',
    'linklabel-OGC:WMS': ' ',
    'createDateYear' : 'anni',
    'createDateYears' : 'aAnno',
    'denominator' : 'risoluzione spaziale',
    'denominators' : 'Risoluzione spaziale',
    'orgName' : 'organizzazione',
    'orgNames' : 'Organizzazioni',
    'serviceTypes' : 'Tipi di servizio',
    'facetMore' : '+ Mostra di più',
    'facetLess' : '- Mostra meno'
};

OpenLayers.Util.extend(OpenLayers.Lang.it, GeoNetwork.GeoCatCh.Lang.it);
