package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> comp;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comp = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }
        T maximum = get(0);
        for (int i = 0; i < size(); i++) {
            if (comp.compare(get(i), maximum) > 0) {
                maximum = get(i);
            }
        }
        return maximum;
    }

    public T max(Comparator<T> c) {
        Comparator<T> temp = comp;
        comp = c;
        T max = max();
        comp = temp;
        return max;
    }
}
