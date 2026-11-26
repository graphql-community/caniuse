package caniuse

import kotlinx.html.*

@JvmInline
internal value class Markdown(val value: String)

internal fun generateProject(id: String, project: Project, features: Map<String, Feature>, projects: Map<String, Project>): String {
  return generatePage(title = "Can I Use ... in ${project.name}?", features = features, projects = projects, pathPrefix = "../") {
    h1 { +project.name }
    div("markdown") { markdown(project.description) }
    p {
      b { +"Type: " }
      val type = projectType(project.type)
      a(href = "../#${type.anchor}") { +type.name }
    }
    p {
      b { +"Website: " }
      a(href = project.url) { +project.url }
    }
    h2 { +"Features" }
    appendTable("Feature", featureEntries(id, project, features))
  }
}