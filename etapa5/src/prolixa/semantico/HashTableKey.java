package prolixa.semantico;

import java.util.Objects;

public class HashTableKey {

    private String key;

    public HashTableKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) 
            return true;
        if (o == null || getClass() != o.getClass()) 
            return false;

        HashTableKey obj = (HashTableKey) o;
        return key.equals(obj.key);
    }
}
