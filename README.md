# StackTrace

**A local document search engine built in Java 25, from first principles.**

StackTrace indexes text files on your computer and lets you search them quickly, without scanning every file for every query. The project's main purpose is to learn **data structures, algorithms, information retrieval, file I/O, testing, and performance engineering** by building a search engine rather than using a ready-made search library.

> **Status:** Planning / Milestone 1. Track completed work in the checklists below. A checked item means confirmed complete, not merely planned.

## What StackTrace will do

1. Recursively discover supported documents in a user-selected folder.
2. Read and normalize their contents into searchable tokens.
3. Build an **inverted index** mapping terms to documents, frequencies, and positions.
4. Accept single-term and Boolean **AND / OR** queries.
5. Rank matching documents with **TF-IDF** and return the best **top-K** matches.
6. Show document titles, filesystem paths, and scores through a command-line interface.
7. Eventually save/reload indexes and support more formats and advanced algorithms.

**MVP file formats:** UTF-8 `.txt` and `.md`. **Out of scope for the MVP:** PDFs, website crawling, AI embeddings, a browser UI, distributed search, and a REST API.

## Example

```text
$ stacktrace index ~/Documents/Notes
Indexed 124 documents (8,420 unique terms)

$ stacktrace search "java AND concurrency" --limit 5
1. java-concurrency.md      score: 4.82  ~/Documents/Notes/java-concurrency.md
2. thread-notes.txt         score: 2.17  ~/Documents/Notes/thread-notes.txt
```

*Example output is illustrative; commands and formatting will be implemented later.*

## Architecture

```text
Folder
  └─ File discovery / document reading
      └─ Text normalizer + tokenizer
          └─ Index builder
              ├─ Document registry (ID -> metadata)
              └─ Inverted index (term -> postings)

Search query
  └─ Query normalizer + parser
      └─ Boolean retrieval (AND / OR)
          └─ TF-IDF scoring
              └─ Top-K min-heap
                  └─ Ranked results in CLI
```

**Design principles**

- **Pure Java search core.** Spring Boot dependencies may already be in the scaffold, but indexing and retrieval should not rely on Spring classes.
- **No Lucene for the learning implementation.** Implement the index, retrieval, and ranking algorithms directly.
- **Metadata separate from content.** `Document` initially contains `id`, `title`, and `Path`; file text is processed during indexing.
- **Consistent text analysis.** Documents and queries must use the same tokenization and normalization rules.
- **Deterministic results.** Equal scores use a specified tie-breaker, such as document ID.
- **Tests before claiming a milestone is done.** Include empty and malformed inputs and verify results using tiny sample corpora.
- **Start in memory.** Persistence and concurrent indexing come after a correct single-process implementation.

**Text normalization policy:** text is normalized with Unicode NFKC, then lowercased with `Locale.ROOT`. The tokenizer treats each contiguous run of Unicode letters and digits as one term; all other code points are separators. Token positions are zero-based among emitted terms, and punctuation does not create gaps.

## Technology

| Layer | Choice |
| --- | --- |
| Language | Java 25 |
| Build | Maven / Maven Wrapper |
| Search algorithms | Java standard library |
| Testing | JUnit 5 (Spring Boot starter-test can provide test dependencies initially) |
| Entry point (MVP) | Command-line application |
| Later additions | JMH benchmarks, persistence, BM25, trie, fuzzy search, PDF parser, optional Spring Boot API |

> **Development environment note (NixOS):** Maven must actually run with JDK 25. Check `./mvnw -version` and `java -version`. A JDK 21 Maven runtime cannot compile with `--release 25`. The system Maven may use a different JDK than the terminal's `java` executable.

## Proposed package structure

Package names should be lowercase. Adjust the namespace consistently if your existing project uses a different root package.

```text
src/
├── main/java/com/chaukz/stacktrace/
│   ├── StackTraceApplication.java       # existing Spring Boot scaffold, if retained
│   ├── model/
│   │   ├── Document.java
│   │   ├── Token.java
│   │   ├── Posting.java
│   │   └── SearchResult.java
│   ├── analysis/
│   │   ├── TextNormalizer.java
│   │   └── Tokenizer.java
│   ├── storage/
│   │   └── FileDocumentReader.java
│   ├── indexing/
│   │   ├── DocumentRegistry.java
│   │   ├── InvertedIndex.java
│   │   └── IndexBuilder.java
│   ├── search/
│   │   ├── QueryProcessor.java
│   │   ├── TopKSelector.java
│   │   └── SearchEngine.java
│   ├── ranking/
│   │   ├── RankingStrategy.java
│   │   ├── TfIdfRanker.java
│   │   └── Bm25Ranker.java                # post-MVP
│   └── cli/
│       └── SearchCommand.java
└── test/java/com/chaukz/stacktrace/
    └── ... matching unit/integration tests
```

