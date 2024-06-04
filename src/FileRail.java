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
        ArrayList<Rail> railList=new ArrayList<>();//구간번호 인덱스 드을 체크하기 위한 arraylist
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Rail rail=new Rail(); //읽어온 줄의 정보를 저장할 Rail 객체
            /*
            각 줄의 모든 인자에 대해 그 인자에 맞는 무결성 검사 먼저 진행하는 부분
             */

            /*
            rail.rainIndex=Integer.parseInt(strArr[0]);
            rail.fromStation=strArr........
            이러한 형태로 Rail 객체의 member들에 값을 할당.
            이렇게 만든 Rail 객체를 railList에 저장해 다른 무결성검사에 활용
             */
        }

        /*
        운행 정보 인덱스가 1인 구간과 -1인 구간이 출발역 도착역만 다르고 나머지 정보는 일치하는지 무결성검사
        그 외 추가 항목들 모두 이 아래에서 검사
         */
    }
}
