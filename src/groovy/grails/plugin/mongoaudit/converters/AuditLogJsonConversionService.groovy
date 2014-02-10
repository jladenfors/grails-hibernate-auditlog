package grails.plugin.mongoaudit.converters

import groovy.json.JsonOutput

/**
 * Converts data types to their String representation. This default implementation converts the given
 * object into a JSON-compliant String.
 */
class AuditLogJsonConversionService implements AuditLogConversionService {

    String convert(def object)  {
        if (object == null) return null

        JsonOutput.toJson(object)
    }
}
