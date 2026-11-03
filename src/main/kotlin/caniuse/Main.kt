package caniuse

import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    val outputDir = File("build/site")
    outputDir.mkdirs()

    val json = Json { ignoreUnknownKeys = true }

    val projects: Map<String, Project> = File("data/projects").listFiles()!!
        .filter { it.extension == "json" }
        .associate {
            try {
                it.nameWithoutExtension to json.decodeFromString<Project>(it.readText())
            } catch (e: Exception) {
                throw Exception("Error while trying to decode $it", e)
            }
        }

    val features: Map<String, Feature> = File("data/features").listFiles()!!
        .filter { it.extension == "json" }
        .associate {
            try {
                it.nameWithoutExtension to json.decodeFromString<Feature>(it.readText())
            } catch (e: Exception) {
                throw Exception("Error while trying to decode $it", e)
            }
        }

    outputDir.resolve("index.html").writeText(generateIndex(projects, features))

    val projectDir = outputDir.resolve("project")
    projectDir.deleteRecursively()
    projectDir.mkdirs()
    projects.forEach { (id, project) ->
        projectDir.resolve("$id.html").writeText(generateProject(id, project, features, projects))
    }

    val featureDir = outputDir.resolve("feature")
    featureDir.deleteRecursively()
    featureDir.mkdirs()
    features.forEach { (id, feature) ->
        featureDir.resolve("$id.html").writeText(generateFeature(id, feature, features, projects))
    }

    println("Loaded ${projects.size} projects and ${features.size} features")
    println("Site generated in ${outputDir.absolutePath}")
}
