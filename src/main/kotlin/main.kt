import com.apptastic.rssreader.RssReader
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

val template = """
---
layout: post
title: !!!TITLE!!!
date: !!!FULL_DATE_AND_TIME!!!
categories: articles
---
!!!DATE!!! on dev.to  
[&#x1F4D6; Read article](!!!URL!!!)
""".trimIndent()

fun main(args: Array<String>) {
    val baseDir = if (args.isNotEmpty()) args[0] else "./_posts"
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://dev.to/feed/tkuenneth/"))
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
        )
        .build()
    val response = client.send(request, BodyHandlers.ofString())
    if (response.statusCode() == 200) {
        val inputStream = response.body().byteInputStream()
        val reader = RssReader()
        val rssFeed = reader.read(inputStream)
        val counter = 1
        rssFeed.forEach {
            val title = it.title.get()
                .replace("#", "&#x23;")
                .replace(":", "&#058;")
            val url = it.link.get()
            val date: String
            val fullDateAndTime: String
            val year: Int
            it.pubDateZonedDateTime.get().run {
                fullDateAndTime = toString()
                date = toLocalDate().toString()
                year = this.year
            }
            val dir = File("$baseDir${File.separatorChar}$year")
            dir.mkdirs()
            val file = File(dir, "${date}-${counter}.md")
            val fileContent = template.replace("!!!TITLE!!!", title)
                .replace("!!!FULL_DATE_AND_TIME!!!", fullDateAndTime)
                .replace("!!!DATE!!!", date)
                .replace("!!!URL!!!", url)
            file.writeText(fileContent)
        }
    }
}