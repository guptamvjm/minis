package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

    private T[] items;
    private int size;
    private int nextStart;
    private int nextEnd;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextStart = 7;
        nextEnd = 0;
    }

    /** Returns the next index so no out of bounds errors occur.
     * for example, if nextLast is 7, and addLast is called, nextLast
     * should be 0 and not 8.
     * @param steps: Denotes how many steps (and direction denoted by negation)
     */
    private int makeIndex(int index, int steps) {
        int sum = index + steps;
        if (sum >= items.length) {
            return sum % items.length;
        } else if (sum < 0) {
            return sum + items.length;
        } else {
            return sum;
        }
    }

    private void resize(int newSize) {
        T[] newItems = (T[]) new Object[newSize];
        for (int i = 0; i < size; i++) {
            newItems[i] = get(i);
        }
        items = newItems;
        nextStart = makeIndex(0, -1);
        nextEnd = size;
    }

    /** Checks if list should be resizes; if so, resizes appropriately */
    private void checkResize() {
        if (size == items.length) {
            resize(size * 2);
        } else if (usageFactor() < 0.25 && items.length >= 16) {
            resize(items.length / 2);
        }
    }

    /** Returns usage factor of the list */
    private double usageFactor() {
        return (double) size / items.length;
    }

    @Override
    public void addFirst(T g) {
        checkResize();
        items[nextStart] = g;
        nextStart = makeIndex(nextStart, -1);
        size++;
    }

    @Override
    public void addLast(T g) {
        checkResize();
        items[nextEnd] = g;
        nextEnd = makeIndex(nextEnd, 1);
        size++;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextEnd = makeIndex(nextEnd, -1);
        T returnItem = items[nextEnd];
        items[nextEnd] = null;
        size--;
        checkResize();
        return returnItem;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextStart = makeIndex(nextStart, 1);
        T returnItem = items[nextStart];
        items[nextStart] = null;
        size--;
        checkResize();
        return returnItem;
    }

    /*
    public boolean isEmpty() {
        return (size == 0);
    }
    */

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        String s = "";
        int i = makeIndex(nextStart, 1);
        int count = 0;
        while (count < size) {
            s += items[i].toString() + " ";
            i = makeIndex(i, 1);
            count++;
        }
        System.out.println(s);
    }

    @Override
    public T get(int i) {
        int index = makeIndex(nextStart, i + 1);
        return items[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Deque)) {
            return false;
        }
        Deque<T> other = (Deque<T>) obj;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            T thisObj = get(i);
            T that = other.get(i);
            if (!(thisObj.equals(that))) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        int currPos;

        ArrayDequeIterator() {
            currPos = 0;
        }

        public boolean hasNext() {
            return (currPos < size);
        }

        public T next() {
            T toReturn = get(currPos);
            currPos = currPos + 1;
            return toReturn;
        }
    }
}
