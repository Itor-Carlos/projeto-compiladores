package prolixa.semantico;

public class Simbolo {
	public final String nome;
	public final Tipagem tipo;
	public final TipoSimbolo tipoSimbolo;
	public boolean alterable;
	
	public Simbolo(String nome, Tipagem tipo,TipoSimbolo tipoSimbolo, boolean alterable) {
		this.nome = nome;
		this.tipo = tipo;
		this.tipoSimbolo = tipoSimbolo;
		this.alterable = alterable;
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
		return this.alterable;
	}
}
