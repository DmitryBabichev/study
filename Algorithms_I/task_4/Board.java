import java.util.Queue;
import java.util.LinkedList;
import java.util.Arrays;

public class Board {
    private final int n;
    private final int[] body;
    private final int m = 0;
    
    public Board(int[][] blocks) {
        // construct a board from an n-by-n array of blocks
        // (where blocks[i][j] = block in row i, column j)
        n = blocks[0].length;
        body = new int[n*n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                body[i*n + j] = blocks[i][j];
                if (blocks[i][j] != 0) {
                    m += Math.abs(i - (blocks[i][j]-1)/n);
                    m += Math.abs(j - (blocks[i][j]-1)%n);
                }
            }
        }
    }
    
    public int dimension() {
        // board dimension n
        return n;
    }
    
    public int hamming() {
        // number of blocks out of place
        int h = 0;
        for (int i = 0; i < n*n - 1; i++) {
            if (body[i] != i+1) h++;
        }
        return h;
    }
    
    public int manhattan() {
        // sum of Manhattan distances between blocks and goal
        return m;
    }
    
    public boolean isGoal() {
        // is this board the goal board?
        for (int i = 0; i < n*n - 1; i++) {
            if (body[i] != i+1) return false;
        }
        return true;
    }
    
    public Board twin() {
        // a board that is obtained by exchanging any pair of blocks
        int[][] blocks = new int[n][n];
        int i = 0;
        while (i < n*n-1) {
            if (body[i] != 0 && body[i+1] != 0) {
                blocks[i/n][i%n] = body[i+1];
                blocks[(i+1)/n][(i+1)%n] = body[i];
                i += 2;
                break;
            }
            else blocks[i/n][i%n] = body[i++];
        }
        while (i < n*n) blocks[i/n][i%n] = body[i++];
        return new Board(blocks);
    }
    
    public boolean equals(Object other) {
        // does this board equal other?
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        
        Board that = (Board) other;
        return Arrays.equals(this.body, that.body);
    }
    
    public Iterable<Board> neighbors() {
        // all neighboring boards
        int col, row;
        int j = 0;
        int[][] blocks = new int[n][n];
        Queue<Board> queue = new LinkedList<Board>();
        for (int i = 0; i < n*n; i++) blocks[i/n][i%n] = body[i];
        while (body[j] != 0) j++;
        row = j / n;
        col = j % n;
        if (col != 0) {
            blocks[row][col] = blocks[row][col-1];
            blocks[row][col-1] = 0;
            queue.add(new Board(blocks));
            blocks[row][col-1] = blocks[row][col];
            blocks[row][col] = 0;
        }
        if (col != n-1) {
            blocks[row][col] = blocks[row][col+1];
            blocks[row][col+1] = 0;
            queue.add(new Board(blocks));
            blocks[row][col+1] = blocks[row][col];
            blocks[row][col] = 0;
        }
        if (row != 0) {
            blocks[row][col] = blocks[row-1][col];
            blocks[row-1][col] = 0;
            queue.add(new Board(blocks));
            blocks[row-1][col] = blocks[row][col];
            blocks[row][col] = 0;
        }
        if (row != n-1) {
            blocks[row][col] = blocks[row+1][col];
            blocks[row+1][col] = 0;
            queue.add(new Board(blocks));
            blocks[row+1][col] = blocks[row][col];
            blocks[row][col] = 0;
        }
        return queue;
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(n + "\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                s.append(String.format("%2d ", body[i*n+j]));
            }
            s.append("\n");
        }
        return s.toString();
    }

    public static void main(String[] args) {
        // unit tests (not graded)
        return;
    }
}