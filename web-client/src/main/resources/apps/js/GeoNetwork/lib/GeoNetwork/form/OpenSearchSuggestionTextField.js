/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace('GeoNetwork.form');

/** api: (define)
 *  module = GeoNetwork.form
 *  class = OpenSearchSuggestionTextField
 *  base_link = `Ext.form.ComboBox <http://extjs.com/deploy/dev/docs/?class=Ext.form.ComboBox>`_
 */
/** api: example
 *
 *
 *  .. code-block:: javascript
 *
 *        var fields = [];
 *
 *        var searchField = new GeoNetwork.form.OpenSearchSuggestionTextField({
 *            url: catalogue.services.opensearchSuggest
 *        });
 *
 *
 *        var titleField = new GeoNetwork.form.OpenSearchSuggestionTextField({
 *            field: 'title',
 *            fieldLabel: 'Title',
 *            url: catalogue.services.opensearchSuggest,
 *            hideTrigger: true
 *        });
 *
 *        fields.push(search, title);
 *        fields.push(GeoNetwork.util.SearchFormTools.getOptions());
 *
 *        var searchForm = new Ext.FormPanel( {
 *             items : fields,
 *             ...
 *
 */
/** api: constructor 
 *  .. class:: OpenSearchSuggestionTextField(config)
 *
 *  Create a new OpenSearch suggestion form field with autocompletion.
 *
 *  Check main.search.suggest configuration in GeoNetwork configuration
 *  file for better results according to catalogue content.
 *
 *
 *  Comments:
 *   * Lucene analyzer should be applied on the server side (TODO).
 *   * When using suggestion, we should be allowed to turn off
 *     FuzzyQuery as the suggested term come from the index. Trigger
 *     change similarity to 1 (queries should then be a bit faster).
 */
GeoNetwork.form.OpenSearchSuggestionTextField = Ext.extend(Ext.form.ComboBox, {

    defaultConfig: {
        hideLabel: true,
        width: 400,
        minChars: 2,
        loadingText: '...',
        hideTrigger: true,
        /** api: config[field] 
         *  ``String`` Optional, GeoNetwork Lucene field to query.
         *  Default any (ie. full text search).
         */
        field: 'any',
        /** api: config[suggestionField]
         *  ``String`` Optional, GeoNetwork Lucene field to use for suggestion.
         *  If undefined, field is used. That could be useful to restrict the suggestion
         *  to a subset of values for this field. For example any match any text
         *  in a metadata record, but suggestion could be limited to title, abstract, keywords
         *  stored in the anylight field.
         */
        suggestionField: '',
        /** api: config[fieldName] 
         *  ``String`` Optional, Field name.
         */
        name: 'E_any',
        /**
         * Don't set to true if Lucene field is not analyzed with an Analyzer using a lowerCaseFilter.
         */
        forceLowerCase: true,
        /** api: config[sortBy] 
         *  ``String`` request elements sorting order. FREQUENCY (default), ALPHA, STARTSWITHFIRST
         */
        sortBy: "STARTSWITHFIRST"
    },
    /** api: config[url] 
     * ``String`` OpenSearch suggestion service URL.
     */
    url: undefined,
    
    /** api: config[fieldLabel] 
     *  ``String`` Optional, Field label.
     *
     */
    fieldLabel: undefined,
    
    displayField: 'value',
    
    /** api: config[tpl] 
     *  ``Ext.XTemplate`` Optional template to use.
     *  Default template, highlight search string in the suggestion returned.
     *
     */
    tpl: undefined,
    
    mode: 'remote',
    store: undefined,
    queryParam: 'q',
    autoSelect: false,
    itemSelector: 'div.search-item',
    /** private: method[initComponent] 
     *  Initializes the metadata results view.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        

        this.id = this.name; // FIXME : You may have 2 or more cmp with same name in the app ?
        GeoNetwork.form.OpenSearchSuggestionTextField.superclass.initComponent.call(this);
        
        if (!this.tpl) {
            /**
             * FIXME : Maybe it could be better to have a property instead
             * of doing getDom
             * FIXME : in case of lower casing, replace could not work that way
             * use /regexp/i instead probably
             * 
             */
            var tpl = '<tpl for="."><div class="search-item">' + 
                '<h3>{[values.value.replace(Ext.getDom(\'' + this.id + 
                '\').value.replace(/[*]/g, \'\'), \'<span>\' + Ext.getDom(\'' + this.id + 
                '\').value.replace(/[*]/g, \'\') + \'</span>\')]}</h3>' + 
              '</div></tpl>';
            this.tpl = new Ext.XTemplate(tpl);
        }
        
        if (this.forceLowerCase) {
            this.on('beforequery', function(q){
                q.query = q.query.toLowerCase();
            });
        }
        this.store = new GeoNetwork.data.OpenSearchSuggestionStore({
            url: this.url,
            rootId: 1,
            baseParams: {
                field: this.suggestionField || this.field,
//                withFrequency: true, // To display frequency info
                sortBy: this.sortBy
            }
        });
    }
});

/** api: xtype = gn_opensearchsuggestiontextfield */
Ext.reg('gn_opensearchsuggestiontextfield', GeoNetwork.form.OpenSearchSuggestionTextField);