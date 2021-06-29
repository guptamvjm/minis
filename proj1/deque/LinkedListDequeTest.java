package deque;

import org.junit.Test;
import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        //System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();

    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        //System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        //System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);

    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {


        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double> lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        //System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        //System.out.println("Make sure to uncomment the lines below (and delete this print statement).");

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    /* Tests the get methods for a LLD*/
    public void testGets() {
        LinkedListDeque<Integer> lst = new LinkedListDeque<Integer>();
        LinkedListDeque<Integer> empty = new LinkedListDeque<Integer>();
        lst.addLast(1);
        lst.addLast(2);
        lst.addLast(3);
        int x = lst.get(2);
        int y = lst.getRecursive(2);
        assertEquals("Iterative get (should have the same value)", 3, x);
        assertEquals("Recursive get (should have the same value)", 3, y);
        assertEquals("Iterative on empty list ", null, empty.get(2));
        assertEquals("Recursive on empty list ", null, empty.getRecursive(2));
    }

    @Test
    public void testEquals() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        LinkedListDeque<Integer> lld2 = new LinkedListDeque<>();
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        lld1.addFirst(3);
        lld2.addFirst(3);
        ad1.addFirst(3);
        lld1.addFirst(2);
        lld2.addFirst(2);
        ad1.addFirst(2);
        assertTrue("LLD-LLD Equality: ", lld1.equals(lld2));
        assertTrue("LLD-AD Equality: ", lld1.equals(ad1));
        assertFalse("Null case: ", lld1.equals(null));
        lld2.removeLast();
        ad1.removeLast();
        assertFalse("LLD-LLD Inequality (Size): ", lld1.equals(lld2));
        assertFalse("LLD-AD Inequality (Size): ", lld1.equals(ad1));
        lld2.addFirst(4);
        ad1.addFirst(4);
        assertFalse("LLD-LLD Inequality (Mismatch): ", lld1.equals(lld2));
        assertFalse("LLD-AD Inequality (Mismatch): ", lld1.equals(ad1));

        LinkedListDeque<Integer> emptyLLD = new LinkedListDeque<>();
        ArrayDeque<Integer> emptyAD = new ArrayDeque<>();
        assertFalse("LLD-LLD Inequality (Empty): ", emptyLLD.equals(lld1));
        assertFalse("LLD-AD Inequality (Empty): ", lld1.equals(emptyAD));

        SampleAList<Integer> x = new SampleAList<>();
        x.addLast(2);
        assertFalse("Non-deque inequality: ", lld1.equals(x));
    }

}
