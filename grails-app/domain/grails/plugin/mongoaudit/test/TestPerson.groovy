package grails.plugin.mongoaudit.test

class TestPerson {

    static auditable = [exclude: ['surName'], include: ['name']]

    String name
    String surName

    static constraints = {
    }
}
