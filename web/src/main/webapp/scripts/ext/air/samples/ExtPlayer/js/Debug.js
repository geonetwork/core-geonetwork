/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Ext.air')
Ext.air.Debug = {
	traceEvents: function(observable) {
		Ext.util.Observable.capture(observable, function(en) {
			air.trace('Id: ' + observable.id + ' Event: ' + en);
		});
	},
	dir: function(obj) {
		for (var a in obj) {
			air.trace(a + ': ' + obj[a]);
		}			
	}	
};
