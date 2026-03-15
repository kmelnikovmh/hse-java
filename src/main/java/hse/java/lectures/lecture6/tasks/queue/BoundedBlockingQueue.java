package hse.java.lectures.lecture6.tasks.queue;

public class BoundedBlockingQueue<T> {

    int head, tail, count;
    Object[] items;

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        items = new Object[capacity];
    }

    public synchronized void put(T item) throws InterruptedException {
        while (count == items.length) {
            wait();
        }
        items[tail] = item;
        tail = (tail + 1) % items.length;
        ++count;
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
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

    public synchronized int size() {
        return count;
    }

    public int capacity() {
        return items.length;
    }
}
