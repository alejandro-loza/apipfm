package db

databaseChangeLog{
    include file: 'migrations/initialSchema.yaml', relativeToChangelogFile: true
}