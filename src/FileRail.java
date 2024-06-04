import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileRail implements FileInterface{
    private String fileName;
    private ArrayList<Rail> raillist;
    Scanner scan;
    //저희 member 수정해야할거같아요

    public FileRail(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {

    }
}
