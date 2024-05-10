import java.io.FileNotFoundException;

public class FileStationInterval implements FileInterface{

    String fileName;

    FileStationInterval(String fileName){
        this.fileName = fileName;
    }
    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {

    }
}
