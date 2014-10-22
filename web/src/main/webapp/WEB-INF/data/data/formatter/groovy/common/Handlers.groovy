package common

public class Handlers {
    def handlers;
    def f
    def env

    common.Matchers matchers
    common.Functions func

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        func = new common.Functions(handlers: handlers, f:f, env:env)
        matchers =  new common.Matchers(handlers: handlers, f:f, env:env)
    }

    def addDefaultStartAndEndHandlers() {
        handlers.start htmlOrXmlStart
        handlers.end htmlOrXmlEnd
    }

    static def applyToChild(handlerFunc, name) {
        {el ->
            def children = el[name]
            if (children.size() == 1) {
                return handlerFunc(children[0])
            } else {
                throw new IllegalStateException("There is supposed to be only a single child when this method is called")
            }
        }
    }
    static def nonEmpty(handlerFunc) {
        {el ->
            if (!el.text().isEmpty()) {
                return handlerFunc(el)
            }
        }
    }

    def htmlOrXmlStart = {
        if (func.hasHtmlParam()) {
            return '''
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8"/>
    <link rel="stylesheet" href="metadata.css"/>
</head>
<body>
<div class="container" gn-metadata="">
'''
        } else {
            return '''
<div class="container" gn-metadata="">
'''
        }
    }
    def htmlOrXmlEnd = {
        if (func.hasHtmlParam()) {
            return '''
</div>
</body>
</html>'''
        } else {
            return '''</div>'''
        }
    }

}