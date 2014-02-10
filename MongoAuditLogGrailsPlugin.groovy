import grails.plugin.mongoaudit.MongoAuditLogPluginSupport

/**
 * Grails plugin descriptor
 */
class MongoAuditLogGrailsPlugin {

    def version = "0.9-SNAPSHOT"
    def grailsVersion = "2.3 > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",

        // exclude the test domain classes
        "grails-app/domain/grails/plugin/mongoaudit/test/Person.groovy",
        "grails-app/domain/grails/plugin/mongoaudit/test/Test*.groovy"
    ]

    def title = "Grails mongo Audit Event Log Plugin"
    def author = "Jonas Ladenfors"
    def authorEmail = "jonas.ladenfors@gmail.com"
    def description = '''\
Enables audit logging for Grails domain classes on the Mongo datastore based on Andre Steingress hibernate audit work. 
'''

    def documentation = "https://github.com/jladenfors/grails-hibernate-auditlog"
    def license = "MIT"
    def developers = [ [ name: "Andre Steingress", email: "me@andresteingress.com" ], [ name: "Jonas Ladenfors", email: "jonas.ladenfors@gmail.com" ]]
    def issueManagement = [ system: "GIT", url: "https://github.com/jladenfors/grails-hibernate-auditlog/issues" ]
    def scm = [ url: "https://github.com/jladenfors/grails-hibernate-auditlog" ]

    def loadAfter = ['mongodb']
    def observe = ['domainClass']

    def doWithSpring = MongoAuditLogPluginSupport.doWithSpring

    def onConfigChange = MongoAuditLogPluginSupport.onConfigChange
}
