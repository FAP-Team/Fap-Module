/**
 * editor_plugin_src.js
 *
 * Copyright 2009, Moxiecode Systems AB
 * Released under LGPL License.
 *
 * License: http://tinymce.moxiecode.com/license
 * Contributing: http://tinymce.moxiecode.com/contributing
 */

(function() {
	tinymce.create('tinymce.plugins.PageBreakPlugin', {
		init : function(ed, url) {
			var pb = '<img src="' + tinyMCE.settings.httpPath + '/public/images/pagebreak.png" style="page-break-after:always; display:block; margin:auto;" />', 
			    cls = 'mcePageBreak';

			// Register commands
			ed.addCommand('mcePageBreak', function() {
				ed.execCommand('mceInsertContent', 0, pb);
			});

			// Register buttons
			ed.addButton('pagebreak', {title : 'pagebreak.desc', cmd : cls});

			ed.onInit.add(function() {
				if (ed.theme.onResolveName) {
					ed.theme.onResolveName.add(function(th, o) {
						if (o.node.nodeName == 'IMG' && ed.dom.hasClass(o.node, cls))
							o.name = 'pagebreak';
					});
				}
			});

			ed.onClick.add(function(ed, e) {
				e = e.target;

				if (e.nodeName === 'IMG' && ed.dom.hasClass(e, cls))
					ed.selection.select(e);
			});

			ed.onNodeChange.add(function(ed, cm, n) {
				cm.setActive('pagebreak', n.nodeName === 'IMG' && ed.dom.hasClass(n, cls));
			});
		},

		getInfo : function() {
			return {
				longname : 'PageBreak',
				author : 'Moxiecode Systems AB',
				authorurl : 'http://tinymce.moxiecode.com',
				infourl : 'http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/pagebreak',
				version : tinymce.majorVersion + "." + tinymce.minorVersion
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('pagebreak', tinymce.plugins.PageBreakPlugin);
})();
