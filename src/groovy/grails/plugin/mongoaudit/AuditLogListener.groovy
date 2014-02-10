package grails.plugin.mongoaudit

import grails.plugin.mongoaudit.domain.AuditLogType
import grails.plugin.mongoaudit.reflect.AuditableClosureReader
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.*
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.context.ApplicationEvent
import org.springframework.web.context.request.RequestContextHolder

/**
 * Grails interceptor for logging saves, updates, deletes and acting on
 * individual properties changes and delegating calls back to the Domain Class
 */

class AuditLogListener extends AbstractPersistenceEventListener  {

    GrailsApplication grailsApplication

    Integer truncateLength = AuditLogEvent.MAX_SIZE

    String sessionAttribute = ""
    String actorKey = ""
    Closure actorClosure = null

    List<String> defaultIncludeList = []
    List<String> defaultExcludeList = []

    AuditLogType defaultInsertAuditLogType = AuditLogType.FULL
    AuditLogType defaultUpdateAuditLogType = AuditLogType.FULL
    AuditLogType defaultDeleteAuditLogType = AuditLogType.FULL

    AuditLogEventActions auditLogEventActions

    AuditLogListener(Datastore datastore) {        
        super(datastore)        
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {        
        return true;
    }
    public boolean supportsSourceType(final Class<?> sourceType) {
        // ensure that this listener only handles its events (e.g. if Mongo and Redis are both installed)        
        return datastore.getClass().isAssignableFrom(sourceType);
    }
    void setActorClosure(Closure closure) {
        closure.delegate = this
        closure.properties['log'] = log
        actorClosure = closure
    }

    String getActor() {
        def actor = null
        if (actorClosure) {
            def attr = RequestContextHolder.getRequestAttributes()
            def session = attr?.session
            if (attr && session) {
                try {
                    actor = actorClosure.call(attr, session)
                }
                catch(ex) {
                    log.error "The auditLog.actorClosure threw this exception", ex
                    log.error "The auditLog.actorClosure will be disabled now."
                    actorClosure = null
                }
            }
            // If we couldn't find an actor, use the configured default or just 'system'
            if (!actor) {
                actor = grailsApplication.config.auditLog.defaultActor ?: 'system'
            }
        }
        return actor?.toString()
    }

    String getUri() {
        def attr = RequestContextHolder?.getRequestAttributes()
        return (attr?.currentRequest?.uri?.toString()) ?: null
    }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {      
        
        if (AuditableClosureReader.isAuditable(event.entityObject?.class)) {
            log.debug "Audit logging: ${event.eventType.name()} for ${event.entityObject.class.name}"            
            switch(event.eventType) {
                case EventType.PreInsert:                    
                    onPreInsert(event as PreInsertEvent)
                    break
                case EventType.PreUpdate:                    
                    onPreUpdate(event as PreUpdateEvent)
                    break
                case EventType.PreDelete:                    
                    onPreDelete(event as PreDeleteEvent)
                    break
                default:
                    log.debug("Unknown event type " + event.eventType);
            }
        }
    }

    protected void onPreDelete(PreDeleteEvent event) {
        def domain = event.entityObject
        auditLogEventActions.onBeforeDelete(domain)
    }

    protected void onPreInsert(PreInsertEvent event) {
        def domain = event.entityObject
        auditLogEventActions.onInsert(domain)
    }

    protected void onPreUpdate(PreUpdateEvent event) {
        def domain = event.entityObject        
        auditLogEventActions.onBeforeUpdate(domain)
    }

    public void runInSession(Closure c)  {
        def session = super.datastore.currentSession
        try {            
            c.call(session)
        } finally {
                        
        }
    }

    @Override
    void persistentEntityAdded(PersistentEntity entity) {
        println "persistentEntityAdded"
    }
}
