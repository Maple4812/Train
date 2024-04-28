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
    }

    public void init() throws FileIntegrityException, IOException, ParseException {
        ArrayList<String> trueList = new ArrayList<>(Arrays.asList("T", "t", "예", "ㅇ", "1"));
        ArrayList<String> falseList = new ArrayList<>(Arrays.asList("F", "f", "아니오", "ㄴ", "0"));
        String lineNum = "";
        boolean isConfirmed = false;
        int tickets = 1;
        Ticket ticket = null;

        while (true) {
            System.out.print("예약하시고 싶은 기차표의 노선번호, 확정 여부, 개수(없을 경우 1개)를 입력해주세요: ");
            String input = scan.next();
            LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
            TempReservation.removeTimeOutReserve();
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
                    if (!(trueList.contains(inputArr[1]) || falseList.contains(inputArr[1]))) {
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
                    if (!(trueList.contains(inputArr[1]) || falseList.contains(inputArr[1]))) {
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
            if (trueList.contains(inputArr[1])) {
                isConfirmed = true;
            } else if (falseList.contains(inputArr[1])) {
                isConfirmed = false;
            }
            ticket = timeTableFile.getTicket(lineNum);  // 티켓의 노선번호로 티켓 객체를 가져오는 임의의 함수입니다.
            if (ticket == null) {
                System.out.println("잘못된 노선번호입니다.");
                continue;
            }
            if (tickets > ticket.extraSeat.getSeat()) {
                System.out.println("해당 열차에서는 최대 " + ticket.extraSeat.getSeat() + "개의 좌석만 예약할 수 있습니다.");
                continue;
            } else {
                break;
            }
        }
        timeTableFile.reduceExtraSeat(lineNum, tickets);
        loginClient = LogInAndTimeInput.getClient();
        //일반 예약인 경우
        if (isConfirmed) {
            for (int i = 0; i < tickets; i++) {
                reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
            }
            System.out.println(FORMATTER.parse(ticket.depTime) + "에 출발하는 " + ticket.lineNum + " " + tickets + "장을 예매 확정지었습니다.");
        }
        //가예약인 경우
        else {
            for (int i = 0; i < tickets; i++) {
                Date now = new Date();
                tempReserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime, LogInAndTimeInput.getNowTime(), FORMATTER.format(new Date()));
            }
            System.out.println(FORMATTER.parse(ticket.depTime) + "에 출발하는 " + ticket.lineNum + " " + tickets + "장을 가예약 했습니다.");
        }
    }

    static public void init2() throws IOException {
        tempReserveFile.repos();
        ArrayList<ArrayList<String>> tempReserves = new ArrayList<>();
        ArrayList<Integer> tempReserveIndexArrayList = new ArrayList<>();

        if (tempReserveFile.getTempList().isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
            int index = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            LocalDateTime departureTime = LocalDateTime.parse(tempReserve.get(3), formatter);
            LocalDateTime reserveTime = LocalDateTime.parse(tempReserve.get(4), formatter);

            System.out.println(tempReserve.get(1) + "/" + tempReserve.get(0) + " 고객님의 예약정보입니다.");
            System.out.println("행 번호 / 노선 번호 / 출발 시각 / 출발 역 / 도착 시간 / 도착 역");
            if (tempReserve.get(0).equals(loginClient.getName()) && departureTime.isAfter(LocalDateTime.now())) {
                long minutesBetween = Duration.between(LocalDateTime.now(), reserveTime).toMinutes();

                tempReserves.add(tempReserve);
                tempReserveIndexArrayList.add(index);

                System.out.print("#" + (index + 1) + " / ");
                System.out.print(tempReserve.get(2) + " / ");
                System.out.print(tempReserve.get(3));
                System.out.print(timeTableFile.getTicket(tempReserve.get(2)).fromStation.getStation());
                System.out.print(timeTableFile.getTicket(tempReserve.get(2)).arrivalTime);
                System.out.print(timeTableFile.getTicket(tempReserve.get(2)).toStation.getStation());
                System.out.println();
                if (minutesBetween > 20)
                    System.out.println("- 20분이 지나 삭제되었습니다.");
                System.out.println();
            }

            index++;
        }

        System.out.println("확정할 가예약을 입력하세요: ");
        Scanner inputScan = new Scanner(System.in);
        TempReservation.timeRenewal();
        TempReservation.removeTimeOutReserve();
        String[] inputArr = inputScan.nextLine().split(",");
        inputScan.close();

        ArrayList<Ticket> confirmedTicketArrayList = new ArrayList<>();

        switch (inputArr.length) {
            case 1:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    int index = Integer.parseInt(inputArr[0].replace("#", "")) - 1;

                    if (index >= 0 && index < tempReserves.size()) {
                        reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                        confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                        new FileOutputStream(tempReserveFile.getFileName()).close();
                        tempReserveFile.getTempList().remove(tempReserveIndexArrayList.get(index));
                        for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                            tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    }
                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    int index = 0;
                    while (index != -1) {
                        index = tempReserveFile.findByLineNum(loginClient.getName(), inputArr[0]);
                        reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), inputArr[0], timeTableFile.getTicket(inputArr[0]).depTime);
                        confirmedTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                        tempReserveFile.getTempList().remove(index);
                    }

                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                }
                break;

            case 2:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();
                    for (int i = 0; i < 2; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < tempReserves.size()) {
                            reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                            confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                            tempReserveFile.getTempList().remove(tempReserveFile.findByLineNum(loginClient.getName(), tempReserves.get(i).get(2)));
                        }
                    }
                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int num = Integer.parseInt(inputArr[1]);

                    int num2 = 0;
                    for (ArrayList<String> tempReserve : tempReserves) {
                        if (inputArr[0].equals(tempReserve.get(2)))
                            num2++;
                    }

                    if (num > num2) {
                        System.out.println("입력하신 표의 개수가 많습니다. 가예약하신 표의 개수만큼 입력해주세요.");
                        return;
                    }

                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    for (int i = 0; i < num; i++) {
                        reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), inputArr[0], timeTableFile.getTicket(inputArr[0]).depTime);
                        confirmedTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                        tempReserveFile.getTempList().remove(tempReserveFile.findByLineNum(loginClient.getName(), inputArr[0]));
                    }

                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                } else if (inputArr[1].equals("출발")) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepStation(inputArr[0]+"역");

                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = tempReserveFile.findByLineNum(loginClient.getName(), ticket.lineNum);
                        if (index != -1) {
                            reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
                            confirmedTicketArrayList.add(ticket);
                            tempReserveFile.getTempList().remove(index);
                        }
                    }

                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                } else if (inputArr[1].equals("도착")) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByArrStation(inputArr[0]+"역");

                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = tempReserveFile.findByLineNum(loginClient.getName(), ticket.lineNum);
                        if (index != -1) {
                            reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
                            confirmedTicketArrayList.add(ticket);
                            tempReserveFile.getTempList().remove(index);
                        }
                    }

                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                }
                break;

            case 3:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    for (int i = 0; i < 3; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < tempReserves.size()) {
                            reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                            confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                            tempReserveFile.getTempList().remove(tempReserveFile.findByLineNum(loginClient.getName(), tempReserves.get(i).get(2)));
                        }
                    }
                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                }
                break;

            case 4:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    for (int i = 0; i < 4; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < tempReserves.size()) {
                            reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                            confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                            tempReserveFile.getTempList().remove(tempReserveFile.findByLineNum(loginClient.getName(), tempReserves.get(i).get(2)));
                        }
                    }
                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                } else if (inputArr[1].equals("출발")) {
                    new FileOutputStream(tempReserveFile.getFileName()).close();

                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepArrStation(inputArr[0]+"역", inputArr[2]+"역");

                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = tempReserveFile.findByLineNum(loginClient.getName(), ticket.lineNum);
                        if (index != -1) {
                            reserveFile.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
                            confirmedTicketArrayList.add(ticket);
                            tempReserveFile.getTempList().remove(index);
                        }
                    }

                    for (ArrayList<String> tempReserve : tempReserveFile.getTempList()) {
                        tempReserveFile.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                    }
                }
                break;
        }
        printConfirmedTickets(confirmedTicketArrayList);
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

    public static void removeTimeOutReserve() {
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
