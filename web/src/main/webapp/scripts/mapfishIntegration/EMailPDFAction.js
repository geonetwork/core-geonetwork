Ext.namespace('mapfish.widgets');
Ext.namespace('mapfish.widgets.print');

mapfish.widgets.print.DlgPrintAction = function(config) {
    mapfish.widgets.print.DlgPrintAction.superclass.constructor.call(this, config);
    this.overrides = {};
    this.overrides[geocat.vectorLayer.name] = { visibility: false };
};

Ext.extend(mapfish.widgets.print.DlgPrintAction, mapfish.widgets.print.PrintAction, {
    popup: false,
    geocat: null,

    getItems: function() {
        return [{
            name: 'title',
            maxLength: 20,
            fieldLabel: translate('rtitle')
        }];
    },

    print: function() {
        this.layerTree = this.geocat.createLayerTree();
        mapfish.widgets.print.DlgPrintAction.superclass.print.call(this);
    },

    handler: function() {
        if (this.popup) return; //only one popup

        var items = this.getItems();
        var self = this;
        var form = new Ext.form.FormPanel({
            xtype: 'form',
            autoHeight: true,
            labelWidth: 70,
            bodyStyle: {
                padding: "10px 10px 6px 10px"
            },
            defaultType: 'textfield',
            defaults: {
                width: "100%"
            },
            monitorValid: true,
            items: items,
            buttons: [{
                text: OpenLayers.Lang.translate('mf.print.print'),
                formBind: true,
                handler: function() {
                    self.formValues = form.getForm().getValues();
                    mapfish.widgets.print.DlgPrintAction.superclass.handler.call(self);
                    popup.destroy();
                }
            }, {
                text: OpenLayers.Lang.translate('mf.cancel'),
                handler: function() {
                    popup.destroy();
                }
            }]
        });

        var popup = this.popup = new Ext.Window({
            title: OpenLayers.Lang.translate('mf.print.print'),
            layout: 'fit',
            resizable: false,
            modal: true,
            constrain: true,
            width: 350,
            border: false,
            autoHeight: true,
            items: [form],
            listeners: {
                destroy: function() {
                    this.popup = null;
                },
                scope: this
            }
        });

        popup.show();

        form.cascade(function(item) {
            if (item.validate) item.validate();
        });
        form.items.first().focus();
    },

    fillSpec: function(printCommand) {
        mapfish.widgets.print.DlgPrintAction.superclass.fillSpec.call(this, printCommand);

        //add the form values to the spec
        OpenLayers.Util.extend(printCommand.spec, this.formValues);
    }
});


mapfish.widgets.print.EMailPDFAction = function(config) {
    config = OpenLayers.Util.extend({
        text: translate('emailPDF'),
        iconCls: 'mf-email-pdf-action'
    }, config);
    mapfish.widgets.print.EMailPDFAction.superclass.constructor.call(this, config);
};

Ext.extend(mapfish.widgets.print.EMailPDFAction, mapfish.widgets.print.DlgPrintAction, {
    getItems: function() {
        return mapfish.widgets.print.EMailPDFAction.superclass.getItems.call(this).concat([{
            xtype: 'fieldset',
            title: translate('email'),
            width: 316,
            autoHeight: true,
            defaultType: 'textfield',
            defaults: {
                width: "100%"
            },
            items: [{
                name: 'emailFrom',
                vtype: 'email',
                fieldLabel: translate('emailFrom')
            },{
                name: 'emailTo',
                vtype: 'email',
                allowBlank: false,
                fieldLabel: translate('emailTo')
            },{
                name: 'emailSubject',
                fieldLabel: translate('subject')
            }]
        }]);
    },


    fillSpec: function(printCommand) {
        mapfish.widgets.print.EMailPDFAction.superclass.fillSpec.call(this, printCommand);

        //change the URL used for printing
        var emailURL = printCommand.config.createURL.replace("create.json", "email.json");
        printCommand.config = OpenLayers.Util.applyDefaults({createURL: emailURL}, printCommand.config);

        printCommand.openPdf = function(answer, success, failure, context) {
            success.call(context);
            Ext.Msg.alert(OpenLayers.Lang.translate('mf.information'),
                    translate('emailSent'));
        };
    }
});
