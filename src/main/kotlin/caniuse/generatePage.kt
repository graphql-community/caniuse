package caniuse

import kotlinx.html.H2
import kotlinx.html.HTMLTag
import kotlinx.html.MAIN
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h4
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe

internal val markdownRenderer = MarkdownRenderer()
internal val repo = "graphql-community/caniuse"

internal fun HTMLTag.markdown(markdown: String) {
  unsafe {
    +markdownRenderer.render(markdown)
  }
}

private const val anchorSvg = """<svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor" aria-hidden="true"><path d="M7.775 3.275a.75.75 0 0 0 1.06 1.06l1.25-1.25a2 2 0 1 1 2.83 2.83l-2.5 2.5a2 2 0 0 1-2.83 0 .75.75 0 0 0-1.06 1.06 3.5 3.5 0 0 0 4.95 0l2.5-2.5a3.5 3.5 0 0 0-4.95-4.95l-1.25 1.25Zm-4.69 9.64a2 2 0 0 1 0-2.83l2.5-2.5a2 2 0 0 1 2.83 0 .75.75 0 0 0 1.06-1.06 3.5 3.5 0 0 0-4.95 0l-2.5 2.5a3.5 3.5 0 0 0 4.95 4.95l1.25-1.25a.751.751 0 0 0-.018-1.042.751.751 0 0 0-1.042-.018l-1.25 1.25a2 2 0 0 1-2.83 0Z"/></svg>"""

internal fun H2.anchored(slug: String, label: String) {
  id = slug
  classes = setOf("anchor-heading")
  a(href = "#$slug") {
    classes = setOf("heading-anchor")
    attributes["aria-label"] = "Link to $label"
    unsafe { +anchorSvg }
  }
  +label
}

