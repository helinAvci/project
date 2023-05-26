import java.io.IOException;
import java.util.Scanner;

public class main {
    public static void main(String[] args) throws IOException {
        System.out.println("Please enter the name of the input file: ");
        Scanner inputScan = new Scanner(System.in);
        String fileName = inputScan.next();
        inputScan.close();
        LexicalAnalyzer.scanner(fileName);
    }
}

