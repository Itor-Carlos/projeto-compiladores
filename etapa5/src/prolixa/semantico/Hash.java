package prolixa.semantico;

import java.util.Objects;

public class Hash {

    private String key;

    public Hash(String key) {
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

        Hash obj = (Hash) o;
        return key.equals(obj.key);
    }
}
