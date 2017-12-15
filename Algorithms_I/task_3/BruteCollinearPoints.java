import java.util.ArrayList;
import java.util.Arrays;

public class BruteCollinearPoints {
    private int n;
    private final ArrayList<LineSegment> ls;
    
    public BruteCollinearPoints(Point[] points) {
    // finds all line segments containing 4 points
        n = 0;
        ls = new ArrayList<LineSegment>();
        if (points == null) throw new IllegalArgumentException("incorrect");
        double s1, s2;
        Arrays.sort(points);
        for (int i = 0; i < points.length-3; i++) {
            for (int j = i+1; j < points.length-2; j++) {
                s1 = points[i].slopeTo(points[j]);
                for (int k = j+1; k < points.length-1; k++) {
                    s2 = points[i].slopeTo(points[k]);
                    if (s1 == s2) {
                        for (int l = k+1; l < points.length; l++) {
                            if (s1 == points[i].slopeTo(points[l])) {
                                n++;
                                ls.add(new LineSegment(points[i], points[l]));
                            }
                        }
                    }
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
        return;
    }
}