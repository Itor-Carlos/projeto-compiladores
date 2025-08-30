package prolixa.semantico;

public class EscopoSimboloTabela {
    private final TabelaDeSimbolos table;
    private final EscopoSimboloTabela parent;
    private final String scopeName;

    public static void main(String args[]){
        System.out.println("aqui");
    }

    public EscopoSimboloTabela(String scopeName, EscopoSimboloTabela parent) {
        this.table = new TabelaDeSimbolos();
        this.parent = parent;
        this.scopeName = scopeName;
    }

    public void define(Simbolo sym) {
        table.put(sym.getNome(), sym);
    }

    public Simbolo resolveLocal(String name) {
        return table.get(name);
    }

    public Simbolo resolve(String name) {
        Simbolo s = resolveLocal(name);
        if (s != null) return s;
        if (parent != null) return parent.resolve(name);
        return null;
    }

    public EscopoSimboloTabela getParent() { return parent; }
    public String getScopeName() { return scopeName; }

    @Override
    public String toString() {
        return "Scope(" + scopeName + ")\n" + table.toString();
    }
}
