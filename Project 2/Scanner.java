import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Scanner {
    Map<String, String> reservedWords;
    Map<Character, String> tokens;
    Map<Character, String> whiteSpace;

    public enum State {
        START, ID, NUM, PUNCT, WHITESPACE, ACCEPT, ERR
    }

    public enum CharType {
        LETTER, DIGIT, PUNCT, WHITESPACE, OTHER
    }

    public CharType[] characterClass;
    public State[][] edges;

    public Scanner() {

        characterClass = new CharType[256];
        for (int i = 0; i < characterClass.length; i++) {
            if (Character.isLetter((char) i)) {
                characterClass[i] = CharType.LETTER;
            } else if (Character.isDigit((char) i)) {
                characterClass[i] = CharType.DIGIT;
            } else if (Character.isWhitespace((char) i)) {
                characterClass[i] = CharType.WHITESPACE;
            } else if (isPunctuation((char) i)) {
                characterClass[i] = CharType.PUNCT;
            } else {
                characterClass[i] = CharType.OTHER;
            }
        }

        // Expand the number of states to match all states used (START, ID, NUM, PUNCT, WHITESPACE, ACCEPT, ERR)
        edges = new State[State.values().length][CharType.values().length];

        // START state transitions
        edges[State.START.ordinal()][CharType.LETTER.ordinal()] = State.ID;
        edges[State.START.ordinal()][CharType.DIGIT.ordinal()] = State.NUM;
        edges[State.START.ordinal()][CharType.PUNCT.ordinal()] = State.PUNCT;
        edges[State.START.ordinal()][CharType.WHITESPACE.ordinal()] = State.WHITESPACE;
        edges[State.START.ordinal()][CharType.OTHER.ordinal()] = State.ERR;

        // ID state transitions
        edges[State.ID.ordinal()][CharType.LETTER.ordinal()] = State.ID; // Continue reading an identifier
        edges[State.ID.ordinal()][CharType.DIGIT.ordinal()] = State.ID; // Identifiers can have digits
        edges[State.ID.ordinal()][CharType.PUNCT.ordinal()] = State.ACCEPT; // Punctuation ends an identifier
        edges[State.ID.ordinal()][CharType.WHITESPACE.ordinal()] = State.ACCEPT;
        edges[State.ID.ordinal()][CharType.OTHER.ordinal()] = State.ERR;

        // NUM state transitions
        edges[State.NUM.ordinal()][CharType.DIGIT.ordinal()] = State.NUM; // Continue reading a number
        edges[State.NUM.ordinal()][CharType.LETTER.ordinal()] = State.ERR; // Numbers can't have letters after digits
        edges[State.NUM.ordinal()][CharType.PUNCT.ordinal()] = State.ACCEPT;
        edges[State.NUM.ordinal()][CharType.WHITESPACE.ordinal()] = State.ACCEPT;
        edges[State.NUM.ordinal()][CharType.OTHER.ordinal()] = State.ERR;

        // PUNCT state transitions
        edges[State.PUNCT.ordinal()][CharType.LETTER.ordinal()] = State.ACCEPT;
        edges[State.PUNCT.ordinal()][CharType.DIGIT.ordinal()] = State.ACCEPT;
        edges[State.PUNCT.ordinal()][CharType.PUNCT.ordinal()] = State.ACCEPT;
        edges[State.PUNCT.ordinal()][CharType.WHITESPACE.ordinal()] = State.ACCEPT;
        edges[State.PUNCT.ordinal()][CharType.OTHER.ordinal()] = State.ERR;

        // ACCEPT state transitions (always go back to START after accepting)
        edges[State.ACCEPT.ordinal()][CharType.LETTER.ordinal()] = State.START;
        edges[State.ACCEPT.ordinal()][CharType.DIGIT.ordinal()] = State.START;
        edges[State.ACCEPT.ordinal()][CharType.PUNCT.ordinal()] = State.START;
        edges[State.ACCEPT.ordinal()][CharType.WHITESPACE.ordinal()] = State.START;
        edges[State.ACCEPT.ordinal()][CharType.OTHER.ordinal()] = State.START;

        // ERR state transitions (once in ERR, stay in ERR)
        edges[State.ERR.ordinal()][CharType.LETTER.ordinal()] = State.ERR;
        edges[State.ERR.ordinal()][CharType.DIGIT.ordinal()] = State.ERR;
        edges[State.ERR.ordinal()][CharType.PUNCT.ordinal()] = State.ERR;
        edges[State.ERR.ordinal()][CharType.WHITESPACE.ordinal()] = State.ERR;
        edges[State.ERR.ordinal()][CharType.OTHER.ordinal()] = State.ERR;

        // WHITESPACE state transitions
        edges[State.WHITESPACE.ordinal()][CharType.LETTER.ordinal()] = State.ACCEPT;
        edges[State.WHITESPACE.ordinal()][CharType.DIGIT.ordinal()] = State.ACCEPT;
        edges[State.WHITESPACE.ordinal()][CharType.PUNCT.ordinal()] = State.ACCEPT;
        edges[State.WHITESPACE.ordinal()][CharType.WHITESPACE.ordinal()] = State.START;
        edges[State.WHITESPACE.ordinal()][CharType.OTHER.ordinal()] = State.ERR;

        // Initialize reserved words
        tokens = new HashMap<>();
        tokens.put('(',"LPAREN");
        tokens.put(')',"RPAREN");
        tokens.put('{',"LBRACE");
        tokens.put('}',"RBRACE");
        tokens.put(',',"COMMA");
        tokens.put(';',"SEMICOLON");
        tokens.put('*',"STAR");
        tokens.put('!',"BANG");
        tokens.put('[',"LSQUARE");
        tokens.put(']',"RSQUARE");
        tokens.put('.',"PERIOD");
        tokens.put('=',"ASSIGN");
        tokens.put('/',"FORDWARDSLASH");
        tokens.put('+',"PLUS");
        tokens.put('-',"MINUS");
        tokens.put('"',"QUOTE");
        tokens.put('&',"BWAND");
        tokens.put('|',"BWOR");
        tokens.put('^',"XOR");
        tokens.put('~',"COMP");
        tokens.put('<',"LESSTHAN");
        tokens.put('>',"GREATERTHAN");

        reservedWords = new HashMap<>();
        reservedWords.put("class","CLASS");
        reservedWords.put("extends","EXTENDS");
        reservedWords.put("public","PUBLIC");
        reservedWords.put("int","INT");
        reservedWords.put("boolean","BOOLEAN");
        reservedWords.put("static","STATIC");
        reservedWords.put("void","VOID");
        reservedWords.put("main","MAIN");
        reservedWords.put("System.out.println","PRINT");
        reservedWords.put("true","TRUE");
        reservedWords.put("false","FALSE");
        reservedWords.put("this","THIS");
        reservedWords.put("new","NEW");
        reservedWords.put("String","STRING");
        reservedWords.put("return","RETURN");
        reservedWords.put("if","IF");
        reservedWords.put("while","WHILE");
        reservedWords.put("length","LENGTH");
        reservedWords.put("char","CHAR");
        reservedWords.put("else","ELSE");
        reservedWords.put("yield","YIELD");
        reservedWords.put("for","FOR");
        reservedWords.put("synchronized","SYNCHRONIZED");
        reservedWords.put("float","FLOAT");
        reservedWords.put("Xinu.print","PRINT");
        reservedWords.put("Xinu.println","PRINTLN");
        reservedWords.put("Xinu.printint","PRINTINT");
        reservedWords.put("Xinu.readint","READINT");
        reservedWords.put("&&", "AND");
        reservedWords.put("!=","NOTEQUAL");
        reservedWords.put("||","OR");
        reservedWords.put("==","EQUAL");

        whiteSpace = new HashMap<>();
        whiteSpace.put(' ',"SPACE");
        whiteSpace.put('\t',"ILLEGAL");
        whiteSpace.put('\r',"ILLEGAL");
        whiteSpace.put('\f',"ILLEGAL");
        whiteSpace.put('\n',"ILLEGAL");

    }

    private boolean isPunctuation(char c) {
        return "\"^~|&!,;(){}[]&*-+=./><".indexOf(c) != -1;
    }

    public String reader(java.io.Reader reader) throws java.io.IOException {
        PushbackReader pbReader = new PushbackReader(reader);
        State currentState = State.START;
        int nextChar;
        StringBuilder tokenBuilder = new StringBuilder();

        while ((nextChar = pbReader.read()) != -1) {
            char c = (char) nextChar;
            if(c == '/'){
                nextChar = pbReader.read();
                c = (char) nextChar;
                if(c != '/' && c != '*'){
                    nextChar = pbReader.read();
                    //System.out.println("FORWARDSLASH");
                    break;
                }
                else if(c  == '/'){ //
                    //System.out.println("RIGHT");
                    while((nextChar = pbReader.read()) != -1){
                        if(c == '\r' || c == '\n'){
                            c = (char)nextChar;
                            break;
                        }
                        else{c = (char)nextChar;}
                    }
                }else if(c == '*'){
                    while((nextChar = pbReader.read()) != -1){
                        if((char)nextChar == '*' && (char)pbReader.read() == '/'){
                            c = (char)pbReader.read();
                            break;
                        }
                        else{
                            c = (char)nextChar;
                        }
                    }
                }
            }
            
            CharType type = characterClass[c];
            State nextState = edges[currentState.ordinal()][type.ordinal()];

            switch (nextState) {
                case ACCEPT:
                    processToken(tokenBuilder.toString());
                    tokenBuilder.setLength(0);
                    if(type != CharType.WHITESPACE){
                        pbReader.unread(c);
                    }
                    currentState = State.START;
                    break;
                case ERR:
                    if(type == CharType.WHITESPACE){
                        currentState = State.START;
                        break;
                    }
                    else{
                    System.err.println("Illegal token.");
                    currentState = State.ERR;
                    }
                    break;
                default:
                    tokenBuilder.append(c);
                     if(punctTwo(pbReader,type,c)){
                        tokenBuilder = punctHelper(pbReader,tokenBuilder);
                        pbReader.read(); //skips next
                    }
                    currentState = nextState;
                    break;
            }
        }

        // Handle end of file and any remaining token
        if (tokenBuilder.length() > 0) {
            processToken(tokenBuilder.toString());
        }

        return ""; // Replace with actual output
    }

        private boolean punctTwo(PushbackReader reader, CharType current, char curChar) throws IOException {
        try {
            char nextChar;
            nextChar = (char) reader.read();
            StringBuilder strg = new StringBuilder();
            strg.append(curChar);
            if((int) nextChar >255 || (int)nextChar <0) {
                return false;
            }
            strg.append(nextChar);
            CharType nextCharType = characterClass[nextChar];

            if (nextCharType == CharType.PUNCT){
             //   System.out.println("a");
                if(current == CharType.PUNCT){
                   // System.out.println("b and the strg: '" +strg+"'");
                    if(reservedWords.containsKey(strg.toString())) {
             //           System.out.println("c");
                        reader.unread(nextChar);
                        return true;
            }}}
            reader.unread(nextChar);
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    private StringBuilder punctHelper(PushbackReader reader, StringBuilder strg) throws IOException {

        char nextChar = (char)reader.read();
        strg.append(nextChar);
        reader.unread(nextChar);
        return strg;
    }

    private void processToken(String token) {
        if (reservedWords.containsKey(token)) {
            System.out.println(reservedWords.get(token));
        } else if (tokens.containsKey(token.charAt(0))) {
            System.out.println(tokens.get(token.charAt(0)));
        } else if (Character.isDigit(token.charAt(0))) {
            System.out.println("INTEGER_LITERAL(" + token + ")");
        } else if (Character.isLetter(token.charAt(0))) {
            System.out.println("ID(" + token+") ");
        } else {
            //System.err.println("Illegal token: " + token);
        }
    }
    public static void main(String[] args) throws java.io.IOException {
        
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        PushbackReader fileReader = new PushbackReader(inputStreamReader);
        Scanner r = new Scanner();
        r.reader(fileReader);
        System.out.println("EOF");
    }
}
