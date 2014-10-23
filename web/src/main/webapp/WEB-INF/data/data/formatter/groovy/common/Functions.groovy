package common

public class Functions {
    def handlers;
    def f
    def env

    def hasHtmlParam = { env.param('html').any{it.toBool()} }

    /**
     * Creates the default html for a label -> text pair.  This is the element for primitive/simple data.
     * This does not return a function it is returns the actual html and thus can be used within handlers/functions to
     * directly get the html
     */
    def textEl(label, text) {
        f.html {
            it.span('class': 'md-text') {
                dt(label)
                dd(text)
            }
        }
    }
}