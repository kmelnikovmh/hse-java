package hse.java.lectures.lecture6.tasks.queue;

public class BoundedBlockingQueue<T> {

    int head, tail, count;
    Object[] items;

    BoundedBlockingQueue(int capacity) {
        items = new Object[capacity];
    }

    synchronized void put(T item) throws InterruptedException {
        while (count == items.length) {
            wait();
        }
        items[tail] = item;
        tail = (tail + 1) % items.length;
        ++count;
        notifyAll();
    }

    synchronized T take() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        T item = (T)items[head];
        items[head] = null;
        head = (head + 1) % items.length;
        --count;
        notifyAll();
        return item;
    }

    synchronized int size() {
        return count;
    }

    private int capacity() {
        return items.length;
    }
}
