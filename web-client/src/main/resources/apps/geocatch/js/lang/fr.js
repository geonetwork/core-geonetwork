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

GeoNetwork.GeoCatCh.Lang.fr = {
	'any': '- Tous -',
	'dataset': 'Données',
	'basicgeodata': 'Geodonnées de base',
	'basicgeodata-federal': ' + Geodonnées de base - fédérales',
	'basicgeodata-cantonal': ' + Geodonnées de base - cantonales',
	'basicgeodata-communal': ' + Geodonnées de base - communales',
	'basicgeodata-other': ' + Geodonnées de base - autres',
	'service': 'Services',
	'service-OGC:WMS': ' + Services WMS',
	'service-OGC:WFS': ' + Services WFS',

	'withinGeo': 'complètement à l\'intérieur ou égal',
	'intersectGeo': 'au moins partiellement à l\'intérieur',
	'containsGeo': 'comprend au moins',

	'kantone': 'Canton(s)',
	'any2': 'Tous',
    'showAdvancedOptions' : 'Recherche avancée',
    'hideAdvancedOptions' : 'Recherche simple',
    
    'startNewPolygonHelp' : 'Dessiner un polygone (Double clique pour terminer)',
    'deletePolygonHelp' : 'Dessiner un nouveau polygone',

    'Login.error.message': 'Login failed. Check your username and password.',

    'what': 'What?',
    'searchText': 'Recherche libre',
    'rtitle': 'Titre',
    'Abstract': 'Résumé',
    'Keyword': 'Mot-clé',
    'theme': 'Thématique',
    'contact': 'Nom de famille',
    'organisationName': 'Organisation',

    'template': 'Modèle',
    'identifier': 'Identifier',
    'formatTxt': 'Format',
    'toEdit': 'A éditer',
    'toPublish': 'A publier',

    'where': 'Où?',
    'wherenone': 'Partout',
    'bbox': 'BBOX',
    'adminUnit': 'Unité administrative',
    'drawOnMap': 'Dessiner sur la carte',
    'when': 'Quan?',
    'from': 'Début',
    'to': 'Fin',

    'source': 'Provenance?',
    'catalog': 'Catalogue(s)',

    'startNewPolygonHelp' : 'Dessiner un polygone (Double clique pour terminer)',
    'deletePolygonHelp' : 'Dessiner un nouveau polygone',

    'changeDate': 'Metadata revision',

    'Search': 'Rechercher',
    'refineSearch': 'Preciséz votre recherche',
    
    'removeLayers' : 'Enlever toutes les couches et de réinitialiser la carte',
    'linklabel-OGC:WMS': ''
};

OpenLayers.Util.extend(OpenLayers.Lang.fr, GeoNetwork.GeoCatCh.Lang.fr);