This is a **target layout**, not a claim that the files exist. Create packages and classes when their milestone requires them; avoid writing unused abstractions upfront.

## Core data structures and algorithms

| Concept | Candidate structure / algorithm | Why |
| --- | --- | --- |
| Document lookup | `Map<Integer, Document>` | Resolve results to filenames and paths |
| Inverted index | `Map<String, Map<Integer, Posting>>` | Find documents containing each term |
| Token offsets | `List<Integer>` in a `Posting` | Later phrase/proximity queries |
| AND query | Posting-list intersection | Find documents containing all terms |
| OR query | Posting-list union | Find documents containing any term |
| Ranking | TF-IDF | Score relevance using term/document frequencies |
| Top K | `PriorityQueue<SearchResult>` (min-heap) | Keep the best K without sorting all matches |
| Later autocomplete | Trie | Prefix search |
| Later typo tolerance | Levenshtein dynamic programming | Edit-distance matching |

**Target complexity (assuming in-memory indexes and appropriate representations):**

- Tokenization/index construction: expected **O(T)** for T tokens, ignoring I/O and hashing pathologies.
- Term lookup: expected **O(1)** per term in a hash map.
- Intersection of sorted posting lists: **O(P)** for P inspected postings.
- Scoring M matching documents: approximately **O(M)** when query-term statistics are available.
- Top-K heap selection: **O(M log K)**.

## Progress tracker

**Legend:** `[x]` confirmed done · `[ ]` not yet verified / pending. Update checkboxes only after implementation and tests pass.

### Setup — repository and build

- [x] Create local Git repository and add GitHub remote.
- [x] Configure Java 25 in IntelliJ and Maven; resolve compiler/JDK mismatch (reported working).
- [x] Confirm the first commit is pushed to GitHub (`git log -1`, `git status`, `git remote -v`).
- [x] Add this README to the repository.
- [x] Confirm `./mvnw test` passes (or document the actual test command).
- [x] Add representative sample `.txt` and `.md` documents.

### Milestone 1 — document modelling and text analysis

- [x] Create `Document` record (`int id`, `String title`, `Path path`).
- [x] Add document validation (positive ID, nonblank title, nonnull path) and tests.
- [x] Create `Token` record (normalized term and zero-based position).
- [x] Implement `TextNormalizer` (document the normalization policy).
- [x] Implement `Tokenizer` with preserved token positions.
- [x] Test case handling, punctuation, whitespace, empty text, repeated terms, and Unicode behavior.
- [ ] Commit: `feat: add document model and text analysis`.

### Milestone 2 — inverted index

- [ ] Implement a document registry (`id -> document`).
- [ ] Implement postings (`documentId`, term frequency, positions).
- [ ] Build the inverted index from tokenized documents.
- [ ] Ensure duplicate terms increment frequency and accumulate positions.
- [ ] Define what happens when indexing the same document twice (no duplicate live entries).
- [ ] Test with a small, hand-calculated sample corpus.
- [ ] Commit: `feat: implement inverted index`.

### Milestone 3 — Boolean retrieval

- [ ] Support single-term searches.
- [ ] Implement AND intersection.
- [ ] Implement OR union.
- [ ] Normalize queries identically to document text.
- [ ] Define behavior for empty/unknown terms and malformed queries.
- [ ] Test exact expected result sets and ordering.
- [ ] Commit: `feat: add boolean query processing`.

### Milestone 4 — relevance ranking

- [ ] Define a documented TF-IDF formula (including smoothing).
- [ ] Track collection statistics: document count, document frequency, document length.
- [ ] Compute deterministic scores and explain them with a worked example.
- [ ] Introduce `RankingStrategy` only if useful for replacing the algorithm.
- [ ] Test against hand-calculated scores.
- [ ] Commit: `feat: rank search results with tf-idf`.

### Milestone 5 — top-K optimization

