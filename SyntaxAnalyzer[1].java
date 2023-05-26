import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Node{
    String name;
    ArrayList<Node> children = new ArrayList<>();
    public Node(String name){
        this.name = name;
    }
}

public  class SyntaxAnalyzer {
    public static ArrayList<String> tokens = LexicalAnalyzer.getTokens(), tokenVars = LexicalAnalyzer.getTokenVars();
    public static int index = 0, height = -1;
    public static Node root = null;
    public static String errorMessage = null;

    public static void start(FileWriter outputFile) throws IOException {
        Program(root);
        preorder(root, outputFile);
        if(errorMessage != null ){
            outputFile.write(errorMessage + "\n");
            System.out.println(errorMessage);
        }
    }

    public static boolean Program(Node parent) {
        parent = addChild(false, parent, "<Program>");
        char val = TopLevelForm(parent);
        if (val == 't') {
            return Program(parent);
        } else if(val == 'n'){
            addChild(false,parent,"  ---");
            return true;
        } return false;
    }

    public static char TopLevelForm(Node parent){
        if(index < tokens.size() && tokens.get(index).equals("LEFTPAR")){
            parent = addChild(false, parent, "<TopLevelForm>");
            addChild(true, parent,"LEFTPAR");
            if(SecondLevelForm(parent)) {
                if (index < tokens.size() && tokens.get(index).equals("RIGHTPAR")) {
                    addChild(true, parent,"RIGHTPAR");
                    return 't';
                } else
                    error(index, ")");
            }
        } else
            return 'n';
        return 'f';
    }

    public  static boolean SecondLevelForm(Node parent){
        char val = Definition("<SecondLevelForm>", parent);
        if(val == 't') {
            return true;
        } else if(val == 'n') {
            if (index < tokens.size() && tokens.get(index).equals("LEFTPAR")) {
                parent = addChild(false, parent, "<SecondLevelForm>");
                addChild(true, parent, "LEFTPAR");
                if (FunCall(parent) == 't') {
                    if (index < tokens.size() && tokens.get(index).equals("RIGHTPAR")) {
                        addChild(true, parent, "RIGHTPAR");
                        return true;
                    } else {
                        error(index, ")");
                    }
                } else {
                    error(index, "identifier");
                }
            } else {
                error(index, "define or (");
            }
        } return false;
    }

    public static char Definition(String caller, Node parent){
        if(index < tokens.size() && tokens.get(index).equals("DEFINE")){
            parent = addChild(false, parent, caller);
            parent = addChild(false, parent, "<Definition>");
            addChild(true, parent, "DEFINE");
            if(DefinitionRight(parent)){
                return 't';
            }
        } else {
            return 'n';
        }return 'f';
    }

    public static boolean DefinitionRight(Node parent){
        if(index < tokens.size() && tokens.get(index).equals("IDENTIFIER")){
            parent = addChild(false, parent, "<DefinitionRight>");
            addChild(true, parent, "IDENTIFIER");
            if(Expression(parent) == 't'){
                return true;
            } else {
                error(index, "identifier, number, char, boolean, string or (");
            }
        } else if(index < tokens.size() && tokens.get(index).equals("LEFTPAR")){
            parent = addChild(false, parent, "<DefinitionRight>");
            addChild(true, parent, "LEFTPAR");
            if(index < tokens.size() && tokens.get(index).equals("IDENTIFIER")){
                addChild(true, parent, "IDENTIFIER");
                if(ArgList(parent)){
                    if(index < tokens.size() && tokens.get(index).equals("RIGHTPAR")){
                        addChild(true, parent, "RIGHTPAR");
                        return Statements(parent);
                    } else{
                        error(index,")");
                    }
                }
            } else {
                error(index,"identifier");
            }
        } else {
            error(index, "identifier or (");
        } return false;
    }
    public static boolean ArgList(Node parent){
        parent = addChild(false, parent, "Arglist");
        if(index < tokens.size() && tokens.get(index).equals("IDENTIFIER")){
            addChild(true,parent, "IDENTIFIER");
            return ArgList(parent);
        } else{
            addChild(false,parent,"  ---");
            return true;
        }
    }
    public static boolean Statements(Node parent){
        char val = Expression("<Statements>", parent);
        if(val == 't'){
            return true;
        } else if (val == 'n'){
            val = Definition("Statements", parent);
            if(val == 't'){
                return Statements(parent);
            } else if(val == 'n') {
                error(index, "define, identifier, number, char, boolean, string or (");
            }
        }return false;
    }
    public static boolean Expressions(Node parent){
        parent = addChild(false, parent, "<Expressions>");
        char val = Expression(parent);
        if(val == 't'){
            return Expressions(parent);
        } else if(val == 'n'){
            addChild(false,parent,"  ---");
            return true;
        }return false;
    }

