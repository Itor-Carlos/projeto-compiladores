package prolixa.semantico;

public enum Tipagem {
	NUMBER,  
    ANSWER,   
    SYMBOL;
	
	public boolean isNumber() { return this == NUMBER; }
    public boolean isAnswer() { return this == ANSWER; }
    public boolean isSymbol()    { return this == SYMBOL; }
}

