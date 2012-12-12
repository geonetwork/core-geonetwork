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

GeoNetwork.GeoCatCh.Lang.en = {
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

	'withinGeo': 'Completely within or equal',
	'intersectGeo': 'Intersects',
	'containsGeo': 'Contains at least',
    'Login.error.message': 'Login failed. Check your username and password.',
};

OpenLayers.Util.extend(OpenLayers.Lang.en, GeoNetwork.GeoCatCh.Lang.en);
