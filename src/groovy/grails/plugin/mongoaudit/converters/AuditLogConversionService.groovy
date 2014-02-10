package grails.plugin.mongoaudit.converters

import org.springframework.stereotype.Service

/**
 * Implementers are used to convert between arbitrary objects and their String representations.
 * This is used when persisting property values to the audit log event table.
 */
@Service
public interface AuditLogConversionService {
    String convert(def object)
}