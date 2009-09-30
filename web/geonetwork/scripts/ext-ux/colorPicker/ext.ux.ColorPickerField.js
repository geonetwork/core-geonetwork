Ext.namespace('Ext.ux');

/**
 * Ext.ux.ColorPickerField Extension Class for ExtJs 2.0
 *
 * @author Pierre GIRAUD (pierre.giraud@camptocamp.com)
 *
 * @class Ext.ux.ColorPickerField
 * @extends Ext.form.TriggerField
 * A trigger field that colors its own background based on the input value.  The
 *     value may be any one of the 16 W3C supported CSS color names
 *     (http://www.w3.org/TR/css3-color/).  The value can also be an arbitrary
 *     RGB hex value prefixed by a '#' (e.g. '#FFCC66').
 *
 *     Clicking on the trigger opens a ColorPickerMenu.
 * 
 * @constructor
 * Create a new ColorPickerField
 * @param {Object} config The config object
 *
 * Here's an example of typical usage:
 * <pre><code>
var cpf = new Ext.ux.ColorPickerField({
    fieldLabel: 'Choose a color',
    value: '#0A9F50'
});
cpf.on('valid', function(field) {
    Ext.example.msg('Color Selected', 'You chose {0}.', field.getValue());
});
</code></pre>
 *
 */
Ext.ux.ColorPickerField = function(config){
    Ext.ux.ColorPickerField.superclass.constructor.call(this, config);

    this.on({
        'change': this.setStyle,
        'valid': this.setStyle,
        scope: this
    });
};
Ext.extend(Ext.ux.ColorPickerField, Ext.form.TriggerField, {
    
    triggerClass : 'x-form-color-trigger',

    // The Javascript RegExp object to be tested against the field value during validation (See Ext.TextField)
    regex: /^#[0-9a-f]{6}$/i,

    // The error text to display if the test fails during validation (See Ext.TextField)
    regexText: 'This is not a valid color',

    /**
     * Property: cssColors
     * {Object} Properties are supported CSS color names.  Values are RGB hex
     *     strings (prefixed with '#').
     */
    cssColors: {
        aqua: "#00FFFF",
        black: "#000000",
        blue: "#0000FF",
        fuchsia: "#FF00FF",
        gray: "#808080",
        green: "#008000",
        lime: "#00FF00",
        maroon: "#800000",
        navy: "#000080",
        olive: "#808000",
        purple: "#800080",
        red: "#FF0000",
        silver: "#C0C0C0",
        teal: "#008080",
        white: "#FFFFFF",
        yellow: "#FFFF00"
    },
    
    /**
     * Method: isDark
     * Determine if a color is dark by avaluating brightness according to the
     *     W3C suggested algorithm for calculating brightness of screen colors.
     *     http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
     *
     * Parameters:
     * hex - {String} A RGB hex color string (prefixed by '#').
     *
     * Returns:
     * {Boolean} The color is dark.
     */
    isDark: function(hex) {
        var dark = false;
        if(hex) {
            // convert hex color values to decimal
            var r = parseInt(hex.substring(1, 3), 16) / 255;
            var g = parseInt(hex.substring(3, 5), 16) / 255;
            var b = parseInt(hex.substring(5, 7), 16) / 255;
            // use w3C brightness measure
            var brightness = (r * 0.299) + (g * 0.587) + (b * 0.144);
            dark = brightness < 0.5;
        }
        return dark;
    },
    
    setStyle: function() {
        var color = this.getValue();
        var hex = this.colorToHex(color) || "#ffffff";
        this.getEl().setStyle({
            "background": hex,
            "color": this.isDark(hex) ? "#ffffff" : "#000000"
        });
    },
    
    /**
     * Method: getHexValue
     * As a compliment to the field's getValue method, this method always
     *     returns the RGB hex string representation of the current value
     *     in the field (given a named color or a hex string).
     *
     * Returns:
     * {String} The RGB hex string for the field's value (prefixed with '#').
     */
    getHexValue: function() {
        return this.colorToHex(this.getValue());
    },
    
    /**
     * Method: colorToHex
     * Return the RGB hex representation of a color string.  If a CSS supported
     *     named color is supplied, the hex representation will be returned.
     *     If a non-CSS supported named color is supplied, null will be
     *     returned.  If a RGB hex string is supplied, the same will be
     *     returned.
     *
     * Returns:
     * {String} A RGB hex color string or null if none found.
     */
    colorToHex: function(color) {
        var hex;
        if(color.match(this.regex)) {
            hex = color;
        } else {
            hex = this.cssColors[color.toLowerCase()] || null;
        }
        return hex;
    },
    
    // private
    menuListeners : {
        select: function(m, d){
            this.setValue(d);
            this.fireEvent('select', m, d);
        },
        show : function(){ // retain focus styling
            this.onFocus();
        },
        hide : function(){
            this.focus.defer(10, this);
            var ml = this.menuListeners;
            this.menu.un("select", ml.select,  this);
            this.menu.un("show", ml.show,  this);
            this.menu.un("hide", ml.hide,  this);
        }
    },
    
    onRender : function(ct, position){
        Ext.ux.ColorPickerField.superclass.onRender.call(this, ct, position);
        this.fireEvent('change', this, this.getValue());  
    },
    
    // private
    // Implements the default empty TriggerField.onTriggerClick function to display the ColorPicker
    onTriggerClick : function(){
        if(this.disabled){
            return;
        }
        var value = this.isValid() ? this.getValue() : '#FFFFFF';
        if(this.menu == null){
            this.menu = new Ext.ux.ColorPickerMenu({
                hideOnClick: false,
                value: value
            });
        } else {
            this.menu.picker.setColor(value);
        }
        this.menu.on(Ext.apply({}, this.menuListeners, {
            scope:this
        }));
        this.menu.show(this.el);
    }
    
});
Ext.reg('ext.ux.colorpicker', Ext.ux.ColorPickerField);

Ext.ux.ColorPickerMenu = function(config){
    Ext.ux.ColorPickerMenu.superclass.constructor.call(this, config);
    this.plain = true;
    var ci = new Ext.ux.ColorItem(config);
    this.add(ci);
    /**
     * The {@link Ext.ux.ColorPicker} instance for this ColorMenu
     * @type ColorPicker
     */
    this.picker = ci.picker;
    /**
     * @event select
     * @param {ColorPicker} palette
     * @param {String} color
     */
    this.relayEvents(ci, ["select"]);
};
Ext.extend(Ext.ux.ColorPickerMenu, Ext.menu.Menu, {
    //private
    beforeDestroy: function(){
        this.picker.destroy();
    }
});

Ext.ux.ColorItem = function(config){
    Ext.ux.ColorItem.superclass.constructor.call(this, new Ext.ux.ColorPicker(config), config);
    this.picker = this.component;
    this.relayEvents(this.picker, ["select"]);
    if(this.selectHandler){
        this.on('select', this.selectHandler, this.scope);
    }
};
Ext.extend(Ext.ux.ColorItem, Ext.menu.Adapter);
