# Assignment 3 Report — Graph Algorithms

**Student:** Taubakabyl Nurlybek  
**Status:** Draft template — replace the marked sections with measurements from your own successful benchmark before submission.

## 1. Complexity table

| Algorithm | Time | Extra space | Justification |
|---|---|---|---|
| DSU find/union | Amortized `O(α(V))` per operation | `O(V)` | Union by rank keeps trees shallow; path compression flattens paths. |
| Kruskal | `O(E log E + E α(V))` | `O(V+E)` | Sorting dominates, followed by DSU checks for edges. |
| Prim | `O((V+E) log E)` | `O(V+E)` | Each examined adjacency arc can cause a priority-queue insertion. |
| Kosaraju | `O(V+E)` | `O(V+E)` | Each vertex and adjacency arc is traversed a constant number of times. |
| Kahn | `O(V+E)` | `O(V)` | Indegrees are calculated and every vertex/edge is processed once. |
| DAG shortest paths | `O(V+E)` | `O(V)` | Topological ordering plus one relaxation of each edge. |

## 2. Correctness: cut property

For any cut that separates the vertices into two nonempty sets, a minimum-weight edge crossing the cut is safe for a minimum spanning tree. If an MST does not contain that edge, adding it creates a cycle. That cycle must contain another edge crossing the same cut. Replacing that other edge with the lightest crossing edge cannot increase the total weight, so an MST containing the selected edge exists. Kruskal applies this idea by considering edges in nondecreasing weight order and accepting an edge only when it joins two different components. Prim applies it by choosing the lightest candidate edge from the current tree to an unvisited vertex. Both therefore make safe choices and build a minimum spanning tree; on disconnected graphs, they produce a minimum spanning forest.

## 3. Correctness: one-pass DAG relaxation

A topological ordering places every vertex before all of its outgoing neighbors. Consider vertices in that order. Every predecessor of the current vertex has already been processed, so all possible incoming paths have already had their distances propagated to it. Relaxing each outgoing edge therefore propagates the best known distance to later vertices. By induction over the topological order, each vertex's distance is optimal when it is processed. Negative edge weights are safe because a DAG has no directed cycles, so no path can repeatedly traverse a negative cycle to decrease its cost without bound. In a general graph, negative cycles can make shortest paths undefined, and Dijkstra's nonnegative-edge assumption would be violated. A virtual super-source is modeled by setting every indegree-zero vertex's initial distance to zero.

## 4. Algorithm selection

| Situation | Recommended algorithm | Why |
|---|---|---|
| Edge list, sparse undirected graph | Kruskal | Sorting and DSU are straightforward and efficient for sparse inputs. |
| Adjacency list, connected undirected graph | Prim | Grows a tree using a priority queue and nearby edges. |
| Need a minimum spanning forest | Kruskal or Prim with component restarts | Both can handle disconnected graphs without throwing. |
| Need strongly connected components | Kosaraju | Two linear passes; reversed graph is simple to construct. |
| Need a topological order and cycle detection | Kahn | A shorter order than `V` proves a cycle exists. |
| DAG with positive or negative weights | DAG shortest paths | Linear-time relaxation after topological ordering. |
| Directed graph may contain cycles | Do not use DAG shortest paths until acyclicity is verified | Topological relaxation is only valid for DAGs. |

## 5. Counters versus formulas

After running the benchmark, paste a table with one row per algorithm/density and measured values. Suggested normalized ratios:

- Kosaraju: `edge_scans / (V + E)` (the handout separately expects `2E` scans).
- Kahn: `edge_scans / E`.
- DAG shortest paths: `edge_scans / E`.
- Kruskal: `edge_scans / E` and `dsu_ops / E`; also discuss sorting comparisons against `E log E`.
- Prim: `edge_scans / E`; clarify whether adjacency arcs or accepted tree edges are counted.

**Measured results to insert:** Run `mvn exec:java "-Dexec.args=benchmark results/results.csv"` and compute the ratios from that CSV. Do not fabricate measurements.

## 6. Slope check and plots

Use `scripts/plot_results.py` after creating the CSV. Report the fitted log-log slope for each edge-scan curve. Linear traversals should approach slope 1 as graph sizes grow. Kruskal's total work includes edge sorting, so its total-operation trend can be slightly steeper than linear.

## 7. Timing paragraph

Insert a paragraph after measuring the median of five runs on your machine. Explain that wall-clock time changes with CPU, JVM warm-up, garbage collection, memory pressure and background processes, so operation counters are more reproducible evidence of complexity than one timing result.

## 8. Limitations to resolve before submission

Compare all generated outputs with the reference table in the assignment sheet. Confirm that the exact generator consumes random numbers in the same order as the instructor's reference implementation. The reference table must be validated by running the code, not hardcoded into output.
