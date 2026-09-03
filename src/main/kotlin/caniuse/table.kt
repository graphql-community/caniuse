package caniuse

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr

private data class Tag(val background: String, val color: String, val tooltip: String)

private fun tagStyle(tag: String): Tag {
  return when (tag) {
    "Experimental" -> Tag("#e535ab", "#fff", "This feature has not been merged in a specification draft yet")
    "draft" -> Tag("#85B913", "#fff", "This feature is merged in the spec draft but not released yet")
    "Sep 2025" -> Tag("#425D09", "#fff", "This feature is part of the September 2025 release")
    "Oct 2021" -> Tag("#425D09", "#fff", "This feature is part of the October 2021 release")
    "Jun 2018" -> Tag("#425D09", "#fff", "This feature is part of the June 2018 release")
    "Jul 2015" -> Tag("#425D09", "#fff", "This feature is part of the July 2015 release")
    "RFC2" -> Tag("#CDF27E", "#000", "This feature is RFC2: draft. It is not merged in the spec draft yet but is a well formed solution")
    "RFC1" -> Tag("#DBF6A2", "#000", "This feature is RFC1: proposal")
    "RFC0" -> Tag("#EDFAD1", "#000", "This feature is RFC0: strawman")
    "GAP" -> Tag("#FFCCEF", "#000", "This feature is a GAP: GraphQL Auxiliary Proposal")
    else -> error("Unknown tag: $tag")
  }
}

internal fun FlowContent.featureTagBadge(tag: String) {
  val style = tagStyle(tag)
  span {
    classes = setOf("badge-tag")
    attributes["style"] = "background-color: ${style.background}; color: ${style.color};"
    attributes["data-tooltip"] = style.tooltip
    attributes["tabindex"] = "0"
    attributes["role"] = "button"
    attributes["aria-label"] = "$tag: ${style.tooltip}"
    +tag
  }
}

internal fun FlowContent.appendTable(columnName: String, entries: List<SupportEntry>) {
  table {
    classes = setOf("support-table")
    thead {
      tr {
        th { +columnName }
        th { +"Supported Since" }
        th { +"Note" }
      }
    }
    tbody {
      entries.forEach {
        tr {
          if (it.status is NotApplicable) {
            classes = setOf("not-applicable")
          } else if (it.status is Unknown) {
            classes = setOf("unknown")
          }
          td {
            a(href = it.link) {
              +it.name
            }
            it.tags.forEach { tag -> featureTagBadge(tag) }
          }
          td {
            classes = setOf(
              when(it.status) {
                is Unknown ->  "support-unknown"
                is NotApplicable ->  "support-unknown"
                is Supported -> "support-yes"
                is Partial -> "support-partial"
                else -> "support-no"
              }
            )
            +it.label
          }
          td {
            if (it.note != null) {
              markdown(it.note.value)
            }
          }
        }
      }
    }
  }
}