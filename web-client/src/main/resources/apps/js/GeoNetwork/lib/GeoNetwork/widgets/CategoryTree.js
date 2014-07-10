Ext.namespace('GeoNetwork');

/** api: (define)
 *  module = GeoNetwork
 *  class = CategoryTree
 */
/** api: constructor 
 *  .. class:: CategoryTree(config)
 *
 *     Create a tree from Categories
 *     (tree will be formated following '/' character as delimiter)
 *     
 *     @author : fgravin
 *     
 */
GeoNetwork.CategoryTree = Ext.extend(Ext.tree.TreePanel, {
    
    /** CategoryStore from suggestion **/
    store: undefined,
    
    /** CategoryStore from xmlinfo?type=categories containing translations **/
    storeLabel: undefined,
    
    /** geonetwork language **/
    lang: undefined,
    
    /** form element name to match with Geonetwork search **/
    name: 'E_category',
    
    /** Tells if the tree has been loaded from the categories service **/
    loaded: false,
    
        
    defaultConfig: {
        border: false,
        stateful: true,
        useArrows:false,
        autoScroll:true,
        animate:true,
        checked:true,
        iconCls: 'search_tree_noicon',
        enableDD:false,
        containerScroll: true,
        rootVisible: false,
        autoHeight: true,
        /**
         *  Separator to use to split the category name 
         *  and create children. If '', no child created and 
         *  only one root node.
         */
        separator: '/',
        bodyCssClass: 'x-form-item search_label', 
        root: new Ext.tree.TreeNode({
            expanded: true,
            text: 'Categories'
        })
    },
   
    /** private: method[initComponent] 
     *  Initializes the search form panel.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        GeoNetwork.CategoryTree.superclass.initComponent.call(this);
        
        this.lang = this.lang || GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode());
        
        this.loadCategoriesLabel();
        
        app.getCatalogue().on('afterLogin', this.loadStore, this);
        app.getCatalogue().on('afterLogout', this.loadStore, this);
        
        this.addEvents('afterload');
        
        if(this.label) {
	        this.on('afterrender', function(c) {
	        	c.body.insertFirst({
	        		tag: 'label',
	        		html: this.label+' :',
	        		cls: 'x-form-item-label cat-root-node'
	        	});
	        });
        }
    },
    
    loadCategoriesLabel : function() {
        this.storeLabel.load({
            callback : this.loadStore,
            scope : this
        });
    },
    
    loadStore: function(records, o, s) {
        this.store.load({
            callback : this.loadCategories,
            scope : this
        });
    },
    
    /**
     * Loads categories from CategoryStore then create the Tree
     */
    loadCategories : function(records,o,s) {
    	var r;
    	this.root.removeAll();
    	for (var i=0; i<records.length; i++) {
    	    var label = this.getLabel(records[i].get('value'));
            if (this.separator != '') {
                // Prepend the separator
                if(label.substring(0,1) != this.separator) {
                    label = this.separator + label;
                }
                r = label.split(this.separator);
            } else {
                r = label;
                records[i].data.label = label;
            }
            this.createNodes(this.root, r, 1);
        }
    	this.restoreSearchedCat();
    },
    
    /**
     * Retrieve translation of the key value for the category
     */
    getLabel: function(value){
        var idx = this.storeLabel.find('name',value);
        if(idx>=0 && this.storeLabel.getAt(idx).get('label') &&
                this.storeLabel.getAt(idx).get('label')[this.lang]) {
            return this.storeLabel.getAt(idx).get('label')[this.lang];
        } else if (idx>=0 && this.storeLabel.getAt(idx).get('label')) {
            return this.storeLabel.getAt(idx).get('label');
            
        } else return OpenLayers.i18n(value); // Will return the value if not defined
    },
    
    /**
     * Retrieve key value from a translation of a category
     */
    getKey: function(value){
        var res;
        var idx = this.storeLabel.findBy(function(record, id) {
            if((record.get('label') && (record.get('label')[this.lang] == value) || record.get('label') == value)) {
                return true;
            } else {
                return false;
            }
        }, this);

        if(idx >= 0 && this.storeLabel.getAt(idx)) {
            res = this.storeLabel.getAt(idx).get('name') || this.storeLabel.getAt(idx).get('value');
        } else {
            res = value;
        }
        return res;
    },
    
    /**
     * Create category node (recursive)
     */
    createNodes : function(node, r, index) {
        var type = typeof r, newCategory = (type == 'string' ? r : r[index]), md;
        if (newCategory) {
            var newNode = node.findChild('text',newCategory);
            if (!newNode) {
                md = '';
                if (type == 'string') {
                    md = newCategory;
                } else {
                    for (var i=0; i<index; i++) {
                        md += (this.separator != '' ? this.separator : '') + r[i+1];
                    }
                }
                newNode = new GeoNetwork.CategoryTreeNode({
                	text: newCategory,
                	category: md,
                	expanded:true
                });
                node.appendChild(newNode);
            }
            if (type != 'string') {
                this.createNodes(newNode, r, index+1);
            }
        }
    },
    
    afterLoad: function() {
    	this.loaded = true;
    	this.fireEvent('afterload', this);
    },
    
    /**
     * Add getName function as ext FormField elements
     */
    getName: function() {
    	return this.name;
    },
    
    /**
     * Return all checked categories as a formated string which fit
     * with geonetwork search engine
     * ex : 'Imagery/Optic or Imagery/Radar'
     */
    getSearchedCat: function() {
    	if(!this.loaded) {
    	    var treeCookie = cookie.get('cat.searchform.'+this.name);
    	    if(treeCookie) {
    	        return treeCookie;
    	    }
    	    else return '';
    	}
    	
    	var selNodes = this.getChecked();
    	var res = '';
    	
    	Ext.each(selNodes, function(node) {
    		if(!node.hasChildNodes()) {
    		    if(res == '') {
    				res = this.getKey(node.attributes.category);
    			} else {
    				res += ' or ' + this.getKey(node.attributes.category);
    			}
    		}
    	}, this);
    	cookie.set('cat.searchform.' + this.name, res);
    	return res;
    },
    
    /**
     * restore all checked value from cookie
     */
    restoreSearchedCat: function() {
    	
    	// node is created with expanded:true, then collapse here to allow to check hidden nodes
    	this.getRootNode().collapse(true, false);
    	
    	var c = cookie.get('cat.searchform.' + this.name);
    	var checkedCat = c ? c.split(' or ') : [];
    	Ext.each(checkedCat, function(label) {
    	    if(this.separator != '' && 
    	    		this.name !== 'E_sextantTheme'	// Hack for sextant theme. Not sure about the leading separator
    	    			) {
    	        label = this.separator + label;
    	    }

    		var node = this.root.findChild('category', this.getLabel(label), true);
    		if(node) {
    			node.getUI().toggleCheck(true);
    		}
    	},this);
    	
    	this.afterLoad();
    },
    
    /**
     * Uncheck all checked nodes
     */
    reset: function() {
    	Ext.each(this.getChecked(), function(node) {
    		node.getUI().toggleCheck(false);
    	});
    	cookie.set('cat.searchform.categorytree','');
    }
});

