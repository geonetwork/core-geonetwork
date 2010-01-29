Ext.namespace("app");

/**
 * Custom search field with a clean button once search is launched.
 */
Ext.app.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent : function() {
        if (!this.store.baseParams) {
            this.store.baseParams = {};
        }
        Ext.app.SearchField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e) {
            if (e.getKey() == e.ENTER) {
                this.onTrigger2Click();
            }
        }, this);
    },

    validationEvent:false,
    validateOnBlur:false,
    trigger1Class:'x-form-clear-trigger',
    trigger2Class:'x-form-search-trigger',
    hideTrigger1:true,
    width:180,
    hasSearch : false,
    paramName : 'query',

    onTrigger1Click : function() {
        if (this.hasSearch) {
            this.store.baseParams[this.paramName] = '';
            this.store.removeAll();
            this.el.dom.value = '';
            this.triggers[0].hide();
            this.hasSearch = false;
            this.focus();
            // conf
            var conf = Ext.get('conf');
            if (conf) {
                conf.enableDisplayMode().show();
            }
        }
    },

    onTrigger2Click : function() {
        var v = this.getRawValue();
        if (v.length < 1) {
            this.store.baseParams[this.paramName] = '*';
        } else {
            this.store.baseParams[this.paramName] = v;
        }
        
        /**
         * If a triggerAction is defined run it. If not
         * reload associated store.
         */
        if (this.triggerAction) {
            this.triggerAction(this.scope, v);
        } else
            this.store.reload();
        this.hasSearch = true;
        this.triggers[0].show();
        this.focus();
        // conf
        var conf = Ext.get('conf');
        if (conf) {
            conf.enableDisplayMode().hide();
        }
    }
});