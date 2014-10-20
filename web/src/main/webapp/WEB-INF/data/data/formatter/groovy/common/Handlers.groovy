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