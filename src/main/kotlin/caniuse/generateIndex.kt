package caniuse

import kotlinx.html.*

fun generateIndex(projects: Map<String, Project>, features: Map<String, Feature>): String {
  return generatePage(title = "Can I Use ... in GraphQL?", features = features, projects = projects, pathPrefix = "./") {
    div {
      h1 { +"Can I use ... in GraphQL ?" }
      p {
        +"A directory of newer GraphQL features, both drafted and experimental, and the state of their support across different projects."
      }
      p {
        +"The data for this website is open source ("
        a(href = "https://github.com/$repo") {
          +"GitHub repository"
        }
        +") and contributions are warmly welcome. Please see "
        a(href = "https://github.com/$repo/blob/main/CONTRIBUTING.md") {
          +"CONTRIBUTING.md"
        }
        + " for how to add a new project, feature, or to add an existing feature to an existing project."
      }

      p {
        h2 { anchored("background", "Background") }
      }
      p {
        +"GraphQL is a wonderful piece of technology. The GraphQL spec has been a very solid foundation for the past 10 years. It's not (yet!) perfect though. Friction points exist and the community has been "
        a(href = "https://github.com/graphql/graphql-wg/", target = "blank") {
          +"hard at work"
        }
        +" improving the daily GraphQL experience."
      }
      p {
        +"Most of the friction points have identified solutions in the form of RFCs. Some of them have made it to the spec (`@oneOf`, schema coordinates, ...). Others remain in experimental state. "
        +"As time passes, it's becoming harder and harder to tell what feature is implemented where. This website bridges that "
        a(href = "https://github.com/graphql/gaps/") { +"GAP" }
        +"."
      }
    }
    div {
      h2 { anchored("projects", "Projects") }
      table {
        classes = setOf("support-table")
        thead {
          tr {
            th { +"Project Name" }
            th { +"Type" }
            th { +"Supported" }
            th { +"Unsupported" }
            th { +"Not applicable" }
          }
        }
        tbody {
          sortedProjects(features, projects).forEach {
            tr {
              td {
                a(href = "project/${it.id}.html") { +it.name }
              }
              td { +it.type }
              td {
                +if (it.supported % 1.0 == 0.0) it.supported.toInt().toString() else it.supported.toString()
              }
              td { +it.unsupported.toString() }
              td { +it.na.toString() }
            }
          }
        }
      }
    }
    div {
      h2 { anchored("features", "Features") }
      table {
        classes = setOf("support-table", "sortable-table")
        thead {
          tr {
            th {
              attributes["data-sort"] = "number"
              +"Feature"
            }
            th {
              attributes["data-sort"] = "number"
              +"Supported"
            }
            th {
              attributes["data-sort"] = "number"
              +"Unsupported"
            }
            th {
              attributes["data-sort"] = "number"
              +"Not Applicable"
            }
          }
        }
        tbody {
          sortedFeatures(features, projects).forEach {
            tr {
              td {
                attributes["data-sort-key"] = tagRank(it.tags).toString()
                a(href = "feature/${it.id}.html") { +it.name }
                it.tags.forEach { tag -> featureTagBadge(tag) }
              }
              td {
                +if (it.supported % 1.0 == 0.0) it.supported.toInt().toString() else it.supported.toString()
              }
              td { +it.unsupported.toString() }
              td { +it.na.toString() }
            }
          }
        }
      }
    }
    script {
      unsafe {
        +"""
          (function() {
            document.querySelectorAll('table.sortable-table').forEach(function(table) {
              var headers = Array.from(table.querySelectorAll('thead th[data-sort]'));
              var tbody = table.querySelector('tbody');
              if (!tbody) return;

              function sortBy(header, direction) {
                var idx = headers.indexOf(header);
                var type = header.getAttribute('data-sort');
                var rows = Array.from(tbody.querySelectorAll('tr'));
                rows.sort(function(a, b) {
                  var ac = a.children[idx], bc = b.children[idx];
                  var ak = ac.getAttribute('data-sort-key');
                  var bk = bc.getAttribute('data-sort-key');
                  var av = ak !== null ? ak : ac.textContent.trim();
                  var bv = bk !== null ? bk : bc.textContent.trim();
                  if (type === 'number') {
                    av = parseFloat(av); bv = parseFloat(bv);
                    if (isNaN(av)) av = 0;
                    if (isNaN(bv)) bv = 0;
                  } else {
                    av = av.toLowerCase(); bv = bv.toLowerCase();
                  }
                  if (av < bv) return direction === 'asc' ? -1 : 1;
                  if (av > bv) return direction === 'asc' ? 1 : -1;
                  return 0;
                });
                rows.forEach(function(r) { tbody.appendChild(r); });
                headers.forEach(function(h) { h.removeAttribute('aria-sort'); });
                header.setAttribute('aria-sort', direction === 'asc' ? 'ascending' : 'descending');
              }

              headers.forEach(function(h) {
                h.addEventListener('click', function() {
                  var current = h.getAttribute('aria-sort');
                  var direction = current === 'ascending' ? 'desc' : 'asc';
                  sortBy(h, direction);
                });
              });
            });
          })();
        """.trimIndent()
      }
    }
  }
}

private class DisplayProject(
  val id: String,
  val name: String,
  val type: String,
  val supported: Double,
  val unsupported: Int,
  val na: Int,
)

private class DisplayFeature(
  val id: String,
  val name: String,
  val supported: Double,
  val unsupported: Int,
  val na: Int,
  val tags: List<String>,
)

private fun sortedProjects(features: Map<String, Feature>, projects: Map<String, Project>): List<DisplayProject> {
  return projects.entries.map { project ->
    project.value.features.keys.forEach {
      if (!features.keys.contains(it)) {
        error("Unkown feature $it in project ${project.key}, please double check ${project.key}.json")
      }
    }
    val statuses = project.value.features.values.map { it.toSupportStatus() }
    DisplayProject(
      id = project.key,
      name = project.value.name,
      type = project.value.type,
      supported = statuses.sumOf { it.score() },
      unsupported = statuses.count { it is NotSupported || it is Unknown },
      na = statuses.count { it is NotApplicable },
    )
  }.sortedBy { it.name }
}

private fun sortedFeatures(features: Map<String, Feature>, projects: Map<String, Project>): List<DisplayFeature> {
  return features.entries.map { feature ->
    val statuses = projects.values.map { it.features.get(feature.key).toSupportStatus() }
    DisplayFeature(
      id = feature.key,
      name = feature.value.name,
      supported = statuses.sumOf { it.score() },
      unsupported = statuses.count { it is NotSupported || it is Unknown },
      na = statuses.count { it is NotApplicable },
      tags = feature.value.tags,
    )
  }.sortedWith(
    compareBy<DisplayFeature> { tagRank(it.tags) }.thenBy { it.name }
  )
}
