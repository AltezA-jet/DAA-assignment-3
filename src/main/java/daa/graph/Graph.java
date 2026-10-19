package daa.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Graph {
    public record Edge(int from, int to, int weight) {}
    public record Arc(int to, int weight) {}
    private final int n;
    private final boolean directed;
    private final List<List<Arc>> adj;
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        this.n = n; this.directed = directed;
        adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }
    public int size() { return n; }
    public boolean directed() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Arc> neighbors(int v) { return Collections.unmodifiableList(adj.get(v)); }
    public void addEdge(int u, int v, int w) {
        checkVertex(u); checkVertex(v);
        edges.add(new Edge(u, v, w));
        adj.get(u).add(new Arc(v, w));
        if (!directed) adj.get(v).add(new Arc(u, w));
    }
    public Graph reverse() {
        Graph r = new Graph(n, true);
        for (Edge e : edges) r.addEdge(e.to(), e.from(), e.weight());
        return r;
    }
    private void checkVertex(int v) { if (v < 0 || v >= n) throw new IndexOutOfBoundsException("vertex " + v); }

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        String[] h = lines.stream().map(String::trim).filter(s -> !s.isEmpty() && !s.startsWith("#")).findFirst()
                .orElseThrow(() -> new IOException("Missing graph header")).split("\\s+");
        if (h.length != 3) throw new IOException("Header must be: n m directed|undirected");
        int n = Integer.parseInt(h[0]), m = Integer.parseInt(h[1]);
        boolean directed = switch (h[2].toLowerCase(Locale.ROOT)) {
            case "directed" -> true; case "undirected" -> false;
            default -> throw new IOException("Graph type must be directed or undirected");
        };
        Graph g = new Graph(n, directed);
        int count = 0;
        boolean skippedHeader = false;
        for (String line : lines) {
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            if (!skippedHeader) { skippedHeader = true; continue; }
            String[] p = s.split("\\s+");
            if (p.length != 3) throw new IOException("Bad edge line: " + line);
            try { g.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])); count++; }
            catch (RuntimeException ex) { throw new IOException("Bad edge line: " + line, ex); }
        }
        if (count != m) throw new IOException("Header says " + m + " edges but read " + count);
        return g;
    }
}
