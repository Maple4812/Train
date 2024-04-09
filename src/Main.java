import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) throws Exception {

        // Scanner 대신 사용할 수 있는 input 입니다.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // 각 기능에 따른 객체 생성
        LogInAndTimeInput logInAndTimeInput = new LogInAndTimeInput();
        CheckTimeTable checkTimeTable = new CheckTimeTable();
        TempReservation tempReservation = new TempReservation();
        ReservationAndCancel reservationAndCancel = new ReservationAndCancel();

        // 무결성 검사
        try{
            /*
            계획서에 적혀있는 본인 파트의 데이터 파일을 각자 검사하여 오류가 있으면 Exception 을 Throws 해줍니다.
            모든 클래스가 repos 함수를 가지고 있으니 추상 클래스로 묶어주는 것이 좋아보이긴 하나,
            추상 클래스로 묶어서 무결성 검사를 히면
            무결성 검사를 한 팀이 하게 되고, 이를 분담하지 못하므로 "각자" 무결성 검사를 해줍니다.
             */

            logInAndTimeInput.repos("userInfo.csv");
            checkTimeTable.repos("timetable.csv");
            tempReservation.repos("tempReserve.csv");
            reservationAndCancel.repos("reserve.csv");
        }catch (Exception e)
        {
            // 무결성 검사에서 오류 발견시 알려주고 프로그램 종료
            System.out.println("데이터 파일에 중대 결함이 발견되어 프로그램을 종료합니다.");
            return;
        }

        System.out.println("명령어 목록: Q(프로그램 종료), 시간표조회, 기차표예약, 예약조회");

        while(true) {
            String cmd = br.readLine();

            switch (cmd){
                case "Q":
                    System.out.println("프로그램을 종료합니다.");
                    return;
                case "시간표조회":
                    checkTimeTable.init();
                    break;
                case "기차표예약":
                    tempReservation.init();
                    break;
                case "예약조회":
                    reservationAndCancel.init();
                    break;
                default:
                    System.out.println("잘못된 입력입니다. 다시 입력해주세요.");
            }
        }

        return;
    }
}
