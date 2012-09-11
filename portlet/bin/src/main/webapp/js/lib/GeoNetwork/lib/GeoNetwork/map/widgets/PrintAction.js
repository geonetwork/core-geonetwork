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

Ext.namespace('Geonetwork', 'Geonetwork.print');

/**
 * Class: Geonetwork.print.PrintAction
 * An Ext.Action that generates a PDF based on the Map's extent
 *
 * Inherits from:
 * - {Ext.Action}
 * - {<mapfish.widgets.print.Base>}
 */

/**
 * Constructor: Geonetwork.print.PrintAction
 *
 * Parameters:
 * config - {Object} Config object
 */
Geonetwork.print.PrintAction = function(config) {
    var actionParams = OpenLayers.Util.extend({
        iconCls: 'mf-print-action',
        text: OpenLayers.Lang.translate('mf.print.print'),
        tooltip: OpenLayers.Lang.translate('mf.print.print-tooltip'),
        handler: this.handler,
        scope: this
    }, config);
    Geonetwork.print.PrintAction.superclass.constructor.call(this, actionParams);
    OpenLayers.Util.extend(this, config);

    this.mask = new Ext.LoadMask(this.map.div, {
        msg: OpenLayers.Lang.translate('mf.print.loadingConfig')
    });
    this.initPrint();
};

Ext.extend(Geonetwork.print.PrintAction, Ext.Action, {

    /**
     * APIMethod: handler
     * Called when the action is executed (button pressed or menu entry selected).
     */
    handler: function() {
        if (!this.printing) {
            // The mask is created when going to print to get the correct map size
            this.mask = null;

            this.mask = new Ext.LoadMask(this.map.div, {
                msg: OpenLayers.Lang.translate('mf.print.loadingConfig')
            });
            this.print();
        }
    },

    /**
     * APIMethod: fillSpec
     * Add the page definitions and set the other parameters.
     *
     * Parameters:
     * printCommand - {<mapfish.PrintProtocol>} The print definition to fill.
     */
    fillSpec: function(printCommand) {
        var singlePage = {
            bbox: this.map.getExtent().toArray()
        };
        var params = printCommand.spec;
        params.pages.push(singlePage);
        params.layout = this.getCurLayout();
    },

    /**
     * APIFunction: getCurDpi
     *
     * Returns:
     * the first DPI.
     */
    getCurDpi: function() {
        return this.config.dpis[0].value;
    },

    /**
     * APIMethod: getCurLayout
     *
     * Returns:
     * the first Layout.
     */
    getCurLayout: function() {
        return this.config.layouts[0].name;
    }
});

OpenLayers.Util.applyDefaults(Geonetwork.print.PrintAction.prototype, mapfish.widgets.print.Base);

Ext.reg('gn_printaction', Geonetwork.print.PrintAction);
