import java.util.Stack;
import java.util.Comparator;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;

public class Solver {
    private final Stack<Board> solution;
    private final int moves;
    
    public Solver(Board initial) {
        // find a solution to the initial board (using the A* algorithm)
        ManhattanOrder order = new ManhattanOrder();
        MinPQ<SearchNode> PQ = new MinPQ<SearchNode>(order);
        MinPQ<SearchNode> twinPQ = new MinPQ<SearchNode>(order);
        SearchNode Node = new SearchNode(initial);
        SearchNode twinNode = new SearchNode(initial.twin());
        
        PQ.insert(Node);
        twinPQ.insert(twinNode);
        SearchNode current = PQ.delMin();
        SearchNode twinCurrent = twinPQ.delMin();
        
        while(!current.board.isGoal() && !twinCurrent.board.isGoal()) {
            
            for (Board b : current.board.neighbors()) {      
                if (current.prev == null || !b.equals(current.prev.board)) {
                    SearchNode n = new SearchNode(b);
                    n.moves = current.moves + 1;
                    n.prev = current;
                    PQ.insert(n);
                }
            }
            
            for (Board b : twinCurrent.board.neighbors()) {
                if (twinCurrent.prev == null ||!b.equals(twinCurrent.prev.board)) {
                    SearchNode n = new SearchNode(b);
                    n.moves = twinCurrent.moves + 1;
                    n.prev = twinCurrent;
                    twinPQ.insert(n);
                }
            }
            
            current = PQ.delMin();
            twinCurrent = twinPQ.delMin();
        }
        
        if (current.board.isGoal()) {
            moves = current.moves;
            solution = new Stack<Board>();
            for (SearchNode s = current; s != null; s = s.prev) 
                solution.push(s.board);
        }
        else {
            moves = 0;
            solution = null;
        }
    }
    
    public boolean isSolvable() {
        // is the initial board solvable?
        return (solution != null);
    }
    
    public int moves() {
        // min number of moves to solve initial board; -1 if unsolvable
        return moves;
    }
    
    public Iterable<Board> solution() {
        // sequence of boards in a shortest solution; null if unsolvable
        return solution;
    }
    
    private class SearchNode {
        int moves;
        Board board;
        SearchNode prev;
        
        public SearchNode(Board initial) {
            moves = 0;
            prev = null;
            board = initial;
        }
    }
    
    private class ManhattanOrder implements Comparator<SearchNode> {
        public int compare(SearchNode a, SearchNode b) {
            int pa = a.board.manhattan() + a.moves;
            int pb = b.board.manhattan() + b.moves;
            if (pa > pb) return 1;
            if (pa < pb) return -1;
            else return 0;
        }
    }
    
    public static void main(String[] args) {
        
        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] blocks = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
            blocks[i][j] = in.readInt();
        Board initial = new Board(blocks);
        
        // solve the puzzle
        Solver solver = new Solver(initial);
        
        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}