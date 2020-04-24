package db

databaseChangeLog{
    include file: 'migrations/initialschema.groovy', relativeToChangelogFile: true
}