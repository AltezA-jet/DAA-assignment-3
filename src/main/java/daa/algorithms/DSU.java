package daa.algorithms;

import daa.metrics.Metrics;

public final class DSU {
    private int[] parent, rank;
    private int components;
    private final Metrics metrics;
    public DSU(int n, Metrics metrics) {
        this.metrics = metrics;
        makeSet(n);
    }
    public void makeSet(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        parent = new int[n]; rank = new int[n]; components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    public int find(int x) {
        metrics.dsuOps++;
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    public boolean union(int x, int y) {
        int a = find(x), b = find(y);
        if (a == b) return false;
        if (rank[a] < rank[b]) parent[a] = b;
        else if (rank[a] > rank[b]) parent[b] = a;
        else { parent[b] = a; rank[a]++; }
        components--; return true;
    }
    public int components() { return components; }
}
