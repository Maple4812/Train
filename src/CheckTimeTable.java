import java.io.IOException;
import java.util.Scanner;

public class CheckTimeTable {

    public void init() throws IOException, FileIntegrityException {
        System.out.println("운형하는 역 : ");

        while(true) {

            System.out.println("원하시는 출발역, 도착역, 출발 시각을 차례대로 입력해주세요.");
            System.out.print("-> ");

            // 시간표 조회 : 사용자로부터 출발역, 도착역, 출발 시각 입력받음
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();
            String[] inputArr = input.split(",");
            // 요소가 3개가 아닐 시 재입력
            if (inputArr.length != 3) {
                System.out.print("잘못된 입력입니다.(인자수가 틀림) ");
                continue;
            }
            // 각 요소가 문법형식에 맞는지 확인
            try {
                Station.checkIntegrity(inputArr[0]);
                Station.checkIntegrity(inputArr[1]);
                Station.checkIntegrity(inputArr[2]);

            }catch (FileIntegrityException e) {
                System.out.print("잘못된 입력입니다.(문법오류) ");
            }
        }
    }

    public void repos(String file){

    }
}