internal fun generatePage(
  title: String,
  features: Map<String, Feature>,
  projects: Map<String, Project>,
  pathPrefix: String,
  content: MAIN.() -> Unit,
): String {
  return createHTML().html {
    head {
      meta(name = "viewport", content = "width=device-width, initial-scale=1")
      title(title)
      link(rel = "icon", type = "image/svg+xml", href = "${pathPrefix}caniuse.svg")
      link(rel = "stylesheet", href = "${pathPrefix}pagefind/pagefind-ui.css")
      link(rel = "stylesheet", href = "${pathPrefix}style.css")
      script(src = "${pathPrefix}pagefind/pagefind-ui.js", type = "text/javascript") {}
      script(src = "${pathPrefix}theme.js") {}
      script(src = "${pathPrefix}tooltip.js") {}
    }
    body {
      header {
        div {
          id = "header-title"
          a(href = "${pathPrefix}index.html") {
            img(src = "${pathPrefix}logo.svg", alt = "Logo") {
              height = "30"
            }
          }
        }
        div {
          id = "header-actions"
          div {
            id = "search"
          }
          a(href = "https://github.com/$repo") {
            attributes["aria-label"] = "GitHub"
            attributes["title"] = "GitHub"
            classes = setOf("header-icon-link")
            img(src = "${pathPrefix}github.svg", alt = "GitHub") {
              width = "16"
              height = "16"
            }
          }
          a(href = "https://discord.graphql.org") {
            attributes["aria-label"] = "Discord"
            attributes["title"] = "Discord"
            classes = setOf("header-icon-link")
            img(src = "${pathPrefix}discord.svg", alt = "Discord") {
              width = "16"
              height = "16"
            }
          }
          div {
            id = "theme-switch"
            button {
              id = "theme-trigger"
              attributes["aria-label"] = "Change theme"
              attributes["title"] = "Change theme"
              // Icon inserted by JS
            }
            div {
              id = "theme-menu"
              // Items inserted by JS
            }
          }
        }
      }
      div {
        id = "layout"
        nav {
          id = "sidebar"
          h4 {
            a(href = "${pathPrefix}index.html") {
              classes = setOf("sidebar-heading-link")
              +"Home"
            }
          }
          h4 {
            a(href = "${pathPrefix}index.html#projects") {
              classes = setOf("sidebar-heading-link")
              +"Projects"
            }
          }
          ul {
            projects.entries.sortedBy { it.value.name }.forEach { (projectId, project) ->
              li {
                a(href = "${pathPrefix}project/$projectId.html") {
                  classes = setOf("sidebar-link")
                  +project.name
                }
              }
            }
          }
          h4 {
            a(href = "${pathPrefix}index.html#features") {
              classes = setOf("sidebar-heading-link")
              +"Features"
            }
          }
          ul {
            features.entries.sortedBy { featureSortKey(it.value.name) }.forEach { (featureId, feature) ->
              li {
                a(href = "${pathPrefix}feature/$featureId.html") {
                  classes = setOf("sidebar-link")
                  +feature.name
                }
              }
            }
          }
        }
        main {
          content()
        }
      }
      script {
        unsafe {
          +"""
            window.addEventListener('DOMContentLoaded', function() {
              new PagefindUI({
                element: "#search",
                showSubResults: true,
                showImages: false
              });

              var input = document.querySelector('#search .pagefind-ui__search-input');
              if (!input) return;

              // Add keyboard shortcut hint
              var kbd = document.createElement('kbd');
              kbd.id = 'search-kbd';
              kbd.innerHTML = navigator.userAgent.includes('Mac')
                ? '<span class="kbd-symbol">&#8984;</span>K'
                : 'CTRL K';
              input.parentNode.style.position = 'relative';
              input.parentNode.appendChild(kbd);

              // Show/hide kbd on focus
              input.addEventListener('focus', function() { kbd.style.display = 'none'; });
              input.addEventListener('blur', function() {
                if (!input.value) kbd.style.display = '';
              });

              // Arrow key navigation through results
              function getResultLinks() {
                return Array.from(document.querySelectorAll('#search .pagefind-ui__result-link'));
              }
              input.addEventListener('keydown', function(e) {
                if (e.key === 'ArrowDown') {
                  var links = getResultLinks();
                  if (links.length) { e.preventDefault(); e.stopPropagation(); links[0].focus(); }
                }
              });
              document.querySelector('#search').addEventListener('keydown', function(e) {
                if (e.key !== 'ArrowDown' && e.key !== 'ArrowUp' && e.key !== 'Escape') return;
                var links = getResultLinks();
                var idx = links.indexOf(document.activeElement);
                if (idx === -1) return;
                e.preventDefault();
                if (e.key === 'ArrowDown' && idx < links.length - 1) {
                  links[idx + 1].focus();
                } else if (e.key === 'ArrowUp') {
                  if (idx === 0) { input.focus(); } else { links[idx - 1].focus(); }
                } else if (e.key === 'Escape') {
                  input.focus();
                }
              });

              // Keyboard shortcut: Cmd+K / Ctrl+K or /
              window.addEventListener('keydown', function(e) {
                var tag = (document.activeElement || {}).tagName;
                if (tag && ['INPUT','SELECT','BUTTON','TEXTAREA'].indexOf(tag) !== -1) return;
                if (e.key === '/' || (e.key === 'k' && (e.metaKey || e.ctrlKey))) {
                  e.preventDefault();
                  input.focus({ preventScroll: true });
                }
              });
            });
          """.trimIndent()
        }
      }
      footer {
        div {
          id = "footer-links"
          div {
            classes = setOf("footer-column")
            h4 { +"Resources" }
            a(href = "https://graphql.org") { +"graphql.org" }
            a(href = "https://spec.graphql.org") { +"GraphQL Specification" }
            a(href = "https://graphql.org/community/") { +"Community" }
          }
          div {
            classes = setOf("footer-column")
            h4 { +"Contribute" }
            a(href = "https://github.com/$repo") { +"GitHub Repository" }
            a(href = "https://github.com/$repo/blob/main/CONTRIBUTING.md") { +"Contributing Guide" }
            a(href = "https://github.com/$repo/issues") { +"Report an Issue" }
          }
          div {
            classes = setOf("footer-column")
            h4 { +"Connect" }
            a(href = "https://discord.graphql.org") { +"GraphQL Discord" }
            a(href = "https://graphql.org/foundation/") { +"GraphQL Foundation" }
          }
        }
        p {
          id = "footer-copyright"
          +"© 2026 GraphQL contributors"
        }
      }
    }
  }
}
