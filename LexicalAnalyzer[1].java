import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LexicalAnalyzer {
    public static int column = 0, row = 0, currentColumn = 0, lastColumn = -1, lastIndex = 0;
    public static boolean error = false;
    public static ArrayList<String> tokens = new ArrayList<String>(), tokenVars = new ArrayList<String>();
    public static ArrayList<Integer> rows = new ArrayList<Integer>(), columns = new ArrayList<Integer>();

    public static void scanner(String fileName) throws IOException {
        File inputFile = new File(fileName);
        Scanner input = new Scanner(inputFile);
        FileWriter outputFile = new FileWriter("output.txt");

        String message = "";
        while (input.hasNextLine() && !error) {
            String line = input.nextLine();
            message = read(line);
            row++;
            column = 0;
        }

        if (error) {
            System.out.println(message);
            outputFile.write(message + "\n");
        } else {
            SyntaxAnalyzer.start(outputFile);
        }

        outputFile.close();
        input.close();
    }

    public static String read(String line) {
        while (column < line.length()) {
            currentColumn = column;
            if (line.charAt(column) == ' ' || line.charAt(column) == '\t');
            else if (line.charAt(column) == '~')
                break;
            else if (isBracket(line.charAt(column)))
                bracket(line.charAt(column));
            else if (keyword(line));
            else if (isIdentifier(line)) {
                tokens.add("IDENTIFIER");
                rows.add(row + 1);
                columns.add(currentColumn + 1);
                tokenVars.add(line.substring(currentColumn, lastIndex+1));
            } else if (isHexNumber(line) || isBiNumber(line) || isNumber(line)) {
                tokens.add("NUMBER");
                rows.add(row + 1);
                columns.add(currentColumn + 1);
                tokenVars.add(line.substring(currentColumn, lastIndex+1));
            } else if (isString(line)) {
                tokens.add("STRING");
                rows.add(row + 1);
                columns.add(currentColumn + 1);
                tokenVars.add(line.substring(currentColumn, lastIndex+1));
            } else if (isCharacter(line)) {
                tokens.add("CHARACTER");
                rows.add(row + 1);
                columns.add(currentColumn + 1);
                tokenVars.add(line.substring(currentColumn, lastIndex+1));
            } else {
                int invalidCol = column;
                String invalid = "";
                error = true;
                if(lastColumn != -1) {
                    invalid = line.substring(column, lastColumn);
                    while (lastColumn < line.length() && !isBracket(line.charAt(lastColumn)) && line.charAt(lastColumn) != ' ') {
                        invalid = invalid + line.charAt(lastColumn);
                        lastColumn++;
                    }
                } else {
                    while (column < line.length() && !isBracket(line.charAt(column)) && line.charAt(column) != ' ') {
                        invalid = invalid + line.charAt(column);
                        column++;
                    }
                } return "LEXICAL ERROR [" + (row + 1) + ":" + (invalidCol + 1) + "]: Invalid token '" + invalid + "'";
            } column++;
        } return "";
    }

    public static boolean isNumber(String line) {
        boolean dotUnused = true, eUnused = true, signUnused = true;
        int ogCol = column;
        if (line.charAt(column) == '+' || line.charAt(column) == '-' || isDecDigit(line.charAt(column)) || line.charAt(column) == '.') {
            if (line.charAt(column) == '.')
                dotUnused = false;
            column++;
            while (column < line.length() && line.charAt(column) != '\t' && line.charAt(column) != ' ' && !isBracket(line.charAt(column))) {
                if (isDecDigit(line.charAt(column)));
                else if ((line.charAt(column) == '.') && dotUnused && column + 1 < line.length() && isDecDigit(line.charAt(column + 1)))
                    dotUnused = false;
                else if ((line.charAt(column) == 'e' || line.charAt(column) == 'E') && eUnused && isDecDigit(line.charAt(column - 1))
                        && (column + 1 < line.length()) && (isDecDigit(line.charAt(column + 1)) || line.charAt(column + 1) == '+'
                        || line.charAt(column + 1) == '-')) {
                    eUnused = false;
                    dotUnused = false;
                } else if ((line.charAt(column) == '+' || line.charAt(column) == '-') && signUnused && (line.charAt(column - 1) == 'e'
                        || line.charAt(column - 1) == 'E') && column + 1 < line.length() && isDecDigit(line.charAt(column + 1))) {
                    signUnused = false;
                } else {
                    column=ogCol;
                    return false;
                } column++;
            } column--;
            lastIndex = column;
            return true;
        } return false;
    }

    public static boolean isHexNumber(String line) {
        int ogCol = column;
        if (column + 2 < line.length() && line.substring(column, column + 2).equals("0x") && isHexDigit(line.charAt(column + 2))) {
            column = column + 2;
            while (column < line.length() && !isBracket(line.charAt(column)) && line.charAt(column) != ' ' && line.charAt(column) != '\t') {
                if (isHexDigit(line.charAt(column)));
                else {
                    column = ogCol;
                    return false;
                } column++;
            } column--;
            lastIndex = column;
            return true;
        } return false;
    }

    public static boolean isBiNumber(String line) {
        int ogCol = column;
        if (column + 2 < line.length() && line.substring(column, column + 2).equals("0b") && isBiDigit(line.charAt(column + 2))) {
            column = column + 2;
            while (column < line.length() && !isBracket(line.charAt(column)) && line.charAt(column) != ' ' && line.charAt(column) != '\t') {
                if (isBiDigit(line.charAt(column)));
                else {
                    column = ogCol;
                    return false;
                } column++;
            } column--;
            lastIndex = column;
            return true;
        } return false;
    }

    public static boolean isIdentifier(String line) {
        int ogCol = column;
        if(line.charAt(column) == '+' || line.charAt(column) == '-' || line.charAt(column) == '.' ){
            column++;
            if(column == line.length() || line.charAt(column) == ' ' || isBracket(line.charAt(column)) || line.charAt(column) == '\t'){
                column--;
                lastIndex = column;
                return true;
            }
        } else if (line.charAt(column) == '!' || line.charAt(column) == '*'|| line.charAt(column) == '/'|| line.charAt(column) == ':'|| line.charAt(column) == '<'|| line.charAt(column) == '='
                || line.charAt(column) == '>'|| line.charAt(column) == '?' || isLetter(line.charAt(column)) ) {
            column++;
            for(;column < line.length() &&!(line.charAt(column) == ' ' || isBracket(line.charAt(column)) || line.charAt(column) == '\t') ; column++) {
                if( !(isLetter(line.charAt(column)) || isDecDigit(line.charAt(column)) || line.charAt(column) == '.' || line.charAt(column) == '+'  || line.charAt(column) == '-')){
                    column= ogCol;
                    return false;
                }
            } column--;
            lastIndex = column;
            return true;
        } column= ogCol;
        return false;
    }

    public static boolean isString(String line) {
        int ogCol = column;
        if(line.charAt(column) == '"') {
            for(++column; column< line.length(); column++){
                if( line.charAt(column) == '"'){
                    if(column != ogCol +1){
                        column++;
                        if(!(column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'))
                            break;
                        column--;
                        lastIndex = column;
                        return true;
                    } else{
                        column = ogCol;
                        return false;
                    }
                } else if(line.charAt(column) == 92){
                    if(!(++column < line.length() && (line.charAt(column) == 92 || line.charAt(column) == '"')))
                        break;
                }
            } lastColumn = column;
        } column = ogCol;
        return false;
    }

    public static boolean isCharacter(String line) {
        int ogCol = column;
        if (line.charAt(column) == '\'') {
            column++;
            if(column< line.length() && line.charAt(column) == '\\') {
                column++;
                if(column< line.length() && line.charAt(column) == '\\' && ++column < line.length() && line.charAt(column) == '\''){
                    if(++column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'){
                        column--;
                        lastIndex = column;
                        return true;
                    }
                } else if(column< line.length() && line.charAt(column) == '\'' && (++column< line.length() && line.charAt(column) == '\'')) {
                    if(++column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'){
                        column--;
                        lastIndex = column;
                        return true;
                    }
                }
            } else {
                column++;
                if(column< line.length() && line.charAt(column) == '\''){
                    if(++column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'){
                        column--;
                        lastIndex = column;
                        return true;
                    }
                }
            }
        } column = ogCol;
        return false;
    }

    public static void bracket(char currentChar) {
        if (currentChar == '(') {
            tokens.add("LEFTPAR");
            rows.add(row + 1);
            columns.add(currentColumn + 1);
            tokenVars.add("(");
        } else if (currentChar == ')') {
            tokens.add("RIGHTPAR");
            rows.add(row + 1);
            columns.add(currentColumn + 1);
            tokenVars.add(")");
        } else if (currentChar == '[') {
            tokens.add("LEFTSQUAREB");
            rows.add(row + 1);
            columns.add(currentColumn + 1);
            tokenVars.add("[");
        } else if (currentChar == ']') {
            tokens.add("RIGHTSQUAREB");
            rows.add(row + 1);
            columns.add(currentColumn + 1);
            tokenVars.add("]");
        } else if (currentChar == '{') {
            tokens.add("LEFTCURLYB");
            rows.add(row + 1);
            columns.add(currentColumn + 1);
            tokenVars.add("{");
        } else if (currentChar == '}') {
            tokens.add("RIGHTCURLYB");
            rows.add(row + 1);
            columns.add(currentColumn + 1);
            tokenVars.add("}");
        }
    }

    public static boolean keyword(String line) {
        String keyword = "";
        for (int i = column; i < line.length() && line.charAt(i) != ' ' && !isBracket(line.charAt(i)) && line.charAt(i) != '\t'; i++)
            keyword = keyword + line.charAt(i);
        switch (keyword) {
            case "define":
                tokens.add("DEFINE"); tokenVars.add("define");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 5;
                return true;
            case "let":
                tokens.add("LET"); tokenVars.add("let");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 2;
                return true;
            case "cond":
                tokens.add("COND"); tokenVars.add("cond");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 3;
                return true;
            case "if":
                tokens.add("IF"); tokenVars.add("if");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 1;
                return true;
            case "begin":
                tokens.add("BEGIN"); tokenVars.add("begin");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 4;
                return true;
            case "true":
                tokens.add("BOOLEAN"); tokenVars.add("true");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 3;
                return true;
            case "false":
                tokens.add("BOOLEAN"); tokenVars.add("false");
                rows.add(row + 1); columns.add(currentColumn + 1);
                column = column + 4;
                return true;
        } return false;
    }

    public static boolean isBracket(char currentChar) {
        return currentChar == '(' || currentChar == ')' || currentChar == '[' || currentChar == ']' ||
                currentChar == '{' || currentChar == '}';
    }

    public static boolean isBiDigit(char currentChar) {
        return currentChar == '0' || currentChar == '1';
    }

    public static boolean isHexDigit(char currentChar) {
        return isDecDigit(currentChar) || (currentChar > 96 && currentChar < 103) || (currentChar > 64 && currentChar < 71);
    }

    public static boolean isDecDigit(char currentChar) {
        return currentChar > 47 && currentChar < 58;
    }

    public static boolean isLetter(char currentChar) {
        return currentChar > 96 && currentChar < 123;
    }

    public static int getRow(int i) {
        return rows.get(i);
    }

    public static int getColumn(int i) {
        return columns.get(i);
    }

    public static ArrayList<String> getTokens() {
        return tokens;
    }

    public static ArrayList<String> getTokenVars() {
        return tokenVars;
    }
}