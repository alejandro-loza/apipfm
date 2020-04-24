package db.migrations

databaseChangeLog {
    changeSet(author: "pinky", id: "1") {
        createTable(tableName: "user") {
            column(name: "id", type: "UUID") {
                constraints(primaryKey: "true")
            }
            column(name: "username", type: "VARCHAR(50)")
            column(name: "first_name", type: "VARCHAR(50)")
            column(name: "last_name", type: "VARCHAR(50)")
            column(name: "email", type: "VARCHAR(50)")
            column(name: "phone", type: "LONG") {
                constraints(nullable: "false")
            }
            column(name: "user_status", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }
}