/**
 * CategoryTreeNode, represent category node in CategoryTree panel
 * Will manage check categories to add them during the serach action
 */
GeoNetwork.CategoryTreeNode = Ext.extend(Ext.tree.TreeNode, {

	constructor: function(config) {
		
		config.checked = config.checked || false;
		config.iconCls = config.iconCls || 'search_tree_noicon';
		
        GeoNetwork.CategoryTreeNode.superclass.constructor.apply(this, arguments);
        
        this.on('checkchange', this.toggleCheck,this);
    },
    
    /**
     * Recursive method to check parent node if one children node is checked
     * Event of the parentnode are disable to avoid infinite loop
     */
    checkParent: function() {
    	if(!this.parentNode.getUI().isChecked()) {
    		this.parentNode.purgeListeners();
        	if(this.parentNode && this.parentNode.checkParent) {
        		this.parentNode.getUI().toggleCheck(true);
        		this.parentNode.checkParent();
        	}
        	this.parentNode.on('checkchange', this.toggleCheck,this);
    	}
    },
    
    toggleCheck: function(n,c) {
    	if(c) {
    		n.checkParent();
    	}
    	
		Ext.each(n.childNodes, function(child){
			child.getUI().toggleCheck(c);
		});
	}
});

/** api: xtype = gn_categorytree */
Ext.reg('gn_categorytree', GeoNetwork.CategoryTree);