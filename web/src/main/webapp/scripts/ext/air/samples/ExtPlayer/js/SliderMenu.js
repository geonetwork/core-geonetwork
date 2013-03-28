/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.menu.SliderItem = function(config){
    Ext.menu.SliderItem.superclass.constructor.call(this, new Ext.Slider(config), config);
    this.slider = this.component;
    this.relayEvents(this.slider, ["change","changecomplete"]);
    if (this.changeHandler) {
        this.slider.on('change', this.changeHandler, this.scope);
    }  
};
Ext.extend(Ext.menu.SliderItem, Ext.menu.Adapter);

Ext.menu.SliderMenu = function(config){
    Ext.menu.SliderMenu.superclass.constructor.call(this, config);
    this.plain = true;
    var ci = new Ext.menu.SliderItem(config);
    this.add(ci);
    this.slider = ci.slider;
    this.relayEvents(ci, ["change","changecomplete"]);
};
Ext.extend(Ext.menu.SliderMenu, Ext.menu.Menu);
