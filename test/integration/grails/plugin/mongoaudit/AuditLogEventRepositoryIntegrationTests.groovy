package grails.plugin.mongoaudit

import grails.plugin.mongoaudit.domain.AuditLogType
import grails.plugin.mongoaudit.test.Tester
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.Test

class AuditLogEventRepositoryIntegrationTests extends GroovyTestCase {

    def auditLogListener
    AuditLogEventRepository auditEventLogRepository
    
    void pretest() {
        AuditLogEvent.collection.drop()
        auditLogListener.defaultIncludeList = ['name']
        auditLogListener.defaultExcludeList = []

        auditLogListener.defaultInsertAuditLogType = AuditLogType.FULL
        auditLogListener.defaultUpdateAuditLogType = AuditLogType.FULL
        auditLogListener.defaultDeleteAuditLogType = AuditLogType.FULL

        auditLogListener.actorClosure = { GrailsWebRequest request, GrailsHttpSession session -> "system" }        
    }

    @Test
    void testInsertEventFull() {
        pretest()
        def p = new Tester(name: "Andre", surName: "Steingress").save(failOnError: true, flush: true)
        
        def auditLogEvent = AuditLogEvent.findByClassName('Tester')
        assert auditLogEvent != null

        assert auditLogEvent.actor == 'system'
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_INSERT
        assert auditLogEvent.newValue == '"Andre"'
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.persistedObjectId == p.id as String
        assert auditLogEvent.propertyName == 'name'
    }

    @Test
    void testInsertEventMedium() {
        pretest()
        auditLogListener.defaultInsertAuditLogType = AuditLogType.MEDIUM

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassName('Tester')
        assert auditLogEvent != null

        assert auditLogEvent.actor == null
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_INSERT
        assert auditLogEvent.newValue == '"Andre"'
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.persistedObjectId == p.id as String
        assert auditLogEvent.propertyName == 'name'
    }

    @Test
    void testInsertEventShort() {
        pretest()
        auditLogListener.defaultInsertAuditLogType = AuditLogType.SHORT

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassName('Tester')
        assert auditLogEvent != null

        assert auditLogEvent.actor == null
        assert auditLogEvent.persistedObjectId == p.id as String
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_INSERT
        assert auditLogEvent.newValue == null
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.propertyName == null
    }

    @Test
    void testUpdateEventFull() {
        pretest()
        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)
        def q = Tester.findByName("Andre")
        q.name = 'Max'
        q.save(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassNameAndEventName('Tester', AuditLogEventRepository.EVENT_NAME_UPDATE)
        assert auditLogEvent != null

        assert auditLogEvent.actor == 'system'
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_UPDATE
        assert auditLogEvent.newValue == '"Max"'
        assert auditLogEvent.oldValue == '"Andre"'
        assert auditLogEvent.persistedObjectId == q.id as String
        assert auditLogEvent.propertyName == 'name'
    }

    @Test
    void testUpdateEventMedium() {
        pretest()
        auditLogListener.defaultUpdateAuditLogType = AuditLogType.MEDIUM

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)
        def q = Tester.findByName("Andre")
        q.name = 'Max'
        q.save(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassNameAndEventName('Tester', AuditLogEventRepository.EVENT_NAME_UPDATE)
        assert auditLogEvent != null

        assert auditLogEvent.actor == null
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_UPDATE
        assert auditLogEvent.newValue == '"Max"'
        assert auditLogEvent.oldValue == '"Andre"'
        assert auditLogEvent.persistedObjectId == q.id as String
        assert auditLogEvent.propertyName == 'name'
    }

    @Test
    void testUpdateEventShort() {
        pretest()
        auditLogListener.defaultUpdateAuditLogType = AuditLogType.SHORT

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)
        def q = Tester.findByName("Andre")
        q.name = 'Max'
        q.save(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassNameAndEventName('Tester', AuditLogEventRepository.EVENT_NAME_UPDATE)
        assert auditLogEvent != null

        assert auditLogEvent.actor == null
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.persistedObjectId == q.id as String
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_UPDATE
        assert auditLogEvent.newValue == null
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.propertyName == null
    }

    @Test
    void testDeleteEventFull() {
        pretest()
        auditLogListener.defaultInsertAuditLogType = AuditLogType.NONE
        auditLogListener.defaultUpdateAuditLogType = AuditLogType.NONE
        auditLogListener.defaultDeleteAuditLogType = AuditLogType.FULL

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)
        p.name = 'Max'
        p.save(flush: true)
        p.delete(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassNameAndEventName('Tester', AuditLogEventRepository.EVENT_NAME_DELETE)
        assert auditLogEvent != null

        assert auditLogEvent.actor == 'system'
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_DELETE
        assert auditLogEvent.newValue == '"Max"'
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.persistedObjectId == p.id as String
        assert auditLogEvent.propertyName == 'name'
    }

    @Test
    void testDeleteEventMedium() {
        pretest()
        auditLogListener.defaultInsertAuditLogType = AuditLogType.NONE
        auditLogListener.defaultUpdateAuditLogType = AuditLogType.NONE
        auditLogListener.defaultDeleteAuditLogType = AuditLogType.MEDIUM

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)
        p.name = 'Max'
        p.save(flush: true)
        p.delete(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassNameAndEventName('Tester', AuditLogEventRepository.EVENT_NAME_DELETE)
        assert auditLogEvent != null

        assert auditLogEvent.actor == null
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_DELETE
        assert auditLogEvent.newValue == '"Max"'
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.persistedObjectId == p.id as String
        assert auditLogEvent.propertyName == 'name'
    }

    @Test
    void testDeleteEventShort() {
        pretest()
        auditLogListener.defaultInsertAuditLogType = AuditLogType.NONE
        auditLogListener.defaultUpdateAuditLogType = AuditLogType.NONE
        auditLogListener.defaultDeleteAuditLogType = AuditLogType.SHORT

        def p = new Tester(name: "Andre", surName: "Steingress").save(flush: true)
        p.name = 'Max'
        p.save(flush: true)
        p.delete(flush: true)

        def auditLogEvent = AuditLogEvent.findByClassNameAndEventName('Tester', AuditLogEventRepository.EVENT_NAME_DELETE)
        assert auditLogEvent != null

        assert auditLogEvent.actor == null
        assert auditLogEvent.className == 'Tester'
        assert auditLogEvent.persistedObjectId == p.id as String
        assert auditLogEvent.dateCreated != null
        assert auditLogEvent.eventName == AuditLogEventRepository.EVENT_NAME_DELETE
        assert auditLogEvent.newValue == null
        assert auditLogEvent.oldValue == null
        assert auditLogEvent.propertyName == null
    }
}
