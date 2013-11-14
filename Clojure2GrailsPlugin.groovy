import clojure.lang.Compiler;

class Clojure2GrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
       def pluginExcludes = [
            "grails-app/views/error.gsp",
            "src/clj/*",
            "grails-app/controllers/*",
            "**/.gitignore",
            "grails-app/views/demo/*"
    ]
    
    def watchedResources = "file:./src/clj/*.clj"
    
    def observe = ['*']
    
    // TODO Fill in these fields
    def author = "Jeff Brown"
    def authorEmail = "jeff.brown@springsource.com"
    def title = "Grails Clojure Plugin"
    def description = '''\\
The Clojure plugin adds support for easily accessing Clojure code in a Grails application.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/Clojure+Plugin"

    def doWithDynamicMethods = { ctx ->
        def clojureFiles
        if(application.warDeployed) {
            clojureFiles = parentCtx?.getResources("**/WEB-INF/clj/*.clj")?.toList()
        } else {
            clojureFiles = plugin.watchedResources
        }
        clojureFiles.each {
            log.info "Compilando (1): ${it.file.getName()}"
            it.file.withReader { reader ->
                Compiler.load(reader)
            }
        }
        addDynamicProperty(application.allClasses)
    }

    private void addDynamicProperty(classes) {
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
        def clojurePropertyName = config.grails?.clojure?.dynamicPropertyName
        if(clojurePropertyName) {
            def propName = clojurePropertyName[0].toUpperCase()
            if(clojurePropertyName.size() > 1) {
                propName += clojurePropertyName[1..-1]
            }
            clojurePropertyName = propName
        } else {
            clojurePropertyName = 'Clj'
        }

        def proxy = new grails.clojure.ClojureProxy()
        classes*.metaClass*."get${clojurePropertyName}" = {
            return proxy
        }
    }

    def onChange = { event ->
        def source = event.source
        if(source instanceof org.springframework.core.io.FileSystemResource &&
            (source.file.name.endsWith('.clj'))) {
                log.info "Compilando (2): ${it.file.getName()}"
                source.file.withReader { reader ->
                    Compiler.load reader
                }
        } else if(source instanceof Class) {
            addDynamicProperty([source])
        }
    }
}
