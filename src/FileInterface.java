import java.io.FileNotFoundException;

public interface FileInterface {
    void checkIntegrity() throws FileNotFoundException, FileIntegrityException;
}
