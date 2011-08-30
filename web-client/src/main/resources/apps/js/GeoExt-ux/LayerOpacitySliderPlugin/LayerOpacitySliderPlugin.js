/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.tree");

/** api: (define)
 *  module = GeoExt.tree
 *  class = LayerOpacitySliderPlugin
 *  
 *  Adapted from http://rcoos.org/carolinasrcoosrev2/secoora_interactive_tst.html
 */

GeoExt.tree.LayerOpacitySliderPlugin = Ext.extend(Ext.util.Observable, {

	// slider: null,

	constructor : function(config) {
		Ext.apply(this.initialConfig, Ext.apply({}, config));
		Ext.apply(this, config);
		this.addEvents("opacityslide");
		GeoExt.tree.LayerOpacitySliderPlugin.superclass.constructor.apply(this,
				arguments);
	},

	init : function(tree) {
		tree.on({
			"rendernode" : this.onRenderNode,
			//"rawslidenode" : this.onRawSlideNode,
			scope : this
		});
	},

	onRenderNode : function(node) {
		var a = node.attributes;
		var layer = node.layer;
		// The loader param slider has to exist as well as another configuration
		// option, opacitySlider, that will look
		// to see if the individual layer wants to use a slider. It is possible
		// that some
		// layers grouped together in a node may or may not use a slider. If
		// opacitySlider is not defined, the slider will get created.
		
		if (a.slider) { 
			this.indentMarkup = node.parentNode ? node.parentNode.ui
					.getChildIndent() : '';
			var elID = node.id + '-tree-slider-';
			elID = Ext.id(null, elID);
			// Use the table to force the slider onto the same line as the
			// indents. Otherwise since the slider object
			// creates <div> tags the browser will put it on a seperate line.
			buf = [ '<br/>', '<table><tr>', '<td>',
					'<span class="x-tree-node-indent">', this.indentMarkup,
					"</span>", '</td><td>',
					'<span class="x-tree-node-indent">', this.indentMarkup,
					"</span>", '</td><td>', '<a id=', elID, '></a>', '</td>',
					'</tr></table>' ];

			Ext.DomHelper.insertAfter(node.ui.anchor, buf.join(""));

			var initValue = 100;
			var slider = new Ext.Slider({
				minValue : 0,
				maxValue : 100,
				value : initValue,
				width : 100,
				aggressive : true,
				layer: layer,
				plugins : new GeoExt.LayerOpacitySliderTip()
			});
			// This is needed to get the thumb on the slider to the correct
			// initial point. The base code
			// for the Ext.slider calls innerEl.getWidth() which does not work
			// since at this point the slider
			// isn't rendered.
			Ext.override(Ext.Slider, {
				getRatio : function() {
					var w = this.innerEl.getComputedWidth();
					var v = this.maxValue - this.minValue;
					return v == 0 ? w : (w / v);
				}
			});
			slider.on("change", function(slider, value, oldValue) {
				this.layer.setOpacity(value / 100.0);		
			});
			slider.render(elID);
		}
	}
});

Ext.preg
		&& Ext.preg("gx_layeropacitysliderplugin",
				GeoExt.tree.LayerOpacitySliderPlugin);