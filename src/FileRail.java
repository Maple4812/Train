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
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Rail rail=new Rail(); //읽어온 줄의 정보를 저장할 Rail 객체
            /*
            무결성 검사 진행하는 부분 여기에 추가
             */

            /*
            rail.rainIndex=Integer.parseInt(strArr[0]);
            rail.fromStation=strArr........
            이러한 형태로 rail 객체의 member들에 값을 할당
             */
        }
    }
}
