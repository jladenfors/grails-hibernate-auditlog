package org.grails.haudit

class Person {

    static auditable = true

    String name
    String surName

    static constraints = {
    }
}