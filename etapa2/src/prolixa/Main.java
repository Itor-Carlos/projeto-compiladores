package prolixa;
import prolixa.lexer.*;
import prolixa.node.*;
import java.io.*;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			String arquivo = "teste_prolixa/teste.prlx";

			Lexer lexer =
					new Lexer(
							new PushbackReader(  
									new FileReader(arquivo), 2048)); 
			Token token;
			while(!((token = lexer.next()) instanceof EOF)) {
				System.out.println(token.getClass());
				System.out.println(" ( "+token.toString()+")");
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}