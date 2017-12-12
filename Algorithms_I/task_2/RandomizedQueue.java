import edu.princeton.cs.algs4.StdRandom;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RandomizedQueue<Item> implements Iterable<Item> {
    private Item[] a;
    private int count;
    private int[] order;
    
    public RandomizedQueue() {
    // construct an empty randomized queue
        a = (Item[]) new Object[2];
        count = 0;
        order = StdRandom.permutation(count);
    }
    
    public boolean isEmpty() {
    // is the randomized queue empty?
        return count == 0;
    }
    
    public int size() {
    // return the number of items on the randomized queue
        return count;
    }
    
    private void resize(int capacity) {
    // resize an array if it is full
        assert capacity >= count;
        int nNull = 0;
        
        Item[] temp = (Item[]) new Object[capacity];
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null) {
                nNull++;
            }
            else {
                temp[i-nNull] = a[i];
            }
        }
        a = temp;
    }
    
    public void enqueue(Item item) {
    // add the item
        if (item == null) throw new IllegalArgumentException("null input");
        if (count == a.length) {
            resize(2*a.length);
            order = StdRandom.permutation(count+1);
        }
        a[count++] = item;
    }
    
    public Item dequeue() {
    // remove and return a random item
        if (isEmpty()) throw new NoSuchElementException("Stack underflow");
        Item item;
        if (count < 2) {
            item = a[0];
            a[0] = null;
        }
        else {
            item = a[order[count-1]];
            a[order[count-1]] = null;
        }
        count--;
        
        if (count > 0 && count == a.length/4) {
            resize(a.length/2);
            order = StdRandom.permutation(count);
        }
        return item;
    }
    
    public Item sample() {
    // return a random item (but do not remove it)
        if (isEmpty()) throw new NoSuchElementException("Stack underflow");
        int k = StdRandom.uniform(a.length);
        while (a[k] == null) {
            k++;
            if (k == a.length) k = 0;
        }
        return a[k];
    }
    
    public Iterator<Item> iterator() {
    // return an independent iterator over items in random order
        return new ArrayIterator();
    }
    
    private class ArrayIterator implements Iterator<Item> {
        private final Item[] b;
        private final int[] order;
        private int current;
        
        
        public ArrayIterator() {
            b = (Item[]) new Object[count];
            order = StdRandom.permutation(count);
            current = 0;
            int nNull = 0;
            for (int i = 0; i < a.length; i++) {
                if (a[i] == null) {
                    nNull++;
                }
                else {
                    b[i-nNull] = a[i];
                }
            }
        }
        
        public boolean hasNext() {
            return current < count;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            return b[order[current++]];
        }
    }
    
    public static void main(String[] args) {
    // unit testing (optional)
        return;
    }
}