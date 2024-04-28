import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TempReservation {
    private final static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");
    private static FileTempReserve tempReserveFile;
    private FileUserInfo userInfoFile;
    private static FileReserve reserveFile;
    private static FileTimeTable timeTableFile;
    private static Client loginClient;
    Scanner scan = new Scanner(System.in);

    public TempReservation(FileInterface userInfoFile, FileInterface reserveFile, FileInterface tempReserveFile, FileInterface timeTableFile) {
        this.tempReserveFile = (FileTempReserve) tempReserveFile;
        this.userInfoFile = (FileUserInfo) userInfoFile;
        this.reserveFile = (FileReserve) reserveFile;
        this.timeTableFile = (FileTimeTable) timeTableFile;
        this.loginClient = loginClient;
    }

    public void init() throws FileIntegrityException, IOException {
        String lineNum = "";
        boolean isConfirmed = false;
        int tickets = 0;
        Ticket ticket = null;

        while (true) {
            System.out.print("예약하시고 싶은 기차표의 노선번호, 확정 여부, 개수(없을 경우 1개)를 입력해주세요: ");
            String input = scan.next();
            String[] inputArr = input.split(",");
            int n = inputArr.length;
            if (n == 1) {
                if (inputArr[0].equals("Q")) {
                    System.out.println("메인 프롬프트로 돌아갑니다");
                    return;
                } else {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                }
            } else if (n == 2) {
                try {
                    Ticket.checkIntegrity(inputArr[0]);
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 노선 번호입니다.");
                    continue;
                }
                try {
                    if (!(inputArr[1].equals("T") || inputArr[1].equals("F"))) {
                        throw new FileIntegrityException();
                    }
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                }
            } else if (n == 3) {
                try {
                    Ticket.checkIntegrity(inputArr[0]);
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 노선 번호입니다.");
                    continue;
                }
                try {
                    if (!(inputArr[1].equals("t") || inputArr[1].equals("f"))) {
                        throw new FileIntegrityException();
                    }
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                }
                try {
                    tickets = Integer.parseInt(inputArr[2]);  // 정수형태로 parseInt
                    if (tickets <= 0) {  //자연수인지 확인
                        throw new FileIntegrityException();
                    }
                } catch (NumberFormatException e) {  // 정수가 아닐 때
                    System.out.println("잘못된 입력입니다.");
                    continue;
                } catch (FileIntegrityException e) {  // 1 이상의 정수가 아닐 때
                    System.out.println("예매 수가 올바르지 않습니다. 1 이상의 정수를 입력하세요.");
                    continue;
                }
            } else {
                System.out.println("잘못된 입력입니다.");
                continue;
            }
            lineNum = inputArr[0];                //여기서 각 인수를 변수에 넣는다
            if (inputArr[1].equals("T")) {
                isConfirmed = true;
            } else if (inputArr[1].equals("F")) {
                isConfirmed = false;
            }
            ticket = timeTableFile.getTicket(lineNum);  // 티켓의 노선번호로 티켓 객체를 가져오는 임의의 함수입니다.
            if (ticket == null) {
                System.out.println("잘못된 노선번호입니다.");
                continue;
            }
            timeTableFile.reduceExtraSeat(lineNum, tickets);
            if (tickets > ticket.extraSeat.getSeat()) {
                System.out.println("해당 열차에서는 최대 " + ticket.extraSeat + "개의 좌석만 예약할 수 있습니다.");
                continue;
            } else {
                break;
            }
        }
        //일반 예약인 경우
        if (isConfirmed) {
            for (int i = 0; i < tickets; i++) {
                reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.arrivalTime);
            }
            System.out.println(ticket.arrivalTime + "에 출발하는 " + ticket.lineNum + " " + tickets + "장을 예매 확정지었습니다.");
        }
        //가예약인 경우
        else {
            for (int i = 0; i < tickets; i++) {
                Date now = new Date();
                String formattedNow = FORMATTER.format(now);
                tempReserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.arrivalTime, ticket.depTime, formattedNow);
            }
            System.out.println(ticket.arrivalTime + "에 출발하는 " + ticket.lineNum + " " + tickets + "장을 가예약 했습니다.");
        }
    }

    public static boolean isTimerOn() {
        return tempReserveFile.isTimerOn();
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

        Date savedNowComputerDate = null;
        try {
            savedNowComputerDate = FORMATTER.parse(LogInAndTimeInput.getNowComputerTime());
            Date nowComputerDate = new Date();
            Date savedNowDate = FORMATTER.parse(LogInAndTimeInput.getNowTime());

            long time1 = savedNowComputerDate.getTime();
            long time2 = nowComputerDate.getTime();
            long nowTime = savedNowDate.getTime();

            long diff = time2 - time1;
            nowTime += diff;

            return FORMATTER.format(new Date(nowTime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getNewTime() {
        // 기능 : 타이머가 켜진경우의 새로운 현재 시각을 return 해준다.
        return tempReserveFile.getNewTime();
    }

    public void removeTimeOutReserve() {
        // 기능 : 5분이 지난 예약들을 현재 시각을 기준으로 해서 삭제해준다.
        tempReserveFile.removeTimeOutReserve();
    }

    public static void printConfirmedTickets(ArrayList<Ticket> ticketArrayList) {
        System.out.println("예약 확정 열차 정보: ");
        for (Ticket ticket : ticketArrayList) {
            System.out.println("노선번호: "+ticket.lineNum);
            System.out.println("출발시각: "+ticket.depTime);
            System.out.println("출발역: "+ticket.fromStation.getStation());
            System.out.println("도착시각: "+ticket.arrivalTime);
            System.out.println("도착역: "+ticket.toStation.getStation());
        }
        System.out.println("예약 확정 되었습니다.");
    }
}
