import java.util.Iterator;
import java.util.NoSuchElementException;


public class Deque<Item> implements Iterable<Item> {
    private Node first;
    private Node last;
    private int count;
    
    private class Node {
        Item item;
        Node next;
        Node previous;
    }
    
    public Deque() {
    // construct an empty deque
        count = 0;
        first = null;
        last = null;
    }
    
    public boolean isEmpty() {
    // is the deque empty?
        return count == 0;
    }
    
    public int size() {
    // return the number of items on the deque
        return count;
    }
    
    public void addFirst(Item item) {
    // add the item to the front
        if (item == null) throw new IllegalArgumentException("incorrect input");
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        if (oldfirst != null) oldfirst.previous = first;
        first.next = oldfirst;
        first.previous = null;
        count++;
        if (count == 1) last = first;
    }
    
    public void addLast(Item item) {
    // add the item to the end
        if (item == null) throw new IllegalArgumentException("incorrect input");
        Node newlast = new Node();
        newlast.item = item;
        newlast.next = null;
        newlast.previous = last;
        if (last != null) last.next = newlast;
        last = newlast;
        count++;
        if (count == 1) first = last;
    }
    
    public Item removeFirst() {
    // remove and return the item from the front
        if (this.isEmpty()) {
            throw new NoSuchElementException("Removing from empty dequeue");
        }
        Item item = first.item;
        first = first.next;
        if (first != null) first.previous = null;
        count--;
        return item;
    }
    
    public Item removeLast() {
    // remove and return the item from the end
        if (this.isEmpty()) {
            throw new NoSuchElementException("Removing from empty dequeue");
        }
        Item item = last.item;
        last = last.previous;
        if (last != null) last.next = null;
        count--;
        return item;
    }
    
    public Iterator<Item> iterator() {
    // return an iterator over items in order from front to end
        return new ListIterator();
    }
    
    private class ListIterator implements Iterator<Item> {
        private Node current = first;
        public boolean hasNext() { return current != null; }
        public void remove() {
            throw new UnsupportedOperationException("Unsupported method");
        }
        public Item next() {
            if (current == null) {
                throw new NoSuchElementException("No more items to return");
            }
            Item item = current.item;
            current = current.next;
            return item;
        }
    }
    
    public static void main(String[] args) {
        // unit testing (optional)
        return;
    }
}