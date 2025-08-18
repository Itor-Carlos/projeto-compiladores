package prolixa;
import prolixa.lexer.*;
import prolixa.node.*;
import prolixa.parser.*;
import java.io.*;

public class Main{
 public static void main(String[] args){
  try{
   String arquivo = "teste_prolixa/teste.prlx";
  
   Lexer lex = new Lexer(
		    new PushbackReader(  
		    new FileReader(arquivo), 1024));
   
   Parser p = new Parser(lex); 
   
   Start tree = p.parse();
   //Imprime árvore na saída padrão
   tree.apply(new ASTPrinter());
   //Imprime árvore em interface gráfica
   //tree.apply(new ASTDisplay());
  }
  catch(Exception e){
   System.out.println(e.getMessage());
  }
 }
}