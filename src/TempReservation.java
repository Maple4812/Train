import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;

public class TempReservation {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    private FileTempReserve tempReserveFile;
    private FileUserInfo userInfoFile;
    private FileReserve reserveFile;
    private FileTimeTable timeTableFile;
    ReservationAndCancel reservationAndCancel = new ReservationAndCancel();
    Scanner scan = new Scanner(System.in);

    public TempReservation(FileInterface userInfoFile, FileInterface reserveFile, FileInterface tempReserveFile, FileInterface timeTableFile)
    {
        this.tempReserveFile = (FileTempReserve) tempReserveFile;
        this.userInfoFile = (FileUserInfo) userInfoFile;
        this.reserveFile = (FileReserve) reserveFile;
        this.timeTableFile = (FileTimeTable) timeTableFile;
    }

    public void init() throws FileIntegrityException {
        String lineNum = "";
        boolean isConfirmed = false;
        int tickets = 0;

        System.out.print("예약 확정/예약 취소 중 선택해주세요:" );
        String input = scan.next();
        String[] inputArr = input.split(",");
        switch (inputArr.length) {  // 입력 인자 개수로 switch
            case 1:
                if (inputArr[0].equals("Q")) {
                    System.out.println("메인 프롬프트로 돌아갑니다");
                    return;
                }
                else {
                    System.out.println("잘못된 입력입니다.");
                }
                break;
            case 2:
                try {
                    Ticket.checkIntegrity(inputArr[0]);
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 노선 번호입니다.");
                }
                try {
                    if (!(inputArr[1].equals("T") || inputArr[1].equals("F"))){
                        throw new FileIntegrityException();
                    }
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 입력입니다.");
                }
            case 3:
                try {
                    Ticket.checkIntegrity(inputArr[0]);
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 노선 번호입니다.");
                }
                try {
                    if (!(inputArr[1].equals("t") || inputArr[1].equals("f"))){
                        throw new FileIntegrityException();
                    }
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 입력입니다.");
                }
                try {
                    tickets = Integer.parseInt(inputArr[2]);  // 정수형태로 parseInt
                    if (tickets <= 0) {  //자연수인지 확인
                        throw new FileIntegrityException();
                    }
                } catch (NumberFormatException e) {  // 정수가 아닐 때
                    System.out.println("잘못된 입력입니다.");
                } catch (FileIntegrityException e) {  // 1 이상의 정수가 아닐 때
                    System.out.println("예매 수가 올바르지 않습니다. 1 이상의 정수를 입력하세요.");
                }
            default:
                System.out.println("잘못된 입력입니다.");

            lineNum = inputArr[0];                //여기서 각 인수를 변수에 넣는다
            if(inputArr[1].equals("T")) {
                isConfirmed = true;
            }
            else if (inputArr[1].equals("F")){
                isConfirmed = false;
            }
            //TODO 여기서 해야하는 것!!!!!!!!: 위에서 받은 내용을 바탕으로 한 티켓이 실제로 timetable.csv에 존재하는지 확인해야 한다.
            Ticket ticket = timeTableFile.getTicket(lineNum);  // 티켓의 노선번호로 티켓 객체를 가져오는 임의의 함수입니다.
            //TODO 예약을 한 다음(예약 확정을 한 다음) timetable.csv 에서 여석 수를 줄인다.
            if (tickets > ticket.extraSeat.getSeat()) {
                System.out.println("해당 열차에서는 최대 " + ticket.extraSeat + "개의 좌석만 예약할 수 있습니다.");
            }
            //일반 예약인 경우
            if(isConfirmed) {
                for (int i = 0; i < tickets; i++) {
                    // TODO client 가져와서 이름 및 전화번호 얻고 예약 컴퓨터 시간까지 넣기
                    tempReserveFile.write(FileUserInfo.userName, FileUserInfo.phoneNumber, ticket.lineNum, ticket.arrivalTime, ticket.depTime, "예약 컴퓨터 시각");
                }
                System.out.println(ticket.arrivalTime +"에 출발하는 " + ticket.lineNum + " " + tickets + "장을 예매 확정지었습니다.");
            }
            //가예약인 경우
            else {
                for (int i = 0; i < tickets; i++) {
                    //TODO 위에랑 마찬가지
                    reserveFile.write(FileUserInfo.userName, FileUserInfo.phoneNumber, ticket.lineNum, ticket.arrivalTime);
                }
                System.out.println(ticket.arrivalTime +"에 출발하는 " + ticket.lineNum + " " + tickets + "가예약 했습니다.");
            }
        }
    }

    public static String timeRenewal() {
        /*
        기능 :
            LogInAndTimeInput 에 저장되어있는 컴퓨터 시각과 함수가 호출된 시각의 차이를 구하고
            기존에 저장되어 있던 시각과 더해서 새로운 시각을 return
        */

        /*
        변수 :
            @param savedNowComputerDate: 저장되어있는 현재의 컴퓨터 시각
            @param nowComputerDate: 현재 컴퓨터 시각
            @param savedNowDate: 저장되어있는 현재 시각
            @param time1: 저장되어있는 현재의 컴퓨터 시각의 getTime 버젼
            @param time2: 현재의 컴퓨터 시각의 getTime 버젼
            @param nowTime: 저장되어있는 현재 시각의 getTime 버젼
            @param diff: 지나간 시간
         */

        Date savedNowComputerDate = formatter.parse(LogInAndTimeInput.getNowComputerTime());
        Date nowComputerDate = new Date();
        Date savedNowDate = formatter.parse(LogInAndTimeInput.getNowTime());

        long time1 = savedNowComputerDate.getTime();
        long time2 = nowComputerDate.getTime();
        long nowTime = savedNowDate.getTime();

        long diff = time2 - time1;
        nowTime += diff;

        return formatter.format(new Date(nowTime));
    }

    public static String getNewTime() {
        // 기능 : 타이머가 켜진경우의 새로운 현재 시각을 return 해준다.
        return tempReserveFile.getNewTime();
    }

    public static void removeTimeOutReserve()
    {
        // 기능 : 5분이 지난 예약들을 현재 시각을 기준으로 해서 삭제해준다.
        tempReserveFile.removeTimeOutReserve();
    }
}
