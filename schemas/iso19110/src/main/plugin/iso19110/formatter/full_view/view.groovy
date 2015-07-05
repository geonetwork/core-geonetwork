def isoHandlers = new iso19110.Handlers(handlers, f, env)

//SummaryFactory.summaryHandler({it.parent() is it.parent()}, isoHandlers)

isoHandlers.addDefaultHandlers()