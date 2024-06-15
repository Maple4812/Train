import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws Exception {

        // 각각의 파일 객체를 생성하고 각 생성자의 인자로 파일 경로를 받는다.
        FileInterface fileUserInfo = new FileUserInfo("UserInfo.csv");
        FileInterface fileReserve = new FileReserve("reserve.csv");
        FileInterface fileTempReserve = new FileTempReserve("tempReserve.csv");
        FileInterface fileRail = new FileRail("rail.csv");
        FileInterface fileTimeTable = new FileTimeTable("timeTable.csv", fileRail);

        try{
            // 각 파일객체에서 무결성검사를 하고, 오류가 있으면 error 를 throw 한다.
            fileUserInfo.checkIntegrity();
            fileRail.checkIntegrity();
            fileTimeTable.checkIntegrity();
            fileReserve.checkIntegrity();
            fileTempReserve.checkIntegrity();
        } catch (Exception e)
        {
            // error 가 catch 되면 이를 사용자에게 알리고 프로그램을 종료한다.
            System.out.println("파일에서 심각한 오류가 발견되었습니다. 프로그램을 종료합니다.");
            return;
        }

        /*
        각 수행을 위해 각 수행에 맞는 객체를 생성합니다.
        각 수행 객체의 생성자의 인자로 FileInterface 를 받아 수행 클래스 내부에서 파일을 수정할 수 있습니다.
        파일내부 내용의 읽기나 쓰기 작업들은 파일 클래스 내부 함수를 통해서 구현하는것이 더 좋아보입니다.

        ... 자리에 각 수행 클래스에서 필요한 파일 객체를 적어 인자로 가져갑니다.
        */

        // NOTE! 구간 정보 파일이 필요한 경우, 기능 객체의 시작 파라미터로 파일 객체를 넣으시면 됩니다.
        LogInAndTimeInput logInAndTimeInput = new LogInAndTimeInput();
        CheckTimeTable checkTimeTable = new CheckTimeTable(fileTimeTable, fileRail);
        TempReservation tempReservation = new TempReservation(fileReserve, fileTempReserve, fileTimeTable, fileRail);
        ReservationAndCancel reservationAndCancel = new ReservationAndCancel(fileTempReserve, fileReserve, fileTimeTable);

        // 가장 처음 사용자로부터 정보들을 입력받습니다.
        logInAndTimeInput.init();

        while(true) {
            // 정상적으로 사용자 정보를 입력 받으면 명령을 입력받습니다.
            System.out.println("명령어 목록: Q(프로그램 종료), 시간표조회, 기차표예약, 예약조회");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String cmd = br.readLine();

            LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
            TempReservation.removeTimeOutReserve();

            switch (cmd) {
                // Q 의 경우 프로그램을 종료합니다.
                case "Q":
                    System.out.println("프로그램을 종료합니다.");
                    return;
                // 시간표조회를 입력한 경우 시간표조회 클래스의 init() 을 호출합니다.
                case "시간표조회":
                    int result = checkTimeTable.init();
                    if(result != 1)
                        break;
                // 기차표예약을 입력한 경우 기차표예약 클래스의 init() 을 호출합니다.
                case "기차표예약":
                    tempReservation.init();
                    break;
                // 예약조회를 입력한 경우 예약조회 클래스의 init() 을 호출합니다.
                case "예약조회":
                    reservationAndCancel.init();
                    break;
                // 명령어 목록에 해당하지 않은 명령어가 입력된 경우 다시 입력받습니다.
                default:
                    System.out.println("잘못된 입력입니다. 다시 입력해주세요.");
            }
        }
    }
}
