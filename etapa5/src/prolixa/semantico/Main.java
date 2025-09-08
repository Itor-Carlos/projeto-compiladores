package prolixa.semantico;

import java.io.File;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.Scanner;

import prolixa.lexer.Lexer;
import prolixa.node.Start;
import prolixa.parser.Parser;

public class Main {
	public static void main (String[] args) {
        try {
        	String arquivo = "src/codigo_teste.prlx";
        	  	
        	Parser p =
        		    new Parser(
        		    new Lexer(
        		    new PushbackReader(  
        		    new FileReader(arquivo), 1024))); 
        		   
        		   Start tree = p.parse();
 
        		   //aplicação da análise semântica em minha AST
        		   tree.apply(new AnalisadorSemantico());
        }
        catch(Exception e){
        	System.out.println(e);
        }

    }
}