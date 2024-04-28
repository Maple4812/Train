import java.io.*;
import java.util.Scanner;

public class FileUserInfo implements FileInterface {
    Scanner scan;
    String fileName;

    public FileUserInfo(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        scan = new Scanner(new File(fileName));
        while (scan.hasNextLine()) {
            String[] strArr = scan.nextLine().split(","); // 한 줄 읽어온 다음 split
            if (strArr.length != 2) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            UserName.checkIntegrity(strArr[0]); // 사용자 이름 무결성 확인
            PhoneNumber.checkIntegrity(strArr[1]); // 전화번호 무결성 확인
        }
    }

    public void append(String userName, String phoneNumber) {
        try (FileWriter fw = new FileWriter(fileName, true);
             PrintWriter writer = new PrintWriter(fw)) {
            String str = userName + "," + phoneNumber;
            writer.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
