Ext.namespace('GeoNetwork');

GeoNetwork.NotifyByEmail = {
  emailDialog: null,
  formPanel: null,
  sendButtonId: 'GeoNetwork.NotifyByEmail.sendButton',
  comboId: 'combo',
  subjectId: 'subject',
  bodyErrorId: 'bodyError',
  bodyId: 'body',
  openDialog: function() {    
    var self = GeoNetwork.NotifyByEmail;
    if(self.emailDialog === null) {
      var store = new Ext.data.XmlStore({
          autoDestroy: true,
          autoLoad: true,
          storeId: 'emailTemplates',
          url: catalogue.services.metadataSelectionInfo,

          record: 'template',
          idProperty: 'name',
          fields: [
              'name', 'subject', 'body', 'bodyError'
          ]
      });
//      store.load();

      self.formPanel = new Ext.form.FormPanel({
        baseCls: 'x-plain',
        labelWidth: 55,
        layout: {
          type: 'vbox',
          align: 'stretch' // Child items are stretched to full width
        },
        defaults: {
          xtype: 'textfield'
        },

        items: [{
          xtype: 'combo',
          autoSelect: true,
          forceSelection: true,
          typeAhead: true,
          typeAheadDelay: 0,
          triggerAction: 'all',
          lazyRender:true,
          mode: 'local',
          store: store,
          plugins: [Ext.ux.FieldLabeler],
          fieldLabel: OpenLayers.i18n('template'),
          name: self.comboId,
          valueField: 'name',
          displayField: 'name',
          listeners:{
             'select': self.selectTemplate
          }
        }, {
          plugins: [Ext.ux.FieldLabeler],
          fieldLabel: OpenLayers.i18n('subject'),
          validator: self.nonEmptyField,
          name: self.subjectId
        }, {
          id: self.bodyErrorId,
          plugins: [Ext.ux.FieldLabeler],
          fieldLabel: OpenLayers.i18n('bodyError'),
          name: self.bodyErrorId
        }, {
          xtype: 'textarea',
          fieldLabel: OpenLayers.i18n('body'),
          hideLabel: true,
          validator: self.nonEmptyField,
//          listeners{'change': self.validateForm},
          name: self.bodyId,
          flex: 1 // Take up all *remaining* vertical space
        }]
      });

      self.emailDialog = new Ext.Window({
        title: OpenLayers.i18n('composeMessage'),
        collapsible: false,
        maximizable: true,
        width: 750,
        height: 500,
        minWidth: 300,
        minHeight: 200,
        layout: 'fit',
        plain: true,
        bodyStyle: 'padding:5px;',
        buttonAlign: 'center',
        items: self.formPanel,
        buttons: [{
          id: self.sendButtonId,
          text: OpenLayers.i18n('send'),
          disabled: true,
          handler: self.sendEmail
        }, {
          text: OpenLayers.i18n('cancel'),
          handler: function(){self.emailDialog.close();}
        }],
        listeners: {
          'afterrender': self.validateForm,
          'close': function() {
            self.emailDialog = null;
            self.form = null;
          }
        }
      });
    }
    self.emailDialog.show();
  },
  selectTemplate: function(combo, record, index) {
    var self = GeoNetwork.NotifyByEmail;
    var form = self.formPanel.getForm();
    var values = {};
    values[self.subjectId] = record.get('subject');
    values[self.bodyErrorId] = record.get('bodyError');
    values[self.bodyId] = record.get('body');
    form.setValues(values);
    self.validateForm();
  },
  nonEmptyField: function(value) {
    if (value.replace(/\s+/g, '') === '') {
      var sendButton = Ext.getCmp(self.sendButtonId);
      if(sendButton) {
        sendButton.disable();
      }
      return OpenLayers.i18n('nonEmptyField');
    } else {
      return true;
    }
  },
  validateForm: function() {
    var self = GeoNetwork.NotifyByEmail;
    if(self.formPanel.getForm().isValid()) {
      Ext.getCmp(self.sendButtonId).enable();
    } else {
      Ext.getCmp(self.sendButtonId).disable();
    }
  },
  sendEmail: function() {
    var self = GeoNetwork.NotifyByEmail;
    var form = self.formPanel.getForm();

    Ext.Ajax.request({
      url: catalogue.services.notifyByMail,
      method: 'POST',
      params: form.getValues(),
      failure: function (r) {
        alert('There was an error when trying to execute the sendMail service');
      }
    });
    self.emailDialog.close();
  }
};

