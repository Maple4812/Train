import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FileReserve implements FileInterface{
    Scanner scan;
    String fileName;
    FileWriter fw;
    PrintWriter writer;
    public FileReserve(String fileName) {
        this.fileName = fileName;
    }
    private ArrayList<ArrayList<String>> tempList = new ArrayList<>();
    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        scan = new Scanner(new File(fileName));
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다은 split
            if(strArr.length != 4) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            UserName.checkIntegrity(strArr[0]);  //사용자 이름 무결성 확인
            PhoneNumber.checkIntegrity(strArr[1]);  //전화번호 무결성 확인
            Ticket.checkIntegrity(strArr[2]);  //노선번호 무결성 확인
            Time.checkIntegrity(strArr[3]);  //출발 시각 무결성 확인
        }
    }

    public void repos(){
        try {
            checkIntegrity();
            tempList = new ArrayList<>();
            scan = new Scanner(new File(fileName));
            while(scan.hasNextLine()) {
                String[] strArr = scan.nextLine().split(",");
                ArrayList<String> list = new ArrayList<>(Arrays.asList(strArr)); // 6개의 인자를 String 형태로 가진 ArrayList (named: list)
                tempList.add(list); // 위에서 생성한 ArrayList를 tempList에 append한다
            }
        } catch (FileNotFoundException | FileIntegrityException e) {
            e.printStackTrace();
        }
    }
    public void write(String userName, String phoneNumber, String lineNum, String startTime) {
        File file = new File(fileName);
        try {
            fw = new FileWriter(file, true);
            writer = new PrintWriter(fw);
            String str = userName + "," + phoneNumber + "," + lineNum + "," + startTime;
            writer.println(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //ReservationAndCancel에서 사용자별 예약정보를 출력하기 위해 이 항목에서 fileName을 get 하기 위해 만든 getter입니다.

    public String getFileName() {
        return fileName;
    }

    public int findByLineNum(String userName, String lineNum) {
        int index = 0;
        for (ArrayList<String> tempReserve : tempList) {
            if (tempReserve.get(0).equals(userName)) {
                if (tempReserve.get(2).equals(lineNum))
                    return index;
            }
            index++;
        }
        return -1;
    }
}
