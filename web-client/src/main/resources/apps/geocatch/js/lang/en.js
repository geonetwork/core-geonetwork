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

GeoNetwork.GeoCatCh.Lang.en = {
	'indexSelection': 'Index Selected Metadata',
	'indexSelectionRunning': 'Metadata Index process running. <br/><br/> Number of metadata being indexed: ',
	'indexSelectionError': 'Error starting indexing: <br/><br/>',
	'any': '- Any -',
	'dataset': 'Data',
	'basicgeodata': 'Basic Geodata',
	'basicgeodata-federal': ' + Basic Geodata - federal',
	'basicgeodata-cantonal': ' + Basic Geodata - cantonal',
	'basicgeodata-communal': ' + Basic Geodata - communal',
	'basicgeodata-other': ' + Basic Geodata - other',
	'service': 'Services',
	'service-OGC:WMS': ' + WMS Services',
	'service-OGC:WFS': ' + WFS Services',
	'geodata': 'Archive',
	'yes': 'Yes',
	'no': 'No',
	'unChecked': 'Unchecked',
	'recordType': 'Type',

	'includearchived': 'Include archived',
	'excludearchived': 'Exclude archived',
	'onlyarchived': 'Only archived',
	'owner': 'Owner',

	'withinGeo': 'Completely within or equal',
	'intersectGeo': 'Intersects',
	'containsGeo': 'Contains at least',
    'Login.error.message': 'Login failed. Check your username and password.',

    'kantone': 'Canton(s)',
    'any2': 'Any',
    'what': 'What?',
    'searchText': 'Text search',
    'rtitle': 'Title',
    'Abstract': 'Abstract',
    'keyword': 'Keyword(s)',
    'theme': 'Topic(s)',
    'contact': 'Contact',
    'organisationName': 'Organisation',

    'template': 'Template',
    'identifier': 'Identifier',
    'formatTxt': 'Format',
    'toEdit': 'To edit',
    'toPublish': 'To publish',

    'where': 'Where?',
    'wherenone': 'Everywhere',
    'bbox': 'BBOX',
    'adminUnit': 'Administrative unit',
    'drawOnMap': 'Draw on map',
    'when': 'When?',

    'source': 'Source?',
    'catalog': 'Catalogue(s)',

    'startNewPolygonHelp' : 'Start new polygon (Doubleclick to finish)',
    'deletePolygonHelp' : 'Delete or Start new polygon',

    'changeDate': 'Metadata revision',

    'refineSearch': 'Refine search',

    'showAdvancedOptions' : 'Show Advanced Options',
    'hideAdvancedOptions' : 'Hide Advanced Options',
    
    'removeLayers' : 'Remove all layers and reset map',
    'linklabel-OGC:WMS': ' ',
    'topicCat' : 'category',
	'topicCats' : 'Categories',
	'denominator' : 'spatial resolution',
	'denominators' : 'Spatial Resolution',
	'sendmail' : 'Send email',
	'unpublishSlection' : 'Unpublish the selection',
    'country': 'Country',
    'city': 'City',
    'body': 'Body',
    'bodyError': 'Error',
    'subject': 'Subject',
    'composeMessage': 'Compose Message',
    'send': 'Send',
    'cancel': 'Cancel',
    'nonEmptyField': "Field cannot be empty"

};

OpenLayers.Util.extend(OpenLayers.Lang.en, GeoNetwork.GeoCatCh.Lang.en);
