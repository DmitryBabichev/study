import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class PercolationStats {
    
    private double[] fractions;
    private int n;
    private int trials;
    
    public PercolationStats(int n, int trials) {
    // perform trials independent experiments on an n-by-n grid
        if (n <= 0 || trials <= 0) throw new IllegalArgumentException("War");
        Percolation[] percs;
        percs = new Percolation[trials];
        for (int i = 0; i < trials; i++) {
            percs[i] = new Percolation(n);
        }
        int col;
        int row;
        this.fractions = new double[trials];
        for (int i = 0; i < trials; i++) {
            while (percs[i].percolates() == false) {
                col = StdRandom.uniform(n) + 1;
                row = StdRandom.uniform(n) + 1;
                while (percs[i].isOpen(row, col) == true) {
                    col = StdRandom.uniform(n) + 1;
                    row = StdRandom.uniform(n) + 1;
                }
                percs[i].open(row, col);
            }
            this.fractions[i] = percs[i].numberOfOpenSites() * 1.0 / (n * n);
        }
        this.n = n;
        this.trials = trials;
    }
    
    public double mean() {
    // sample mean of percolation threshold
        return StdStats.mean(this.fractions);
    }
    public double stddev() {
    // sample standard deviation of percolation threshold
        return StdStats.stddev(this.fractions);
    }
    public double confidenceLo() {
    // low  endpoint of 95% confidence interval
        return this.mean() - 1.96 * this.stddev() / Math.sqrt(this.trials);
    }
    public double confidenceHi() {
    // high endpoint of 95% confidence interval
        return this.mean() + 1.96 * this.stddev() / Math.sqrt(this.trials);
    }

    public static void main(String[] args) {
        // test client (described below)
        int n = Integer.parseInt(args[0]);
        int trials = Integer.parseInt(args[1]);
        PercolationStats s = new PercolationStats(n, trials);
        System.out.printf("mean                    = %1.16f\n", s.mean());
        System.out.printf("stddev                  = %1.16f\n", s.stddev());
        System.out.printf("95%% confidence interval = [%1.16f, %1.16f]\n",
            s.confidenceLo(), s.confidenceHi());
    }
}