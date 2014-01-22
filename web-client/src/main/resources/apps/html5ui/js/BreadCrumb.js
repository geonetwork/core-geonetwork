/*
 * Copyright (C) 2012 GeoNetwork
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
 * @author Maria Arias de Reyna
 */

Ext.namespace('GeoNetwork');

/**
 * All breadcrumb functionality
 */
GeoNetwork.BreadCrumb = function() {

    // Public Space
    return {
        previous : [],
        current : '',
        separator : "  >>  ",
        div : "bread-crumb-app",
        defaultSteps : [ {
            text : OpenLayers.i18n("HOME"),
            func : "showBrowse()"
        }, {
            text : OpenLayers.i18n("SEARCH RESULTS"),
            func : "showSearch()"
        }, {
            text : OpenLayers.i18n("ABOUT"),
            func : "showAbout()"
        } ],
        /**
         * Adds another crumb to the breadcrumb.
         * 
         * @param crumb,
         *            which should have a text to display and a func to execute
         *            when clicking on it
         */
        setCurrent : function(crumb) {
            if (this.current) {
                this.previous.push(this.current);
            }
            this.current = crumb;
            this.update();
        },
        /**
         * Deletes all previous bread crumbs and replaces it with param
         * 
         * @param prev
         * @returns
         */
        setPrevious : function(prev) {
            this.previous = prev;
            this.current = null;
        },
        /**
         * Deletes all previous bread crumbs and replaces it with
         * defaultPrevious
         * 
         * @param index
         * @returns
         */
        setDefaultPrevious : function(index) {
            var i = 0;
            var prev = [];

            while (i < index && i < this.defaultSteps.length) {
                prev.push(this.defaultSteps[i]);
                i++;
            }
            this.previous = prev;
            this.current = null;
        },

        /**
         * Updates the breadcrumb with the existing data
         */
        update : function() {
            var breadcrumb = null;
            var separator = this.separator;

            if (this.previous.length == 0) {
                Ext.get(this.div).update('');
                return;
            }

            Ext.each(this.previous, function(elem) {
                if (breadcrumb) {
                    breadcrumb = breadcrumb + separator;
                } else {
                    breadcrumb = '';
                }
                breadcrumb = breadcrumb + "<a href=\"javascript:" + elem.func
                        + "\">" + elem.text + "</a>";
            });

            if (breadcrumb) {
                breadcrumb = breadcrumb + this.separator;
            } else {
                breadcrumb = '';
            }
            breadcrumb = breadcrumb + "<span class=\"current\">" + this.current.text + "</span>";

            if (this.div) {
                Ext.get(this.div).update(breadcrumb);
            }
        }
    }

}
