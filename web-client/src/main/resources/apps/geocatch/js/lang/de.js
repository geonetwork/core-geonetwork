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

GeoNetwork.GeoCatCh.Lang.de = {
	'indexSelection': 'Index Selected Metadata',
	'indexSelectionRunning': 'Metadata Index process running. <br/><br/> Number of metadata being indexed: ',
	'indexSelectionError': 'Error starting indexing: <br/><br/>',
	'any': '- Alle -',
	'dataset': 'Daten',
	'basicgeodata': 'Geobasisdaten',
	'basicgeodata-federal': ' + Geobasisdaten - Bund',
	'basicgeodata-cantonal': ' + Geobasisdaten - Kanton',
	'basicgeodata-communal': ' + Geobasisdaten - Gemeinde',
	'basicgeodata-other': ' + Geobasisdaten - Andere',
	'service': 'Dienste',
	'service-OGC:WMS': ' + WMS',
	'service-OGC:WFS': ' + WFS',
	'owner': 'Besitzer',
	'geodata': 'Archiv',
	'yes': 'Ja',
	'no': 'Nein',
	'unChecked': 'Unkontrolliert',
	'recordType': 'Type',
    'exportMetadataSummary': 'Export Summary',
	
	'includearchived': 'inklusive Archiv',
	'excludearchived': 'exklusive Archiv',
	'onlyarchived': 'nur Archiv',
	'owner': 'Eigentümer',

	'withinGeo': 'vollständig innerhalb oder gleich',
	'intersectGeo': 'schneidet',
	'containsGeo': 'umfasst mindestens',

	'kantone': 'Kanton(e)',
	'any2': 'Alle',

    'what': 'Was?',
    'searchText': 'Alle Felder',
    'rtitle': 'Titel',
    'Abstract': 'Zusammenfassung',
    'keyword': 'Schlüsselwort',
    'theme': 'Thema',
    'contact': 'Nachname',
    'organisationName': 'Organisation',

    'template': 'Vorlage',
    'identifier': 'Geobasisdaten-ID',
    'formatTxt': 'Format',
    'toEdit': 'In Bearbeitung',
    'toPublish': 'Zu publizieren',

    'where': 'Wo?',
    'wherenone': 'Überall',
    'bbox': 'Im aktuellen Kartenausschnitt',
    'adminUnit': 'Administrative Einheit',
    'drawOnMap': 'Auf der Karte zeichnen',
    'when': 'Wann?',
    'from': 'Von',
    'to': 'Nach',

    'source': 'Herkunft?',
    'catalog': ' Katalog(e)',

    'startNewPolygonHelp' : 'Bitte zeichnen Sie ein Polygon (mit Doppelklick beenden)',
    'deletePolygonHelp' : 'Polygon löschen oder ein neues zeichnen',

    'changeDate': 'Aktualisierung Metadaten',

    'Search': 'Suche',
    'refineSearch': 'Suche verfeinern',

    'showAdvancedOptions' : 'Erweiterte Suche',
    'hideAdvancedOptions' : 'Einfache Suche',
    
    'removeLayers' : 'Entfernen Sie alle Schichten und zurücksetzen Karte',
    'linklabel-OGC:WMS': ' ',
    'topicCats' : 'Thematik',
    'topicCat' : 'thematik',
	'denominators' : 'Räumliche Auflösung',
	'denominator' : 'räumliche auflösung',
    'country': 'Staat',
    'city': 'Statd',
    'body': 'Body',
    'bodyError': 'Error',
    'subject': 'Subject',
    'composeMessage': 'Compose Message',
    'send': 'Send',
    'cancel': 'Cancel',
    'nonEmptyField': "Field cannot be empty"
};

OpenLayers.Util.extend(OpenLayers.Lang.de, GeoNetwork.GeoCatCh.Lang.de);
