package caniuse

/**
 * @param id the value used in the `type` field of `data/projects/<project-id>.json`
 */
internal class ProjectType(
  val id: String,
  val name: String,
  val description: String,
) {
  /**
   * The HTML anchor of this project type, linkable as `<root>#<anchor>`
   */
  val anchor: String = "type-" + id.replace(' ', '-')
}

internal val projectTypes = listOf(
  ProjectType(
    id = "implementation",
    name = "Implementation",
    description = "An implementation of the GraphQL specification, including parser, validation, and execution. Those projects are network agnostic.",
  ),
  ProjectType(
    id = "server",
    name = "Server",
    description = "A server deals with the network.",
  ),
  ProjectType(
    id = "client",
    name = "Client",
    description = "A client makes requests to a server, optionally caches them. Some clients have APIs to customize their network layer.",
  ),
  ProjectType(
    id = "gateway",
    name = "Gateway",
    description = "A gateway composes subgraphs into a supergraph, often adding observability, and/or caching.",
  ),
  ProjectType(
    id = "ide",
    name = "IDE",
    description = "An IDE allows executing and debugging GraphQL requests.",
  ),
  ProjectType(
    id = "tool",
    name = "Tool",
    description = "Everything else!",
  ),
)

internal fun Project.projectTypes(): List<ProjectType> {
  check(types.isNotEmpty()) { "Project '$name' must have at least one type" }
  check(types.distinct().size == types.size) { "Project '$name' has duplicate types: $types" }
  return types.map { projectType(it) }
}

internal fun projectType(id: String): ProjectType {
  return projectTypes.firstOrNull { it.id == id }
    ?: error("Unknown project type '$id'. Valid values are: ${projectTypes.joinToString { "'${it.id}'" }}")
}
