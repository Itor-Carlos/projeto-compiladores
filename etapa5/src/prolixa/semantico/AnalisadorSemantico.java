package prolixa.semantico;

import prolixa.node.*;
import prolixa.analysis.DepthFirstAdapter;

public class AnalisadorSemantico extends DepthFirstAdapter {

    private final TabelaSimbolos tabela;

    public AnalisadorSemantico() {
        this.tabela = new TabelaSimbolos();
    }

    @Override
    public void inStart(Start node) {
        System.out.println("-------------------------------------------------");
        System.out.println("Iniciando análise semântica...");
    }

    @Override
    public void outStart(Start node) {
        System.out.println("-------------------------------------------------");
        System.out.println("Fim da análise semântica");
        System.out.println("-------------------------------------------------");
    }

    // === Escopos ===
    @Override
    public void inABlocoComando(ABlocoComando node) {
        tabela.push();
    }

    @Override
    public void outABlocoComando(ABlocoComando node) {
        tabela.pop();
    }

    // === Declarações ===
    @Override
    public void outAVariavelDeclaracao(AVariavelDeclaracao node) {
        String nome = node.getIdentificador().getText();
        Tipagem tipo = traduzTipo(node.getTipo());

        if (tabela.search(nome) != null) {
            errorMessageSemantico(node, "Identificador " + nome + " já declarado.");
        } else {
            Simbolo simbolo = new Simbolo(
                nome, tipo, TipoSimbolo.VARIAVEL, true, false, 0
            );
            tabela.add(nome, simbolo);
        }
    }

    @Override
    public void outAConstDeclaracao(AConstDeclaracao node) {
        String nome = node.getIdentificador().getText();
        Tipagem tipo = traduzTipo(node.getTipo());

        if (tabela.search(nome) != null) {
            errorMessageSemantico(node, "Constante " + nome + " já declarada.");
        } else {
            if (node.getValor() != null) {
                Tipagem tipagemExp = this.infereTipoExp(node.getValor());
                if (tipo != tipagemExp) {
                    errorMessageSemantico(node, "Tipo incompatível na constante " + nome);
                }
                Simbolo simbolo = new Simbolo(
                    nome, tipo, TipoSimbolo.CONSTANTE, false, true, 0
                );
                tabela.add(nome, simbolo);
            } else {
                Simbolo simbolo = new Simbolo(
                    nome, tipo, TipoSimbolo.CONSTANTE, false, false, 0
                );
                tabela.add(nome, simbolo);
            }
        }
    }

    @Override
    public void outAVetorDeclaracao(AVetorDeclaracao node) {
        String nome = node.getIdentificador().getText();
        Tipagem tipo = traduzTipo(node.getTipo());

        if (tabela.search(nome) != null) {
            errorMessageSemantico(node, "Vetor " + nome + " já declarado.");
        } else {
            // valida dimensões > 0
            for (PExp exp : node.getDimensoes()) {
                Tipagem tipagem = infereTipoExp(exp);
                if (tipagem != Tipagem.NUMBER) {
                    errorMessageSemantico(node, "Dimensão do vetor deve ser number.");
                }
            }
            Simbolo simbolo = new Simbolo(
                nome, tipo, TipoSimbolo.VETOR, true, false, 0
            );
            tabela.add(nome, simbolo);
        }
    }

    // === Uso de variáveis ===
    @Override
    public void outAVarExp(AVarExp node) {
        String nome = node.getVar().toString().trim();
        Simbolo simbolo = tabela.search(nome);

        if (simbolo == null) {
            errorMessageSemantico(node, "Variável " + nome + " não declarada.");
        }
    }

    // === Atribuições ===
    @Override
    public void outAAtribuicaoVarComando(AAtribuicaoVarComando node) {
        String nome = node.getVar().toString().trim();
        Simbolo simbolo = tabela.search(nome);

        if (simbolo == null) {
            errorMessageSemantico(node, "Variável " + nome + " não declarada.");
        } else if (!simbolo.isAlterable()) {
            errorMessageSemantico(node, "Não é permitido atribuir valor à constante " + nome + ".");
        } else {
            Tipagem tipoExp = infereTipoExp(node.getExp());
            if (tipoExp != null && simbolo.getTipo() != tipoExp) {
                errorMessageSemantico(
                    node,
                    "Tipo incompatível: esperado " + simbolo.getTipo() +
                    " mas encontrado " + tipoExp
                );
            }
            simbolo.setInicializada(true);
        }
    }

    @Override
    public void inAAtribuicaoConstComando(AAtribuicaoConstComando node) {
        String nome = node.getIdentificador().toString().trim();
        Simbolo simbolo = tabela.search(nome);

        if (simbolo == null) {
            errorMessageSemantico(node, "Constante " + nome + " não declarada.");
        } else if (simbolo.getTipoSimbolo() != TipoSimbolo.CONSTANTE) {
            errorMessageSemantico(node, nome + " não é constante.");
        } else if (simbolo.isInicializada()) {
            errorMessageSemantico(node, "Constante " + nome + " já foi inicializada.");
        } else {
            Tipagem tipoExp = infereTipoExp(node.getExp());
            if (tipoExp != null && simbolo.getTipo() != tipoExp) {
                errorMessageSemantico(
                    node,
                    "Tipo incompatível: esperado " + simbolo.getTipo() +
                    " mas encontrado " + tipoExp
                );
            }
            simbolo.setInicializada(true);
        }
    }
    
    
    @Override
    public void outAJustInCaseComando(AJustInCaseComando node) {
        Tipagem cond = infereTipoExp(node.getCondicao());
        if (cond != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Condição do 'just in case' deve ser do tipo answer (yes/no).");
        }
    }

