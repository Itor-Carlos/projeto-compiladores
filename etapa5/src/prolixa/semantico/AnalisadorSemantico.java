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
        System.out.println("-------------------------------------------------");
    }

    @Override
    public void outStart(Start node) {
        System.out.println("-------------------------------------------------");
        System.out.println("Fim da análise semântica");
        System.out.println("-------------------------------------------------");
    }

    @Override
    public void inABlocoComando(ABlocoComando node) {
        tabela.push();
    }

    @Override
    public void outABlocoComando(ABlocoComando node) {
        tabela.pop();
    }
    
    @Override
    public void outAVariavelDeclaracao(AVariavelDeclaracao node) {
        String nome = node.getIdentificador().getText();
        Tipagem tipo = traduzTipo(node.getTipo());
       
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
                Simbolo simbolo = new Simbolo(nome, tipo, TipoSimbolo.CONSTANTE, false, false, 0, null);
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
        } 
        else {
            int qtdDimensoes = node.getDimensoes().size();
            
            for (PExp expression : node.getDimensoes()) {
                Tipagem tipagem = infereTipoExp(expression);
                if (tipagem != Tipagem.NUMBER) {
                    errorMessageSemantico(node, "Dimensão do vetor deve ser number.");
                }
            }
            Simbolo simbolo = new Simbolo(nome, tipo, TipoSimbolo.VETOR, true, false, qtdDimensoes, null);
            tabela.add(nome, simbolo);
        }
    }

    @Override
    public void outAVarExp(AVarExp node) {
        PVar variaveis = node.getVar();
        String nome;
        Simbolo simbolo;
        if (variaveis instanceof AIdentificadorVar) {
            nome = ((AIdentificadorVar) variaveis).getIdentificador().getText();
            simbolo = tabela.search(nome);

            if (simbolo == null) {
                errorMessageSemantico(node, "Variável " + nome + " não declarada.");
            } 
            else if (!simbolo.isInicializada()) {
                errorMessageSemantico(node, "Variável " + nome + " usada sem inicialização.");
            }
        } 
        else if (variaveis instanceof AIdentificadorVetorVar) {
            AIdentificadorVetorVar varVetor = (AIdentificadorVetorVar) variaveis;
            nome = varVetor.getIdentificador().getText();
            simbolo = tabela.search(nome);

            if (simbolo == null) {
                errorMessageSemantico(node, "Vetor " + nome + " não declarado.");
                return;
            }
            if (simbolo.getTipoSimbolo() != TipoSimbolo.VETOR) {
                errorMessageSemantico(node, nome + " não é um vetor.");
                return;
            }

            int qtdIndices = varVetor.getIndices().size();
            if (qtdIndices != simbolo.getDimensoes()) {
                errorMessageSemantico(node, "Número de índices incompatível para vetor " + nome);
            }

            for (PExp indices : varVetor.getIndices()) {
                if (infereTipoExp(indices) != Tipagem.NUMBER) {
                    errorMessageSemantico(node, "Índice de vetor deve ser do tipo number.");
                }
            }
        }
    }

    
    @Override
    public void outAAtribuicaoVarComando(AAtribuicaoVarComando node) {
        PVar variavel = node.getVar();
        String nome;
        Simbolo simbolo;

        if (variavel instanceof AIdentificadorVar) {
            nome = ((AIdentificadorVar) variavel).getIdentificador().getText();
            simbolo = tabela.search(nome);

            if (simbolo == null) {
                errorMessageSemantico(node, "Variável " + nome + " não declarada.");
            } 
            else if (!simbolo.isAlterable()) {
                errorMessageSemantico(node, "Não é permitido atribuir valor à constante " + nome + ".");
            } 
            else {
                Tipagem tipoExp = infereTipoExp(node.getExp());
                if (tipoExp != null && simbolo.getTipo() != tipoExp) {
                    errorMessageSemantico(node, "Tipo incompatível: esperado " + simbolo.getTipo() + " mas encontrado " + tipoExp);
                }
                simbolo.setInicializada(true);
            }
        } 
        else if (variavel instanceof AIdentificadorVetorVar) {
            AIdentificadorVetorVar varVetor = (AIdentificadorVetorVar) variavel;
            nome = varVetor.getIdentificador().getText();
            simbolo = tabela.search(nome);

            if (simbolo == null) {
                errorMessageSemantico(node, "Vetor " + nome + " não declarado.");
                return;
            }
            if (simbolo.getTipoSimbolo() != TipoSimbolo.VETOR) {
                errorMessageSemantico(node, nome + " não é um vetor.");
                return;
            }

            int quantidadeIndices = varVetor.getIndices().size();
            if (quantidadeIndices != simbolo.getDimensoes()) {
                errorMessageSemantico(node, "Número de índices incompatível para vetor " + nome);
            }
            for (PExp indice : varVetor.getIndices()) {
                if (infereTipoExp(indice) != Tipagem.NUMBER) {
                    errorMessageSemantico(node, "Índice de vetor deve ser do tipo number.");
                }
            }

            Tipagem tipoExpression = infereTipoExp(node.getExp());
            if (tipoExpression != null && simbolo.getTipo() != tipoExpression) {
                errorMessageSemantico(node, "Tipo incompatível: vetor " + nome +" é " + simbolo.getTipo() + " mas recebeu " + tipoExpression);
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
        } 
        else if (simbolo.getTipoSimbolo() != TipoSimbolo.CONSTANTE) {
            errorMessageSemantico(node, nome + " não é constante.");
        } 
        else if (simbolo.isInicializada()) {
            errorMessageSemantico(node, "Constante " + nome + " já foi inicializada.");
        } 
        else {
            Tipagem tipoExpression = infereTipoExp(node.getExp());
            if (tipoExpression != null && simbolo.getTipo() != tipoExpression) {
                errorMessageSemantico(node, "Tipo incompatível: esperado " + simbolo.getTipo() +" mas encontrado " + tipoExpression);
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
        Tipagem condicional = infereTipoExp(node.getCondicao());
        if (condicional != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Condição do 'just in case' deve ser do tipo answer (yes/no).");
        }
    }

    @Override
    public void outAAslongasComando(AAslongasComando node) {
        Tipagem condicional = infereTipoExp(node.getCondicao());
        if (condicional != Tipagem.ANSWER) {
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
            errorMessageSemantico(node, "Expressão inicial do comando 'considering' deve ser number ou symbol.");
        }
        if (fim != Tipagem.NUMBER && fim != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Expressão final do comando 'considering' deve ser number ou symbol.");
        }
        if (incremento != Tipagem.NUMBER && incremento != Tipagem.SYMBOL) {
            errorMessageSemantico(node, "Incremento do comando 'considering' deve ser number ou symbol.");
        }
        
        simbolo.setInicializada(true);
    }

    @Override
    public void outACaptureComando(ACaptureComando node) {
        for (PVar variaveis : node.getVariaveis()) {
            String nome;
            Simbolo simbolo;
            if (variaveis instanceof AIdentificadorVar) {
                nome = ((AIdentificadorVar) variaveis).getIdentificador().getText();
                simbolo = tabela.search(nome);


                if (simbolo == null) {
                    errorMessageSemantico(node, "Variável ou Constante " + nome + " não declarada.");
                } 
                
                else if(simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL) {
                	System.out.println("Capture de Variável com as seguintes informações -> : " + "Nome: " + nome + " | Tipo: " + (simbolo != null ? simbolo.getTipo() : "não declarada"));
                	simbolo.setInicializada(true);
                }
                else if(simbolo.getTipoSimbolo() == TipoSimbolo.CONSTANTE) {
                	if (!simbolo.isAlterable() && simbolo.isInicializada() == true) {
                        errorMessageSemantico(node, "Não é possível capturar valor em constante " + nome);
                    }
                	System.out.println("Capture de Constante com as seguintes informações -> : " + "Nome: " + nome + " | Tipo: " + (simbolo != null ? simbolo.getTipo() : "não declarada"));
                	simbolo.setInicializada(true);
                }
            } 
            else if (variaveis instanceof AIdentificadorVetorVar) {
                AIdentificadorVetorVar varVetor = (AIdentificadorVetorVar) variaveis;
                nome = varVetor.getIdentificador().getText();
                simbolo = tabela.search(nome);

                System.out.println("Capture em um Vetor com as seguintes informações: Vetor -> " + "Nome: "  + nome + " | Tipagem: " + (simbolo != null ? simbolo.getTipo() : "não declarado") + " | Dimensões: " + (simbolo != null ? simbolo.getDimensoes() : "?") + " | Dimensões usadas: " + varVetor.getIndices().size());

                if (simbolo == null) {
                    errorMessageSemantico(node, "Vetor " + nome + " não declarado.");
                    continue;
                }
                if (simbolo.getTipoSimbolo() != TipoSimbolo.VETOR) {
                    errorMessageSemantico(node, nome + " não é um vetor.");
                    continue;
                }

                int qtdIndices = varVetor.getIndices().size();
                if (qtdIndices != simbolo.getDimensoes()) {
                    errorMessageSemantico(node, "Número de índices incompatível para vetor " + nome);
                }
                for (PExp indice : varVetor.getIndices()) {
                    if (infereTipoExp(indice) != Tipagem.NUMBER) {
                        errorMessageSemantico(node, "Índice de vetor deve ser do tipo number.");
                    }
                }
                simbolo.setInicializada(true);
            }
        }
    }



    @Override
    public void outAShowComando(AShowComando node) {
        for (PExp expression : node.getExpressoes()) {
            Tipagem tipoExpression = infereTipoExp(expression);

            System.out.println("Expressão em 'show': " + expression + " | Tipo: " + tipoExpression);

            if (tipoExpression == null) {
                errorMessageSemantico(node, "Expressão em 'show' inválida: " + expression);
            }
        }
    }

    @Override
    public void outAAbandonComando(AAbandonComando node) {
    	System.out.println("Comando 'abandon' encontrado dentro de um laço: finalização de laço.");
    }

    @Override
    public void outAGotonextComando(AGotonextComando node) {
    	System.out.println("Comando 'go to next iteration' encontrado: avança para a próxima iteração do laço em questão");
    }
    
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
        if (exp instanceof AStringExp) {
        	return Tipagem.SYMBOL;
        }
          
        if (exp instanceof APlusExp) {
            return Tipagem.NUMBER;
        }
        if (exp instanceof AMinusExp) {
            return Tipagem.NUMBER;
        }
        
        if (exp instanceof ATimesExp) {
            return Tipagem.NUMBER;
        }
       
        if(exp instanceof ADivideExp) {
        	return Tipagem.NUMBER;
        }
        if(exp instanceof AIntDivideExp) {
        	return Tipagem.NUMBER;
        }
        
        if (exp instanceof AAndExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof AEqualExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof AOrExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof ANotExp) {
            return Tipagem.ANSWER;
        }
        if (exp instanceof ANotEqualExp) {
            return Tipagem.ANSWER;
        }
        if(exp instanceof ALessExp) {
        	return Tipagem.ANSWER;
        }
        if(exp instanceof ALessEqualExp) {
        	return Tipagem.ANSWER;
        }
        if(exp instanceof AGreaterExp) {
        	return Tipagem.ANSWER;
        }
        if(exp instanceof AGreaterEqualExp) {
        	return Tipagem.ANSWER;
        }
        
        if(exp instanceof AMinusExpExp) {
        	return Tipagem.NUMBER;
        }
        
        if (exp instanceof AVarExp) {
            PVar variavelExpression = ((AVarExp) exp).getVar();

            if (variavelExpression instanceof AIdentificadorVar) {
                String nome = ((AIdentificadorVar) variavelExpression).getIdentificador().getText();
                Simbolo simbolo = tabela.search(nome);
                if (simbolo == null) {
                    errorMessageSemantico(exp, "Variável " + nome + " não declarada.");
                    return null;
                }
                return simbolo.getTipo();
            }
            else if (variavelExpression instanceof AIdentificadorVetorVar) {
                AIdentificadorVetorVar varVetor = (AIdentificadorVetorVar) variavelExpression;
                String nome = varVetor.getIdentificador().getText();
                Simbolo simbolo = tabela.search(nome);

                if (simbolo == null) {
                    errorMessageSemantico(exp, "Vetor " + nome + " não declarado.");
                    return null;
                }
                if (simbolo.getTipoSimbolo() != TipoSimbolo.VETOR) {
                    errorMessageSemantico(exp, nome + " não é um vetor.");
                    return null;
                }

                int quantidadeIndices = varVetor.getIndices().size();
                if (quantidadeIndices != simbolo.getDimensoes()) {
                    errorMessageSemantico(exp, "Número de índices incompatível para vetor " + nome);
                }

                for (PExp indice : varVetor.getIndices()) {
                    Tipagem tipoIndice = infereTipoExp(indice);
                    if (tipoIndice != Tipagem.NUMBER) {
                        errorMessageSemantico(exp, "Índice de vetor deve ser do tipo number.");
                    }
                }

                return simbolo.getTipo();
            }
        }

        return null;
    }
    
    @Override
    public void outAPlusExp(APlusExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        	errorMessageSemantico(node, "Operador '+' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outAMinusExp(AMinusExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        	errorMessageSemantico(node, "Operador '-' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outATimesExp(ATimesExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
        	errorMessageSemantico(node, "Operador '*' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outADivideExp(ADivideExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador '/' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outAIntDivideExp(AIntDivideExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador '//' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }

    @Override
    public void outALessExp(ALessExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador '<' exige NUMBER, mas encontrou "  + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outALessEqualExp(ALessEqualExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador '<=' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outAGreaterExp(AGreaterExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador '>' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outAGreaterEqualExp(AGreaterEqualExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.NUMBER || operadorDireita != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador '>=' exige NUMBER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outAMinusExpExp(AMinusExpExp node){
        Tipagem operador = infereTipoExp(node.getExp());
        if (operador != Tipagem.NUMBER) {
            errorMessageSemantico(node, "Operador unário '-' exige NUMBER, mas encontrou " + operador);
        }
    }
    
    @Override
    public void outAOrExp(AOrExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.ANSWER || operadorDireita != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Operador 'or' exige ANSWER, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outANotExp(ANotExp node){
        Tipagem operadorUnitario = infereTipoExp(node.getExp());
        if (operadorUnitario != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Operador 'not' exige ANSWER, mas encontrou " + operadorUnitario);
        }
    }

    
    @Override
    public void outAAndExp(AAndExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.ANSWER || operadorDireita != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Operador 'and' exige ANSWER, mas encontrou "  + operadorEsquerda + " e " + operadorDireita);
        }
    }

    @Override
    public void outAEqualExp(AEqualExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda == null || operadorDireita == null || operadorEsquerda != operadorDireita) {
            errorMessageSemantico(node, "Operador '==' exige tipos iguais, mas encontrou "  + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outANotEqualExp(ANotEqualExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda == null || operadorDireita == null || operadorEsquerda != operadorDireita) {
            errorMessageSemantico(node, "Operador '!=' exige tipos iguais, mas encontrou " + operadorEsquerda + " e " + operadorDireita);
        }
    }
    
    @Override
    public void outAXorExp(AXorExp node){
        Tipagem operadorEsquerda = infereTipoExp(node.getLeft());
        Tipagem operadorDireita = infereTipoExp(node.getRight());
        if (operadorEsquerda != Tipagem.ANSWER || operadorDireita != Tipagem.ANSWER) {
            errorMessageSemantico(node, "Operador 'xor' exige ANSWER, mas encontrou "
                + operadorEsquerda + " e " + operadorDireita);
        }
    }

    private void errorMessageSemantico(Node node, String mensagem) {
        System.err.println(mensagem);
    }

    @Override
    public String toString() {
        return tabela.toString();
    }
}
