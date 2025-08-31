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
        
        System.out.println(nome.toString());

        if (tabela.searchLocal(nome) != null) {
            errorMessageSemantico(node, "Identificador " + nome + " já declarado.");
        } 
        else {
            Simbolo simbolo = new Simbolo( nome, tipo, TipoSimbolo.VARIAVEL, true, false, 0, null);
            tabela.add(nome, simbolo);
        }
    }

    @Override
    public void outAConstDeclaracao(AConstDeclaracao node) {
        String nome = node.getIdentificador().getText();
        Tipagem tipo = traduzTipo(node.getTipo());

        if (tabela.searchLocal(nome) != null) {
            errorMessageSemantico(node, "Constante " + nome + " já declarada.");
        } else {
            if (node.getValor() != null) {
                Tipagem tipagemExp = this.infereTipoExp(node.getValor());
                if (tipo != tipagemExp) {
                    errorMessageSemantico(node, "Tipo incompatível na constante " + nome);
                }
                Simbolo simbolo = new Simbolo(nome, tipo, TipoSimbolo.CONSTANTE, false, true, 0, node.getValor());
                tabela.add(nome, simbolo);
            } else {
                Simbolo simbolo = new Simbolo(
                    nome, tipo, TipoSimbolo.CONSTANTE, false, false, 0, null);
                tabela.add(nome, simbolo);
            }
        }
    }

    @Override
    public void outAVetorDeclaracao(AVetorDeclaracao node) {
        String nome = node.getIdentificador().getText();
        Tipagem tipo = traduzTipo(node.getTipo());

        if (tabela.searchLocal(nome) != null) {
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
                nome, tipo, TipoSimbolo.VETOR, true, false, 0, null);
            tabela.add(nome, simbolo);
        }
    }

    // === Uso de variáveis ===
    @Override
    public void outAVarExp(AVarExp node) {
        String nome = node.getVar().toString().trim();
        Simbolo simbolo = tabela.search(nome);
       
        System.out.println(node.getVar());
        System.out.println("aahdasuihdasd");
        
        if (simbolo == null) {
            errorMessageSemantico(node, "Variável " + nome + " não declarada.");
        } else if (!simbolo.isInicializada()) {
            errorMessageSemantico(node, "Variável " + nome + " usada sem inicialização.");
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
    public void inAConsideringComando(AConsideringComando node){
        String nome = node.getVar().toString().trim();
        Simbolo simbolo = tabela.search(nome);
        
        if (simbolo == null) {
            //errorMessageSemantico(node, "Variável " + nome + " não declarada.");
            return;
        }
        simbolo.setInicializada(true);
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
            return;
        }

        if (simbolo.getTipo() != Tipagem.NUMBER && simbolo.getTipo() != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Variável de controle de 'considering' deve ser number ou symbol.");
        }
        
        Tipagem inicio = infereTipoExp(node.getInicio());
        Tipagem fim = infereTipoExp(node.getFim());
        Tipagem incremento = infereTipoExp(node.getIncremento());
        
        System.out.println(incremento);
        
        if (inicio != Tipagem.NUMBER && inicio != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Expressão inicial de 'considering' deve ser number ou symbol.");
        }
        if (fim != Tipagem.NUMBER && fim != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Expressão final de 'considering' deve ser number ou symbol.");
        }
        if (incremento != Tipagem.NUMBER && incremento != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Incremento de 'considering' deve ser number ou symbol.");
        }
        
        simbolo.setInicializada(true);
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

    }

    // === Comandos de interrupção ===
    @Override
    public void outAAbandonComando(AAbandonComando node) {
        
    }

    @Override
    public void outAGotonextComando(AGotonextComando node) {

    }

    // === Auxiliares ===
    private Tipagem traduzTipo(Node tipo) {
        if (tipo instanceof ANumberTipo) return Tipagem.NUMBER;
        if (tipo instanceof AAnswerTipo) return Tipagem.ANSWER;
        if (tipo instanceof ASymbolTipo) return Tipagem.SYMBOL;
        return null;
    }

    private Tipagem infereTipoExp(PExp exp) {
        if (exp instanceof ANumeroExp) return Tipagem.NUMBER;
        if (exp instanceof ABoolExp) return Tipagem.ANSWER;
        if (exp instanceof ACharExp) return Tipagem.SYMBOL;
        if (exp instanceof AVarExp) {
            String nome = ((AVarExp) exp).getVar().toString().trim();
            Simbolo s = tabela.search(nome);
            return (s != null) ? s.getTipo() : null;
        }
        if (exp instanceof APlusExp || exp instanceof AMinusExp ||
            exp instanceof ATimesExp || exp instanceof ADivideExp ||
            exp instanceof AIntDivideExp) {
            return Tipagem.NUMBER;
        }
        if (exp instanceof AAndExp || exp instanceof AOrExp || exp instanceof AXorExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof AEqualExp || exp instanceof ANotEqualExp ||
            exp instanceof ALessExp || exp instanceof ALessEqualExp ||
            exp instanceof AGreaterExp || exp instanceof AGreaterEqualExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof ANotExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof AMinusExpExp) {
            return Tipagem.NUMBER;
        }
        return null;
    }
    
    // === Mensagens de erro ===
    private void errorMessageSemantico(Node node, String mensagem) {
        System.err.println("[Erro Semântico] " + mensagem);
    }

    @Override
    public String toString() {
        return tabela.toString();
    }
}
