package prolixa.semantico;

import java.util.HashMap;
import java.util.LinkedList;

public class TabelaSimbolos {

    private LinkedList<HashMap<Hash, Simbolo>> scopeList;

    public TabelaSimbolos() {
        scopeList = new LinkedList<>();
    }

    public void push() {
        scopeList.addFirst(new HashMap<Hash, Simbolo>());
    }

    public HashMap<Hash, Simbolo> pop() {
        return scopeList.removeFirst();
    }

    public HashMap<Hash, Simbolo> getTopSimbolo() {
        return scopeList.getFirst();
    }

    public void add(String key, Simbolo informacoesSimbolo) {
        if (scopeList.size() < 1)
            this.push();

        HashMap<Hash, Simbolo> tabelaSimbolo = scopeList.getFirst();
        tabelaSimbolo.put(new Hash(key), informacoesSimbolo);
    }

    public Simbolo search(String key) {
        int sizeScopeList = this.scopeList.size();

        for (int i = 0; i < sizeScopeList; i++) {
            HashMap<Hash, Simbolo> table = scopeList.get(i);
            Simbolo element = table.get(new Hash(key));

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
        HashMap<Hash, Simbolo> tabelaSimbolo = scopeList.getFirst();
        return tabelaSimbolo.get(new Hash(key));
    }
}
