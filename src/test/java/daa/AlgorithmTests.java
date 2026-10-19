package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AlgorithmTests {
    @Test void dsuTracksComponentsAndUnions() {
        DSU d=new DSU(5,new Metrics());
        assertTrue(d.union(0,1)); assertTrue(d.union(1,2)); assertFalse(d.union(0,2));
        assertEquals(3,d.components()); assertEquals(d.find(0),d.find(2)); assertNotEquals(d.find(0),d.find(4));
    }
    @Test void mstAlgorithmsAgreeAndDisconnectedGraphReturnsForest() {
        Graph g=new Graph(5,false);g.addEdge(0,1,4);g.addEdge(1,2,1);g.addEdge(0,2,2);g.addEdge(3,4,7);
        MST.Result a=MST.kruskal(g,new Metrics()),b=MST.prim(g,new Metrics());
        assertEquals(a.weight(),b.weight());assertEquals(3,a.edges());assertEquals(2,a.components());
    }
    @Test void kosarajuFindsComponents() {
        Graph g=new Graph(5,true);g.addEdge(0,1,1);g.addEdge(1,0,1);g.addEdge(1,2,1);g.addEdge(2,3,1);g.addEdge(3,2,1);
        assertEquals(3,GraphAlgorithms.kosaraju(g,new Metrics()).count());
    }
    @Test void kahnDetectsCycleAndReturnsValidDagOrder() {
        Graph g=new Graph(4,true);g.addEdge(0,1,1);g.addEdge(0,2,1);g.addEdge(1,3,1);g.addEdge(2,3,1);
        var t=GraphAlgorithms.kahn(g,new Metrics());assertTrue(t.acyclic());
        int[] pos=new int[4];for(int i=0;i<t.order().size();i++)pos[t.order().get(i)]=i;
        for(var e:g.edges())assertTrue(pos[e.from()]<pos[e.to()]);
        g.addEdge(3,0,1);assertFalse(GraphAlgorithms.kahn(g,new Metrics()).acyclic());
    }
    @Test void dagShortestPathsAllowNegativeWeightsAndRestorePath() {
        Graph g=new Graph(4,true);g.addEdge(0,1,5);g.addEdge(0,2,2);g.addEdge(2,1,-4);g.addEdge(1,3,3);
        var p=GraphAlgorithms.dagShortestPaths(g,new Metrics());
        assertEquals(0,p.distance()[0]);assertEquals(-2,p.distance()[1]);assertEquals(1,p.distance()[3]);
        assertEquals(List.of(0,2,1,3),p.pathTo(3));
    }
    @Test void sccIsStackSafeOnOneHundredThousandVertexPath() {
        Graph g=new Graph(100_000,true);for(int i=0;i<99_999;i++)g.addEdge(i,i+1,1);
        assertEquals(100_000,GraphAlgorithms.kosaraju(g,new Metrics()).count());
    }
    @Test void selfLoopsParallelEdgesAndIsolatedVerticesAreSafe() {
        Graph g=new Graph(3,false);g.addEdge(0,0,1);g.addEdge(0,1,4);g.addEdge(0,1,2);
        assertEquals(2,MST.kruskal(g,new Metrics()).weight());
        assertEquals(2,MST.prim(g,new Metrics()).weight());
    }
}
