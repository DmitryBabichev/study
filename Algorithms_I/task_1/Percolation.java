import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private int n;
    private WeightedQuickUnionUF grid;
    private int[] opened;
    private int openedCount;
    
    private int getIndex(int i, int j) {
        if (i <= 0 || i > this.n) throw new IndexOutOfBoundsException("row");
        if (j <= 0 || j > this.n) throw new IndexOutOfBoundsException("col");
        return (i - 1) * this.n + j - 1;
    }
    
    public Percolation(int n) {
        // create n-by-n grid, with all sites blocked
        this.n = n;
        this.grid = new WeightedQuickUnionUF(n * n + 2);
        this.opened = new int[n * n];
        this.openedCount = 0;
    }
    
    public void open(int row, int col) {
        // open site (row, col) if it is not open already
        //System.out.printf("%d %d %d\n", this.n, row, col);
        int id;
        int id_temp;
        id = this.getIndex(row, col);
        if (this.opened[id] == 0) {
            this.opened[id] = 1;
            this.openedCount += 1;
            if (this.n < 2) return;
            if (row == 1) {
                if (this.isOpen(row + 1, col)) {
                    id_temp = this.getIndex(row + 1, col);
                    this.grid.union(id, id_temp);
                }
                this.grid.union(id, this.n * this.n);
            }
            else if (row == this.n) {
                if (this.isOpen(row - 1, col)) {
                    id_temp = this.getIndex(row - 1, col);
                    this.grid.union(id, id_temp);
                }
                this.grid.union(id, this.n * this.n + 1);
            }
            else if (col == 1) {
                if (this.isOpen(row, col + 1)) {
                    id_temp = this.getIndex(row, col + 1);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row - 1, col)) {
                    id_temp = this.getIndex(row - 1, col);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row + 1, col)) {
                    id_temp = this.getIndex(row + 1, col);
                    this.grid.union(id, id_temp);
                }                      
            }
            else if (col == this.n) {
                if (this.isOpen(row, col - 1)) {
                    id_temp = this.getIndex(row, col - 1);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row - 1, col)) {
                    id_temp = this.getIndex(row - 1, col);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row + 1, col)) {
                    id_temp = this.getIndex(row + 1, col);
                    this.grid.union(id, id_temp);
                }                      
            }
            else {
                if (this.isOpen(row, col - 1)) {
                    id_temp = this.getIndex(row, col - 1);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row, col + 1)) {
                    id_temp = this.getIndex(row, col + 1);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row - 1, col)) {
                    id_temp = this.getIndex(row - 1, col);
                    this.grid.union(id, id_temp);
                }
                if (this.isOpen(row + 1, col)) {
                    id_temp = this.getIndex(row + 1, col);
                    this.grid.union(id, id_temp);
                }                
            }
        }
    }
    
    public boolean isOpen(int row, int col) {
        // is site (row, col) open?
        return (this.opened[this.getIndex(row, col)] == 1);
    }
    
    public boolean isFull(int row, int col) {
        // is site (row, col) full?
        // is union this site with any site in top?
        int id;
        if (this.isOpen(row, col) == false) return false;
        id = this.getIndex(row, col);
        return this.grid.connected(id, this.n * this.n);
    }
    
    public int numberOfOpenSites() {
        // number of open sites
        return this.openedCount;
    }
    
    public boolean percolates() {
        // does the system percolate?
        // are any site in top union withsite in bottom?
        return this.grid.connected(this.n * this.n + 1, this.n * this.n);
    }
        
    public static void main(String[] args) {
        // test client (optional)
    }
}