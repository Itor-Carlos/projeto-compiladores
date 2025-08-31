package prolixa.semantico;

public class Simbolo {
	public final String nome;
	public final Tipagem tipo;
	public final TipoSimbolo tipoSimbolo;
	public final boolean alterable;
	public boolean inicializada;
	public final int dimensoes;
	public Object valor;
	 
	
	//Criar um segundo Simbolo para colocar sem as dimensões
	public Simbolo(String nome, Tipagem tipo,TipoSimbolo tipoSimbolo, boolean alterable, boolean inicializada, int dimensoes, Object valor) {
		this.nome = nome;
		this.tipo = tipo;
		this.tipoSimbolo = tipoSimbolo;
		this.alterable = alterable;
		this.inicializada = inicializada;
		this.dimensoes = dimensoes;
		this.valor = valor;
	}
	
	public String getNome() {
		return this.nome;
	}
	
	public Tipagem getTipo() {
		return this.tipo;
	}
	
	public TipoSimbolo getTipoSimbolo() {
		return this.tipoSimbolo;
	}
	
	public boolean isAlterable() {
		return alterable;
	}
	
	public int getDimensoes() {
		return this.dimensoes;
	}
	
	public void setInicializada(boolean inicializada) {
		this.inicializada = inicializada;
	}
	
	public boolean isInicializada() { return inicializada; }
	
	public void setValor(Object valor) {
		this.setInicializada(true);
		this.valor = valor;
	}
		
//	@Override
//	public String toString() {
//	    return "Simbolo{" +
//	            "nome='" + nome + '\'' +
//	            ", tipo=" + tipo +
//	            ", tipoSimbolo=" + tipoSimbolo +
//	            ", alterable=" + alterable +
//	            ", inicializada=" + inicializada +
//	            '}';
//	}
}


