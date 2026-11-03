# Contributing

Contributions are **strongly encouraged**! This project is only as useful as the data it contains, and we'd love your help keeping it accurate and complete.

## Adding data

The most impactful way to contribute is by adding or updating data. All data lives in the `data/` directory as simple JSON files.

### Add support for a feature in an existing project

Edit the project file in `data/projects/<project-id>.json` and add an entry to the `features` map:

```json
{
  "features": {
    "feature-id": {
      "since": "1.2.0",
      "note": "[doc](https://example.com/doc)"
    }
  }
}
```

The `feature-id` must match the filename (without `.json`) of an existing feature in `data/features/`.

Each feature entry supports the following fields:

- `since` — the first version that supports the feature or one of the special values:
  - `-` if not supported
  - `partial` if partially supported (use "note")
  - `n/a` if not applicable 
  - `?` if unknown
- `note` (optional) — any clarification about the support (Markdown is supported).

### Add a new project

Create a new file `data/projects/<project-id>.json`:

```json
{
  "name": "My Project",
  "type": "client",
  "description": "A short description of the project.",
  "url": "https://github.com/org/my-project",
  "features": {}
}
```

`type` is typically `client`, `server`, `gateway`, `ide`, or `tool`.

Then add feature support entries as described above.

### Add a new feature

Create a new file `data/features/<feature-id>.json`:

```json
{
  "name": "Feature Name",
  "description": "A description of the feature.",
  "url": "https://link-to-spec-or-proposal"
}
```

- `description` — a plain-text description of the feature.
- `url` (optional) — a link to the relevant spec, proposal, or RFC.

Then add support entries for this feature in the relevant project files.

## Scoring

The projects are sorted on the main page by depending on their unsupported features. The fewer unsupported features, the better the overall score.

The aim is to showcase projects that track the latest spec changes and RFCs.

We recognize that projects have very different surfaces and not every feature applies to every project. For this reason, features that do not apply to a given project are counted as supported for the overall score.

If a feature is provided tranparently by another project, mark it as supported by adding "since" and link to the project in the note:

```json5
{
  "features": {
    // This project relies on GraphQL.js handling of fragment arguments.
    "fragment-arguments": {
      "since": "1.2.0",
      "note": "Provided by [GraphQL.js](../project/graphql-js)"
    }
  }
}
```

If a feature does not apply, mark it as `n/a` with a comment in the note:

```json5
{
  "features": {
    // This feature does not apply to this project.
    "graphql-response+json": {
      "since": "n/a",
      "note": "This project is transport agnostic"
    }
  }
}
```

Sometimes a feature is provided by another project at a higher version than the one pulled transitively:

```json5
{
  "features": {
    // Upgrade GraphQL.js to 16.12.0 to use fragment arguments
    "fragment-arguments": {
      "since": "n/a",
      "note": "Provided by [GraphQL.js](../project/graphql-js) starting with 16.12.0"
    }
  }
}
```

Because `n/a` features are not "unsupported", they count toward the overall score. They do serve as a secondary sort, though: if two projects have the same overall score, the one with the less `n/a` scores higher. 

## Project types

Valid values for `type` are:

* **implementation**: an implementation of the GraphQL specification, including parser, validation, and execution. Those projects are network agnostic.
  * Examples: `GraphQL.js`, `GraphQL Java`, `Apollo Kotlin`, `Hot Chocolate`
* **schema builder**: a schema builder builds an executable schema. Schema builders can be implementation-first or SDL first.
  * Examples: `Grats`, `GraphQL Kotlin`, `Hot Chocolate`
* **server**: a server deals with the network. 
  * Examples: `GraphQL Yoga`, `Apollo Server`, `Hot Chocolate`
* **client**: a client makes requests to a server, optionally caches them. Some clients have APIs to customize their network layer.
  * Examples: `Apollo Client`, `urql`, `Relay`
* **code generator**: a code generator generates code and types from operations and/or servers.
  * Examples: `GraphQL code generator`, `Apollo iOS`, `DGS`
* **gateway**: a gateway composes subgraphs into a supergraph, often adding observability, and/or caching.
  * Examples: `TheGuild Hive`, `Wundergraph Cosmo`, `Chillicream Fusion`, `Apollo Router`
* **IDE**: an IDE allows executing and debugging GraphQL requests. 
  * Examples: `GraphiQL`, `Playground`, `Laboratory`, `Sandbox`
* **tool**: everything else! 

## Building the site locally

```bash
./gradlew buildSite # requires a JVM installed
```

The generated site is in `build/site/`.

## Guidelines

- For new features and projects, link to the relevant spec or proposal via the feature's `url` field.
- For support information, try to include a note do the documentation and/or pull request.
- When in doubt, open an issue or a draft PR — we're happy to help!
