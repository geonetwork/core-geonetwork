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

GeoNetwork.GeoCatCh.Lang.de = {
	'any': '- Alle -',
	'dataset': 'Daten',
	'basicgeodata': 'Geobasisdaten',
	'basicgeodata-federal': ' + Geobasisdaten - Bund',
	'basicgeodata-cantonal': ' + Geobasisdaten - Kanton',
	'basicgeodata-communal': ' + Geobasisdaten - Gemeinde',
	'basicgeodata-other': ' + Geobasisdaten - Andere',
	'service': 'Dieste',
	'service-OGC:WMS': ' + WMS',
	'service-OGC:WFS': ' + WFS',

	'withinGeo': 'vollständig innerhalb oder gleich',
	'intersectGeo': 'schneidet',
	'containsGeo': 'umfasst mindestens',

	'kantone': 'Kanton(e)',
	'any2': 'Alle'
};

OpenLayers.Util.extend(OpenLayers.Lang.de, GeoNetwork.GeoCatCh.Lang.de);
