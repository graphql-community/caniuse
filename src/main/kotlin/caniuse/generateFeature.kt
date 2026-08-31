package caniuse

import kotlinx.html.*


fun generateFeature(id: String, feature: Feature, features: Map<String, Feature>, projects: Map<String, Project>): String {
  return generatePage(title = "Can I Use ${feature.name}?", features = features, projects = projects, pathPrefix = "../") {
    h1 {
      +feature.name
      feature.tags.forEach { tag -> featureTagBadge(tag) }
    }
    div("markdown") { markdown(feature.description) }
    if (feature.url != null) {
      p {
        b { +"More info: " }
        a(href = feature.url) { +feature.url }
      }
    }
    h2 { +"Projects" }
    appendTable("Project Name", projectEntries(id, projects))
  }
}
