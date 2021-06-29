package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class GenericNode {
        private GenericNode older;
        private T now;
        private GenericNode later;
        GenericNode(GenericNode og, T g, GenericNode gn) {
            older = og;
            now = g;
            later = gn;
        }
    }

    private int size;
    private GenericNode sentinel;

    public LinkedListDeque() {
        sentinel = new GenericNode(null, null, null);
        sentinel.older = sentinel;
        sentinel.later = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T g) {
        GenericNode toAdd = new GenericNode(sentinel, g, sentinel.later);
        sentinel.later.older = toAdd;
        sentinel.later = toAdd;
        size++;
    }

    @Override
    public T removeFirst() {
        if (sentinel.later == sentinel) {
            return null;
        }
        T toReturn = sentinel.later.now;
        sentinel.later = sentinel.later.later;
        sentinel.later.older = sentinel;
        size--;
        return toReturn;
    }

    @Override
    public void addLast(T g) {
        GenericNode toAdd = new GenericNode(sentinel.older, g, sentinel);
        sentinel.older.later = toAdd;
        sentinel.older = toAdd;
        size++;
    }

    @Override
    public T removeLast() {
        if (sentinel.older == sentinel) {
            return null;
        }
        T toReturn = sentinel.older.now;
        sentinel.older = sentinel.older.older;
        sentinel.older.later = sentinel;
        size--;
        return toReturn;
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
        GenericNode currNode = sentinel.later;
        String s = "";
        while (currNode != sentinel) {
            s += currNode.now.toString() + ' ';
            currNode = currNode.later;
        }
        System.out.println(s);
    }

    @Override
    public T get(int i) {
        GenericNode currNode = sentinel.later;
        int x = 0;
        while (currNode != sentinel) {
            if (x == i) {
                return currNode.now;
            }
            x++;
            currNode = currNode.later;
        }
        return null;
    }

    public T getRecursive(int i) {
        return recursiveHelper(sentinel.later, i);
    }

    /** Helper function for getRecursive */
    private T recursiveHelper(GenericNode g, int i) {
        if (g == sentinel || i < 0) {
            return null;
        } else if (i == 0) {
            return g.now;
        } else {
            return recursiveHelper(g.later, i - 1);
        }
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
        return new LinkedDequeIterator();
    }

    private class LinkedDequeIterator implements Iterator<T> {
        private GenericNode currNode;

        LinkedDequeIterator() {
            currNode = sentinel.later;
        }

        public boolean hasNext() {
            return (currNode != sentinel);
        }

        public T next() {
            T toReturn = currNode.now;
            currNode = currNode.later;
            return toReturn;
        }
    }


}
