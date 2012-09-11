Ext.namespace('cat');
Ext.namespace('cat.Templates');

cat.Templates.TITLE = '<h1><a href="#" onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;">{title}</a>'
		+ '</h1>';

cat.list = function() {

	var createSelect = function(str, options) {
		var disabled = (str.length) ? '' : 'disabled="disabled" ';
		var btn = new Ext.Button({
			fieldLabel : 'download',
		});
		return [
				'  <div class="buttonSet ui-helper-clearfix field select id={uuid}">',
				'    <select ', disabled, 'id="', options.id, '" name="',
				options.selectName, '">',
				'        <option value="-" selected="selected" class="',
				options.spanClass, '">', options.defaultText, '</option>', str,
				'    </select>', '  </div>' ].join('');
	};

	var getPanierBtn = function() {
		var defaultOptions = {
			id : 'panier-',
			selectName : "result-add",
			defaultText : 'download',
			spanClass : "cart-add",
			btnTitle : 'download'
		};
		return createSelect('', defaultOptions);
	};

	var template = undefined;

	var initTemplate = function() {

		template = new Ext.XTemplate(
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
			'<p class="abstract">{[Ext.util.Format.ellipsis(values.abstract, 350)]}</p>', 
			'<div class="md-contact">',
			'<tpl if="credit!=\'\'">',
			'<span>',
			OpenLayers.i18n('result-list-source'),
			'&nbsp;<tpl for="credit">{value}{[xindex==xcount?"":", "]}</tpl>',
			'</span>',
			'</tpl>',
			'</div>',
			'<hr/>',
			'<div class="md-links">',
			'<div class="md-action-menu">&nbsp;<span class="icon"></span>'+ OpenLayers.i18n('administrer') + '</div>',
			'<div class="btn-separator">&nbsp;</div>',
			'<div class="downloadMenu"><span class="icon"></span>'+ OpenLayers.i18n('result-list-download')+ '<span class="list-icon"></span></div>',
			'<div class="btn-separator">&nbsp;</div>',
			'<div class="wmsMenu"><span class="icon"></span>'+ OpenLayers.i18n('result-list-view')+ '<span class="list-icon"></span></div>',
			'<tpl for="links">',
			'<tpl if="values.type == \'application/vnd.ogc.wms_xml\' || values.type == \'OGC:WMS\'">',
			'<div class="mdHiddenMenu wmsLink dynamic-{parent.dynamic}" title="'
					+ OpenLayers.i18n('addToMap') + ' {title}">',
			'<tpl if="values.title">',
			'{title}',
			'</tpl>',
			'<tpl if="values.title==\'\'">',
			OpenLayers.i18n('result-list-view'),
			'</tpl>',
			'</div>',
			'</tpl>',
			'<tpl if="values.type == \'DB\'">',
			'<div class="mdHiddenMenu downloadLink download-{parent.download}" title="'
					+ OpenLayers.i18n('viewKml')
					+ ' {title}">{title}</div>',
			'</tpl>',
			'</tpl>',
			'<tpl if="this.hasDownloadLinks(values.links)">',
			'<a href="#" onclick="catalogue.metadataPrepareDownload({id});" class="md-mn downloadAllIcon" title="'
					+ OpenLayers.i18n('prepareDownload')
					+ '" alt="download">&nbsp;</a>', '</tpl>', '</div>',
			'</td></tr></table>', 
			'<div class="relation" title="' + OpenLayers.i18n('relateddatasets') + '"><span></span><ul id="md-relation-{id}"></ul></div>',
			'</li>', '</tpl>', '</ul>', 
			
			{
				isFirstSource : function(idx) {
					return idx == 1;
				},
				isLastSource : function(idx, length) {
					return idx == length - 1;
				},
				hasDownloadLinks : function(values) {
					var i;
					for (i = 0; i < values.length; i++) {
						if (values[i].type === 'application/x-compressed') {
							return true;
						}
					}
					return false;
				}
			}
		);
	};
	
	return {
		getTemplate : function() {
			if(!template) {
				initTemplate();
			}
			return template;
		}
	}
}();