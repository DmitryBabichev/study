import java.util.ArrayList;
import java.util.Arrays;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdDraw;

public class FastCollinearPoints {
    private int n;
    private final ArrayList<LineSegment> ls;
    
    public FastCollinearPoints(Point[] points) {
    // finds all line segments containing 4 or more points
        int k = 2;
        n = 0;
        ls = new ArrayList<LineSegment>();
        LineSegment l;
        Point minp, maxp;
        if (points == null) throw new IllegalArgumentException("incorrect");
        Point[] cp = points.clone();
        for (int i = 0; i < points.length; i++) {
            minp = points[i];
            maxp = points[i];
            k = 2;
            Arrays.sort(cp, points[i].slopeOrder());
            for (int j = 1; j < points.length; j++) {
                if (points[i].slopeTo(cp[j]) == points[i].slopeTo(cp[j-1])) {
                    k++;
                    if (minp.compareTo(cp[j]) == -1) minp = cp[j];
                    if (maxp.compareTo(cp[j]) == 1) maxp = cp[j];
                    if (minp.compareTo(cp[j-1]) == -1) minp = cp[j-1];
                    if (maxp.compareTo(cp[j-1]) == 1) maxp = cp[j-1];
                }
                else {
                    if (k > 3) {
                        l = new LineSegment(minp, maxp);
                        for (int t = 0; t < ls.size(); t++) {
                            if (l.toString().equals(ls.get(t).toString())) k = -1;
                        }
                        if (k > 0) {
                            ls.add(l);
                            n++;
                        }
                    }
                    minp = points[i];
                    maxp = points[i];
                    if (minp.compareTo(cp[j]) == -1) minp = cp[j];
                    if (maxp.compareTo(cp[j]) == 1) maxp = cp[j];
                    k = 2;
                }
            }
            if (k > 3) {
                l = new LineSegment(minp, maxp);
                for (int t = 0; t < ls.size(); t++) {
                    if (l.toString().equals(ls.get(t).toString())) k = -1;
                }
                if (k > 0) {
                    ls.add(l);
                    n++;
                }
            }
        }
    }
    
    public int numberOfSegments() {
    // the number of line segments
        return n;
    }
    
    public LineSegment[] segments() {
    // the line segments
        return ls.toArray(new LineSegment[ls.size()]);
    }
    
    public static void main(String[] args) {
        
        // read the n points from a file
        In in = new In(args[0]);
        int n = in.readInt();
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            int x = in.readInt();
            int y = in.readInt();
            points[i] = new Point(x, y);
        }
        
        // draw the points
        StdDraw.enableDoubleBuffering();
        StdDraw.setXscale(0, 32768);
        StdDraw.setYscale(0, 32768);
        for (Point p : points) {
            p.draw();
        }
        StdDraw.show();
        
        // print and draw the line segments
        FastCollinearPoints collinear = new FastCollinearPoints(points);
        for (LineSegment segment : collinear.segments()) {
            StdOut.println(segment);
            segment.draw();
        }
        StdDraw.show();
    }
}