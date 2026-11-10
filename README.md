# StackTrace

**StackTrace** is a local document search engine written in **Java 25**. It is a project-driven exploration of data structures, algorithms, and search-engine design: document ingestion, tokenization, inverted indexing, Boolean retrieval, and relevance ranking are implemented in Java rather than delegated to a search library.

StackTrace indexes UTF-8 `.txt` and `.md` files from a directory and lets you search them from a command line or an interactive terminal session.

## Features

- **Recursive document discovery:** indexes `.txt` and `.md` files in nested directories; skips unsupported file types and symbolic links.
- **Text processing:** normalization, tokenization, term frequencies, and token positions.
- **Inverted index:** maps terms to document postings for efficient retrieval.
- **Boolean search:** single terms and two-term `AND`, `OR`, and `NOT` queries.
- **Relevance ranking:** document-length-normalized TF-IDF with deterministic tie-breaking.
- **Top-K results:** uses a bounded priority queue to return the highest-ranked matches.
- **Interactive CLI:** index once, run multiple queries, and exit when finished.
- **Automated tests:** unit, integration, and interactive CLI regression tests using JUnit 5.

## Requirements

- Java **25**
- A terminal; the included Maven wrapper (`./mvnw`) handles the Maven build

The project includes Spring Boot dependencies, but the search algorithms and standalone CLI do not require a running Spring Boot server.

## Getting started

Clone the repository and build it:

```bash
git clone https://github.com/chaukz/StackTrace.git
cd StackTrace
./mvnw clean verify
```

Create a small sample corpus:

```bash
mkdir -p sample-docs
echo 'Java is used for backend development' > sample-docs/java.txt
echo 'Python is used for machine learning' > sample-docs/python.txt
echo 'Java and Python are programming languages' > sample-docs/programming.md
```

Run the CLI directly from the compiled classes:

```bash
java -cp target/classes com.chaukz.stacktrace.cli.StackTraceCli interactive sample-docs
```

Alternatively, open `StackTraceCli.java` in IntelliJ IDEA, run its `main()` method, and set **Program arguments** to `interactive sample-docs`. Set the working directory to the repository root.

> **Note:** `stacktrace` in the examples below represents the CLI interface, not a globally installed executable. Use `java -cp target/classes com.chaukz.stacktrace.cli.StackTraceCli` in its place unless you create a shell alias or launcher.

## CLI commands

| Command | Description |
| --- | --- |
| `index <directory>` | Scan and index supported documents in the current process. The index is **not saved** to disk. |
| `search <directory> <query>` | Build an in-memory index and print up to 10 ranked matches. |
| `interactive <directory>` | Build the index once and accept multiple queries until `exit` or end-of-input. |

### One-off search

```bash
java -cp target/classes com.chaukz.stacktrace.cli.StackTraceCli search sample-docs 'java AND python'
```

Example output (scores depend on the corpus):

```text
programming.md
Path: /path/to/StackTrace/sample-docs/programming.md
Score: 0.13515503603605478
```

### Interactive search

```bash
java -cp target/classes com.chaukz.stacktrace.cli.StackTraceCli interactive sample-docs
```

Example session:

```text
StackTrace Interactive Search
Enter a query or type 'exit'.
stacktrace> java
1. java.txt
   Path: /path/to/StackTrace/sample-docs/java.txt
   Score: 0.0676
2. programming.md
   Path: /path/to/StackTrace/sample-docs/programming.md
   Score: 0.0676
stacktrace> java AND python
1. programming.md
   Path: /path/to/StackTrace/sample-docs/programming.md
   Score: 0.1352
stacktrace> rust
No results found.
stacktrace> exit
Goodbye!
```

The interactive command keeps the same in-memory index and search engine alive between queries. Invalid queries are reported without terminating the session.

## Query syntax

| Query | Meaning |
| --- | --- |
| `java` | Documents containing `java` |
| `java AND python` | Documents containing both terms |
| `java OR python` | Documents containing either term |
| `java NOT python` | Documents containing `java` but not `python` |

