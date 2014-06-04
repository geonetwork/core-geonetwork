/**
 * @class Ext.ux.form.ISODateTime
 * @extends Ext.form.Field
 *
 * ISODateTime field, combination of DateField and TimeField with widget
 * handling as per Ext.ux.form.DateTime but using moment.js to specifically 
 * handle ISO date-time fields (gco:DateTime).
 *
 * Basis is:
 * @author      Ing. Jozef Sak<E1>lo<9A>
 * @copyright (c) 2008, Ing. Jozef Sak<E1>lo<9A>
 * @version   2.0
 * @revision  $Id: Ext.ux.form.DateTime.js 813 2010-01-29 23:32:36Z jozo $
 *
 * @author sppigot - adapt to handle ISO date times using moment.js -
 * depends on moment.js : http://momentjs.com
 *
 */

Ext.ns('Ext.ux.form');

/**
 * Creates new ISODateTime
 * @constructor
 * @param {Object} config A config object
 */
Ext.ux.form.ISODateTime = Ext.extend(Ext.form.Field, {
    /**
     * @cfg {String} hiddenFormat Format of datetime used to store value in hidden field
     * and submitted to server (defaults to 'YYYY-MM-DDTHH:mm:ss' ISO DateTime)
		 * NOTE: This format doesn't use the ExtJS Date formatting! It uses 
		 * moment.js date formatting as per 
		 * http://momentjs.com/docs/#/parsing/string-format/
     */
     hiddenFormat: 'YYYY-MM-DDTHH:mm:ss'
		 ,hiddenTimeFormats: "HH:mm|HH:mm:ss|HH:mm:ss.S|HH:mm:ss.SS|HH:mm:ss.SSS|hh:mm|hh:mm:ss|hh:mm:ss.S|hh:mm:ss.SS|hh:mm:ss.SSS"
    /**
     * @cfg {Function} dateValidator A custom validation function to be called during date field
     * validation (defaults to null)
     */
    ,dateValidator:null
    /**
     * @cfg {String/Object} defaultAutoCreate DomHelper element spec
     * Let superclass to create hidden field instead of textbox. Hidden will be submittend to server
     */
    ,defaultAutoCreate:{tag:'input', type:'hidden'}
    /**
     * @cfg {Boolean} emptyToNow Set field value to now on attempt to set empty value.
     * If it is true then setValue() sets value of field to current date and time (defaults to false)
     */
    /**
     * @cfg {String} timePosition Where the time field should be rendered. 'right' is suitable for forms
     * and 'below' is suitable if the field is used as the grid editor (defaults to 'right')
     */
    ,timePosition:'right' // valid values:'below', 'right'
    /**
     * @cfg {Function} timeValidator A custom validation function to be called during time field
     * validation (defaults to null)
     */
    ,timeValidator:null
    /**
     * @cfg {Number} timeWidth Width of time field in pixels (defaults to 100)
     */
    ,timeWidth:100
    /**
     * @cfg {String} dateFormat Format of DateField. Can be localized. (defaults to 'm/y/d')
     */
    ,dateFormat:'m/d/y'
    /**
     * @cfg {String} dateAltFormats altFormats of DateField. Can be localized. (defaults to DateField value)
     */
    ,dateAltFormats:Ext.form.DateField.prototype.altFormats
    /**
     * @cfg {String} timeFormat Format of TimeField. Can be localized. (defaults to 'g:i A')
     */
    ,timeFormat:'g:i A'
    /**
     * @cfg {String} timeAltFormats altFormats of TimeField. Can be localized. (defaults to TimeField value)
     */
    ,timeAltFormats:Ext.form.TimeField.prototype.altFormats
    /**
     * @cfg {Object} dateConfig Config for DateField constructor.
     */
    /**
     * @cfg {Object} timeConfig Config for TimeField constructor.
     */

    // {{{
    /**
     * @private
     * creates DateField and TimeField and installs the necessary event handlers
     */
    ,initComponent:function() {
        // call parent initComponent
        Ext.ux.form.ISODateTime.superclass.initComponent.call(this);

        // create DateField
        var dateConfig = Ext.apply({}, {
             id:this.id + '-date'
            ,format:this.dateFormat || Ext.form.DateField.prototype.format
            ,altFormats:this.dateAltFormats || Ext.form.DateField.prototype.altFormats
            ,width:this.timeWidth
            ,selectOnFocus:this.selectOnFocus
            ,validator:this.dateValidator
            ,beforeBlur: function(){} // don't perform check on blur which may changes the value.
            ,listeners:{
                  blur:{scope:this, fn:this.onBlur}
                 ,focus:{scope:this, fn:this.onFocus}
            }
        }, this.dateConfig);
        this.df = new Ext.ux.form.DateFieldFormats(dateConfig);
        this.df.ownerCt = this;
        delete(this.dateFormat);
        delete(this.dateAltFormats);

        // create TimeField
        var timeConfig = Ext.apply({}, {
             id:this.id + '-time'
            ,format:this.timeFormat || Ext.form.TimeField.prototype.format
            ,altFormats:this.timeAltFormats || Ext.form.TimeField.prototype.altFormats
            ,width:this.timeWidth
            ,selectOnFocus:this.selectOnFocus
            ,validator:this.timeValidator
            ,listeners:{
                  blur:{scope:this, fn:this.onBlur}
                 ,focus:{scope:this, fn:this.onFocus}
            }
        }, this.timeConfig);
        this.tf = new Ext.form.TimeField(timeConfig);
        this.tf.ownerCt = this;
        delete(this.timeFormat);
        delete(this.timeAltFormats);

        // relay events
        this.relayEvents(this.df, ['focus', 'specialkey', 'invalid', 'valid']);
        this.relayEvents(this.tf, ['focus', 'specialkey', 'invalid', 'valid']);

        this.on('specialkey', this.onSpecialKey, this);

    } // eo function initComponent
    // }}}
    // {{{
    /**
     * @private
     * Renders underlying DateField and TimeField and provides a workaround for side error icon bug
     */
    ,onRender:function(ct, position) {
        // don't run more than once
        if(this.isRendered) {
            return;
        }

        // render underlying hidden field
        Ext.ux.form.ISODateTime.superclass.onRender.call(this, ct, position);

        // render DateField and TimeField
        // create bounding table
        var t;
        if('below' === this.timePosition || 'bellow' === this.timePosition) {
            t = Ext.DomHelper.append(ct, {tag:'table',style:'border-collapse:collapse',children:[
                 {tag:'tr',children:[{tag:'td', style:'padding-bottom:1px', cls:'ux-datetime-date'}]}
                ,{tag:'tr',children:[{tag:'td', cls:'ux-datetime-time'}]}
            ]}, true);
        }
        else {
            t = Ext.DomHelper.append(ct, {tag:'table',style:'border-collapse:collapse',children:[
                {tag:'tr',children:[
                    {tag:'td',style:'padding-right:4px', cls:'ux-datetime-date'},{tag:'td', cls:'ux-datetime-time'}
                ]}
            ]}, true);
        }

        this.tableEl = t;
        this.wrap = t.wrap({cls:'x-form-field-wrap'});
//        this.wrap = t.wrap();
        this.wrap.on("mousedown", this.onMouseDown, this, {delay:10});

        // render DateField & TimeField
        this.df.render(t.child('td.ux-datetime-date'));
        this.tf.render(t.child('td.ux-datetime-time'));

        // workaround for IE trigger misalignment bug
        // see http://extjs.com/forum/showthread.php?p=341075#post341075
//        if(Ext.isIE && Ext.isStrict) {
//            t.select('input').applyStyles({top:0});
//        }

        this.df.el.swallowEvent(['keydown', 'keypress']);
        this.tf.el.swallowEvent(['keydown', 'keypress']);

        // create icon for side invalid errorIcon
        if('side' === this.msgTarget) {
            var elp = this.el.findParent('.x-form-element', 10, true);
            if(elp) {
                this.errorIcon = elp.createChild({cls:'x-form-invalid-icon'});
            }

            var o = {
                 errorIcon:this.errorIcon
                ,msgTarget:'side'
                ,alignErrorIcon:this.alignErrorIcon.createDelegate(this)
            };
            Ext.apply(this.df, o);
            Ext.apply(this.tf, o);
//            this.df.errorIcon = this.errorIcon;
//            this.tf.errorIcon = this.errorIcon;
        }

        // setup name for submit
        this.el.dom.name = this.hiddenName || this.name || this.id;

        // prevent helper fields from being submitted
        this.df.el.dom.removeAttribute("name");
        this.tf.el.dom.removeAttribute("name");

        // we're rendered flag
        this.isRendered = true;

        // update hidden field
        this.updateHidden();

    } // eo function onRender
    // }}}
    // {{{
    /**
     * @private
     */
    ,adjustSize:Ext.BoxComponent.prototype.adjustSize
    // }}}
    // {{{
    /**
     * @private
     */
    ,alignErrorIcon:function() {
        this.errorIcon.alignTo(this.tableEl, 'tl-tr', [2, 0]);
    }
    // }}}
    // {{{
    /**
     * @private initializes internal dateValue
     */
    ,initDateValue:function() {
        this.dateValue = moment.utc('9999-99-99'); // make it invalid
    }
    // }}}
    // {{{
    /**
     * Calls clearInvalid on the DateField and TimeField
     */
    ,clearInvalid:function(){
        this.df.clearInvalid();
        this.tf.clearInvalid();
    } // eo function clearInvalid
    // }}}
    // {{{
    /**
     * Calls markInvalid on both DateField and TimeField
     * @param {String} msg Invalid message to display
     */
    ,markInvalid:function(msg){
        this.df.markInvalid(msg);
        this.tf.markInvalid(msg);
    } // eo function markInvalid
    // }}}
    // {{{
    /**
     * @private
     * called from Component::destroy. 
     * Destroys all elements and removes all listeners we've created.
     */
    ,beforeDestroy:function() {
        if(this.isRendered) {
//            this.removeAllListeners();
            this.wrap.removeAllListeners();
            this.wrap.remove();
            this.tableEl.remove();
            this.df.destroy();
            this.tf.destroy();
        }
    } // eo function beforeDestroy
    // }}}
    // {{{
    /**
     * Disable this component.
     * @return {Ext.Component} this
     */
    ,disable:function() {
        if(this.isRendered) {
            this.df.disabled = this.disabled;
            this.df.onDisable();
            this.tf.onDisable();
        }
        this.disabled = true;
        this.df.disabled = true;
        this.tf.disabled = true;
        this.fireEvent("disable", this);
        return this;
    } // eo function disable
    // }}}
    // {{{
    /**
     * Enable this component.
     * @return {Ext.Component} this
     */
    ,enable:function() {
        if(this.rendered){
            this.df.onEnable();
            this.tf.onEnable();
        }
        this.disabled = false;
        this.df.disabled = false;
        this.tf.disabled = false;
        this.fireEvent("enable", this);
        return this;
    } // eo function enable
    // }}}
    // {{{
    /**
     * @private Focus date filed
     */
    ,focus:function() {
        this.df.focus();
    } // eo function focus
    // }}}
    // {{{
    /**
     * @private
     */
    ,getPositionEl:function() {
        return this.wrap;
    }
    // }}}
    // {{{
    /**
     * @private
     */
    ,getResizeEl:function() {
        return this.wrap;
    }
    // }}}
    // {{{
    /**
     * @return {Date/String} Returns value of this field
     */
    ,getValue:function() {
        return (this.dateValue && this.dateValue.isValid()) ? this.dateValue.format(this.hiddenFormat) : '';
    } // eo function getValue
    // }}}
    // {{{
    /**
     * @return {Boolean} true = valid, false = invalid
     * @private Calls isValid methods of underlying DateField and TimeField and returns the result
     */
    ,isValid:function() {
        return this.df.isValid() && this.tf.isValid();
    } // eo function isValid
    // }}}
    // {{{
    /**
     * Returns true if this component is visible
     * @return {boolean} 
     */
    ,isVisible : function(){
        return this.df.rendered && this.df.getActionEl().isVisible();
    } // eo function isVisible
    // }}}
    // {{{
    /** 
     * @private Handles blur event
     */
    ,onBlur:function(f) {
        // called by both DateField and TimeField blur events

        // revert focus to previous field if clicked in between
        if(this.wrapClick) {
            f.focus();
            this.wrapClick = false;
        }

        // update underlying value
        if(f === this.df) {
            this.updateDate();
        }
        else {
            this.updateTime();
        }
        this.updateHidden();

        this.validate();

        // fire events later
        (function() {
            if(!this.df.hasFocus && !this.tf.hasFocus) {
                var v = this.getValue();
                if(String(v) !== String(this.startValue)) {
                    this.fireEvent("change", this, v, this.startValue);
                }
                this.hasFocus = false;
                this.fireEvent('blur', this);
            }
        }).defer(100, this);

    } // eo function onBlur
    // }}}
    // {{{
    /**
     * @private Handles focus event
     */
    ,onFocus:function() {
        if(!this.hasFocus){
            this.hasFocus = true;
            this.startValue = this.getValue();
            this.fireEvent("focus", this);
        }
    }
    // }}}
    // {{{
    /**
     * @private Just to prevent blur event when clicked in the middle of fields
     */
    ,onMouseDown:function(e) {
        if(!this.disabled) {
            this.wrapClick = 'td' === e.target.nodeName.toLowerCase();
        }
    }
    // }}}
    // {{{
    /**
     * @private
     * Handles Tab and Shift-Tab events
     */
    ,onSpecialKey:function(t, e) {
        var key = e.getKey();
        if(key === e.TAB) {
            if(t === this.df && !e.shiftKey) {
                e.stopEvent();
                this.tf.focus();
            }
            if(t === this.tf && e.shiftKey) {
                e.stopEvent();
                this.df.focus();
            }
            this.updateValue();
        }
        // otherwise it misbehaves in editor grid
        if(key === e.ENTER) {
            this.updateValue();
        }

    } // eo function onSpecialKey
    // }}}
    // {{{
    /**
     * Resets the current field value to the originally loaded value 
     * and clears any validation messages. See Ext.form.BasicForm.trackResetOnLoad
     */
    ,reset:function() {
        this.df.setValue(this.originalValue);
        this.tf.setValue(this.originalValue);
    } // eo function reset
    // }}}
    // {{{
    /**
     * @private Sets the value of DateField
     */
    ,setDate:function(date) {
				if (date === '') this.df.setValue(date);
				else {
					this.df.setValue(date.format('YYYY-MM-DD'));
				}
    } // eo function setDate
    // }}}
    // {{{
    /** 
     * @private Sets the value of TimeField
     */
    ,setTime:function(date) {
				if (date === '') this.tf.setValue(date);
				else {
        	this.tf.setValue(date.format('HH:mm'));
				}
    } // eo function setTime
    // }}}
    // {{{
    /**
     * @private
     * Sets correct sizes of underlying DateField and TimeField
     * With workarounds for IE bugs
     */
    ,setSize:function(w, h) {
        if(!w) {
            return;
        }
        if('below' === this.timePosition) {
            this.df.setSize(w, h);
            this.tf.setSize(w, h);
            if(Ext.isIE) {
                this.df.el.up('td').setWidth(w);
                this.tf.el.up('td').setWidth(w);
            }
        }
        else {
            this.df.setSize(w - this.timeWidth - 4, h);
            this.tf.setSize(this.timeWidth, h);

            if(Ext.isIE) {
                this.df.el.up('td').setWidth(w - this.timeWidth - 4);
                this.tf.el.up('td').setWidth(this.timeWidth);
            }
        }
    } // eo function setSize
    // }}}
    // {{{
    /**
     * @param {Mixed} val Value to set
     * Sets the value of this field
     */
    ,setValue:function(val) {

        if(!val && true === this.emptyToNow) {
            this.dateValue = moment.utc();
            return;
        }
        else if (!val) {
            this.setDate('');
            this.setTime('');
            this.updateHidden();
            return;
        } else {
						// parse the date using moment - expect all valid ISO forms - moment
						// will parse any ISO 8601 format without a format string
						this.dateValue = moment.utc(val);
				}


        if (this.dateValue.isValid()) {
        	this.setDate(this.dateValue);
        	this.setTime(this.dateValue);
        	this.updateHidden();
				} else {
           // This is a freeform date so lets null the value.
           this.setDate('');
           // Set the value in the superclass so the user can see it...
           Ext.form.DateField.superclass.setValue.call(this.df, val);
        }
    } // eo function setValue
    // }}}
    // {{{
    /**
     * Hide or show this component by boolean
     * @return {Ext.Component} this
     */
    ,setVisible: function(visible){
        if(visible) {
            this.df.show();
            this.tf.show();
        }else{
            this.df.hide();
            this.tf.hide();
        }
        return this;
    } // eo function setVisible
    // }}}
    //{{{
    ,show:function() {
        return this.setVisible(true);
    } // eo function show
    //}}}
    //{{{
    ,hide:function() {
        return this.setVisible(false);
    } // eo function hide
    //}}}
    // {{{
    /**
     * @private Updates the date part
     */
    ,updateDate:function() {

        var d = this.df.getRawValue();
        if(d) {
          this.dateValue = moment.utc(this.df.getRawValue());

          // If the dateValue doesn't have a time (hours == 0) and we have an 
					// instance of t then set that value of t into the dateValue
					if (this.dateValue.hour() === 0) {
           	var t = this.tf.getRawValue();
           	if (t) {
								t = moment.utc(t, this.hiddenTimeFormats.split('|'));	
               	this.dateValue.hour(t.hour());
               	this.dateValue.minute(t.minute());
               	this.dateValue.second(t.second());
           	}
					}
          // Now update the fields.
          this.setDate(this.dateValue);
          this.setTime(this.dateValue);
        } else {
          this.dateValue = moment.utc('9999-99-99'); // make it invalid
          this.setTime('');
        }
    } // eo function updateDate
    // }}}
    // {{{
    /**
     * @private
     * Updates the time part
     */
    ,updateTime:function() {
        var t = this.tf.getRawValue();

        // Need to call updateDate as this will make sure that dateValue is in 
				// ready to accept the time we parse here
        if(t && t!== null && (t.trim() != '') && !(this.dateValue.isValid())) {
           this.updateDate();
        }
        if(t && !this.df.getRawValue()) {
            this.initDateValue();
            this.setDate(this.dateValue);
        }
        if(this.dateValue.isValid()) {
            if (t) {
								t = moment.utc(t, this.hiddenTimeFormats.split('|'));	
                this.dateValue.hour(t.hour());
                this.dateValue.minute(t.minute());
                this.dateValue.second(t.second());
            }
            else {
                this.dateValue.hour(0);
                this.dateValue.minute(0);
                this.dateValue.second(0);
            }
          	// Now update the field.
          	this.setTime(this.dateValue);
        }
    } // eo function updateTime
    // }}}
    // {{{
    /**
     * @private Updates the underlying hidden field value
     */
    ,updateHidden:function() {
        if(this.isRendered) {
            this.el.dom.value = this.dateValue && this.dateValue.isValid() ? this.dateValue.format(this.hiddenFormat) : '';
        }
    }
    // }}}
    // {{{
    /**
     * @private Updates all of Date, Time and Hidden
     */
    ,updateValue:function() {

        this.updateDate();
        this.updateTime();
        this.updateHidden();

        return;
    } // eo function updateValue
    // }}}
    // {{{
    /**
     * @return {Boolean} true = valid, false = invalid
     * calls validate methods of DateField and TimeField
     */
    ,validate:function() {
        return this.df.validate() && this.tf.validate();
    } // eo function validate
    // }}}
    // {{{
    /**
     * Returns renderer suitable to render this field
     * @param {Object} Column model config
     */
    ,renderer: function(field) {
        var format = field.editor.dateFormat || Ext.ux.form.ISODateTime.prototype.dateFormat;
        format += ' ' + (field.editor.timeFormat || Ext.ux.form.ISODateTime.prototype.timeFormat);
        var renderer = function(val) {
            var retval = Ext.util.Format.date(val, format);
            return retval;
        };
        return renderer;
    } // eo function renderer
    // }}}

}); // eo extend

// register xtype
Ext.reg('xisodatetime', Ext.ux.form.ISODateTime);
