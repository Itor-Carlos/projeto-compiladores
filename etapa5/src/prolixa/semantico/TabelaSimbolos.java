package prolixa.semantico;

import java.util.HashMap;
import java.util.LinkedList;

public class TabelaSimbolos {

    private LinkedList<HashMap<HashTableKey, Simbolo>> scopeList;

    public TabelaSimbolos() {
        scopeList = new LinkedList<>();
    }

    public void push() {
        scopeList.addFirst(new HashMap<HashTableKey, Simbolo>());
    }

    public HashMap<HashTableKey, Simbolo> pop() {
        return scopeList.removeFirst();
    }

    public HashMap<HashTableKey, Simbolo> getTopSimbolo() {
        return scopeList.getFirst();
    }

    public void add(String key, Simbolo informacoesSimbolo) {
        if (scopeList.size() < 1)
            this.push();

        HashMap<HashTableKey, Simbolo> tabelaSimbolo = scopeList.getFirst();
        tabelaSimbolo.put(new HashTableKey(key), informacoesSimbolo);
    }

    public Simbolo search(String key) {
        int sizeScopeList = this.scopeList.size();

        for (int i = 0; i < sizeScopeList; i++) {
            HashMap<HashTableKey, Simbolo> table = scopeList.get(i);
            Simbolo element = table.get(new HashTableKey(key));

            if (element != null) {
                return element;
            }
        }
        return null;
    }
    
    public Simbolo searchLocal(String key) {
        if (scopeList.isEmpty()) {
            return null;
        }
        HashMap<HashTableKey, Simbolo> tabelaSimbolo = scopeList.getFirst();
        return tabelaSimbolo.get(new HashTableKey(key));
    }
}
