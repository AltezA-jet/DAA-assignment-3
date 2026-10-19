package daa.benchmark;

import daa.algorithms.*;
import daa.graph.Graph;
import daa.metrics.Metrics;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class Benchmark {
    private static final int[] SIZES={1000,3162,10000,31623,100000};
    private Benchmark() {}
    private static void swap(int[] a,int i,int j){int t=a[i];a[i]=a[j];a[j]=t;}
    private static void shuffle(int[] a,Random r){for(int i=a.length-1;i>0;i--)swap(a,i,r.nextInt(i+1));}
    private static Graph undirected(int n,int m) {
        Random r=new Random(42); Graph g=new Graph(n,false); int[] p=new int[n];for(int i=0;i<n;i++)p[i]=i;shuffle(p,r);
        for(int i=1;i<n;i++)g.addEdge(p[r.nextInt(i)],p[i],1+r.nextInt(1000));
        while(g.edges().size()<m){int a=r.nextInt(n),b=r.nextInt(n);if(b==a)continue;g.addEdge(a,b,1+r.nextInt(1000));}
        return g;
    }
    private static Graph directed(int n,int m,boolean dag) {
        Random r=new Random(42); Graph g=new Graph(n,true);int[] rank=new int[n];for(int i=0;i<n;i++)rank[i]=i;if(dag)shuffle(rank,r);
        while(g.edges().size()<m){int a=r.nextInt(n),b=r.nextInt(n);if(a==b)continue;if(dag&&rank[a]>rank[b]){int t=a;a=b;b=t;}g.addEdge(a,b,1+r.nextInt(1000));}
        return g;
    }
    private static long medianTime(Runnable task) {long[] t=new long[5];for(int i=0;i<5;i++){long s=System.nanoTime();task.run();t[i]=(System.nanoTime()-s)/1_000_000;}Arrays.sort(t);return t[2];}
    private static void row(PrintWriter out,String alg,String graph,int n,int m,long time,Metrics x,String result){
        out.printf(Locale.ROOT,"%s,%s,%d,%d,%d,%d,%d,%d,%d,%s%n",alg,graph,n,m,time,x.dsuOps,x.edgeScans,x.comparisons,x.maxDepth,result);
    }
    public static void run(Path output) throws IOException {
        Files.createDirectories(output.getParent()); try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(output))){
            out.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            for(int n:SIZES) for(int factor:new int[]{4,20}) {
                int m=factor*n; String suffix=factor==4?"sparse":"dense";
                Graph ug=undirected(n,m); Metrics k=new Metrics(),p=new Metrics();
                final MST.Result[] kr={null},pr={null};
                long kt=medianTime(()->{k.reset();kr[0]=MST.kruskal(ug,k);});
                long pt=medianTime(()->{p.reset();pr[0]=MST.prim(ug,p);});
                if(kr[0].weight()!=pr[0].weight())throw new IllegalStateException("MST mismatch for n="+n+" m="+m+": "+kr[0]+" vs "+pr[0]);
                row(out,"Kruskal","undirected-"+suffix,n,m,kt,k,Long.toString(kr[0].weight()));
                row(out,"Prim","undirected-"+suffix,n,m,pt,p,Long.toString(pr[0].weight()));
                Graph dg=directed(n,m,false); Metrics s=new Metrics();final GraphAlgorithms.SCCResult[] sr={null};
                long st=medianTime(()->{s.reset();sr[0]=GraphAlgorithms.kosaraju(dg,s);});
                row(out,"Kosaraju","directed-"+suffix,n,m,st,s,Integer.toString(sr[0].count()));
                Graph dag=directed(n,m,true); Metrics q=new Metrics();final GraphAlgorithms.TopoResult[] qr={null};
                long qt=medianTime(()->{q.reset();qr[0]=GraphAlgorithms.kahn(dag,q);});
                if(!qr[0].acyclic())throw new IllegalStateException("Generated DAG has a cycle");
                row(out,"Kahn","dag-"+suffix,n,m,qt,q,Integer.toString(qr[0].order().size()));
                Metrics d=new Metrics();final GraphAlgorithms.PathResult[] dr={null};
                long dt=medianTime(()->{d.reset();dr[0]=GraphAlgorithms.dagShortestPaths(dag,d);});
                row(out,"DagShortestPath","dag-"+suffix,n,m,dt,d,Long.toString(dr[0].sumDistances()));
                System.out.printf("Finished n=%d m=%d (%s)%n",n,m,suffix);
            }
        }
    }
}
