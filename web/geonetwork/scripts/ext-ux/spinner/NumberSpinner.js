Ext.namespace('Ext.ux');

Ext.ux.NumberSpinner = Ext.extend(Ext.form.NumberField,  {
    defaultValue: null,

    initComponent: function() {
        this.addSpinner();
        Ext.ux.NumberSpinner.superclass.initComponent.apply(this, arguments);
    },

    addSpinner: function() {
        var spinner=new Ext.ux.form.Spinner({
            strategy: new Ext.ux.form.Spinner.NumberStrategy({
                minValue: this.minValue,
                maxValue: this.maxValue,
                allowDecimals: this.allowDecimals,
                decimalPrecision: this.decimalPrecision,
                defaultValue: this.defaultValue||0,
                incrementValue: this.incrementValue||1
            })
        });
        this.on('render', function() {
            spinner.applyToMarkup(this.getEl());
            spinner.splitter.hide(); //not well placed and not very useful => I get rid of it
        }, this);
        spinner.on('spin', function() {
            this.fireEvent('change', this, null, null);
        }, this);
        this.on('enable', spinner.enable,  spinner);
        this.on('disable', spinner.disable,  spinner);
    }
});
Ext.reg('numberspinner', Ext.ux.NumberSpinner);