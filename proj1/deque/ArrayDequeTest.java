package deque;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    /** Tests basic functionality of the ArrayDeque */
    public void beginningFunctionality() {
        ArrayDeque<Integer> alist = new ArrayDeque<>();
        alist.addFirst(3);
        alist.addLast(4);
        alist.addLast(5);
        alist.addFirst(2);
        alist.removeLast();
        alist.removeFirst();
        int x = alist.get(0);
        //alist.printDeque();
    }

    @Test
    /** Tests resizing of the ArrayDeque.
     * In the visualizer, the array length should always resize properly.
     */
    public void primitiveResize() {
        ArrayDeque<Integer> alist = new ArrayDeque<>();
        for (int i = 0; i < 19; i++) {
            alist.addLast(i);
        }
        for (int i = 0; i < 19; i++) {
            alist.removeFirst();
        }
        Integer x = alist.removeLast();
    }

    @Test
    public void randomizedTest() {
        SampleAList<Integer> L = new SampleAList<>();
        ArrayDeque<Integer> a = new ArrayDeque<>();
        int N = 50000;
        int operationNumber;
        for (int i = 0; i < N; i += 1) {
            if (L.size() == 0 || a.size() == 0) {
                operationNumber = StdRandom.uniform(0, 2);
            } else {
                operationNumber = StdRandom.uniform(0, 4);
            }

            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                a.addLast(randVal);
                //System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int size2 = a.size();
                //System.out.println("size: " + size);
                assertEquals("Size of lists: ", size, size2);
            } else if (operationNumber == 2) {
                int randVal = StdRandom.uniform(0, a.size());
                int x = L.get(randVal);
                int y = a.get(randVal);
                //System.out.println("Last item: " + x);
                assertEquals("Item at index: " + randVal + ": ", x, y);
            } else if (operationNumber == 3) {
                int x = L.removeLast();
                int y = a.removeLast();
                assertEquals("Removed item ", x, y);
                //System.out.println("Removed item: " + y);
            }
        }
    }

    @Test
    public void testEquals() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        ad1.addFirst(3);
        ad2.addFirst(3);
        lld1.addFirst(3);
        ad1.addFirst(2);
        ad2.addFirst(2);
        lld1.addFirst(2);
        assertTrue("LLD-LLD Equality: ", ad1.equals(ad2));
        assertTrue("LLD-AD Equality: ", ad1.equals(lld1));
        assertFalse("Null case: ", ad1.equals(null));
        ad2.removeLast();
        lld1.removeLast();
        assertFalse("LLD-LLD Inequality (Size): ", ad1.equals(ad2));
        assertFalse("LLD-AD Inequality (Size): ", ad1.equals(lld1));
        ad2.addFirst(4);
        lld1.addFirst(4);
        assertFalse("LLD-LLD Inequality (Mismatch): ", ad1.equals(ad2));
        assertFalse("LLD-AD Inequality (Mismatch): ", ad1.equals(lld1));
    }
}
