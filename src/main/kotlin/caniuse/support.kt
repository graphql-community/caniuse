package caniuse

internal class SupportEntry(
  val link: String,
  val name: String,
  val label: String,
  val note: Markdown?,
  val status: SupportStatus,
  val tags: List<String> = emptyList(),
)

sealed interface SupportStatus
class Supported(val version: String) : SupportStatus
class Partial(val version: String) : SupportStatus
object NotApplicable : SupportStatus
object Unknown : SupportStatus
object NotSupported : SupportStatus

private val featureTagOrder = listOf("Jul 2015", "Jun 2018", "Oct 2021", "Sep 2025", "draft", "RFC2", "RFC1", "RFC0", "GAP")

internal fun tagRank(tags: List<String>): Int {
  return tags.mapNotNull { featureTagOrder.indexOf(it).takeIf { idx -> idx >= 0 } }.minOrNull() ?: Int.MAX_VALUE
}

internal fun featureEntries(projectId: String, project: Project, features: Map<String, Feature>): List<SupportEntry> {
  return features.map { feature ->
    project.features.get(feature.key).toSupportEntry(
      projectId,
      feature.value.name,
      "../feature/${feature.key}.html",
      "https://github.com/$repo/edit/main/data/projects/$projectId.json",
      feature.value.tags
    )
  }.sortedWith(compareBy<SupportEntry> { tagRank(it.tags) }.thenBy { it.name })
}

internal fun projectEntries(featureId: String, projects: Map<String, Project>): List<SupportEntry> {
  return projects.map { project ->
    project.value.features.get(featureId).toSupportEntry(
      featureId,
      project.value.name,
      "../project/${project.key}.html",
      "https://github.com/$repo/edit/main/data/projects/${project.key}.json",
      emptyList()
    )
  }.sortedBy { it.name }
}

internal fun SupportStatus?.score(): Double = when (this) {
  is Partial -> 0.5
  is Supported -> 1.0
  else -> 0.0
}

internal fun SupportInfo?.toSupportStatus(): SupportStatus {
  return if (this?.since == null || since == "?") {
    Unknown
  } else if (since == "n/a") {
    NotApplicable
  } else if (since == "-") {
    NotSupported
  } else if (since.endsWith("(partial)")) {
    Partial(since.removeSuffix("(partial)").trim())
  } else {
    Supported(since)
  }
}

private fun SupportInfo?.toSupportEntry(
  id: String,
  name: String,
  link: String,
  editLink: String,
  tags: List<String>
): SupportEntry {
  val status = toSupportStatus()

  val note = if (status is Unknown) {
    "Know the status of this feature? [Let us know!]($editLink)"
  } else {
    this?.note.orEmpty()
  }

  val label = when (status) {
    NotApplicable -> "N/A"
    NotSupported -> "-"
    is Partial -> status.version
    is Supported -> status.version
    Unknown -> "?"
  }

  return SupportEntry(
    link,
    name,
    label,
    note?.let { Markdown(it) },
    status,
    tags,
  )
}