Operators are case-insensitive. The current parser supports **one term** or **exactly two terms with one operator**; it does not implement parentheses, operator precedence across longer expressions, quoted phrases, or `XOR`.

## How it works

```text
Directory
   |
   v
DocumentDiscovery --> UTF-8 documents
   |
   v
Tokenizer / TextNormalizer
   |
   v
IndexBuilder --> InvertedIndex + DocumentRegistry
                          |
Query --> SearchEngine (AND / OR / NOT)
                          |
                          v
                     TfIdfScorer
                          |
                          v
                     Top-K results
                          |
                          v
                    CLI output
```

The core packages are:

| Package | Responsibility |
| --- | --- |
| `model` | Documents, tokens, postings, and search results |
| `analysis` | Text normalization and tokenization |
| `io` | Filesystem document discovery |
| `indexing` | Document registry, inverted index, and index building |
| `search` | Boolean retrieval, TF-IDF scoring, and ranked results |
| `cli` | Command dispatch, one-off search, and interactive mode |

### Ranking

For a term `t` and document `d`, the normalized score is:

```text
score(t, d) = [tf(t, d) / length(d)] * ln(N / df(t))
```

Here, `tf` is the term frequency in the document, `length` is the document token count, `N` is the number of registered documents, and `df` is the number of documents containing the term. `AND` and `OR` sum the scores of their query terms; `NOT` uses its excluded term only to filter results. Empty documents score zero.

Results are sorted by descending score, with ascending document ID as the tie-breaker. The bounded top-K selection takes **O(m log k)** time and **O(k)** heap space for `m` matching documents, in addition to the matching IDs held during retrieval.

## Tests

Run the full suite:

```bash
./mvnw test
```

Run a clean build and verification:

```bash
./mvnw clean verify
```

The latest locally reported clean verification passed **90 tests with 0 failures**. Coverage includes tokenization, document discovery, indexing, Boolean retrieval, ranking, CLI commands, interactive input, invalid operators, and end-of-input behavior.

Interactive CLI tests temporarily replace `System.in` and `System.out` and restore them afterward. They currently assume **sequential test execution**, not parallel execution.

## Current limitations

- **In-memory only:** the index is not persisted. Running `index` and then `search` as separate processes does not reuse an index; `search` rebuilds it, while `interactive` builds it once per session.
- **Supported formats:** UTF-8 `.txt` and `.md`; no PDFs, Office documents, or binary formats.
- **Limited query grammar:** one term or a two-term Boolean expression; no phrase queries, parentheses, or chained expressions.
- **No incremental filesystem watching:** a running interactive session does not automatically detect file changes.
- **No packaged launcher yet:** run the CLI through its Java main class or an IDE configuration.
- **Not yet benchmarked for large corpora:** memory usage and indexing performance require further measurement.

## Roadmap

### MVP — v0.1.0

- [x] Document ingestion and tokenization
- [x] Inverted index and document registry
- [x] Boolean queries (`AND`, `OR`, `NOT`)
- [x] Length-normalized TF-IDF ranking and top-K selection
- [x] One-off and interactive CLI
- [x] Unit and integration tests
- [x] README usage and limitations
- [ ] Final release verification and `v0.1.0` tag

### Beyond the MVP

- [ ] Persist and reload an index
- [ ] Incremental updates and deletions
- [ ] Query language improvements and phrase search
- [ ] Benchmark indexing, latency, and memory
- [ ] BM25, fuzzy matching, and autocomplete
- [ ] PDF support and optional REST API

## Development approach

StackTrace is built milestone by milestone, with an emphasis on understanding the underlying algorithms and testing each change. The search core stays separate from Spring Boot so it can be reused in future adapters without rewriting the indexing or ranking logic.

## License

No license is declared here. Refer to the repository for its current licensing status before reuse or redistribution.