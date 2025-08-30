package prolixa.semantico;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class TabelaDeSimbolos {
    private static final int DEFAULT_CAPACITY = 32;
    private List<LinkedList<Entry>> buckets;
    private int capacity;

    private static class Entry {
        final String key;
        final Simbolo value;
        Entry(String key, Simbolo value) {
        	this.key = key; this.value = value; 
        }
    }

    public TabelaDeSimbolos() {
        this(DEFAULT_CAPACITY);
    }

    public TabelaDeSimbolos(int capacity) {
        this.capacity = Math.max(4, capacity);
        buckets = new java.util.ArrayList<>(this.capacity);
        for (int i = 0; i < this.capacity; i++) buckets.add(new LinkedList<>());
    }

    private int index(String key) {
        int h = Objects.hashCode(key);
        return (h & 0x7FFFFFFF) % capacity;
    }

    public void put(String key, Simbolo symbol) {
        int idx = index(key);
        LinkedList<Entry> bucket = buckets.get(idx);
        for (Entry e : bucket) {
            if (e.key.equals(key)) {
                // substituir
                bucket.remove(e);
                bucket.add(new Entry(key, symbol));
                return;
            }
        }
        bucket.add(new Entry(key, symbol));
    }

    public Simbolo get(String key) {
        int idx = index(key);
        for (Entry e : buckets.get(idx)) {
            if (e.key.equals(key)) return e.value;
        }
        return null;
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public boolean remove(String key) {
        int idx = index(key);
        LinkedList<Entry> bucket = buckets.get(idx);
        for (Entry e : bucket) {
            if (e.key.equals(key)) {
                bucket.remove(e);
                return true;
            }
        }
        return false;
    }
}
