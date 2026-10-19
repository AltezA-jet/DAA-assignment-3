package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;
import java.util.*;

public final class MST {
    public record Result(long weight, int edges, int components) {}
    private MST() {}
    public static Result kruskal(Graph g, Metrics m) {
        if (g.directed()) throw new IllegalArgumentException("MST requires an undirected graph");
        List<Graph.Edge> edges = new ArrayList<>(g.edges());
        edges.sort((a,b) -> { m.comparisons++; return Integer.compare(a.weight(), b.weight()); });
        DSU dsu = new DSU(g.size(), m); long total = 0; int used = 0;
        for (Graph.Edge e : edges) {
            m.edgeScans++;  
            if (dsu.find(e.from()) != dsu.find(e.to())) {
                dsu.union(e.from(), e.to()); total += e.weight(); used++;
            }
        }
        return new Result(total, used, dsu.components());
    }
    private record Candidate(int from, int to, int weight) {}
    public static Result prim(Graph g, Metrics m) {
        if (g.directed()) throw new IllegalArgumentException("MST requires an undirected graph");
        int n = g.size(); boolean[] seen = new boolean[n];
        PriorityQueue<Candidate> pq = new PriorityQueue<>((a,b) -> { m.comparisons++; return Integer.compare(a.weight(), b.weight()); });
        long total = 0; int used = 0, components = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            components++; seen[start] = true;
            for (Graph.Arc a : g.neighbors(start)) { m.edgeScans++; pq.add(new Candidate(start, a.to(), a.weight())); }
            while (!pq.isEmpty()) {
                Candidate e = pq.poll();
                if (seen[e.to()]) continue;
                seen[e.to()] = true; total += e.weight(); used++;
                for (Graph.Arc a : g.neighbors(e.to())) {
                    m.edgeScans++;
                    if (!seen[a.to()]) pq.add(new Candidate(e.to(), a.to(), a.weight()));
                }
            }
        }
        return new Result(total, used, components);
    }
}
