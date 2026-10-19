import csv
import math
from pathlib import Path
import matplotlib.pyplot as plt

csv_path = Path("results/results.csv")
out = Path("results/plots")
out.mkdir(parents=True, exist_ok=True)
if not csv_path.exists():
    raise SystemExit("Missing results/results.csv. Run the benchmark first.")

with csv_path.open(newline="", encoding="utf-8") as f:
    rows = list(csv.DictReader(f))

# Plot 1: edge scans against n, split by algorithm and density.
fig, ax = plt.subplots()
for alg in sorted({r["algorithm"] for r in rows}):
    for density in ("sparse", "dense"):
        selected = [r for r in rows if r["algorithm"] == alg and density in r["graph"]]
        selected.sort(key=lambda r: int(r["n"]))
        if selected:
            ax.plot([int(r["n"]) for r in selected], [max(1, int(r["edge_scans"])) for r in selected],
                    marker="o", label=f"{alg} ({density})")
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("Vertices n"); ax.set_ylabel("Edge scans"); ax.set_title("Operations vs n")
ax.legend(fontsize="small"); ax.grid(True, which="both", alpha=.25)
fig.tight_layout(); fig.savefig(out / "operations_vs_n.png", dpi=160); plt.close(fig)

# Plot 2: scans per input edge for algorithms where the metric is meaningful.
fig, ax = plt.subplots()
for alg in ("Kosaraju", "Kahn", "DagShortestPath", "Kruskal", "Prim"):
    selected = [r for r in rows if r["algorithm"] == alg]
    selected.sort(key=lambda r: int(r["n"]))
    if selected:
        ax.plot([int(r["n"]) for r in selected],
                [int(r["edge_scans"]) / max(1, int(r["m"])) for r in selected],
                marker="o", label=alg)
ax.set_xscale("log"); ax.set_xlabel("Vertices n"); ax.set_ylabel("edge_scans / m")
ax.set_title("Scans per edge"); ax.legend(fontsize="small"); ax.grid(True, which="both", alpha=.25)
fig.tight_layout(); fig.savefig(out / "scans_per_edge.png", dpi=160); plt.close(fig)

# Plot 3: fraction of input edges examined by Kruskal, by density.
fig, ax = plt.subplots()
for density in ("sparse", "dense"):
    selected = [r for r in rows if r["algorithm"] == "Kruskal" and density in r["graph"]]
    selected.sort(key=lambda r: int(r["n"]))
    if selected:
        ax.plot([int(r["n"]) for r in selected],
                [int(r["edge_scans"]) / max(1, int(r["m"])) for r in selected],
                marker="o", label=density)
ax.set_xscale("log"); ax.set_xlabel("Vertices n"); ax.set_ylabel("Kruskal edge_scans / m")
ax.set_title("Share of edges examined by Kruskal"); ax.legend(); ax.grid(True, which="both", alpha=.25)
fig.tight_layout(); fig.savefig(out / "kruskal_edge_share.png", dpi=160); plt.close(fig)

# Slope estimates from log-log linear regression (least squares).
print("Log-log slope estimates (edge_scans vs n):")
for alg in sorted({r["algorithm"] for r in rows}):
    selected = [r for r in rows if r["algorithm"] == alg]
    if len(selected) < 2: continue
    xs = [math.log(int(r["n"])) for r in selected]
    ys = [math.log(max(1, int(r["edge_scans"]))) for r in selected]
    xbar, ybar = sum(xs)/len(xs), sum(ys)/len(ys)
    slope = sum((x-xbar)*(y-ybar) for x,y in zip(xs,ys)) / sum((x-xbar)**2 for x in xs)
    print(f"  {alg}: {slope:.3f}")
