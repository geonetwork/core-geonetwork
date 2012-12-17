/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace('GeoNetwork');

GeoNetwork.Message = function () {
    var msgCt;
    function createBox(t, s, c) {
        return ['<div class="alert"><span class="label label-', c, '">', t, '</span><div>', s, '</div></div>'].join('');;
    }
    return {
        /** api: method[msg]
         *  :param config: ``Object`` configuration for the message
         *    title: the message title
         *    msg: the message content
         *    tokens: the token to substitute in the msg if any. Default is undefined.
         *    status: the CSS class to use (success, warning). Default style if undefined.
         *    target: an element id. document if undefined
         * 
         * 
         *  Display a message or alert
         */
        msg : function (config) {
            // title, format, status, target
            if (!msgCt) {
                msgCt = Ext.DomHelper.insertFirst(document.body, {id: 'msg-div'}, true);
            }
            msgCt.alignTo(config.target || document, 't-t');
            var s = OpenLayers.String.format(config.msg, config.tokens);
            var m = Ext.DomHelper.append(msgCt, {html: createBox(config.title, s, config.status)}, true);
            m.slideIn('t').pause(2).fadeOut({remove: true});
        }
    };
};
