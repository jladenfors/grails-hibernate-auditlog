package grails.plugin.mongoaudit

import grails.plugin.mongoaudit.converters.AuditLogConversionService
import grails.plugin.mongoaudit.domain.AuditLogType
import grails.plugin.mongoaudit.domain.AuditableDomainObject
import grails.plugin.mongoaudit.validation.AuditLogEventPreparation
import org.grails.datastore.mapping.mongo.MongoSession

/**
 * DAO for audit log events.
 */
class AuditLogEventRepository {

    static final String EVENT_NAME_INSERT = "INSERT"
    static final String EVENT_NAME_UPDATE = "UPDATE"
    static final String EVENT_NAME_DELETE = "DELETE"

    AuditLogListener auditLogListener
    AuditLogEventPreparation auditLogEventPreparation
    AuditLogConversionService auditLogConversionService

    def insert(AuditableDomainObject domain) {
        def type = domain.insertAuditLogType()
        if (type == AuditLogType.NONE) return

        auditLogListener.runInSession { MongoSession session ->
            if (type in [AuditLogType.FULL, AuditLogType.MEDIUM])  {
                def map = domain.toMap()
                map.each { key, value ->
                    this."saveAuditLogEvent${type.toString()}"(session, EVENT_NAME_INSERT, domain, key, value)
                }
            }

            if (type == AuditLogType.SHORT)  {
                saveAuditLogEventSHORT(session, EVENT_NAME_INSERT, domain)
            }
        }
    }

    def update(AuditableDomainObject domain)  {
        def type = domain.updateAuditLogType()
        if (type == AuditLogType.NONE) return

        Collection<String> dirtyProperties = domain.dirtyPropertyNames
        if (!dirtyProperties) return

        auditLogListener.runInSession { MongoSession session ->            
            if (type in [AuditLogType.FULL, AuditLogType.MEDIUM])  {                
                Map newMap = domain.toMap(dirtyProperties)
                Map oldMap = domain.toPersistentValueMap(dirtyProperties)

                newMap.each { String key, def value ->
                    def oldValue = oldMap[key]
                    if (oldValue != value)  {
                        this."saveAuditLogEvent${type.toString()}"(session, EVENT_NAME_UPDATE, domain, key, value, oldValue)
                    }
                }
            }

            if (type == AuditLogType.SHORT)  {
                saveAuditLogEventSHORT(session, EVENT_NAME_UPDATE, domain)
            }
        }
    }

    def delete(AuditableDomainObject domain) {
        def type = domain.deleteAuditLogType()
        if (type == AuditLogType.NONE) return

        auditLogListener.runInSession { MongoSession session ->
            if (type in [AuditLogType.FULL, AuditLogType.MEDIUM])  {
                def map = domain.toMap()
                map.each { key, value ->
                    this."saveAuditLogEvent${type.toString()}"(session, EVENT_NAME_DELETE, domain, key, value)
                }
            }

            if (type == AuditLogType.SHORT)  {
                saveAuditLogEventSHORT(session, EVENT_NAME_DELETE, domain)
            }
        }
    }


        protected void saveAuditLogEventFULL(MongoSession session, String eventName, AuditableDomainObject domain, String key, value, oldValue = null) {
        def audit = auditLogEventPreparation.prepare new AuditLogEvent(
                actor: auditLogListener.getActor(),
                uri: auditLogListener.getUri(),
                className: domain.className,
                eventName: eventName,
                persistedObjectId: domain.id?.toString() ?: "NA",
                propertyName: key,
                oldValue: auditLogConversionService.convert(oldValue),
                newValue: auditLogConversionService.convert(value))

        if (!audit.validate()) throw new RuntimeException("Audit log event validation failed: ${audit.errors}")
        
        session.insert(audit)
    }

    protected void saveAuditLogEventMEDIUM(MongoSession session, String eventName, AuditableDomainObject domain, String key, value, oldValue = null) {
        def audit = auditLogEventPreparation.prepare new AuditLogEvent(
                actor: "",
                uri: "",
                className: domain.className,
                eventName: eventName,
                persistedObjectId: domain.id?.toString() ?: "NA",
                propertyName: key,
                oldValue: auditLogConversionService.convert(oldValue),
                newValue: auditLogConversionService.convert(value))

        if (!audit.validate()) throw new RuntimeException("Audit log event validation failed: ${audit.errors}")
        
        session.insert(audit)
    }

    protected void saveAuditLogEventSHORT(MongoSession session, String eventName, AuditableDomainObject domain) {
        def audit = auditLogEventPreparation.prepare new AuditLogEvent(
                actor: "",
                uri: "",
                className: domain.className,
                eventName: eventName,
                persistedObjectId: domain.id?.toString() ?: "NA",
                propertyName: null,
                oldValue: null,
                newValue: null)

        if (!audit.validate()) throw new RuntimeException("Audit log event validation failed: ${audit.errors}")

        session.insert(audit)
    }
}