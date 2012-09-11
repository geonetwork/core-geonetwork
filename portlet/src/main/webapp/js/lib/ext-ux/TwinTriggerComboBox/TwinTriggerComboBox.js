/****
 * Ext.ux.form.TwinTriggerComboBox
 * a Combination of both ComboBox and TwinTriggerField.
 * adds a clear button and
 * adds a button beside the std ComboBox trigger.
 *
 * like this: [ selected value][x][v][+]  <--- the button.
 *
 * usage is identical to Ext.form.ComboBox.
 * listen to the third button via trigger3Click event.
 *
 * <script>
 * // create combo using standard ComboBox configuration.
 * var combo = new Ext.ux.form.TwinTriggerComboBox({
 *     ...
 *     trigger3TipConfig: {
 *         text: 'a text for the tip'
 *     }, // some valid Ext.ToolTip configuration
 *     ...
 * });
 *
 * // listen to clear trigger via "clear" event
 * combo.on('clear', function(ev) {
 *     alert('you clicked the clear button');  // <-- you might show a form on a dialog here
 * });
 *
 * // listen to 3rd trigger via "trigger3Click" event
 * combo.on('trigger3Click', function(ev) {
 *     alert('you clicked the third button');  // <-- you might show a form on a dialog here
 * });
 *
 * </script>
 *
 * NB: you must supply your own style implementation for the 3rd trigger.
 * It has the class "x-form-trigger3-trigger" by default.
 * you might style it like so:
 * <style>
 * .x-form-add-trigger {
 *   background-image: url(/images/icons/add.png) !important;
 *   background-position: center center !important;
 *   cursor: pointer;
 *   border: 0 !important;
 *   margin-left: 2px;
 * }
 * </style>
 *
 *
 * If you only want the clear button, and want to hide the third one, use the following in your config :
 * trigger3Class: 'x-form-trigger-no-width x-hidden'
 * And add the following class to your css :
 * .x-form-trigger-no-width {
 *     width: 0 !important;
 * }
 *
 *
 * Inspired from Chris Scott's great ComboBoxAdd
 * http://extjs.com/forum/showthread.php?t=20511
 *
 */
Ext.namespace("Ext.ux.form");
Ext.ux.form.TwinTriggerComboBox = function(config) {
    Ext.ux.form.TwinTriggerComboBox.superclass.constructor.apply(this, arguments);
};
Ext.extend(Ext.ux.form.TwinTriggerComboBox, Ext.form.ComboBox, {

    /***
     * trigger classes.
     */
    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: '',
    trigger3Class: 'x-form-trigger3-trigger',

    hideTrigger1:true,

    /**
     * @cfg {String} tooltipType
     * The type of tooltip to use. Either "qtip" (default) for QuickTips or "title" for title attribute.
     */
    tooltipType : 'qtip',

    /***
     * initComponent
     */
    initComponent : function(){
        Ext.ux.form.TwinTriggerComboBox.superclass.initComponent.call(this);

        /***
         * @event add
         * @param {field: Ext.ux.form.TwinTriggerComboBox, button: Ext.Element}
         * fires when 2nd trigger is clicked
         */
        this.addEvents({clear : true, trigger3 : true});

        // implement triggerConfig from Ext.form.TwinTriggerField
        this.triggerConfig = {
            tag:'span', cls:'x-form-twin-triggers', cn:[
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger2Class},
            {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger3Class}
        ]};
    },

    /***
     * getTrigger
     * copied from Ext.form.TwinTriggerField
     * @param {Object} index
     */
    getTrigger : function(index){
        return this.triggers[index];
    },

    /***
     * initTrigger
     * copied from Ext.form.TwinTriggerField
     */
    initTrigger : function(){
        var ts = this.trigger.select('.x-form-trigger', true);
        //this.wrap.setStyle('overflow', 'hidden');
        var triggerField = this;
        ts.each(function(t, all, index){
            t.hide = function(){
                var w = triggerField.wrap.getWidth();
                this.dom.style.display = 'none';
                triggerField.el.setWidth(w-triggerField.trigger.getWidth());
            };
            t.show = function(){
                var w = triggerField.wrap.getWidth();
                this.dom.style.display = '';
                triggerField.el.setWidth(w-triggerField.trigger.getWidth());
            };
            var triggerIndex = 'Trigger'+(index+1);

            if(this['hide'+triggerIndex]){
                t.dom.style.display = 'none';
            }
            t.on("click", this['on'+triggerIndex+'Click'], this, {preventDefault:true});
            t.addClassOnOver('x-form-trigger-over');
            t.addClassOnClick('x-form-trigger-click');
        }, this);
        this.triggers = ts.elements;

        if (this.trigger3TipConfig) {
            var config = {
                target: this.getTrigger(2)
            };
            for (var i in this.helpTipConfig) {
                config[i] = this.helpTipConfig[i];
            }
            var tip = new Ext.ToolTip(config);
        }

        if (this.trigger3TipConfig){
            if (typeof this.trigger3TipConfig == 'object'){
                Ext.QuickTips.register(Ext.apply({
                      target: this.getTrigger(2)
                }, this.trigger3TipConfig));
            } else {
                this.getTrigger(2).dom[this.tooltipType] = this.trigger3TipConfig;
            }
        }
    },

    /***
     * onTrigger1Click
     * event triggered when clicking on the clear trigger
     */
    onTrigger1Click : function() {
        this.clearValue();
        this.triggerBlur.defer(50, this);
    },

    /***
     * onTrigger2Click
     * defer to std ComboBox trigger method
     */
    onTrigger2Click : function() {
        this.onTriggerClick();
    },

    /***
     * onTrigger3Click
     * this is the third button handler.  fire 'trigger3Click' event
     */
    onTrigger3Click : function() {
        this.fireEvent('trigger3Click', this);
    },

    /**
     * private onSelect
     */
    onSelect: function(record, index) {
        Ext.ux.form.TwinTriggerComboBox.superclass.onSelect.apply(this, [record, index]);
        this.triggers[0].show();
    },

    /**
     * private clearValue
     */
    clearValue: function() {
        Ext.ux.form.TwinTriggerComboBox.superclass.clearValue.call(this);
        this.triggers[0].hide();
        this.fireEvent('clear', this);
    },

    /***
     * insert
     * provide a convenience method to insert ONE AND ONLY ONE record to the store.
     * @param {Object} index
     * @param {Object} data (
     */
    insert : function(index, data) {
        this.reset();

        var rec = new this.store.recordType(data);
        rec.id = rec.data.id;
        this.store.insert(index, rec);
        this.setValue(rec.data.id);
        this.fireEvent('select', this, rec, index);
    }
});

Ext.reg('twintriggercombo', Ext.ux.form.TwinTriggerComboBox);
