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

Ext.namespace('GeoNetwork');

/**
 * Function: GeoNetwork.TimeSelector
 * Constructor. A form panel to let the user change the date time for
 * a time-aware WMS layer.
 *
 * Parameters:
 * config - {Object}
 */
GeoNetwork.TimeSelector = function(config){
    Ext.apply(this, config);
    GeoNetwork.TimeSelector.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.TimeSelector, Ext.form.FormPanel, { 

    /**
     * APIProperty: layer
     * {<OpenLayers.Layer.WMS>}
     */
    layer: null,

    /**
     * APIProperty: numberOfSteps
     * {Integer} The number of steps to use in the movie loop.
     */
    numberOfSteps: 12,
    
    border: false,
    
    /**
     * Property: originalFormat
     * {String} Original mime type of the layer
     */
    originalFormat: null,
    
    /**
     * Method: initComponent
     * Initialize this component
     */
    initComponent: function() {
        this.buttons = [new Ext.Button({text: OpenLayers.i18n('wmsTimeUpdateButtonText'), 
                handler: this.updateValue, scope: this})];
        GeoNetwork.TimeSelector.superclass.initComponent.call(this);
    },
   
    /**
     * Method: beforeDestroy
     * Before the component is destroyed, unset the animated GIF
     */
    beforeDestroy: function() {
        // make sure the animated GIF is removed
        this.updateValue();
        GeoNetwork.TimeSelector.superclass.beforeDestroy.call(this);
    },

    /**
     * APIMethod: setLayer
     * Attach a WMS layer to this widget
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>}
     */
    setLayer: function(layer) {
        this.layer = layer;
        // store original image format if we want to play a movie in between
        this.originalFormat = this.layer.params.FORMAT || this.layer.params.format;
        if (this.layer.dimensions && this.layer.dimensions.time) {
            this.add(new Ext.form.Label({text: OpenLayers.i18n('WMSTimePositionTitle')}));
            this.add(new Ext.BoxComponent({height: 10}));
            this.add(this.createDateTimeField());
            // check if we can play movies (animated gifs for instance)                
            if (this.layer.dimensions.time.multipleVal) {
                this.add(new Ext.BoxComponent({height: 25}));
                this.add(new Ext.form.Label({text: OpenLayers.i18n('WMSTimeMovieTitle')}));
                this.add(new Ext.BoxComponent({height: 10}));
                this.add({xtype: 'checkbox', 
                    listeners: {check: {fn: this.playMovie, scope: this}},
                    hideLabel: true, 
                    boxLabel: OpenLayers.i18n('WMSTimeAnimationCheckbox', {steps: this.numberOfSteps})});
            }            
            this.doLayout();
        }
    },

    /**
     * Function: getInterval
     * Gets the interval (assumption here is that it is a minute based interval)
     *
     * Parameters:
     * interval - {String} The interval string which has a form of PTXM
     *
     * Returns: 
     * {Integer} The time interval
     */
    getInterval: function(interval) {
        return parseInt(interval.substring(interval.indexOf("PT")+2, interval.indexOf("M")));
    },
    
    /**
     * Method: playMovie
     * This gets called when somebody (un)checks the movie loop checkbox.
     *
     * Parameters:
     * a - {<Ext.form.Checkbox>}
     * checked - {Boolean}
     */
    playMovie: function(a, checked) {
        if (checked) {
            var max, interval, timeRange;
            if (this.layer.dimensions.time.values && this.layer.dimensions.time.values.length > 0) {
                var values = this.layer.dimensions.time.values[0].split("/");
                max = values[1];
                interval = this.getInterval(values[2]);
                var start = Date.parseDate(max, "c");
                start = start - (1000*60*interval*this.numberOfSteps);
                start = new Date(start);
                timeRange = this.formatTimeAsUTC(start) + '/' + max;
            }
            this.layer.mergeNewParams({'TIME': timeRange, 'FORMAT': 'image/gif'});
        } else {
            this.updateValue();
        }
    },

    /**
     * Function: formatTimeAsUTC
     * Change the timezone of the string back to UTC
     *
     * Parameters:
     * dateObj - {Date}
     *
     * Returns:
     * {String} The date time formatted according to ISO8601 and UTC timezone
     */
    formatTimeAsUTC: function(dateObj) {
        // we always need to send times in UTC!
        // TODO: there should be a better way to change the timezone, but I can't find it
        // dateFormat with format 'c' gives back the date in the browser timezone and not UTC.
        var dt = dateObj.dateFormat("c");
        // concatenate with an empty string so it gets to be a string
        var utcHours = '' + dateObj.getUTCHours();
        if (utcHours.length < 2) {
            utcHours = '0'+utcHours;
        }
        dt = dt.replace(dt.substring(dt.indexOf('T'), dt.indexOf('T')+3), 'T'+utcHours);
        // replace timezone part with Z
        dt = dt.replace(dt.substring(dt.indexOf('+'), dt.indexOf('+')+6), 'Z');
        return dt;
    },

    /**
     * Method: updateValue
     * Update the value of the TIME parameter for the WMS layer
     */
    updateValue: function() {
        this.layer.mergeNewParams({
                'TIME': this.formatTimeAsUTC(this.getForm().findField('current').getValue()),
                'FORMAT': this.originalFormat
            }
        );
    },

    /**
     * Function: createDateTimeField
     * Create a date time field which is initialized with the default
     * time value of the WMS layer.
     *
     * Returns:
     * {<Ext.ux.form.DateTime>}
     */
    createDateTimeField: function() {
        var min, max, interval;
        if (this.layer.dimensions.time.values && this.layer.dimensions.time.values.length > 0) {
            var values = this.layer.dimensions.time.values[0].split("/");
            min = values[0];
            max = values[1];
            interval = this.getInterval(values[2]);
        }
        return new Ext.ux.form.DateTime({
            hiddenFormat: "c",
            /* use null so it will default to the locale */
            dateFormat: null,
            hideLabel: true,
            name: 'current',
            dateConfig: {minValue: Date.parseDate(min, "c"), maxValue: Date.parseDate(max, "c")},
            timeConfig: {increment: interval},
            value: (this.layer.params.TIME) ? this.layer.params.TIME : this.layer.dimensions.time["default"],
            width: 340});
    }

});

Ext.reg('gn_timeselector', GeoNetwork.TimeSelector);
