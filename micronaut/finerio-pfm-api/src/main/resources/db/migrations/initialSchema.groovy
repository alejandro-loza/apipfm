package db.migrations

databaseChangeLog {
    changeSet(author: "pinky", id: "1") {
        createTable(tableName: "user") {
            column(name: "id", type: "UUID") {
                constraints(primaryKey: "true")
            }
            column(name: "name", type: "VARCHAR(50)")
            column(name: "date_created", type: "DATE")
        }
    }
}