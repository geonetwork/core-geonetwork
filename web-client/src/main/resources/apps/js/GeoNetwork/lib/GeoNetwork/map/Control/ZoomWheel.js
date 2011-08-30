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

if (!window.GeoNetwork) {
    window.GeoNetwork = {};
}
if (!GeoNetwork.Control) {
    GeoNetwork.Control = {};
}

/**
 * Class: GeoNetwork.Control.ZoomWheel
 * A control which uses a wheelhandler to navigate the map (zoom in/out).
 * This control differs from OpenLayers.Control.Navigation wrt not having a
 * zoombox and dragpan.
 *
 * Inherits:
 *  - <OpenLayers.Control>
 */
GeoNetwork.Control.ZoomWheel = OpenLayers.Class(OpenLayers.Control, {

    wheelChange: OpenLayers.Control.Navigation.prototype.wheelChange,

    /**
     * Method: draw
     */
    draw: function() {
        this.handler = new OpenLayers.Handler.MouseWheel( this,
            {'up': OpenLayers.Control.Navigation.prototype.wheelUp,
            'down': OpenLayers.Control.Navigation.prototype.wheelDown });
        this.activate();
    },

    CLASS_NAME: "GeoNetwork.Control.ZoomWheel"

});
