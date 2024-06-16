import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TempReservation {
    private final static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");
    private static FileTempReserve tempReserveFile;
    private static FileReserve reserveFile;
    private static FileTimeTable timeTableFile;
    private static FileRail railFile;
    Scanner scan = new Scanner(System.in);

    public TempReservation(FileInterface reserveFile, FileInterface tempReserveFile, FileInterface timeTableFile, FileInterface fileRail) {
        this.tempReserveFile = (FileTempReserve) tempReserveFile;
        this.reserveFile = (FileReserve) reserveFile;
        this.timeTableFile = (FileTimeTable) timeTableFile;
        this.railFile = (FileRail) fileRail;
    }

    public void init() throws FileIntegrityException, IOException, ParseException {
        ArrayList<String> trueList = new ArrayList<>(Arrays.asList("T", "t", "예", "ㅇ", "1"));
        ArrayList<String> falseList = new ArrayList<>(Arrays.asList("F", "f", "아니오", "ㄴ", "0"));
        railFile.printRail();

        while (true) {
            System.out.print("예약하시고 싶은 기차표의 노선번호, 노선 정보, 확정 여부, 개수(없을 경우 1개)를 입력해주세요: ");
            String input = scan.next();
            LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
            TempReservation.removeTimeOutReserve();
            String[] inputArr = input.split(",");
            Ticket ticket = new Ticket();
            TempTicket tempTicket = new TempTicket();
            int n = inputArr.length;
            // 1개가 입력되었을 때
            if (n == 1) {
                if (inputArr[0].equals("Q")) {
                    System.out.println("메인 프롬프트로 돌아갑니다");
                    return;
                } else {
                    System.out.println("잘못된 입력입니다.");
                }
            }
            // 3개가 입력되었을 때
            else if (n == 3) {
                String[] strArr = inputArr[1].split("/");
                ArrayList<Rail> list = new ArrayList<>();
                try {
                    for (String s : strArr) {
                        // 2024-06-14 수정
                        // Rail 파일에 존재하지 않은 Rail 객체를 리턴받으려 할때 null 값이 반환되는데
                        // 해당 null 에 대한 예외처리를 여기서 해주지 않으면 다른 곳에서 NullPointerException 이 발생하므로
                        // 예외 처리를 추가
                        Rail r = timeTableFile.railFile.getRailByIndex(Integer.parseInt(s));
                        if(r == null)
                            throw new FileIntegrityException();
                        list.add(r);
                    }
                } catch (NumberFormatException e1) {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                } catch (FileIntegrityException e2){
                    System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
                    continue;
                }
                try {
                    if (!(trueList.contains(inputArr[2]) || falseList.contains(inputArr[2]))) {
                        throw new FileIntegrityException();
                    }
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                }

                // 예매 확정인 경우
                if(trueList.contains(inputArr[2])) {
                    try {
                        ticket.line = timeTableFile.getLine(inputArr[0]);
                    } catch (FileIntegrityException e) {
                        System.out.println("잘못된 노선 번호입니다");
                        continue;
                    }
                    ticket.client = LogInAndTimeInput.getClient();
                    ticket.railIndices = list;
                    try {
                        ticket.checkIntegrity();
                    } catch (FileIntegrityException e) {
                        System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
                        continue;
                    }
                    // 해당 노선 정보에 해당하는 여석이 남아있지 않은 경우
                    int remainSeat = ticket.line.calculateSeat(list.get(0).railIndex, list.get(list.size() - 1).railIndex);
                    if(remainSeat < 1) {
                        System.out.println("해당 열차에서는 최대 0개의 좌석만 예약할 수 있습니다.");
                        continue;
                    }
                    try {
                        ticket.depTime = ticket.line.calculateDepTime(list.get(0).railIndex);
                        ticket.checkIntegrityAboutTime(LogInAndTimeInput.getNowTime());
                    } catch (FileIntegrityException e) {
                        System.out.println("출발시간을 확인해주세요. 현재 시각은 " + LogInAndTimeInput.getNowTime() + "입니다.");
                        continue;
                    }
                    ticket.reserveTime = LogInAndTimeInput.getNowTime();
                    ticket.arrivalTime = ticket.calculateArrivalTime();
                    ticket.price = new Price(Integer.toString(ticket.calculatePrice()));
                    reserveFile.addTicket(ticket);
                    System.out.println(FORMATTER.parse(ticket.depTime) + "에 출발하는 " + ticket.line.lineNum + " 1장을 예매 확정지었습니다.");
                    reserveFile.update();
                }
                // 가예약인 경우
                else {
                    try {
                        tempTicket.line = timeTableFile.getLine(inputArr[0]);
                    } catch (FileIntegrityException e) {
                        System.out.println("잘못된 노선 번호입니다.");
                        continue;
                    }
                    tempTicket.client = LogInAndTimeInput.getClient();
                    tempTicket.railIndices = list;
                    try{
                        tempTicket.checkIntegrity();
                    }catch (FileIntegrityException e){
                        System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
                        continue;
                    }
                    // 해당 노선 정보에 해당하는 여석이 남아있지 않은 경우
                    int remainSeat = tempTicket.line.calculateSeat(list.get(0).railIndex, list.get(list.size() - 1).railIndex);
                    if(remainSeat < 1) {
                        System.out.println("해당 열차에서는 최대 0개의 좌석만 예약할 수 있습니다.");
                        continue;
                    }
                    try {
                        tempTicket.depTime = tempTicket.line.calculateDepTime(list.get(0).railIndex);
                        tempTicket.checkIntegrityAboutTime(LogInAndTimeInput.getNowTime());
                    } catch (FileIntegrityException e) {
                        System.out.println("출발시간을 확인해주세요. 현재 시각은 " + LogInAndTimeInput.getNowTime() + "입니다.");
                        continue;
                    }
                    tempTicket.reserveTime = LogInAndTimeInput.getNowTime();
                    tempTicket.arrivalTime = tempTicket.calculateArrivalTime();
                    tempTicket.price = new Price(Integer.toString(tempTicket.calculatePrice()));
                    tempTicket.setReserveComputerTime(LogInAndTimeInput.getNowComputerTime());
                    tempReserveFile.addTempTicket(tempTicket);
                    tempReserveFile.update();
                    System.out.println(FORMATTER.parse(tempTicket.depTime) + "에 출발하는 " + tempTicket.line.lineNum + " 1장을 가예약 했습니다.");
                }
                timeTableFile.reduceExtraSeat(inputArr[0], list.get(0).railIndex, list.get(list.size() - 1).railIndex, 1);
            } else if (n == 4) {
                // 중요함@@@@@@@ : 예약 개수와 실제 여석을 비교하는 무결성 검사 필수!!!!
                // 2024-06-14 추가
                // 검사 도중 인자 개수는 맞지만 입력의 형태가 다른 경우를 예외처리 하고있지 않아서 추가함
                // 입력받는 예매 수가 음수이거나 0 인 경우에 대한 예외 처리 추가
                try {
                    if(Integer.parseInt(inputArr[3]) <= 0){
                        System.out.println("예매 수가 올바르지 않습니다. 1 이상의 정수를 입력하세요.");
                        continue;
                    }
                } catch(NumberFormatException e){
                    System.out.println("잘못된 입력입니다.");
                    continue;
                }
                int numberOfReservation = Integer.parseInt(inputArr[3]);
                String[] strArr = inputArr[1].split("/");
                ArrayList<Rail> list = new ArrayList<>();
                try {
                    for (String s : strArr) {
                        // 2024-06-14 수정
                        // Rail 파일에 존재하지 않은 Rail 객체를 리턴받으려 할때 null 값이 반환되는데
                        // 해당 null 에 대한 예외처리를 여기서 해주지 않으면 다른 곳에서 NullPointerException 이 발생하므로
                        // 예외 처리를 추가
                        Rail r = timeTableFile.railFile.getRailByIndex(Integer.parseInt(s));
                        if(r == null)
                            throw new FileIntegrityException();
                        list.add(r);
                    }
                } catch (NumberFormatException e1) {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                } catch (FileIntegrityException e2){
                    System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
                    continue;
                }
                try {
                    if (!(trueList.contains(inputArr[2]) || falseList.contains(inputArr[2]))) {
                        throw new FileIntegrityException();
                    }
                } catch (FileIntegrityException e) {
                    System.out.println("잘못된 입력입니다.");
                    continue;
                }
                // 예약 확정인 경우
                if(trueList.contains(inputArr[2])) {
                    try {
                        ticket.line = timeTableFile.getLine(inputArr[0]);
                    } catch (FileIntegrityException e) {
                        System.out.println("잘못된 노선 번호입니다.");
                        continue;
                    }
                    ticket.client = LogInAndTimeInput.getClient();
                    ticket.railIndices = list;
                    try {
                        ticket.checkIntegrity();
                    } catch (FileIntegrityException e) {
                        System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
                        continue;
                    }
                    // 해당 노선 정보에 해당하는 여석이 남아있지 않은 경우
                    int remainSeat = ticket.line.calculateSeat(list.get(0).railIndex, list.get(list.size() - 1).railIndex);
                    if(remainSeat < numberOfReservation) {
                        System.out.println("해당 열차에서는 최대 "+ remainSeat + "개의 좌석만 예약할 수 있습니다.");
                        continue;
                    }
                    ticket.reserveTime = LogInAndTimeInput.getNowTime();

                    try {
                        ticket.depTime = ticket.line.calculateDepTime(list.get(0).railIndex);
                        ticket.checkIntegrityAboutTime(LogInAndTimeInput.getNowTime());
                    } catch (FileIntegrityException e) {
                        System.out.println("출발시간을 확인해주세요. 현재 시각은 " + LogInAndTimeInput.getNowTime() + "입니다.");
                        continue;
                    }
                    ticket.arrivalTime = ticket.calculateArrivalTime();
                    ticket.price = new Price(Integer.toString(ticket.calculatePrice()));
                    for (int i = 0; i < numberOfReservation; i++) {
                        reserveFile.addTicket(ticket);
                    }
                    System.out.println(FORMATTER.parse(ticket.depTime) + "에 출발하는 " + ticket.line.lineNum + " " + numberOfReservation + "장을 예매 확정지었습니다.");
                    reserveFile.update();
                } else {
                    try {
                        tempTicket.line = timeTableFile.getLine(inputArr[0]);
                    } catch (FileIntegrityException e) {
                        System.out.println("잘못된 노선 번호입니다.");
                        continue;
                    }
                    tempTicket.client = LogInAndTimeInput.getClient();
                    tempTicket.railIndices = list;
                    try{
                        tempTicket.checkIntegrity();
                    }catch (FileIntegrityException e){
                        System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
                        continue;
                    }
                    // 해당 노선 정보에 해당하는 여석이 남아있지 않은 경우
                    int remainSeat = tempTicket.line.calculateSeat(list.get(0).railIndex, list.get(list.size() - 1).railIndex);
                    if(remainSeat < numberOfReservation) {
                        System.out.println("해당 열차에서는 최대 "+ remainSeat + "개의 좌석만 예약할 수 있습니다.");
                        break;
                    }
                    try {
                        tempTicket.depTime = tempTicket.line.calculateDepTime(list.get(0).railIndex);
                        tempTicket.checkIntegrityAboutTime(LogInAndTimeInput.getNowTime());
                    } catch (FileIntegrityException e) {
                        System.out.println("출발시간을 확인해주세요. 현재 시각은 " + LogInAndTimeInput.getNowTime() + "입니다.");
                        continue;
                    }
                    tempTicket.reserveTime = LogInAndTimeInput.getNowTime();
                    tempTicket.arrivalTime = tempTicket.calculateArrivalTime();
                    tempTicket.price = new Price(Integer.toString(tempTicket.calculatePrice()));
                    tempTicket.setReserveComputerTime(LogInAndTimeInput.getNowComputerTime());
                    for (int i = 0; i < numberOfReservation; i++) {
                        tempReserveFile.addTempTicket(tempTicket);
                    }
                    System.out.println(FORMATTER.parse(tempTicket.depTime) + "에 출발하는 " + tempTicket.line.lineNum + " " + numberOfReservation + "장을 가예약 했습니다.");
                    tempReserveFile.update();
                }
                timeTableFile.reduceExtraSeat(inputArr[0], list.get(0).railIndex, list.get(list.size() - 1).railIndex, numberOfReservation);
            } else {
                System.out.println("잘못된 입력입니다.");
            }
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

            LogInAndTimeInput.setNowComputerTime(FORMATTER.format(new Date(time2)));

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
        if (ticketArrayList.isEmpty()) {
            System.out.println("입력값이 잘못되었습니다.");
            return;
        }

        System.out.println();
        int num = 0;
        for (Ticket ticket : ticketArrayList) {
            System.out.println("노선번호: " + ticket.line.lineNum);
            System.out.println("출발시각: " + ticket.depTime);
            System.out.println("출발역: " + ticket.railIndices.get(0).fromStation.getStation());
            System.out.println("도착시각: " + ticket.arrivalTime);
            System.out.println("도착역: " + ticket.railIndices.get(ticket.railIndices.size()-1).toStation.getStation());
            System.out.println();
            num++;
        }
        System.out.println("총 " + num + "개의 " + "예약 확정 되었습니다.");
    }
}
