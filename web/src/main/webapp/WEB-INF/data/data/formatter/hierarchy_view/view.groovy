handlers.add select: ~/.*/, {els ->
    handlers.fileResult("html/links.html", ["links" : new common.Handlers(handlers, f, env).loadHierarchyLinkBlocks()])
}