- [ ] Implement heap-based top-K results.
- [ ] Define stable tie-breaking for equal scores.
- [ ] Compare heap selection with sorting all results.
- [ ] Test K=0, K>matches, ties, and no results.
- [ ] Commit: `perf: implement top-k result selection`.

### Milestone 6 — folder indexing and CLI

- [ ] Recursively discover `.txt` and `.md` files under a chosen directory.
- [ ] Read UTF-8 text and handle unreadable/missing files clearly.
- [ ] Wire reader → tokenizer → indexer → query → ranker → CLI.
- [ ] Display useful result paths, titles, and scores.
- [ ] Run an end-to-end integration test against fixture documents.
- [ ] Commit: `feat: add local folder indexing and search cli`.

### Milestone 7 — persistence, documentation and measurement

- [ ] Save the index to disk with an explicit file format/version.
- [ ] Load it without needing to reindex unchanged files.
- [ ] Document index invalidation and rebuild behavior.
- [ ] Record indexing time, search latency, document count and vocabulary size.
- [ ] Write architecture and complexity notes in the README or `docs/`.
- [ ] Commit: `feat: persist index and add benchmarks`.

### Beyond MVP — optional challenges

- [ ] BM25 ranking.
- [ ] Phrase search using positional postings.
- [ ] Trie autocomplete.
- [ ] Fuzzy matching with edit distance.
- [ ] Incremental indexing and file change detection.
- [ ] PDF extraction.
- [ ] Optional REST API/UI.
- [ ] Concurrent indexing and larger-scale performance tuning.

## Suggested month-long schedule

| Week | Focus | Expected result |
| --- | --- | --- |
| 1 | Setup, document model, normalization, tokenization, index fundamentals | Text-processing pipeline and initial index |
| 2 | Inverted index, Boolean retrieval, unit tests | Correct document matching |
| 3 | TF-IDF, top-K heap, filesystem traversal, CLI | Useful ranked local search |
| 4 | Persistence, benchmarks, refactoring, documentation | Usable MVP with measured performance |

This is a flexible plan, not a deadline. Algorithm correctness and comprehension matter more than matching exact dates.

## Working with Codex

Use Codex to accelerate **boilerplate and integration**, not to bypass the algorithmic learning goal.

**Codex can handle:**

- Creating Maven package folders and empty files.
- Configuring JUnit, compiler settings, `.gitignore`, and test fixtures.
- Writing CLI argument parsing and filesystem error handling.
- Running formatting, builds, and tests.
- Preparing documentation and mechanical refactors.

**Prefer to implement/review personally:**

- Tokenizer and text normalization rules.
- Posting-list construction and inverted index.
- AND/OR retrieval algorithms.
- TF-IDF scoring and heap-based top-K selection.
- Complexity explanations and benchmark interpretation.

**Suggested first Codex prompt:**

> Read `README.md` and implement **setup + Milestone 1 scaffold only**. Preserve the current Maven/Spring Boot project and existing source files. Create the documented packages, `Document` and `Token` records, text-analysis class signatures, and JUnit test scaffolding; add three tiny UTF-8 sample documents. Do **not** implement the tokenizer, inverted index, ranking or later milestones. Keep the search core free of Spring imports. Run the available Maven tests, report results, and stop. Do not commit automatically.

Review generated code before committing. Keep each commit scoped to one coherent feature, and check off tasks only when verified.

## Build and development commands

```bash
# Confirm the JDK actually used by Maven
./mvnw -version

# Compile and run tests
./mvnw clean test

# Inspect local changes
git status
git diff

# Save reviewed progress
git add .
git commit -m "feat: add document model and text analysis"
git push
```

If the Maven wrapper does not work correctly in your environment, use your system `mvn` **after verifying it runs with Java 25**.

## Reference material

- [Java 25 record classes](https://docs.oracle.com/en/java/javase/25/language/records.html)
- [Java `Path` API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Path.html)
- [Java collections framework](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/package-summary.html)
- [Java `PriorityQueue`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/PriorityQueue.html)
- [JUnit 5 user guide](https://junit.org/junit5/docs/current/user-guide/)
- [Stanford Introduction to Information Retrieval (free textbook)](https://nlp.stanford.edu/IR-book/)

## Project philosophy

**Build the mechanisms, measure them, and understand the tradeoffs.** StackTrace is both a usable local search tool and a portfolio record of learning how search engines work under the hood.
