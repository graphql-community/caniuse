package caniuse.serve

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.net.URI

/**
 * A minimal static file server for local preview.
 *
 * Mimics GitHub Pages: `/foo` is served from `foo.html` and `/foo/` from `foo/index.html`.
 */
fun serve(root: File, port: Int) {
  val rootDir = root.canonicalFile
  val server = HttpServer.create(InetSocketAddress("localhost", port), 0)
  server.createContext("/") { exchange ->
    exchange.use {
      val path = URI(exchange.requestURI.rawPath).path
      val file = resolve(rootDir, path)
      if (file == null) {
        val body = "404 Not Found".toByteArray()
        exchange.sendResponseHeaders(404, body.size.toLong())
        exchange.responseBody.write(body)
      } else {
        exchange.responseHeaders.add("Content-Type", contentType(file))
        exchange.sendResponseHeaders(200, file.length())
        file.inputStream().use { it.copyTo(exchange.responseBody) }
      }
    }
  }
  server.start()
  println("Serving ${rootDir.path} at http://localhost:$port/")
}

private fun resolve(rootDir: File, path: String): File? {
  val candidate = File(rootDir, path).canonicalFile
  if (!candidate.path.startsWith(rootDir.path)) return null

  return listOf(
    candidate,
    File(candidate, "index.html"),
    File(candidate.path + ".html"),
  ).firstOrNull { it.isFile }
}

private fun contentType(file: File): String = when (file.extension.lowercase()) {
  "html" -> "text/html; charset=utf-8"
  "css" -> "text/css; charset=utf-8"
  "js", "mjs" -> "text/javascript; charset=utf-8"
  "json" -> "application/json"
  "svg" -> "image/svg+xml"
  "png" -> "image/png"
  "jpg", "jpeg" -> "image/jpeg"
  "woff2" -> "font/woff2"
  "wasm" -> "application/wasm"
  else -> "application/octet-stream"
}

fun main(args: Array<String>) {
  serve(File(args.getOrElse(0) { "build/site" }), args.getOrElse(1) { "8080" }.toInt())
}
