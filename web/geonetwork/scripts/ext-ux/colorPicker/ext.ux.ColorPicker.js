/**
 * Ext.ux.ColorPicker Extension Class for ExtJs 2.0
 *
 * @author Pierre GIRAUD (pierre.giraud@camptocamp.com)
 *
 * @class Ext.ux.ColorPicker
 * @extends Ext.Component
 * Simple color picker class for choosing colors.  The picker can be rendered to any container.<br />
 * Here's an example of typical usage:
 * <pre><code>
var cp = new Ext.ux.ColorPicker({value:'#993300'});  // initial selected color
cp.render('my-div');

cp.on('select', function(picker, selColor){
    // do something with selColor
});
</code></pre>
 * @constructor
 * Create a new ColorPicker
 * @param {Object} config The config object
 *
 * This work has been initialy inspired by :
 *  - http://ryanpetrello.com/ext-ux/ColorField/
 *  - http://ux.theba.hu/colorpicker2/ and http://ux.theba.hu/cp/
 *
 *
 * 
 * 
 * TODO (enhancements)
 * - add HSV and RGB fields
 * - get image with 256 pixels dimensions
 * - allow nicer images for thumbs
 * - improve performance by comparing values (before moving thumbs for example) or by caching some values
 */
Ext.ux.ColorPicker = function(config){
    Ext.ux.ColorPicker.superclass.constructor.call(this, config);
    this.addEvents(
        /**
         * @event select
         * Fires when a color is selected
         * @param {ColorPicker} this
         * @param {String} color The 6-digit color hex code (without the # symbol)
         */
        'select'
    );
    
    if (!this.value) {
        this.value = this.defaultValue;
    }

    if (this.handler){
        this.on("select", this.handler, this.scope, true);
    }
};
Ext.extend(Ext.ux.ColorPicker, Ext.Component, {
    /**
     * @cfg {String} itemCls
     * The CSS class to apply to the containing element (defaults to "x-color-picker")
     */
    itemCls : "x-color-picker",
    /**
     * @cfg {String} value
     * The initial color to highlight (should be a valid 6-digit color hex code with the # symbol).  Note that
     * the hex codes are case-sensitive.
     */
    value : null,
    // private
    ctype: "Ext.ux.ColorPicker",
    
    /**
     * The rgb picker
     */
    rgbPicker: null,
    
    /**
     * The hue picker
     */
    hueSlider: null,
    
    /**
     * the hsv value
     */
    HSV: {
        h: 0,
        s: 0,
        v: 0
    },
    
    defaultValue: '#FFFFFF',

    // private
    onRender : function(container, position){
        var el = document.createElement("div");
        el.id = this.getId();
        el.className = this.itemCls;
        
        container.dom.insertBefore(el, position);
        this.createRgbPicker(el);
        this.createHuePicker(el);
        
        this.el = Ext.get(el);
    },

    // private
    afterRender : function(){
        Ext.ux.ColorPicker.superclass.afterRender.call(this);

        if (this.value){
            var s = this.value;
            this.value = null;
            this.setColor(s);
        }
        
        // Initialize RGB Picker DD
        this.rgbDD = new Ext.dd.DD(this.rgbThumb, 'rgbPicker');
        this.rgbDD.startDrag = (function() {
            this.rgbDD.constrainTo(this.rgbInnerEl, - parseInt(this.halfRgbThumb), true);
        }).createDelegate(this);
        this.rgbDD.endDrag = this.onDragEnd.createDelegate( this );
    },
    
    /**
     *
     * @param {String} rgb color with the '#'
     */
    setColor: function(hex) {
        var hsv = this.rgbToHsv(this.hexToRgb(hex));
        this.HSV = {
            h: hsv[0],
            s: hsv[1],
            v: hsv[2]
        }
        this.updateColor();
    },
    
    /**
     * Creates the rgb picker and its thumb
     */
    createRgbPicker: function(el) {
        this.rgbPicker = Ext.DomHelper.append(el, {
            cls: 'x-cp-rgbpicker',
            cn: {
                'cls': 'x-cp-rgbpicker-inner'
            }
        }, true);
        
        this.rgbInnerEl = this.rgbPicker.first();
                
        this.rgbThumb = Ext.DomHelper.append(el, {
            cls: 'x-cp-rgbpicker-thumb'
        }, true);
        // we admit that the thumb is square
        this.halfRgbThumb = this.rgbThumb.getWidth()/2;

        // initialize onclick on the rgb picker
        this.rgbInnerEl.on( 'mousedown', this.rgbOnMouseDown, this);
        // initialize start position
        this.rgbThumb.moveTo(this.rgbPicker.getLeft() - this.halfRgbThumb,
            this.rgbPicker.getTop() - this.halfRgbThumb);
    },
    
    /**
     * Creates the hue picker and its thumb
     */
    createHuePicker: function(el) {
        var div = Ext.DomHelper.append(el, {
            cls: 'x-cp-hueslider'
        });
    
        this.hueSlider = new Ext.Slider({
            renderTo: div,
            vertical: true,
            height: 183,
            minValue: 0,
            maxValue: 360
        });
        
        this.hueSlider.on('changecomplete', function(slider, value) {
            this.HSV.h = value;
            this.updateColor();
        }, this /* scope */);
    },
    
    /**
     *
     */
    rgbOnMouseDown: function(e) {
        if (e.target != this.rgbThumb.dom) {
            var el = this.rgbInnerEl;
            var local = el.translatePoints(e.getXY());
            this.HSV.s = local.left / el.getWidth();
            var height = el.getHeight();
            this.HSV.v = (height - local.top) / height;
            this.updateColor();
        }
    },
    
    /**
     * 
     */
    onDragEnd: function(e) {
        var el = this.rgbInnerEl;
        var local = el.translatePoints(this.rgbThumb.getXY());
        this.HSV.s = (local.left + this.halfRgbThumb) / el.getWidth();
        var height = el.getHeight();
        this.HSV.v = (height - local.top - this.halfRgbThumb) / height;
        this.updateColor();
    },
    
    /**
     *
     * Parameters
     * animate {Boolean] true to animate
     * silent {Boolean} if set to true don't fire any event
     */
    updateColor: function(animate, silent) {
        var rgb = this.hsvToRgb(this.HSV.h, this.HSV.s, this.HSV.v);
        this.hueSlider.setValue(this.HSV.h);
        this.updateRgbPosition(animate !== false);
        this.updateRgbPickerBgColor();
        if (!silent) {
            this.fireEvent('select', this, '#' + this.rgbToHex(rgb));
        }
    },
    
    /**
     *
     */
    updateRgbPosition: function(animate) {
        var el = this.rgbInnerEl;
        var x = this.HSV.s * el.getWidth();
        var height = el.getHeight();
        var y = height - (this.HSV.v * height);
        this.rgbThumb.moveTo(
            this.rgbPicker.getLeft() + x - this.halfRgbThumb,
            this.rgbPicker.getTop() + y - this.halfRgbThumb,
            animate
        );
    },
    
    /**
     *
     */
    updateRgbPickerBgColor: function(color) {
        this.rgbInnerEl.setStyle(
            {'background-color': '#' + this.rgbToHex(this.hsvToRgb(this.HSV.h, 1, 1))}
        );
    },
    
    /**
     * Convert HSV color format to RGB color format
     * @param {Integer/Array( h, s, v )} h
     * @param {Integer} s (optional)
     * @param {Integer} v (optional)
     * @return {Array}
     */
    hsvToRgb: function( h, s, v ) {
        if( h instanceof Array ) { return this.hsvToRgb.call( this, h[0], h[1], h[2] ); }
        var r, g, b, i, f, p, q, t;
        i = Math.floor( ( h / 60 ) % 6 );
        f = ( h / 60 ) - i;
        p = v * ( 1 - s );
        q = v * ( 1 - f * s );
        t = v * ( 1 - ( 1 - f ) * s );
        switch(i) {
            case 0: r=v; g=t; b=p; break;
            case 1: r=q; g=v; b=p; break;
            case 2: r=p; g=v; b=t; break;
            case 3: r=p; g=q; b=v; break;
            case 4: r=t; g=p; b=v; break;
            case 5: r=v; g=p; b=q; break;
        }
        return [this.realToDec( r ), this.realToDec( g ), this.realToDec( b )];
    },
    
    /**
     * Convert RGB color format to Hexa color format
     * @param {Integer/Array( r, g, b )} r
     * @param {Integer} g (optional)
     * @param {Integer} b (optional)
     * @return {String}
     */
    rgbToHex: function(r, g, b) {
        if (r instanceof Array) {
            return this.rgbToHex.call(this, r[0], r[1], r[2]);
        }

        var chars = '0123456789ABCDEF';

        return (
            chars.charAt(parseInt(r/16)) + chars.charAt(parseInt(r%16)) +
            chars.charAt(parseInt(g/16)) + chars.charAt(parseInt(g%16)) +
            chars.charAt(parseInt(b/16)) + chars.charAt(parseInt(b%16))
        );
    },
    
    /**
     * Convert RGB color format to HSV color format
     * @param {Integer/Array( r, g, b )} r
     * @param {Integer} g (optional)
     * @param {Integer} b (optional)
     * @return {Array}
     */
    rgbToHsv: function( r, g, b ) {
        if( r instanceof Array ) { return this.rgbToHsv.call( this, r[0], r[1], r[2] ); }
        r = r / 255;
        g = g / 255;
        b = b / 255;
        var min, max, delta, h, s, v;
        min = Math.min( Math.min( r, g ), b );
        max = Math.max( Math.max( r, g ), b );
        delta = max - min;
        switch (max) {
            case min: h = 0; break;
            case r:   h = 60 * ( g - b ) / delta;
                      if ( g < b ) { h += 360; }
                      break;
            case g:   h = ( 60 * ( b - r ) / delta ) + 120; break;
            case b:   h = ( 60 * ( r - g ) / delta ) + 240; break;
        }
        s = ( max === 0 ) ? 0 : 1 - ( min / max );
        return [Math.round( h ), s, max];
    },
    
    /**
     * Convert a float to decimal
     * @param {Float} n
     * @return {Integer}
     */
    realToDec: function( n ) {
        return Math.min( 255, Math.round( n * 256 ) );
    },
    
    /**
     * Convert a hexa string to RGB color format
     * @param {String} hex
     * @return {Array}
     */
    hexToRgb: function(hex) {
        var h2d = function(d){ return parseInt(d, 16); }
        var rgb = [
            h2d(hex.slice(1, 3)),
            h2d(hex.slice(3, 5)),
            h2d(hex.slice(5))
        ];

        return rgb;
    }
});
Ext.reg('colorpicker', Ext.ux.ColorPicker);