    @Override
    public void outAAslongasComando(AAslongasComando node) {
        Tipagem cond = infereTipoExp(node.getCondicao());
        if (cond != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Condição do 'as long as' deve ser do tipo answer (yes/no).");
        }
    }

    @Override
    public void outAConsideringComando(AConsideringComando node) {
        String nome = node.getVar().toString().trim();
        Simbolo simbolo = tabela.search(nome);
        if (simbolo == null) {
            errorMessageSemantico(node, "Variável " + nome + " não declarada.");
            return;
        }
        // início, fim e incremento precisam ser NUMBER ou SYMBOL
        Tipagem inicio = infereTipoExp(node.getInicio());
        Tipagem fim = infereTipoExp(node.getFim());
        Tipagem inc = infereTipoExp(node.getIncremento());

        if (inicio != Tipagem.NUMBER && inicio != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Expressão inicial de 'considering' deve ser number ou symbol.");
        }
        if (fim != Tipagem.NUMBER && fim != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Expressão final de 'considering' deve ser number ou symbol.");
        }
        if (inc != Tipagem.NUMBER && inc != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Incremento de 'considering' deve ser number ou symbol.");
        }
    }

    // === Procedimentos Primitivos ===
    @Override
    public void outACaptureComando(ACaptureComando node) {
        for (PVar v : node.getVariaveis()) {
            String nome = v.toString().trim();
            Simbolo simbolo = tabela.search(nome);
            if (simbolo == null) {
                errorMessageSemantico(node, "Variável " + nome + " não declarada.");
            } else if (!simbolo.isAlterable()) {
                errorMessageSemantico(node, "Não é possível capturar valor em constante " + nome);
            }
        }
    }

    @Override
    public void outAShowComando(AShowComando node) {
        for (PExp e : node.getExpressoes()) {
            infereTipoExp(e); // só força validação
        }
    }

    // === Comandos de interrupção ===
    @Override
    public void outAAbandonComando(AAbandonComando node) {
        // nada semântico além de checar se está dentro de laço (opcional)
        // você pode implementar uma flag de "contexto de laço"
    }

    @Override
    public void outAGotonextComando(AGotonextComando node) {
        // idem ao abandon
    }

    // === Uso de Vetores ===
//    @Override
//    public void outAVarIdentificadorVetor(AVarIdentificadorVetor node) {
//        String nome = node.getIdentificador().getText();
//        Simbolo simbolo = tabela.search(nome);
//
//        if (simbolo == null) {
//            errorMessageSemantico(node, "Vetor " + nome + " não declarado.");
//        } else if (simbolo.getTipoSimbolo() != TipoSimbolo.VETOR) {
//            errorMessageSemantico(node, nome + " não é vetor.");
//        } else {
//            for (PExp idx : node.getIndices()) {
//                Tipagem tipagem = infereTipoExp(idx);
//                if (tipagem != Tipagem.NUMBER) {
//                    errorMessageSemantico(node, "Índice de vetor deve ser do tipo number.");
//                }
//            }
//        }
//    }

    // === Auxiliares ===
    private Tipagem traduzTipo(Node tipo) {
        if (tipo instanceof ANumberTipo) return Tipagem.NUMBER;
        if (tipo instanceof AAnswerTipo) return Tipagem.ANSWER;
        if (tipo instanceof ASymbolTipo) return Tipagem.SYMBOL;
        return null;
    }

    private Tipagem infereTipoExp(PExp exp) {
        if (exp instanceof ANumeroExp) {
            return Tipagem.NUMBER;
        } else if (exp instanceof ABoolExp) {
            return Tipagem.ANSWER;
        } else if (exp instanceof ACharExp) {
            return Tipagem.SYMBOL;
        } else if (
            exp instanceof APlusExp ||
            exp instanceof AMinusExp ||
            exp instanceof ATimesExp ||
            exp instanceof ADivideExp
        ) {
            return Tipagem.NUMBER;
        } else if (
            exp instanceof AAndExp ||
            exp instanceof AOrExp
        ) {
            return Tipagem.ANSWER;
        } else if (
            exp instanceof AEqualExp ||
            exp instanceof AMinusExpExp ||
            exp instanceof ALessExp ||
            exp instanceof AGreaterExp
        ) {
            return Tipagem.ANSWER;
        }
        return Tipagem.ANSWER;
    }

    // === Mensagens de erro ===
    private void errorMessageSemantico(Node node, String mensagem) {
//        Token token = (Token) node.();
//        int linha = token.getLine();
//        int coluna = token.getPos() + 1; // +1 porque normalmente é indexado de 0
//        System.err.println("[Erro Semântico] Linha " + linha + ", Coluna " + coluna + ": " + mensagem);
    }

    @Override
    public String toString() {
        return tabela.toString();
    }
}
