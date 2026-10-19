# DAA Assignment 3 — Graphs, DSU, MST, SCC and DAG shortest paths

**Student:** Temujin Gonchar
**Version:** 1.0  
**Java:** 17+ | **Build:** Maven | **Tests:** JUnit 5

## What is included

- One adjacency-list `Graph` class for directed and undirected weighted graphs.
- DSU with union by rank and path compression.
- Kruskal and Prim; disconnected undirected graphs produce a minimum spanning forest.
- Iterative Kosaraju SCC (no recursive DFS), Kahn topological ordering with cycle detection.
- DAG shortest paths with a virtual super-source (all indegree-zero vertices start at distance 0), negative edge weights and path restoration.
- A deterministic benchmark runner that writes CSV with operation counters and median-of-five timings.
- JUnit tests and three sample input graphs.

## Build and run

Prerequisites: install a JDK 17 or newer and Maven. Check them in PowerShell:

```powershell
java -version
mvn -version
```

Open PowerShell in the project root (the directory containing `pom.xml`).

Run all tests:

```powershell
mvn test
```

Run an example graph:

```powershell
mvn exec:java "-Dexec.args=run data/disconnected.txt"
mvn exec:java "-Dexec.args=run data/cyclic-directed.txt"
mvn exec:java "-Dexec.args=run data/dag-negative.txt"
```

Generate the benchmark CSV (this tests large graphs up to 100,000 vertices and 2,000,000 edges; it can take a while and may need several GB of heap):

```powershell
mvn exec:java "-Dexec.args=benchmark results/results.csv"
```

The CSV has the required columns: `algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result`. The benchmark compares Kruskal and Prim weights and fails if they disagree; it checks that generated DAGs remain acyclic. Times are median of five runs. Counters are reset for each run and the final run's counts are written.

## Input format

First line: `n m directed|undirected`; then exactly `m` lines `u v w`. Vertices are zero-based. Lines beginning with `#` and blank lines are ignored. Self-loops and parallel edges are accepted.

## Algorithm selection notes

- **Kruskal:** natural choice for an edge list and often attractive on sparse graphs; sorting costs `O(E log E)`, DSU operations are amortized `O(α(V))`.
- **Prim:** adjacency list plus priority queue; often attractive when a graph is connected and represented by adjacency lists. This implementation also restarts at each unvisited vertex to produce a forest.
- **Kosaraju:** use when SCCs are needed and storing the reversed graph is acceptable; two linear passes, `O(V+E)`.
- **Kahn:** use when a topological order is needed and cycle detection is useful; `O(V+E)`.
- **DAG shortest paths:** use only when acyclicity is established; handles negative edges in `O(V+E)`.
- **DSU:** use for dynamic connectivity while processing undirected edges; path compression + union by rank yields amortized near-constant operations.

## Complexity summary

| Algorithm             | Time                              | Extra space | Reason |
|---|---:|---:|---|
| DSU `find` / `union` | amortized `O(α(V))` per operation | `O(V)` total | Path compression and union by rank |
| Kruskal | `O(E log E + E α(V))` | `O(V+E)` | Sort edges then scan with DSU |
| Prim (binary heap) | `O((V+E) log E)` | `O(V+E)` | Priority queue may hold multiple edge candidates |
| Kosaraju | `O(V+E)` | `O(V+E)` | Two iterative DFS passes and reversed graph |
| Kahn | `O(V+E)` | `O(V)` | Indegree calculation, queue and edge traversal |
| DAG shortest paths | `O(V+E)` | `O(V)` | Topological order and one relaxation pass |

## Benchmark caveats / reference values

The supplied assignment handout lists expected reference numbers. Treat them as a validation target, not as values to hardcode into `results.csv`: the CSV must contain actual measured output. The generator in this implementation uses `new Random(42)` per generated graph and follows the specified tree/shuffle/direction rules. Exact reference matching depends on matching the handout's precise random-number consumption and graph-generation interpretation; compare the generated results with the handout before submitting. Do not claim a match unless your local run confirms it.

The handout says the SCC traversal should count exactly `2m` edge scans and Kahn and DAG relaxation exactly `m` each. In this implementation those counters are incremented in the traversal loops; SCC counts each adjacency arc visited in both passes. For reproducible comparison, counters are algorithmic evidence; wall-clock times vary by machine/JVM/load.

## Git workflow

Recommended branch order and commits are listed in `FILE_AND_COMMIT_ORDER.md`. Before final submission, ensure `main` builds, tests pass, and tag `v1.0` points to the final commit.

## AI-use acknowledgement

AI assistance was used to draft and review parts of the project structure, implementation, tests, and documentation. The student is responsible for understanding, testing, correcting, and defending every submitted part.
