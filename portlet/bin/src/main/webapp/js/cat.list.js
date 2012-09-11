Ext.namespace('cat');
Ext.namespace('cat.Templates');

cat.Templates.TITLE = '<h1><a href="#" onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;">{title}</a>' +
'</h1>';

cat.list = function() {
	
	var createSelect = function(str, options) {
        var disabled = (str.length) ? '' : 'disabled="disabled" ' ;
        var btn = new Ext.Button({
        	fieldLabel: 'download',
        });
        return [
            '  <div class="buttonSet ui-helper-clearfix field select id={uuid}">',
            '    <select ', disabled ,'id="', options.id ,'" name="', options.selectName ,'">',
            '        <option value="-" selected="selected" class="', options.spanClass ,'">', options.defaultText ,'</option>',
                    str,
            '    </select>',
            '  </div>'].join('');
    };
    
	var getPanierBtn = function () {
		var defaultOptions = {
            id: 'panier-',
            selectName: "result-add",
            defaultText: 'download',
            spanClass: "cart-add",
            btnTitle: 'download'
        };
		return createSelect('', defaultOptions);
	};
	var template= new Ext.XTemplate(
	        '<ul class="result-list">',
	          '<tpl for=".">',
	            '<li class="md md-full" style="{featurecolorCSS}">',
	                '<table><tr>',
	                '<td class="thumb">',
	                '<div class="thumbnail">',
	                    '<tpl if="thumbnail">',
	                        '<a rel="lightbox" href="{thumbnail}"><img src="{thumbnail}" alt="Thumbnail"/></a>', 
	                    '</tpl>',
	                    '<tpl if="thumbnail==\'\'"></tpl>',
	                '</div>',
	                '</td>',
	                '<td id="{uuid}" class="content">',
	                    cat.Templates.TITLE,
	                    '<p class="abstract">{[values.abstract.substring(0, 350)]} ...</p>',    // FIXME : 250 as parameters
	                    '<div class="md-contact">',
		                    '<tpl for="contact">',
		                        '<tpl if="applies==\'resource\'">',
			                        '<tpl if="name">',
				                        '<tpl {[if="this.isFirstSource(xindex)"]}>',
				                    		'<span>',
				                    			OpenLayers.i18n('result-list-source'),
				                    		'</span>',
				                    	'</tpl>',
			                            '<span title="{role} - {applies}"><tpl if="values.logo !== undefined && values.logo !== \'\'">',
			                                '<img src="{logo}" class="orgLogo"/>',
			                            '</tpl>',
			                            '{name}',
			                            '<tpl {[if="this.isLastSource(xindex, xcount) == false"]}>',
			                    			',&nbsp;',
			                    		'</tpl>',
			                    		'</span>',
			                    	'</tpl>',
		                        '</tpl>',
		                    '</tpl>',
		                '</div>',
		                getPanierBtn(),
	                    '</td></tr></table>',
	            '</li>',
	        '</tpl>',
	    '</ul>',
	    {
	        isFirstSource: function(idx) {
	            return idx == 1;
	        },
	        isLastSource: function(idx, length) {
	            return idx == length-1;
	        }
	    }
	);
	
	return {
		getTemplate : function() {
			return template;
		}
	}
}();