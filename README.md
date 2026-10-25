# StackTrace

**StackTrace** is a local document search engine built with **Java 25** to learn data structures, algorithms, and search-engine design by implementing the core search logic from scratch.

The MVP indexes UTF-8 `.txt` and `.md` files in a selected directory, then supports fast keyword search, Boolean queries, relevance ranking, and top-K results through a CLI.

## Scope

**MVP:** recursive file discovery, text normalization, inverted index, AND/OR search, TF-IDF scoring, top-K ranking, CLI, automated tests.

**Later:** persisted indexes, incremental updates, snippets, BM25, fuzzy search, autocomplete, PDF parsing, and a REST API. No web crawler or semantic/AI search in the MVP.

## Architecture

```text
Local files -> File reader -> Tokenizer/normalizer -> Index builder -> Inverted index
                                                                  |
CLI query -> Query parser -> Boolean retrieval -> TF-IDF scorer -> Top-K results
```

Keep the **search core independent of Spring Boot**. Spring Boot may be used later as an adapter/API; core algorithms must be usable and testable without a running framework or database.

Suggested packages (adapt to the existing package root):

```text
model/       Document, Token, Posting, SearchResult
analysis/    Tokenizer, TextNormalizer
index/       InvertedIndex, IndexBuilder, DocumentRegistry
io/          File discovery and document reading
query/       Query parsing and Boolean retrieval
ranking/     RankingStrategy, TfIdfRanker, TopKSelector
cli/         Command-line entry point
```

Create classes **when needed**, not all upfront. Prefer a small vertical slice that compiles and works end to end.

## Implementation checklist

> Check an item only after its code, tests, and acceptance criteria pass. Existing repository contents need verification; nothing below is assumed implemented.

### 0. Foundation
- [ ] Verify Java 25, Maven build, and reproducible `mvn test`
- [ ] Add `.gitignore`, meaningful project metadata, and a sample document corpus
- [ ] Establish JUnit 5 tests and a framework-independent core

### 1. Document ingestion
- [ ] Define document metadata and stable IDs within an index
- [ ] Recursively discover `.txt` / `.md`; ignore unsupported files and symlinks by policy
- [ ] Read UTF-8 safely; handle empty, unreadable, and duplicate files predictably
- [ ] Test discovery and file-reading edge cases

### 2. Text analysis
- [ ] Implement case normalization and tokenization consistently for files and queries
- [ ] Preserve token positions and count term occurrences
- [ ] Test punctuation, whitespace, Unicode, case, and empty input

### 3. Inverted index
- [ ] Implement `term -> postings` with document ID, frequency, and positions
- [ ] Maintain document metadata and document-frequency statistics
- [ ] Ensure repeated indexing does not duplicate postings
- [ ] Test index invariants against a small known corpus

### 4. Query processing
- [ ] Implement single-term queries and AND/OR operators with documented precedence
- [ ] Use postings intersections/unions instead of rescanning all documents
- [ ] Handle unknown terms, empty queries, and invalid syntax
- [ ] Test retrieval correctness and deterministic ordering

### 5. Ranking and results
- [ ] Implement a documented TF-IDF formula with length normalization
- [ ] Implement top-K selection with a bounded min-heap
- [ ] Return path, title, score, and consistent tie-breaking
- [ ] Test relevance ordering and compare heap selection with full sort

### 6. Usable MVP
- [ ] Provide CLI commands to index a directory and run searches
- [ ] Add integration tests using a fixed sample corpus
- [ ] Document build, run, test, limitations, and example output
- [ ] Verify clean build; tag `v0.1.0` when all MVP criteria pass

### 7. After MVP
- [ ] Persist and reload an index with format/version checks
- [ ] Add incremental updates, modifications, and deletions
- [ ] Benchmark indexing time, query latency, and memory usage
- [ ] Experiment with BM25, autocomplete, fuzzy matching, and PDF support

## Engineering standards

- **Correctness before optimization.** Prefer understandable, measured implementations over premature abstractions.
- **Separation of concerns.** Keep I/O, parsing, indexing, query evaluation, scoring, and presentation separate.
- **Explicit contracts.** Validate public inputs and define behavior for nulls, duplicates, missing files, and malformed queries.
- **Determinism.** Stable document IDs within an index, reproducible tests, and deterministic tie-breaking.
- **Security and filesystem safety.** Index only an explicitly selected directory; never execute document content; bound file size/resource use; handle symlinks deliberately.
- **Tests at every step.** Unit tests for algorithms, integration tests for the pipeline, and regression tests for bugs.
- **Complexity awareness.** Document expected time/space complexity of significant algorithms; benchmark before optimizing.
- **Dependency discipline.** Use Java standard library for search algorithms; keep dependencies minimal and justified. Do not use Lucene for core indexing/ranking during the learning phase.
- **Clean Git history.** Commit one coherent, working change at a time; use descriptive commit messages (`feat:`, `test:`, `fix:`, `refactor:`, `docs:`).

## Codex workflow

1. Read this README and inspect existing files **before** editing.
2. Implement **only the next unchecked checklist item** unless asked otherwise; do not mass-scaffold future milestones.
3. Explain key design choices, trade-offs, and algorithm complexity briefly.
4. Add/update tests, run `mvn test`, and report results honestly. Do not mark a task complete if tests fail or cannot run.
5. Show changed files and a proposed commit message. **Do not commit or push unless explicitly asked.**
6. Preserve existing project conventions and avoid unrelated refactors.

## Getting started

From the repository root:

```bash
mvn test
```

A runnable CLI command will be documented once Milestone 6 is implemented. Until then, do not assume a CLI or search API already exists.

## Progress

**Current status:** foundation/configuration work has been attempted, but implementation milestones have not been verified here. Update checkboxes based on the repository and passing tests, not assumptions.