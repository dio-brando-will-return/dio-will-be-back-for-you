package main.kotlin

import java.sql.ResultSet
import java.io.File

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.html.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.mustache.Mustache
import io.ktor.server.mustache.MustacheContent

data class UserSession(val username: String)

fun main() {
    val db = SQLiteDatabase("dio.db")

    db.open()
    db.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT, password TEXT)")

    db.executeUpdate("CREATE TABLE IF NOT EXISTS news (id INTEGER PRIMARY KEY, title TEXT, body TEXT)")

    embeddedServer(Netty, port = 8080) {
        install(Sessions) {
            cookie<UserSession>("user_session")
        }

        install(Mustache) {
            mustacheFactory = com.github.mustachejava.DefaultMustacheFactory("")
        }

        routing {
            get("/css/main.css") {
                call.respondFile(File("src/main/kotlin/resources/static/css/main.css"))
            }

            get("/img/header.png") {
                call.respondFile(File("src/main/kotlin/resources/static/img/header.png"))
            }

            get("/") {
                val id = call.parameters["id"]

                val news = if (id != null) {
                    getNewsById(db, id)
                } else {
                    getAllNews(db)
                }

                call.respond(MustacheContent("src/main/kotlin/resources/templates/index.mustache", mapOf("news" to news)))
            }


            post("/register") {
                handleRegister(call, db)
            }

            get("/register") {
                call.respondFile(File("src/main/kotlin/resources/static/register.html"))
            }

            get("/login") {
                call.respondFile(File("src/main/kotlin/resources/static/login.html"))
            }

            post("/login") {
                handleLogin(call, db)
            }

            post("/logout") {
                call.sessions.clear<UserSession>()
                call.respondRedirect("/")
            }
        }
    }.start(wait = true)
}

data class News(val id: String, val title: String, val body: String)

fun getAllNews(db: SQLiteDatabase): List<News> {
    val resultSet: ResultSet = db.executeQuery("SELECT * FROM news")
    val newsList = mutableListOf<News>()
    while (resultSet.next()) {
        val id = resultSet.getString("id")
        val title = resultSet.getString("title")
        val body = resultSet.getString("body")
        newsList.add(News(id, title, body))
    }
    return newsList
}


fun getNewsById(db: SQLiteDatabase, id: String): List<News> {
    val resultSet: ResultSet = db.executeQuery("SELECT * FROM news WHERE id = $id")
    val newsList = mutableListOf<News>()
    while (resultSet.next()) {
        val title = resultSet.getString("title")
        val body = resultSet.getString("body")
        newsList.add(News(id, title, body))
    }

    return newsList
}

suspend fun handleRegister(call: ApplicationCall, db: SQLiteDatabase) {
    val params = call.receiveParameters()
    val username = params["username"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing username")
    val password = params["password"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing password")

    db.executeUpdate("INSERT INTO users (username, password) VALUES ('$username', '$password')")
    call.sessions.set(UserSession(username))
    call.respondRedirect("/")

}

suspend fun handleLogin(call: ApplicationCall, db: SQLiteDatabase) {
    val params = call.receiveParameters()
    val username = params["username"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing username")
    val password = params["password"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing password")

    val resultSet: ResultSet = db.executeQuery("SELECT * FROM users WHERE username = '$username' AND password = '$password'")
    if (resultSet.next()) {
        call.sessions.set(UserSession(username))
        call.respondRedirect("/")
    } else {
        call.respondText("Invalid username or password", status = HttpStatusCode.Unauthorized)
    }
}