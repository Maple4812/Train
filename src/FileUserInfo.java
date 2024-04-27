import java.io.FileNotFoundException;
import java.io.*;
import java.util.Scanner;

public class FileUserInfo implements FileInterface{
    Scanner scan;
    String fileName;
    FileWriter fw;
    PrintWriter writer;
    public FileuserInfo(String fileName) {
        this.fileName = fileName;
    }
    @Override
    public void checkIntegrity() throws FileNotFoundException {

    }
}
