package Lexer;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Lexer.Token.Tipo;


public class Lexer {
    static ArrayList<Token> tokens = new ArrayList<>();

    private static String definirColorStyle(Tipo tipoToken){
        if(tipoToken.equals(Tipo.NUMERO) || tipoToken.equals(Tipo.NUMERO_EXPONENCIAL)){
            return "skyblue";
        } else if (tipoToken.equals(Tipo.LOGICO)){
            return "darkgoldenrod";
        } else if (tipoToken.equals(Tipo.SIMBOLO)){
            return "forestgreen";
        } else if (tipoToken.equals(Tipo.OPERADOR)){
            return "red";
        } else if (tipoToken.equals(Tipo.VARIABLE)){
            return "gray";
        } else if (tipoToken.equals(Tipo.ESPECIAL)){
            return "coral";
        } else if (tipoToken.equals(Tipo.COMENTARIO)){
            return "plum";
        } 
        return "saddlebrown";
    }

    private static boolean matches(String palabra, StringTokenizer st){
        boolean matched = false;
        for (Tipo tokenTipo : Tipo.values()) {
            Pattern patron = Pattern.compile(tokenTipo.patron);
            Matcher matcher = patron.matcher(palabra);
            if(matcher.find()) {
                Token tk = new Token();
                tk.setTipo(tokenTipo);
                tk.setValor(palabra);
                matched = true;
                if (tokenTipo == Tipo.VARIABLE){
                    patron = Pattern.compile(Tipo.PALABRA_RESERVADA.patron);
                    matcher = patron.matcher(palabra);
                    if(matcher.find()){
                        tk.setTipo(Tipo.PALABRA_RESERVADA);
                    } 
                } else if(tokenTipo == Tipo.COMENTARIO) {
                    while(!(palabra.equals("\n")) && st.hasMoreTokens()){
                        palabra = st.nextToken();
                        tk.setValor(tk.getValor() + " " + palabra);
                    }
                    
                } 
                tk.setColor(definirColorStyle(tk.getTipo()));
                tokens.add(tk);
                break;
            }
        }
        return matched;
    }

    private static boolean esDelimitador(int i, String linea, int type){
        ArrayList<Integer> delimitadores = new ArrayList<>(Arrays.asList(39,40,41,42,43,45,47,59,61,94));
        if (type == 1){
            delimitadores= new ArrayList<>(Arrays.asList(39,40,41,42,43,47,59,61,94));
        }
        int code = linea.codePointAt(i);
        return delimitadores.contains(code);
    }
    /*separar una linea de texto pegada por los diferentes tipos de tokens ahora separada por espacios */
    private static String split(String linea){
        String nvaLinea = "";
        String lexema;
        int i=0;
        while (i<linea.length()){
            lexema = String.valueOf(linea.charAt(i)); 
            if (lexema.equals("#")){
                i++;
                while (i<linea.length() && !esDelimitador(i, linea, 0)){
                    lexema += String.valueOf(linea.charAt(i));
                    i++; 
                }
                nvaLinea += lexema + " ";
            }else {
                for (Tipo tokenTipo : Tipo.values()) {
                    lexema = String.valueOf(linea.charAt(i));
                    Pattern patron = Pattern.compile(tokenTipo.patron);
                    Matcher matcher = patron.matcher(lexema);
                    if(matcher.find()) {
                        if (tokenTipo == Tipo.OPERADOR || tokenTipo == Tipo.ESPECIAL){
                            nvaLinea += lexema + " ";
                            i++;
                        } else if (tokenTipo == Tipo.SIMBOLO || tokenTipo == Tipo.COMENTARIO){
                            i++;
                            while (i<linea.length()){
                                lexema += String.valueOf(linea.charAt(i));
                                i++;
                            }
                            nvaLinea += lexema + " ";
                        } else if (tokenTipo == Tipo.VARIABLE){
                            i++;
                            while (i<linea.length() && !esDelimitador(i, linea, 0)){
                                lexema += String.valueOf(linea.charAt(i));
                                i++; 
                            }
                            nvaLinea += lexema + " ";
                        } else if (tokenTipo == Tipo.NUMERO) {
                            i++;
                            while (i<linea.length() && !esDelimitador(i, linea, 1)){
                                lexema += String.valueOf(linea.charAt(i));
                                i++; 
                            }
                            nvaLinea += lexema + " ";
                        }
                        break;
                    } 
                }
            }
        }
        return nvaLinea;
    } 

    private static void lexer(String input, boolean alreadySplit) {
        final StringTokenizer st = new StringTokenizer(input);

        while(st.hasMoreTokens()) {
            String palabra = st.nextToken();
            boolean matched = false;

            matched = matches(palabra, st);

            if (!matched) {
                if (!alreadySplit){
                    String nvoInput = split(palabra);
                    System.out.println(nvoInput);
                    lexer(nvoInput, true);
                } else {
                    Token tk = new Token();
                    tk.setColor("purple");
                    tk.setValor(palabra);
                    tokens.add(tk);
                    alreadySplit = false;
                }
            }
        }
    }

    /*private static void imprimirTokens(){
        for (Token token: tokens){
            System.out.printf("%-20s %-20s %n", token.getTipo(), token.getValor());
        }
    }*/

    private static void lexerAritmetico(String archivo){
        Vector<String> texto = ReadWrite.leerArchivo(archivo);

        for(String linea: texto){
            lexer(linea, false);
            Token saltoLinea = new Token();
            saltoLinea.setValor("<br>");
            tokens.add(saltoLinea);
        }

        //imprimirTokens();
    }

    public static void main(String[] args) {
        String nombreArchivo = "expresiones.txt";
        lexerAritmetico(nombreArchivo);
        ReadWrite.escribirArchivoHTML(tokens);
    }   
}
