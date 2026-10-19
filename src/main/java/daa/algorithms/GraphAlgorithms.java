package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;
import java.util.*;

public final class GraphAlgorithms {
    private GraphAlgorithms() {}
    public record SCCResult(List<List<Integer>> components, int count) {}
    public record TopoResult(List<Integer> order, boolean acyclic) {}
    public record PathResult(long[] distance, int[] parent) {
        public List<Integer> pathTo(int target) {
            if (target < 0 || target >= parent.length) throw new IndexOutOfBoundsException();
            if (distance[target] == Long.MAX_VALUE) return List.of();
            ArrayList<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path); return path;
        }
        public long sumDistances() { long s = 0; for (long d : distance) if (d != Long.MAX_VALUE) s += d; return s; }
    }
    private static void requireDirected(Graph g) { if (!g.directed()) throw new IllegalArgumentException("Directed graph required"); }

    private record Frame(int vertex, int nextIndex) {}
    public static SCCResult kosaraju(Graph g, Metrics m) {
        requireDirected(g); int n = g.size(); boolean[] seen = new boolean[n]; ArrayList<Integer> finish = new ArrayList<>(n);
        for (int s = 0; s < n; s++) if (!seen[s]) {
            ArrayDeque<Frame> st = new ArrayDeque<>(); st.push(new Frame(s, 0)); seen[s] = true; m.maxDepth = Math.max(m.maxDepth, st.size());
            while (!st.isEmpty()) {
                Frame f = st.pop(); List<Graph.Arc> adj = g.neighbors(f.vertex());
                if (f.nextIndex() < adj.size()) {
                    st.push(new Frame(f.vertex(), f.nextIndex()+1));
                    Graph.Arc a = adj.get(f.nextIndex()); m.edgeScans++;
                    if (!seen[a.to()]) { seen[a.to()] = true; st.push(new Frame(a.to(), 0)); m.maxDepth = Math.max(m.maxDepth, st.size()); }
                } else finish.add(f.vertex());
            }
        }
        Graph rev = g.reverse(); Arrays.fill(seen, false); ArrayList<List<Integer>> comps = new ArrayList<>();
        for (int i = finish.size()-1; i >= 0; i--) {
            int s = finish.get(i); if (seen[s]) continue;
            ArrayList<Integer> comp = new ArrayList<>(); ArrayDeque<Integer> st = new ArrayDeque<>(); st.push(s); seen[s] = true;
            while (!st.isEmpty()) {
                m.maxDepth = Math.max(m.maxDepth, st.size()); int v = st.pop(); comp.add(v);
                for (Graph.Arc a : rev.neighbors(v)) { m.edgeScans++; if (!seen[a.to()]) { seen[a.to()] = true; st.push(a.to()); } }
            }
            comps.add(comp);
        }
        return new SCCResult(comps, comps.size());
    }
    public static TopoResult kahn(Graph g, Metrics m) {
        requireDirected(g); int n = g.size(); int[] indeg = new int[n];
        for (int u=0;u<n;u++) for (Graph.Arc a:g.neighbors(u)) indeg[a.to()]++;
        ArrayDeque<Integer> q = new ArrayDeque<>(); for(int i=0;i<n;i++) if(indeg[i]==0) q.add(i);
        ArrayList<Integer> order = new ArrayList<>(n);
        while(!q.isEmpty()) { int u=q.remove(); order.add(u); for(Graph.Arc a:g.neighbors(u)) { m.edgeScans++; if(--indeg[a.to()]==0) q.add(a.to()); } }
        return new TopoResult(order, order.size()==n);
    }
    public static PathResult dagShortestPaths(Graph g, Metrics m) {
        TopoResult topo = kahn(g, m);
        if (!topo.acyclic()) throw new IllegalArgumentException("DAG shortest paths require an acyclic graph");
        int n=g.size(); long[] dist=new long[n]; int[] parent=new int[n]; Arrays.fill(parent,-1);
        // Virtual super-source: every original source starts at distance zero.
        boolean[] hasIncoming=new boolean[n]; for(int u=0;u<n;u++) for(Graph.Arc a:g.neighbors(u)) hasIncoming[a.to()]=true;
        Arrays.fill(dist, Long.MAX_VALUE); for(int i=0;i<n;i++) if(!hasIncoming[i]) dist[i]=0;
        for(int u:topo.order()) {
            if(dist[u]==Long.MAX_VALUE) continue;
            for(Graph.Arc a:g.neighbors(u)) { m.edgeScans++; long candidate=dist[u]+a.weight(); m.comparisons++;
                if(candidate<dist[a.to()]) { dist[a.to()]=candidate; parent[a.to()]=u; }
            }
        }
        return new PathResult(dist,parent);
    }
}
