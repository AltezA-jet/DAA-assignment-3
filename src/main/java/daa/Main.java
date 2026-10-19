package daa;

import daa.algorithms.*;
import daa.benchmark.Benchmark;
import daa.graph.Graph;
import daa.metrics.Metrics;
import java.nio.file.Path;
import java.util.*;

public final class Main {
    public static void main(String[] args) throws Exception {
        if(args.length==0 || args[0].equalsIgnoreCase("help")) {
            System.out.println("Commands:\n  java -jar ... benchmark [results/results.csv]\n  java -jar ... run <data-file>\n  mvn test");
            return;
        }
        switch(args[0].toLowerCase(Locale.ROOT)) {
            case "benchmark" -> Benchmark.run(Path.of(args.length>1?args[1]:"results/results.csv"));
            case "run" -> runGraph(Graph.read(Path.of(args[1])));
            default -> throw new IllegalArgumentException("Unknown command: "+args[0]);
        }
    }
    private static void runGraph(Graph g) {
        System.out.printf("Graph: V=%d E=%d type=%s%n",g.size(),g.edges().size(),g.directed()?"directed":"undirected");
        Metrics m=new Metrics();
        if(!g.directed()) {
            System.out.println("Kruskal: "+MST.kruskal(g,m));m.reset();
            System.out.println("Prim: "+MST.prim(g,m));
        } else {
            GraphAlgorithms.SCCResult s=GraphAlgorithms.kosaraju(g,m);
            System.out.println("SCC count: "+s.count()+"; components: "+s.components());
            GraphAlgorithms.TopoResult t=GraphAlgorithms.kahn(g,new Metrics());
            System.out.println("Topological order: "+(t.acyclic()?t.order():"cycle detected"));
            if(t.acyclic()) {
                GraphAlgorithms.PathResult p=GraphAlgorithms.dagShortestPaths(g,new Metrics());
                System.out.println("DAG distance sum: "+p.sumDistances());
                if(g.size()>0) System.out.println("Path to "+(g.size()-1)+": "+p.pathTo(g.size()-1));
            }
        }
    }
}
