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
 * Class: GeoNetwork.DisclaimerWindow
 *      Window to show the disclaimer of a WMS Capabilities.
 *
 * Inherits from:
 *  - {Ext.Window}
 */

/**
 * Constructor: GeoNetwork.DisclaimerWindow
 * Create an instance of GeoNetwork.DisclaimerWindow
 *
 * Parameters:
 * config - {Object} A config object used to set
 *     window's properties.
 */
GeoNetwork.DisclaimerWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.DisclaimerWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.DisclaimerWindow, Ext.Window, {

    /**
     * APIProperty: disclaimer to show
     * {<String>}
     */
    disclaimer: null,

    /**
     * Method: init
     *     Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.BaseWindow.superclass.initComponent.call(this);

        this.id = "disclaimerwindow";
        this.constrainHeader = true;
        this.layout = 'fit';
        this.plain = true;
        this.stateful = false;
        this.title = OpenLayers.i18n("disclaimer.windowTitle");
        this.minWidth = 440;
        this.minHeight = 280;
        this.width = 440;
        this.height = 280;
        this.autoScroll = true;
        this.modal = true;
        
        this.addButton(OpenLayers.i18n("disclaimer.buttonClose"),
                function(){
                    this.close();
                }, this);

        if (OpenLayers.String.startsWith(this.disclaimer, "http://")) {
            this.on("show", this.showDisclaimerUrl);
            
        } else {
            var textArea = new Ext.form.TextArea({
                hideLabel: true,
                name: 'msg',
                value: this.disclaimer,
                anchor: '100% -53',  // anchor width by percentage and height by raw adjustment
                enableKeyEvents: true,
                listeners: {'keydown': function(field, event)
                    {
                        // We let copy text
                          if (!(event.getKey() == 67 && event.ctrlKey)){
                                event.stopEvent();
                          }
                     }
                }

            });

            this.add(textArea);
        }
        
        this.doLayout();
    },

    /**
     * Method: init
     *     Loads the disclaimer url in the window
     */
    showDisclaimerUrl: function() {
        this.load({
            url: OpenLayers.ProxyHost + this.disclaimer,
            text: OpenLayers.i18n("disclaimer.loading"),
            timeout: 30,
            scripts: false});
    }
});
