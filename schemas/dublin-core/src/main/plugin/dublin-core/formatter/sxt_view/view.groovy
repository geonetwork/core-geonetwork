import dublincore.SxtSummaryFactory

def dcHandlers = new dublincore.Handlers(handlers, f, env)

new SxtSummaryFactory(dcHandlers, {it.parent() is it.parent()})

dcHandlers.addDefaultHandlers()
