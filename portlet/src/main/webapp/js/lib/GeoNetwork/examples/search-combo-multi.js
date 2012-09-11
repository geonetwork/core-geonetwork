var searchForm;
var metadataStore;
var catalogue;

Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
    new GeoNetwork.MetadataResultsView({
        tpl: GeoNetwork.Templates.FULL,
        renderTo: 'metadata-view-full',
        catalogue: catalogue
    });
    
    // Load store for the combo
    var store = GeoNetwork.data.CategoryStore(catalogue.services.getCategories);
    store.load();
    
    // Optional template
    var imgUrl = '../../../images/default/category/';
    var tpl = (imgUrl ?
                '<tpl for="."><div class="x-combo-list-item"><img src="' + imgUrl + '{name}.png"/>{[values.label.' + OpenLayers.Lang.getCode() + ']}</div></tpl>':
                '<tpl for="."><div class="x-combo-list-item">{[values.label.' + OpenLayers.Lang.getCode() + ']}</div></tpl>');
    var displaytpl = (imgUrl ?
            '<img src="' + imgUrl + '{name}.png"/>{[values.label.' + OpenLayers.Lang.getCode() + ']}':
            '{[values.label.' + OpenLayers.Lang.getCode() + ']}');

    
    var multiCb = new Ext.ux.form.SuperBoxSelect ({
        name: 'E_category',
        mode: 'local',
        fieldLabel: OpenLayers.i18n('category'),
        triggerAction: 'all',
        store: store,
        stackItems: true,
        valueField: 'name',
        valueDelimiter: ' or ',
        displayField: 'name',
        tpl: tpl,
        displayFieldTpl: displaytpl
    });
    searchForm = new Ext.FormPanel({
        border: false,
        id: 'searchForm',
        renderTo: 'search-form',
        width: 350,
        defaults: {
            width: 230
        },
        defaultType: 'textfield',
        items: multiCb,
        buttons: [{
            text: 'Search',
            id: 'searchBt',
            iconCls: 'md-mn md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    catalogue.search('searchForm', null, null);
                }
            }
        }]
    });
    
    // Launch search when enter key press
    var map = new Ext.KeyMap("searchForm", [{
        key: [10, 13],
        fn: function(){
            Ext.getCmp('searchBt').fireEvent('click');
        }
    }]);
});

