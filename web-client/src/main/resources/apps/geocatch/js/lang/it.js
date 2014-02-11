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
	'indexSelection': 'Index Selected Metadata',
	'indexSelectionRunning': 'Metadata Index process running. <br/><br/> Number of metadata being indexed: ',
	'indexSelectionError': 'Error starting indexing: <br/><br/>',
	'any': '- Qualunque -',
	'dataset': 'Set di dati',
	'basicgeodata': 'Basic Geodata',
	'basicgeodata-federal': ' + Geodati di base - federali',
	'basicgeodata-cantonal': ' + Geodati di base - cantonali',
	'basicgeodata-communal': ' + Geodati di base - communali',
	'basicgeodata-other': ' + Geodati di base - altro',
	'service': 'Servizi',
	'service-OGC:WMS': ' + Servizi WMS',
	'service-OGC:WFS': ' + Servizi WFS',
	'geodata': 'Archivio',
	'yes': 'Si',
	'no': 'No',
	'unChecked': 'Incontrollato',
	'recordType': 'Typo',
    'exportMetadataSummary': 'Export Summary',
	
	'includearchived': 'Archivi inclusi',
	'excludearchived': 'Archivi esclusi',
	'onlyarchived': 'Solo archivi',
	'owner': 'Proprietario',

	'withinGeo': 'Completamente all interno o uguale' ,
	'intersectGeo': 'Interseca',
	'containsGeo': 'Contiene almeno',
    'Login.error.message': 'Login fallito, controllare nome utente e password',

    'kantone': 'Cantone',
    'any2': 'Qualunque',
    'showAdvancedOptions' : 'Ricerca avanzata',
    'hideAdvancedOptions' : 'Nascondere opzioni avanzati',

    'what': 'Cosa',
    'searchText': 'Ricerca testuale',
    'rtitle': 'Titolo',
    'abstract': 'Descrizione',
    'keyword': 'Parola chiave',
    'theme': 'Tema',
    'contact': 'Contatto',
    'organisationName': 'Organizzazione',

    'template': 'Modello',
    'identifier': 'Identificatore',
    'formatTxt': 'Formato',
    'toEdit': 'Da adattare',
    'toPublish': 'Da pubblicare',

    'where': 'Dove',
    'wherenone': 'Dappertutto',
    'bbox': 'BBOX',
    'adminUnit': 'Unità amministrativa',
    'drawOnMap': 'Disegnare sulla carta',
    'when': 'Quando',

    'source': 'Provenienza?',
    'catalog': 'Catalogo',

    'startNewPolygonHelp' : 'Disegna un nuovo poligono (doppio clic per terminare)',
    'deletePolygonHelp' : 'Annulare o ricominciare un nuovo poligono',

    'changeDate': 'Revisione metadati',

    'Search': 'Ricerca',
    'refineSearch': 'Affinare la ricerca',
    'hideAdvancedOptions': 'Ricerca semplice',
    
    'removeLayers' : 'Rimuovere tutti i livelli e ripristinare la mappa',
    'linklabel-OGC:WMS': ' ',
    'sendmail': 'Invia email',
    'createDateYear' : 'anni',
    'createDateYears' : 'aAnno',
    'denominator' : 'risoluzione spaziale',
    'denominators' : 'Risoluzione spaziale',
    'orgName' : 'organizzazione',
    'orgNames' : 'Organizzazioni',
    'serviceTypes' : 'Tipo di servizio',
    'facetMore' : '+ Mostra di più',
    'facetLess' : '- Mostra meno',
    'topicCat' : 'Categoria',
    'topicCats' : 'Categoria',
    'body': 'Corpo',
    'bodyError': 'Error',
    'subject': 'Oggetto',
    'composeMessage': 'Componi messaggio',
    'send': 'Invia',
    'cancel': 'Cancella',
    'nonEmptyField': "In campo non puo essere vuoto",
    'unpublishSlection': 'Nascondi selezione'
};

OpenLayers.Util.extend(OpenLayers.Lang.it, GeoNetwork.GeoCatCh.Lang.it);
