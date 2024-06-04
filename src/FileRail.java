import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileRail implements FileInterface{
    private String fileName;
    private ArrayList<Rail> raillist;
    Scanner scan;

    public FileRail(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        scan= new Scanner(new File(fileName));
    }
}
