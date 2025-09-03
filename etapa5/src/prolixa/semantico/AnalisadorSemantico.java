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
        
        //System.out.println(nome.toString());

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
       
        //System.out.println(node.getVar());
        //System.out.println("aahdasuihdasd");
        
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
        
        //System.out.println(node);       
        
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
    public void inAPlusExp(APlusExp node){
        //System.out.println(node);
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
        for (PExp exp : node.getExpressoes()) {
            Tipagem tipoExp = infereTipoExp(exp);
            
            System.out.println(exp);
            System.out.println(tipoExp);

            if (tipoExp == null) {
                errorMessageSemantico(node, "Expressão em 'show' inválida");
            }
        }
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
        if (exp instanceof AStringExp) return Tipagem.SYMBOL;
        
        if (exp instanceof AVarExp) {
            String nome = ((AVarExp) exp).getVar().toString().trim();
            Simbolo s = tabela.search(nome);
            return (s != null) ? s.getTipo() : null;
        }
        //Operações numéricas
        if (exp instanceof APlusExp) {
        	//System.out.println("aqui");
            APlusExp e = (APlusExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(e.getLeft());
            Tipagem operadorDireita = infereTipoExp(e.getRight());
            if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            	errorMessageSemantico(exp, "Operador '+' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.NUMBER;
        }
        if (exp instanceof AMinusExp) {
            AMinusExp e = (AMinusExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(e.getLeft());
            Tipagem operadorDireita = infereTipoExp(e.getRight());
            if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            	errorMessageSemantico(exp, "Operador '-' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.NUMBER;
        }
        
        if (exp instanceof ATimesExp) {
        	ATimesExp expTimes = (ATimesExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(expTimes.getLeft());
            Tipagem operadorDireita = infereTipoExp(expTimes.getRight());
            if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            	errorMessageSemantico(exp, "Operador '*' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.NUMBER;
        }
       
      //Não permitir divisão por zero: 
        if(exp instanceof ADivideExp) {
        	ADivideExp expDivide = (ADivideExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expDivide.getLeft());
        	Tipagem operadorDireita = infereTipoExp(expDivide.getRight());
        	if(operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operador '/' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        	}
        	return Tipagem.NUMBER;
        }
        if(exp instanceof AIntDivideExp) {
        	AIntDivideExp expIntDivide = (AIntDivideExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expIntDivide.getLeft());
        	Tipagem operadorDireita = infereTipoExp(expIntDivide.getRight());
        	if(operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operador '//' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        	}
        	return Tipagem.NUMBER;
        }
        
        
        //Operações booleanas
        if (exp instanceof AAndExp) {
            AAndExp e = (AAndExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(e.getLeft());
            Tipagem operadorDireita = infereTipoExp(e.getRight());
            if (operadorEsquerda != Tipagem.ANSWER || operadorDireita != Tipagem.ANSWER) {
            	errorMessageSemantico(exp, "Operação 'and' exige ANSWER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.ANSWER;
        }
        if (exp instanceof AEqualExp) {
            AEqualExp e = (AEqualExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(e.getLeft());
            Tipagem operadorDireita = infereTipoExp(e.getRight());
            if (operadorEsquerda == null || operadorDireita == null || operadorEsquerda != operadorDireita) {
            	errorMessageSemantico(exp, "Operação '==' exige tipos iguais, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.ANSWER;
        }
        if (exp instanceof AOrExp) {
        	AOrExp expOr = (AOrExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(expOr.getLeft());
            Tipagem operadorDireita = infereTipoExp(expOr.getRight());
            if (operadorEsquerda != Tipagem.ANSWER || operadorDireita != Tipagem.ANSWER) {
            	errorMessageSemantico(exp, "Operação 'or' exige ANSWER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.ANSWER;
        }
        if (exp instanceof ANotExp) {
        	ANotExp expOr = (ANotExp) exp;
            Tipagem operadorUnitario = infereTipoExp(expOr.getExp());
            if (operadorUnitario != Tipagem.ANSWER) {
                errorMessageSemantico(exp, "Operação 'not' exige ANSWER, mas encontrou " + operadorUnitario);
                return null;
            }
            return Tipagem.ANSWER;
        }
        if (exp instanceof ANotEqualExp) {
        	ANotEqualExp expNotEqual = (ANotEqualExp) exp;
            Tipagem operadorEsquerda = infereTipoExp(expNotEqual.getLeft());
            Tipagem operadorDireita = infereTipoExp(expNotEqual.getRight());
            if (operadorEsquerda == null || operadorDireita == null || operadorEsquerda != operadorDireita) {
                errorMessageSemantico(exp, "Operador '!=' exige tipos iguais, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
                return null;
            }
            return Tipagem.ANSWER;
        }
        if(exp instanceof ALessExp) {
        	ALessExp expLess = (ALessExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expLess.getLeft());
        	Tipagem operadorDireita = infereTipoExp(expLess.getRight());
        	if(operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operação '<' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        	}
        	return Tipagem.ANSWER;
        }
        if(exp instanceof ALessEqualExp) {
        	ALessEqualExp expLessEqual = (ALessEqualExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expLessEqual.getLeft());
        	Tipagem operadorDireita = infereTipoExp(expLessEqual.getRight());
        	if(operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operação '<=' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        	}
        	return Tipagem.ANSWER;
        }
        if(exp instanceof AGreaterExp) {
        	AGreaterExp expGreater = (AGreaterExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expGreater.getLeft());
        	Tipagem operadorDireita = infereTipoExp(expGreater.getRight());
        	if(operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operação '>' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        	}
        	return Tipagem.ANSWER;
        }
        if(exp instanceof AGreaterEqualExp) {
        	AGreaterEqualExp expGreaterEqual = (AGreaterEqualExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expGreaterEqual.getLeft());
        	Tipagem operadorDireita = infereTipoExp(expGreaterEqual.getRight());
        	if(operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operação '>=' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        	}
        	return Tipagem.ANSWER;
        }
        
        if(exp instanceof AMinusExpExp) {
        	AMinusExpExp expMinusExp = (AMinusExpExp) exp;
        	Tipagem operadorEsquerda = infereTipoExp(expMinusExp.getExp());
        	if(operadorEsquerda != Tipagem.NUMBER) {
        		errorMessageSemantico(exp, "Operação '-' exige NUMBER, mas encontrou " + operadorEsquerda);
        	}
        	return Tipagem.NUMBER;
        }
        return null;
    }
    
    // === Mensagens de erro ===
    private void errorMessageSemantico(Node node, String mensagem) {
        throw new SemanticoError("Erro semântico: "+ mensagem);
    }


    @Override
    public String toString() {
        return tabela.toString();
    }
}
