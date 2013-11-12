/*
 * Copyright (C) 2012 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace('GeoNetwork');

/**
 * Based on the Ext.History class
 * 
 * Helps Permalink.js
 */

if (!GeoNetwork.state) {
    GeoNetwork.state = {};
}

GeoNetwork.state.History = (function() {
    var iframe, hiddenField;
    var ready = false;
    var currentToken;
    var hash = undefined;

    function timeout_function() {
        var newHash = getHash();
        if (newHash !== hash) {
            hash = newHash;
            handleStateChange(hash);
            doSave();
        }
    }
    function getHash() {
        var href = top.location.href, i = href.indexOf("#");
        return i >= 0 ? href.substr(i + 1) : null;
    }

    function doSave() {
        hiddenField.value = currentToken;
    }

    function handleStateChange(token) {
        currentToken = token;
        GeoNetwork.state.History.fireEvent('change', token);
    }

    function updateIFrame(token) {
        var html = [ '<html><body><div id="state">', token,
                '</div></body></html>' ].join('');
        try {
            var doc = iframe.contentWindow.document;
            doc.open();
            doc.write(html);
            doc.close();
            return true;
        } catch (e) {
            return false;
        }
    }

    function checkIFrame() {
        if (!iframe.contentWindow || !iframe.contentWindow.document) {
            setTimeout(checkIFrame, 10);
            return;
        }

        var doc = iframe.contentWindow.document;
        var elem = doc.getElementById("state");
        var token = elem ? elem.innerText : null;

        hash = getHash();

        setInterval(function() {

            doc = iframe.contentWindow.document;
            elem = doc.getElementById("state");

            var newtoken = elem ? elem.innerText : null;

            var newHash = getHash();

            if (newtoken !== token) {
                token = newtoken;
                handleStateChange(token);
                top.location.hash = token;
                hash = token;
                doSave();
            } else if (newHash !== hash) {
                hash = newHash;
                updateIFrame(newHash);
            }

        }, 50);

        ready = true;

        GeoNetwork.state.History.fireEvent('ready', GeoNetwork.state.History);
    }

    return {
        /**
         * The id of the hidden field required for storing the current history
         * token.
         * 
         * @type String
         * @property
         */
        fieldId : 'x-history-field',
        /**
         * The id of the iframe required by IE to manage the history stack.
         * 
         * @type String
         * @property
         */
        iframeId : 'x-history-frame',

        events : {},

        startUp : function() {

            currentToken = hiddenField.value ? hiddenField.value : getHash();

            if (Ext.isIE) {
                checkIFrame();
            } else {
                this.startEvents();
                if (!ready) {
                    ready = true;
                    GeoNetwork.state.History.fireEvent('ready',
                            GeoNetwork.state.History);
                }
            }
        },

        startEvents : function() {
            GeoNetwork.state.History.suspendEvents();
            hash = getHash();
            setInterval(timeout_function, 50);
        },

        /**
         * Initialize the global History instance.
         * 
         * @param {Boolean}
         *            onReady (optional) A callback function that will be called
         *            once the history component is fully initialized.
         * @param {Object}
         *            scope (optional) The callback scope
         */
        init : function(onReady, scope) {
            if (ready) {
                Ext.callback(onReady, scope, [ this ]);
                return;
            }
            if (!Ext.isReady) {
                Ext.onReady(function() {
                    GeoNetwork.state.History.init(onReady, scope);
                });
                return;
            }
            hiddenField = Ext.getDom(GeoNetwork.state.History.fieldId);
            if (Ext.isIE) {
                iframe = Ext.getDom(GeoNetwork.state.History.iframeId);
            }
            this.addEvents('ready', 'change');
            if (onReady) {
                this.on('ready', onReady, scope, {
                    single : true
                });
            }
            this.startUp();
        },

        /**
         * Add a new token to the history stack. This can be any arbitrary
         * value, although it would commonly be the concatenation of a component
         * id and another id marking the specifc history state of that
         * component. Example usage:
         * 
         * <pre><code>
         * // Handle tab changes on a TabPanel
         * tabPanel.on('tabchange', function(tabPanel, tab) {
         *     Ext.History.add(tabPanel.id + ':' + tab.id);
         * });
         * </code></pre>
         * 
         * @param {String}
         *            token The value that defines a particular
         *            application-specific history state
         * @param {Boolean}
         *            preventDuplicates When true, if the passed token matches
         *            the current token it will not save a new history step. Set
         *            to false if the same state can be saved more than once at
         *            the same history stack location (defaults to true).
         */
        add : function(token, preventDup) {
            if (preventDup !== false) {
                if (this.getToken() == token) {
                    return true;
                }
            }
            this.suspendEvents();
            var res = false;
            if (Ext.isIE) {
                res = updateIFrame(token);
            } else {
                top.location.hash = token;
                res = true;
            }
            currentToken = token;
            this.resumeEvents();
            return res;
        },

        /**
         * Programmatically steps back one step in browser history (equivalent
         * to the user pressing the Back button).
         */
        back : function() {
            history.go(-1);
        },

        /**
         * Programmatically steps forward one step in browser history
         * (equivalent to the user pressing the Forward button).
         */
        forward : function() {
            history.go(1);
        },

        /**
         * Retrieves the currently-active history token.
         * 
         * @return {String} The token
         */
        getToken : function() {
            return ready ? currentToken : getHash();
        }
    };
})();

Ext.apply(GeoNetwork.state.History, new Ext.util.Observable());

Ext.apply(GeoNetwork.state.History, {
    suspendEvents : function(queueSuspended) {
        this.eventsSuspended = true;
        if (queueSuspended && !this.eventQueue) {
            this.eventQueue = [];
        }
        if (!Ext.isIE) {
            window.clearInterval(this.timeout_function);
        }
    },
    resumeEvents : function() {
        var me = this, queued = me.eventQueue || [];
        me.eventsSuspended = false;
        delete me.eventQueue;
        Ext.each(queued, function(e) {
            me.fireEvent.apply(me, e);
        });

        if (!Ext.isIE) {
            this.startEvents();
        }
    }
});
