package hse.java.lectures.lecture3.practice.randomSet;

import java.util.Random;

public class RandomSet<T> {

    private static final int INIT_CAP = 16;
    private final Random rnd = new Random();

    private Object[] elements;
    private int size;

    private Object[] tableKeys;
    private int[] tableIdx;
    private int tableCap;
    private int tableSize;

    public RandomSet() {
        elements = new Object[INIT_CAP];
        size = 0;
        tableCap = INIT_CAP;
        tableKeys = new Object[tableCap];
        tableIdx = new int[tableCap];
        for (int i = 0; i < tableCap; ++i) {
            tableIdx[i] = -1;
        }
        tableSize = 0;
    }

    private int mixHash(Object key) {
        int h = key.hashCode(); h ^= (h >>> 16);
        return h & (tableCap - 1);
    }

    private int findSlot(Object key) {
        int pos = mixHash(key);
        for (;;) {
            Object k = tableKeys[pos];
            if (k == null) {
                return - (pos + 1);
            }
            if (k.equals(key)) {
                return pos;
            }
            pos = (pos + 1) & (tableCap - 1);
        }
    }

    private void growTable() {
        int newCap = tableCap * 2;
        Object[] oldKeys = tableKeys;
        int[] oldIdx = tableIdx;
        tableKeys = new Object[newCap];
        tableIdx = new int[newCap];
        for (int i = 0; i < newCap; ++i) {
            tableIdx[i] = -1;
        }
        int oldCap = tableCap;
        tableCap = newCap;
        tableSize = 0;
        for (int i = 0; i < oldCap; ++i) {
            if (oldKeys[i] != null) {
                insertKey((T) oldKeys[i], oldIdx[i]);
            }
        }
    }

    private void insertKey(T key, int elementIdx) {
        int pos = mixHash(key);
        while (tableKeys[pos] != null) {
            pos = (pos + 1) & (tableCap - 1);
        }
        tableKeys[pos] = key;
        tableIdx[pos] = elementIdx;
        ++tableSize;
    }

    // Task

    public boolean insert(T value) {
        if (value == null) {
            return false;
        }
        if (tableCap <= (tableSize + 1) * 2) {
            growTable();
        }
        int slot = findSlot(value);
        if (0 <= slot) {
            return false;
        }
        if (size == elements.length) {
            Object[] tmp = new Object[elements.length * 2];
            System.arraycopy(elements, 0, tmp, 0, elements.length);
            elements = tmp;
        }
        elements[size] = value;
        tableKeys[-slot - 1] = value;
        tableIdx[-slot - 1] = size;
        ++tableSize;
        ++size;
        return true;
    }

    public boolean remove(T value) {
        if (value == null) {
            return false;
        }
        int slot = findSlot(value);
        if (slot < 0) {
            return false;
        }
        
        int removeElementIdx = tableIdx[slot];
        tableKeys[slot] = null;
        tableIdx[slot] = -1;
        --tableSize;
        int curr = (slot + 1) & (tableCap - 1);
        while (tableKeys[curr] != null) {
            Object reKey = tableKeys[curr];
            int reIdx = tableIdx[curr];
            tableKeys[curr] = null;
            tableIdx[curr] = -1;
            --tableSize;
            insertKey((T) reKey, reIdx);
            curr = (curr + 1) & (tableCap - 1);
        }

        int lastIdx = size - 1;
        if (removeElementIdx != lastIdx) {
            T move = (T) elements[lastIdx];
            elements[removeElementIdx] = move;

            int moveSlot = findSlot(move);
            if (0 <= moveSlot) {
                tableIdx[moveSlot] = removeElementIdx;
            }
        }
        elements[lastIdx] = null;
        --size;
        return true;
    }

    public boolean contains(T value) {
        if (value != null) {
            return 0 <= findSlot(value);
        }
        return false;
    }

    public T getRandom() {
        if (size == 0) {
            throw new EmptySetException("Oh, RandomSet is empty, plak-plak");
        }
        T val = (T) elements[rnd.nextInt(size)];
        return val;
    }
}
