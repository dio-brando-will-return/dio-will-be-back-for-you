package main.kotlin

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class SQLiteDatabase(private val dbFilePath: String) {

    private var connection: Connection? = null

    fun open(): Connection {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$dbFilePath")
        return connection!!
    }

    fun close() {
        connection?.close()
    }

    fun executeQuery(query: String): ResultSet {
        val statement = connection?.createStatement()
        return statement?.executeQuery(query)!!
    }

    fun executeUpdate(query: String): Int {
        val statement = connection?.createStatement()
        return statement?.executeUpdate(query)!!
    }
}