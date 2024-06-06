import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class ReservationAndCancel {
    /**
     * repos로 파일들을 불러와서 tempReservation, Reservation 에 ArrayList 형태로 저장
     *
     * @author 변수혁
     */
    private FileTempReserve fileTempReserve;
    private FileReserve fileReserve;
    private ArrayList<TempTicket> clientTempReservationList;
    private ArrayList<Ticket> clientReservationList;
    private ArrayList<TempTicket> tempList;
    private ArrayList<Ticket> reserveList;
    ArrayList<TempTicket> confirmedTempTicketList = new ArrayList<TempTicket>();
    private FileTimeTable timeTableFile;
    private Client client = LogInAndTimeInput.getClient();

    public ReservationAndCancel(FileInterface fileTempReserve, FileInterface fileReserve, FileInterface timeTableFile) {
        this.fileReserve = (FileReserve) fileReserve;
        this.fileTempReserve = (FileTempReserve) fileTempReserve;
        this.timeTableFile = (FileTimeTable) timeTableFile;
    }

    public void init() throws IOException {
        while (true) {
            System.out.println("가예약 확정 또는 예약취소를 입력해주세요: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equals("가예약 확정")) {
                init1();
                break;
            } else if (input.equals("예약취소")) {
                init2();
                break;
            } else if (input.equals("Q")) {
                System.out.println("메인 프롬프트로 돌아갑니다");
                return;
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    public void init1() throws IOException {
        fileTempReserve.repos();
        fileReserve.repos();

        clientTempReservationList = fileTempReserve.getTempTicketListByClient(client);
        tempList = fileTempReserve.getTempList();
        reserveList = fileReserve.getReserveList();

        if (fileTempReserve.getTempList().isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        System.out.println(client.getPhoneNumber() + "/" + client.getName() + " 고객님의 예약정보입니다.");
        System.out.println("행 번호 / 노선 번호 / 출발 시각 / 출발 역 / 도착 시간 / 도착 역");

        int rowNum = 0;
        for (TempTicket tempTicket : clientTempReservationList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            LocalDateTime departureTime = LocalDateTime.parse(tempTicket.depTime, formatter);
            LocalDateTime reserveTime = LocalDateTime.parse(tempTicket.getReserveTime(), formatter);

            if (departureTime.isAfter(LocalDateTime.now())) {
                long minutesBetween = Duration.between(LocalDateTime.now(), reserveTime).toMinutes();

                System.out.print("#" + rowNum + " / ");
                System.out.print(tempTicket.line.lineNum + " / ");
                System.out.print(tempTicket.depTime + " / ");
                System.out.print(tempTicket.railIndices.get(0).fromStation + " / ");
                System.out.print(tempTicket.arrivalTime + " / ");
                System.out.print(tempTicket.railIndices.get(tempTicket.railIndices.size() - 1).toStation);
                System.out.println();
                if (minutesBetween > 20) {
                    System.out.println("- 20분이 지나 삭제되었습니다.");
                    clientTempReservationList.remove(rowNum);
                    tempList.remove(tempTicket);
                    fileTempReserve.update();
                }
                System.out.println();
                rowNum++;
            }
        }

        if (clientTempReservationList.isEmpty()) {
            System.out.println("가예약된 기차표의 출발시각이 이미 지났으므로 자동 삭제되었습니다.");
            return;
        }

        int flag = 1;
        do {
            System.out.print("확정할 가예약을 입력하세요: ");
            Scanner inputScan = new Scanner(System.in);
            String[] inputArr = inputScan.nextLine().split(",");

            fileTempReserve.repos();
            fileReserve.repos();

            switch (inputArr.length) {
                case 1:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = rowIndicesHandle(inputArr);
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        clientTempReservationList = fileTempReserve.getTempTicketListByLineNum(inputArr[0], client);
                        for (TempTicket tempTicket : clientTempReservationList) {
                            reserveList.add(tempTicket);
                            confirmedTempTicketList.add(tempTicket);
                            tempList.remove(tempTicket);
                        }
                    }
                    break;

                case 2:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = rowIndicesHandle(inputArr);
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        clientTempReservationList = fileTempReserve.getTempTicketListByLineNum(inputArr[0], client);
                        int num = Integer.parseInt(inputArr[1]);

                        if (num > clientTempReservationList.size()) {
                            System.out.println("입력하신 표의 개수가 많습니다. 가예약하신 표의 개수만큼 입력해주세요.");
                            flag = -1;
                        }

                        for (int i = 0; i < num; i++) {
                            reserveList.add(clientTempReservationList.get(i));
                            confirmedTempTicketList.add(clientTempReservationList.get(i));
                            tempList.remove(clientTempReservationList.get(i));
                        }
                    } else if (inputArr[1].equals("출발")) {
                        try {
                            Station fromStation = new Station(inputArr[0]);
                            clientTempReservationList = fileTempReserve.getTempTicketListByfromStation(fromStation, client);

                            for (TempTicket tempTicket : clientTempReservationList) {
                                reserveList.add(tempTicket);
                                confirmedTempTicketList.add(tempTicket);
                                tempList.remove(tempTicket);
                            }
                        } catch (FileIntegrityException e) {
                            e.printStackTrace();
                            // 출발역 잘못 입력 시 에러 메시지 출력
                            flag = -1;
                        }
                    } else if (inputArr[1].equals("도착")) {
                        try {
                            Station toStation = new Station(inputArr[0]);
                            clientTempReservationList = fileTempReserve.getTempTicketListBytoStation(toStation, client);

                            for (TempTicket tempTicket : clientTempReservationList) {
                                reserveList.add(tempTicket);
                                confirmedTempTicketList.add(tempTicket);
                                tempList.remove(tempTicket);
                            }
                        } catch (FileIntegrityException e) {
                            e.printStackTrace();
                            // 도착역 잘못 입력 시 에러 메시지 출력
                            flag = -1;
                        }
                    }
                    break;

                case 3:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = rowIndicesHandle(inputArr);
                    }
                    break;

                case 4:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = rowIndicesHandle(inputArr);
                    } else if (inputArr[1].equals("출발")) {
                        try {
                            Station fromStation = new Station(inputArr[0]);
                            Station toStation = new Station(inputArr[2]);
                            clientTempReservationList = fileTempReserve.getTempTicketListByStation(fromStation, toStation, client);

                            for (TempTicket tempTicket : clientTempReservationList) {
                                reserveList.add(tempTicket);
                                confirmedTempTicketList.add(tempTicket);
                                tempList.remove(tempTicket);
                            }
                        } catch (FileIntegrityException e) {
                            e.printStackTrace();
                            // 출발역 도착역 잘못 입력 시 에러 메시지 출력
                            flag = -1;
                        }
                    }
                    break;

                default:
                    flag = rowIndicesHandle(inputArr);
            }

            fileTempReserve.update();
            fileReserve.update();

            for (TempTicket tempTicket : confirmedTempTicketList) {
                System.out.println(tempTicket.toString());
            }
        } while (flag == -1);
    }

    public void init2() {
        fileTempReserve.repos();
        fileReserve.repos();
        Client client = LogInAndTimeInput.getClient();
        this.clientName = client.getName();
        this.clientPhoneNumber = client.getPhoneNumber();
        //private FileTimeTable fileTimeTable;
        ArrayList<String[]> tempReservationList = new ArrayList<>(readCsv(fileTempReserve.getFileName(), 6)); // 파일 형식에 따라 개수 수정 필요
        ArrayList<String[]> reservationList = new ArrayList<>(readCsv(fileReserve.getFileName(), 4)); // 파일 형식에 따라 개수 수정 필요
        //client 객체가 예약한 것만 어레이 리스트에 저장
        clientReservationList = makeClientReserveList(reservationList, clientPhoneNumber);
        clientTempReservationList = makeClientReserveList(tempReservationList, clientPhoneNumber);
        System.out.println("가예약 및 예약 취소 메뉴입니다.");
        //Client 객체 생성 및 출력(loginAndTimeInput에서 받아옴)


        if (!clientTempReservationList.isEmpty()) {
            System.out.println(clientName + " / " + clientPhoneNumber + "님의 가예약 정보입니다.");
            System.out.println("행 번호 / 노선 번호 / 출발 시간 / 출발 역 / 도착 시간 / 도착 역");
            printReserveList(clientTempReservationList);
            ArrayList<Ticket> tempCancelTicketList = new ArrayList<>();
            tempCancelTicketList = makeTempCancelList();
            printCancelInfo(tempCancelTicketList);

        }
        if (!clientReservationList.isEmpty()) {
            System.out.println(clientName + " / " + clientPhoneNumber + "님의 예약 정보입니다.");
            System.out.println("행 번호 / 노선 번호 / 출발 시간 / 출발 역 / 도착 시간 / 도착 역");
            printReserveList(clientReservationList);
            ArrayList<Ticket> CancelTicketList = new ArrayList<>();
            CancelTicketList = makeCancelList();
            printCancelInfo(CancelTicketList);
        }
    }


    public ArrayList<String[]> makeClientReserveList(ArrayList<String[]> reservationList, String clientPhoneNumber) {
        ArrayList<String[]> tempList = new ArrayList<>();
        for (String[] row : reservationList) {
            if (row[1].equals(clientPhoneNumber)) {
                tempList.add(row); // Add matching rows to tempList
            }
        }
        return tempList; // Return the tempList containing client's reservation list
    }


    public void printReserveList(ArrayList<String[]> reservationList) {
        if (reservationList.isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        System.out.println(this.clientName + "님이 예약한 열차 목록:");
        int i = 1;

        for (String[] row : reservationList) {
            String trainNum = row[2];
            Ticket ticket = timeTableFile.getTicket(trainNum);
            String ticketInfo = ticket.lineNum + " " + ticket.depTime + " " + ticket.fromStation.getStation() + " " + ticket.arrivalTime + " " + ticket.toStation.getStation();
            System.out.println("#" + (i++) + " " + ticketInfo);
        }
    }


    // 각 줄마다 String배열에 저장후 각 배열을 ArrayList에 저장
    public List<String[]> readCsv(String csvFile, int rowLength) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            if (br.ready()) { // BOM 제거 과정
                br.mark(1);
                if (br.read() != 0xFEFF) {
                    br.reset(); // BOM 없음
                }
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                if (row.length == rowLength) { // 파일 형식에 따라 개수 수정 필요
                    data.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public int calcCancelFee(String departureTime, String arrivalTime, int ticketPrice) {

        String nowTime = LogInAndTimeInput.getNowTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime depTime = LocalDateTime.parse(departureTime, formatter);
        LocalDateTime arrTime = LocalDateTime.parse(arrivalTime, formatter);
        LocalDateTime currentTime = LocalDateTime.parse(nowTime, formatter);

        long minutesDifference = java.time.Duration.between(currentTime, depTime).toMinutes();
        long arrTimeIscurrentTime = java.time.Duration.between(currentTime, arrTime).toMinutes();

        if (minutesDifference >= 1440) { // 1개월 전 ~ 출발 1일 전
            return 0;
        } else if (minutesDifference > 180) { // 당일 ~ 출발 3시간 전
            return (int) Math.round(0.05 * ticketPrice);
        } else if (minutesDifference >= 0) { // 출발 3시간 이내
            return (int) Math.round(0.1 * ticketPrice);
        } else if (minutesDifference >= -20) { // 출발 20분 이후 ~ 출발 60분 미만
            return (int) Math.round(0.15 * ticketPrice);
        } else if (minutesDifference >= -60) { // 출발 60분 경과 후 ~ 출발 3시간 이내
            return (int) Math.round(0.4 * ticketPrice);
        } else if (arrTimeIscurrentTime >= 0) { // 출발 3시간 경과 후 ~ 도착
            return (int) Math.round(0.7 * ticketPrice);
        } else {
            return ticketPrice;
        }
    }


    public void printCancelInfo(ArrayList<Ticket> ticketList) {
        for (Ticket ticket : ticketList) {
            System.out.println("취소 열차 정보:");
            System.out.println("노선 번호: " + ticket.lineNum);
            System.out.println("출발 시각: " + ticket.depTime);
            System.out.println("출발 역: " + ticket.fromStation);
            System.out.println("도착 시각: " + ticket.arrivalTime);
            System.out.println("도착 역: " + ticket.toStation);
            System.out.println("취소 수수료: " + calcCancelFee(ticket.depTime, ticket.arrivalTime, Integer.parseInt(ticket.price.getPrice())) + "원");
        }
    }

    // 취소 입력 구현


    public ArrayList<Ticket> makeTempCancelList() {
        System.out.println("RSVD Cancel: ");
        Scanner inputScan = new Scanner(System.in);
        String[] inputArr = inputScan.nextLine().split(",");
        inputScan.close();

        ArrayList<Ticket> cancelTicketArrayList = new ArrayList<>();

        switch (inputArr.length) {
            case 1:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    int index = Integer.parseInt(inputArr[0].replace("#", "")) - 1;

                    if (index >= 0 && index < clientTempReservationList.size()) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(clientTempReservationList.get(index)[2]));
                        removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2], 1);
                        clientTempReservationList.remove(index);

                    }
                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int index = 0;
                    while (index != -1) {
                        index = fileTempReserve.findByLineNum(clientName, inputArr[0]);
                        cancelTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                        clientTempReservationList.remove(index);
                    }

                    removeRowsByTrainNumber(fileTempReserve.getFileName(), inputArr[0]);
                }
                break;

            case 2:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    for (int i = 0; i < 2; i++) {
                        //csv에서 항목 삭제
                        //clientReservation에서 항목 삭제
                        //cancelTicketArrayList에 추가
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < clientTempReservationList.size()) {
                            cancelTicketArrayList.add(timeTableFile.getTicket(clientTempReservationList.get(index)[2]));
                        }
                        removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2], 1);
                    }

                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int num = Integer.parseInt(inputArr[1]);
                    int num2 = 0;
                    for (String[] tempReserve : clientTempReservationList) {
                        if (inputArr[0].equals(tempReserve[2]))
                            num2++;
                    }

                    if (num > num2) {
                        System.out.println("입력하신 표의 개수가 많습니다. 가예약하신 표의 개수만큼 입력해주세요.");
                        break;
                    }
                    for (int i = 0; i < num; i++) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                    }
                    removeRowsByTrainNumber(fileTempReserve.getFileName(), inputArr[0], num);

                } else if (inputArr[1].equals("출발")) {
                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepStation(inputArr[0] + "역");
                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = fileTempReserve.findByLineNum(clientName, ticket.lineNum);
                        if (index != -1) {
                            cancelTicketArrayList.add(ticket);
                        }
                    }
                    removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2]);

                } else if (inputArr[1].equals("도착")) {
                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepStation(inputArr[0] + "역");
                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = fileTempReserve.findByLineNum(clientName, ticket.lineNum);
                        if (index != -1) {
                            cancelTicketArrayList.add(ticket);
                        }
                    }
                    removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2]);
                }
                break;

            case 3:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {

                    for (int i = 0; i < 3; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < clientTempReservationList.size()) {
                            cancelTicketArrayList.add(timeTableFile.getTicket(clientTempReservationList.get(index)[2]));
                        }
                        removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2], 1);
                    }
                }
                break;

            case 4:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {

                    for (int i = 0; i < 4; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < clientTempReservationList.size()) {
                            cancelTicketArrayList.add(timeTableFile.getTicket(clientTempReservationList.get(index)[2]));
                        }
                        removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2], 1);
                    }
                } else if (inputArr[1].equals("출발")) {

                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepArrStation(inputArr[0] + "역", inputArr[2] + "역");

                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = fileTempReserve.findByLineNum(clientName, ticket.lineNum);
                        if (index != -1) {
                            cancelTicketArrayList.add(ticket);
                        }
                    }
                    removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2]);
                }
                break;
        }
        return cancelTicketArrayList;
    }

    public ArrayList<Ticket> makeCancelList() {
        System.out.println("RSVD Cancel: ");
        Scanner inputScan = new Scanner(System.in);
        String[] inputArr = inputScan.nextLine().split(",");
        inputScan.close();

        ArrayList<Ticket> cancelTicketArrayList = new ArrayList<>();

        switch (inputArr.length) {
            case 1:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    int index = Integer.parseInt(inputArr[0].replace("#", "")) - 1;

                    if (index >= 0 && index < clientReservationList.size()) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(clientReservationList.get(index)[2]));
                        removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2], 1);
                        clientReservationList.remove(index);

                    }
                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int index = 0;
                    while (index != -1) {
                        index = fileReserve.findByLineNum(clientName, inputArr[0]);
                        cancelTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                        clientReservationList.remove(index);
                    }

                    removeRowsByTrainNumber(fileReserve.getFileName(), inputArr[0]);
                }
                break;

            case 2:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    for (int i = 0; i < 2; i++) {
                        //csv에서 항목 삭제
                        //clientReservation에서 항목 삭제
                        //cancelTicketArrayList에 추가
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < clientReservationList.size()) {
                            cancelTicketArrayList.add(timeTableFile.getTicket(clientReservationList.get(index)[2]));
                        }
                        removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2], 1);
                    }

                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int num = Integer.parseInt(inputArr[1]);
                    int num2 = 0;
                    for (String[] Reserve : clientReservationList) {
                        if (inputArr[0].equals(Reserve[2]))
                            num2++;
                    }

                    if (num > num2) {
                        System.out.println("입력하신 표의 개수가 많습니다. 가예약하신 표의 개수만큼 입력해주세요.");
                        break;
                    }
                    for (int i = 0; i < num; i++) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                    }
                    removeRowsByTrainNumber(fileReserve.getFileName(), inputArr[0], num);

                } else if (inputArr[1].equals("출발")) {
                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepStation(inputArr[0] + "역");
                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = fileReserve.findByLineNum(clientName, ticket.lineNum);
                        if (index != -1) {
                            cancelTicketArrayList.add(ticket);
                        }
                    }
                    removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2]);

                } else if (inputArr[1].equals("도착")) {
                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepStation(inputArr[0] + "역");
                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = fileReserve.findByLineNum(clientName, ticket.lineNum);
                        if (index != -1) {
                            cancelTicketArrayList.add(ticket);
                        }
                    }
                    removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2]);
                }
                break;

            case 3:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {

                    for (int i = 0; i < 3; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < clientReservationList.size()) {
                            cancelTicketArrayList.add(timeTableFile.getTicket(clientReservationList.get(index)[2]));
                        }
                        removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2], 1);
                    }
                }
                break;

            case 4:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {

                    for (int i = 0; i < 4; i++) {
                        int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                        if (index >= 0 && index < clientReservationList.size()) {
                            cancelTicketArrayList.add(timeTableFile.getTicket(clientReservationList.get(index)[2]));
                        }
                        removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2], 1);
                    }
                } else if (inputArr[1].equals("출발")) {

                    ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepArrStation(inputArr[0] + "역", inputArr[2] + "역");

                    int index = -1;
                    for (Ticket ticket : ticketArrayList) {
                        index = fileReserve.findByLineNum(clientName, ticket.lineNum);
                        if (index != -1) {
                            cancelTicketArrayList.add(ticket);
                        }
                    }
                    removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2]);
                }
                break;
        }
        return cancelTicketArrayList;
    }


    public void removeRowsByTrainNumber(String csvFilePath, String trainNumber) {
        removeRowsByTrainNumber(csvFilePath, trainNumber, Integer.MAX_VALUE);
    }

    public void removeRowsByTrainNumber(String csvFilePath, String trainNumber, int count) {
        List<String[]> matchingRows = new ArrayList<>();
        int removedCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                if (row[2].equals(trainNumber)) {
                    matchingRows.add(row);
                    removedCount++;
                    if (removedCount == count) {
                        break; // 지정된 개수만큼 삭제하면 종료
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 파일 재작성: 매칭되는 행을 제외한 나머지 행을 파일에 씀
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {
            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] row = line.split(",");
                    boolean found = false;
                    for (String[] matchingRow : matchingRows) {
                        if (row[2].equals(matchingRow[2])) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //열차 취소시 csv파일에서 삭제

    public static void clearCSVContent(String fileName) {
        try {
            // FileWriter 객체를 생성할 때, 두 번째 매개변수로 false를 주어 파일의 내용을 덮어쓰게 함
            FileWriter fw = new FileWriter(fileName, false);

            // 파일을 빈 내용으로 덮어쓰기
            fw.write("");

            // FileWriter 닫기
            fw.close();

            System.out.println(fileName + "의 내용이 성공적으로 지워졌습니다.");
        } catch (IOException e) {
            // IO 예외 처리
            e.printStackTrace();
            System.out.println(fileName + "의 내용을 지우는 데 실패했습니다.");
        }
    }

    private int rowIndicesHandle(String[] inputArr) {
        int arrLength = inputArr.length;
        boolean patternFlag = true;
        for (int i = 0; i < arrLength; i++) {
            if (!(Pattern.matches("^\\#[1-9]$", inputArr[i])))
                patternFlag = false;
        }

        if (!patternFlag) {
            return -1;
        }

        for (int i = 0; i < arrLength; i++) {
            int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

            if (index >= 0 && index < clientTempReservationList.size()) {
                reserveList.add(clientTempReservationList.get(i));
                confirmedTempTicketList.add(clientTempReservationList.get(i));
                tempList.remove(clientTempReservationList.get(i));
            }
        }

        return 1;
    }
}