    public static char Expression(String caller, Node parent){
        if(index < tokens.size()) {
            switch (tokens.get(index)) {
                case "IDENTIFIER":
                    parent = addChild(false, parent, caller);
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "IDENTIFIER");
                    return 't';
                case "NUMBER":
                    parent = addChild(false, parent, caller);
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "NUMBER");
                    return 't';
                case "CHAR":
                    parent = addChild(false, parent, caller);
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "CHAR");
                    return 't';
                case "BOOLEAN":
                    parent = addChild(false, parent, caller);
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "BOOLEAN");
                    return 't';
                case "STRING":
                    parent = addChild(false, parent, caller);
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "STRING");
                    return 't';
                case "LEFTPAR":
                    parent = addChild(false, parent, caller);
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "LEFTPAR");
                    if (Expr(parent)) {
                        if (index < tokens.size() && tokens.get(index).equals("RIGHTPAR")) {
                            addChild(true, parent, "RIGHTPAR");
                            return 't';
                        } else {
                            error(index, ")");
                        }
                    }break;
                default:
                    return 'n';
            }
        }return 'f';
    }

    public static char Expression(Node parent){
        if(index < tokens.size()) {
            switch (tokens.get(index)) {
                case "IDENTIFIER":
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "IDENTIFIER");
                    return 't';
                case "NUMBER":
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "NUMBER");
                    return 't';
                case "CHAR":
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "CHAR");
                    return 't';
                case "BOOLEAN":
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "BOOLEAN");
                    return 't';
                case "STRING":
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "STRING");
                    return 't';
                case "LEFTPAR":
                    parent = addChild(false, parent, "<Expression>");
                    addChild(true, parent, "LEFTPAR");
                    if (Expr(parent)) {
                        if (index < tokens.size() && tokens.get(index).equals("RIGHTPAR")) {
                            addChild(true, parent, "RIGHTPAR");
                            return 't';
                        } else {
                            error(index, ")");
                        }
                    }break;
                default:
                    return 'n';
            }
        }return 'f';
    }

    public static boolean Expr(Node parent){
        char val = LetExpression(parent);
        if(val == 'n'){
            val = CondExpression(parent);
            if(val == 'n'){
                val = BeginExpression(parent);
                if(val == 'n'){
                    val = FunCall(parent);
                    if(val == 'n'){
                        val = IfExpression(parent);
                        if(val == 'n'){
                            error(index, "let,cond, begin, identifier, if");
                        }
                    }
                }
            }
        }return val == 't';
    }

    public static char FunCall(Node parent){
        if(index < tokens.size() && tokens.get(index).equals("IDENTIFIER")){
            parent = addChild(false,parent, "<Expr>");
            parent = addChild(false,parent, "<FunCall>");
            addChild(true,parent, "IDENTIFIER");
            if(Expressions(parent)){
                return 't';
            }
        } else{
            return 'n';
        }return 'f';
    }
    public static char LetExpression(Node parent){
        if(index < tokens.size() && tokens.get(index).equals("LET")){
            parent = addChild(false,parent, "<Expr>");
            parent = addChild(false,parent, "<LetExpression>");
            addChild(true,parent, "LET");
            if(LetExpr(parent)){
                return 't';
            }
        } else{
            return 'n';
        }return 'f';
    }

    public static boolean LetExpr(Node parent){
        if(index < tokens.size() && tokens.get(index).equals("LEFTPAR")){
            parent = addChild(false,parent, "<LetExpr>");
            addChild(true,parent, "LEFTPAR");
            if(VarDefs(parent) == 't'){
                if(index < tokens.size() && tokens.get(index).equals("RIGHTPAR")){
                    addChild(true,parent, "RIGHTPAR");
                    return Statements(parent);
                } else{
                    error(index,")");
                }
            } else{
                error(index, "(");
            }
        } else if(index < tokens.size() && tokens.get(index).equals("IDENTIFIER")){
            parent = addChild(false,parent, "<LetExpr>");
            addChild(true,parent, "IDENTIFIER");
            if(index < tokens.size() && tokens.get(index).equals("LEFTPAR")){
                addChild(true,parent, "LEFTPAR");
                if(VarDefs(parent) == 't'){
                    if(index < tokens.size() && tokens.get(index).equals("RIGHTPAR")){
                        addChild(true,parent, "RIGHTPAR");
                        return Statements(parent);
                    } else{
                        error(index,")");
                    }
                } else{
                    error(index, "(");
                }
            } else{
                error(index, "(");
            }
        }else{
            error(index,"( or identifier");
        }return false;
    }

    public static char BeginExpression(Node parent) {
        if(index < tokens.size() && tokens.get(index).equals("BEGIN")) {
            parent = addChild(false,parent, "<Expr>");
            parent = addChild(false,parent, "<BeginExpression>");
            addChild(true,parent, "BEGIN");
            if(Statements(parent)){
                return 't';
            }
        } else{
            return 'n';
        }return 'f';
    }

    public static boolean EndExpression(Node parent) {
        parent = addChild(false,parent, "<EndExpression>");
        char val = Expression(parent);
        if(val == 'n'){
            addChild(false,parent, "  ---");
        }return val == 't';
    }

    public static char IfExpression(Node parent) {
        if(index < tokens.size() && tokens.get(index).equals("IF")) {
            parent = addChild(false,parent, "<Expr>");
            parent = addChild(false,parent, "<IfExpression>");
            addChild(true,parent, "IF");
            if(Expression(parent) == 't') {
                if(Expression(parent) == 't') {
                    if (EndExpression(parent)) {
                        return 't';
                    }
                }error(index, "identifier, number, char, boolean, string or (");
            } else{
                error(index, "identifier, number, char, boolean, string or (");
            }
        }else{
            return 'n';
        }error(index, "identifier, number, char, boolean, string or (");
        return 'f';
    }
    public static boolean CondBranches(Node parent){
        if(index < tokens.size() && tokens.get(index).equals("LEFTPAR")){
            parent = addChild(false,parent, "<CondBranches>");
            addChild(true,parent, "LEFTPAR");
            if(Expression(parent) == 't'){
                if(Statements(parent)){
                    if(index < tokens.size() && tokens.get(index).equals("RIGTHPAR")){
                        addChild(true,parent, "RIGHTPAR");
                        return CondBranch(parent);
                    } else{
                        error(index,")");
                    }
                }
            } else{
                error(index, "identifier, number, char, boolean, string or (");
            }
        } else{
            error(index, "(");
        }return false;
    }

    public static boolean CondBranch(Node parent) {
        parent = addChild(false,parent, "<CondBranch>");
        if(index < tokens.size() && tokens.get(index).equals("LEFTPAR")) {
            addChild(true,parent, "LEFTPAR");
            if (Expression(parent) == 't') {
                if (Statements(parent)) {
                    if (index < tokens.size() && tokens.get(index).equals("RIGTHPAR")) {
                        addChild(true,parent, "RIGHTPAR");
                        return true;
                    } else{
                        error(index,")");
                    }
                }
            } else{
                error(index, "identifier, number, char, boolean, string or (");
            }
        } else {
            addChild(false,parent, " ---");
            return true;
        }return false;
    }

    public static char CondExpression(Node parent) {
        if(index < tokens.size() && tokens.get(index).equals("COND")) {
            parent = addChild(false,parent, "Expr");
            parent = addChild(false,parent, "CondExpression");
            addChild(true,parent, "COND");
            if(CondBranches(parent)){
                return 't';
            }
        } else {
            return 'n' ;
        }return 'f';
    }

    public static boolean VarDef(Node parent) {
        parent = addChild(false,parent, "<VarDef>");
        char val = VarDefs(parent);
        if(val == 't'){
            return true;
        } else if (val == 'n'){
            addChild(false,parent,"  ---");
            return true;
        }return false;
    }

    public static char VarDefs(Node parent) {
        if (index < tokens.size() && tokens.get(index).equals("LEFTPAR")) {
            parent = addChild(false,parent, "<Vardefs>");
            addChild(true,parent, "LEFTPAR");
            if (index < tokens.size() && tokens.get(index).equals("IDENTIFIER")) {
                addChild(true,parent, "IDENTIFIER");
                if (Expression(parent) == 't') {
                    if (index < tokens.size() && tokens.get(index).equals("RIGHTPAR")) {
                        addChild(true,parent, "RIGHTPAR");
                        if (VarDef(parent)) {
                            return 't';
                        }
                    } else{
                        error(index,")");
                    }
                } else {
                    error(index, "identifier, number, char, boolean, string or (");
                }
            } else{
                error(index,"identifier");
            }
        } else{
            return 'n';
        }return 'f';
    }

    public static void error(int i , String token) {
        if(i >= LexicalAnalyzer.columns.size()){
            int row = LexicalAnalyzer.getRow(i-1);
            int column = LexicalAnalyzer.getColumn(i-1) + tokenVars.get(i-1).length() ;
            errorMessage = "Syntax error [" + row + ":" + column + "]  '" + token + "'  is expected.";
        } else {
            errorMessage = "Syntax error [" + LexicalAnalyzer.getRow(i) + ":" + LexicalAnalyzer.getColumn(i) + "]  '" + token + "'  is expected.";
        }
    }

    public static String printParser( String str, int shiftNumber){
        String blank = "";
        for(int i= 0; i<shiftNumber; i++){
            blank += " ";
        }
        System.out.println(blank + str);
        return blank + str;
    }

    public static Node addChild(boolean terminal, Node parent, String name){
        if(terminal){
            name  = getLexeme(name, index);
            index++;
        } if(root == null){
            root = new Node(name);
            return root;
        } else{
            Node child = new Node(name);
            parent.children.add(child);
            return child;
        }
    }

    public static String getLexeme(String name, int currIndex){
        return name + " (" + tokenVars.get(currIndex) + ")" ;
    }

    public static void preorder(Node parent, FileWriter outputFile) throws IOException {
        if(parent != null){
            height++;
            outputFile.write(printParser(parent.name, height) + "\n");
            for (int i= 0; i<parent.children.size(); i++){
                preorder(parent.children.get(i), outputFile);
                height--;
            }
        } else
            height--;
    }
}