/*!
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.ns("Ext.ux");

/**
 * @class Ext.ux.FieldLabeler
 * <p>A plugin for Field Components which renders standard Ext form wrapping and labels
 * round the Field at render time regardless of the layout of the Container.</p>
 * <p>Usage:</p>
 * <pre><code>
    {
        xtype: 'combo',
        plugins: [ Ext.ux.FieldLabeler ],
        triggerAction: 'all',
        fieldLabel: 'Select type',
        store: typeStore
    }
 * </code></pre>
 */
Ext.ux.FieldLabeler = (function(){

//  Pulls a named property down from the first ancestor Container it's found in
    function getParentProperty(propName) {
        for (var p = this.ownerCt; p; p = p.ownerCt) {
            if (p[propName]) {
                return p[propName];
            }
        }
    }

    return {

//      Add behaviour at important points in the Field's lifecycle.
        init: function(f) {
//          Replace the Field's onRender method with a sequence that calls the plugin's onRender after the Field's onRender
            f.onRender = f.onRender.createSequence(this.onRender);

//          We need to completely override the onResize method because of the complexity
            f.onResize = this.onResize;

//          Replace the Field's onDestroy method with a sequence that calls the plugin's onDestroy after the Field's onRender
            f.onDestroy = f.onDestroy.createSequence(this.onDestroy);
        },

        onRender: function() {
//          Do nothing if being rendered by a form layout
            if (this.ownerCt) {
                if (this.ownerCt.layout instanceof Ext.layout.FormLayout) {
                    return;
                }
            }

            this.resizeEl = (this.wrap || this.el).wrap({
                cls: 'x-form-element',
                style: (Ext.isIE || Ext.isOpera) ? 'position:absolute;top:0;left:0;overflow:visible' : ''
            });
            this.positionEl = this.itemCt = this.resizeEl.wrap({
                cls: 'x-form-item '
            });
            if (this.nextSibling()) {
                this.margins = {
                    top: 0,
                    right: 0,
                    bottom: this.positionEl.getMargins('b'),
                    left: 0
                };
            }
            this.actionMode = 'itemCt';

//          If our Container is hiding labels, then we're done!
            if (!Ext.isDefined(this.hideLabels)) {
                this.hideLabels = getParentProperty.call(this, "hideLabels");
            }
            if (this.hideLabels) {
                this.resizeEl.setStyle('padding-left', '0px');
                return;
            }

//          Collect the info we need to render the label from our Container.
            if (!Ext.isDefined(this.labelSeparator)) {
                this.labelSeparator = getParentProperty.call(this, "labelSeparator");
            }
            if (!Ext.isDefined(this.labelPad)) {
                this.labelPad = getParentProperty.call(this, "labelPad");
            }
            if (!Ext.isDefined(this.labelAlign)) {
                this.labelAlign = getParentProperty.call(this, "labelAlign") || 'left';
            }
            this.itemCt.addClass('x-form-label-' + this.labelAlign);

            if(this.labelAlign === 'top'){
                if (!this.labelWidth) {
                    this.labelWidth = 'auto';
                }
                this.resizeEl.setStyle('padding-left', '0px');
            } else {
                if (!Ext.isDefined(this.labelWidth)) {
                    this.labelWidth = getParentProperty.call(this, "labelWidth") || 100;
                }
                this.resizeEl.setStyle('padding-left', (this.labelWidth + (this.labelPad || 5)) + 'px');
                this.labelWidth += 'px';
            }

            this.label = this.itemCt.insertFirst({
                tag: 'label',
                cls: 'x-form-item-label',
                style: {
                    width: this.labelWidth
                },
                html: this.fieldLabel + (this.labelSeparator || ':')
            });
        },

//      private
//      Ensure the input field is sized to fit in the content area of the resizeEl (to the right of its padding-left)
//      We perform all necessary sizing here. We do NOT call the current class's onResize because we need this control
//      we skip that and go up the hierarchy to Ext.form.Field
        onResize: function(w, h) {
            Ext.form.Field.prototype.onResize.apply(this, arguments);
            w -= this.resizeEl.getPadding('l');
            if (this.getTriggerWidth) {
                this.wrap.setWidth(w);
                this.el.setWidth(w - this.getTriggerWidth());
            } else {
                this.el.setWidth(w);
            }
            if (this.el.dom.tagName.toLowerCase() === 'textarea') {
                var h = this.resizeEl.getHeight(true);
                if (!this.hideLabels && (this.labelAlign === 'top')) {
                    h -= this.label.getHeight();
                }
                this.el.setHeight(h);
            }
        },

//      private
//      Ensure that we clean up on destroy.
        onDestroy: function() {
            this.itemCt.remove();
        }
    };
})();
