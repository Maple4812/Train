import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ReservationAndCancel {
    /**
     * repos로 파일들을 불러와서 tempReservation, Reservation 에 ArrayList 형태로 저장
     *
     * @author 변수혁
     */
    private final FileTempReserve fileTempReserve;
    private final FileReserve fileReserve;
    private final FileTimeTable timeTableFile;
    ArrayList<TempTicket> confirmedTempTicketList = new ArrayList<>();
    ArrayList<TempTicket> cancelTempTicketList = new ArrayList<>();
    ArrayList<Ticket> cancelTicketList = new ArrayList<>();
    private Client client;
    private ArrayList<TempTicket> clientTempReservationList;
    private ArrayList<Ticket> clientReservationList;
    private ArrayList<TempTicket> tempList;
    private ArrayList<Ticket> reserveList;

    public ReservationAndCancel(FileInterface fileTempReserve, FileInterface fileReserve, FileInterface timeTableFile) {
        this.fileReserve = (FileReserve) fileReserve;
        this.fileTempReserve = (FileTempReserve) fileTempReserve;
        this.timeTableFile = (FileTimeTable) timeTableFile;
        this.reserveList = ((FileReserve) fileReserve).getReserveList();
    }

    public void init() throws IOException {
        label:
        while (true) {
            System.out.println("가예약 확정 또는 예약취소를 입력해주세요: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            switch (input) {
                case "가예약 확정":
                    init1();
                    break label;
                case "예약취소":
                    init2();
                    break label;
                case "Q":
                    System.out.println("메인 프롬프트로 돌아갑니다");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
                    break;

            }
        }
    }

    public void init1() throws IOException {
        client = LogInAndTimeInput.getClient();
        fileTempReserve.repos();
        fileReserve.repos();

        clientTempReservationList = fileTempReserve.getTempTicketListByClient(client);
        tempList = fileTempReserve.getTempList();
        reserveList = fileReserve.getReserveList();

        if (fileTempReserve.getTempList().isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        printClientInfo();

        int rowNum = 0;
        for (TempTicket tempTicket : clientTempReservationList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            LocalDateTime departureTime = LocalDateTime.parse(tempTicket.depTime, formatter);
            LocalDateTime reserveTime = LocalDateTime.parse(tempTicket.getReserveTime(), formatter);
            LocalDateTime nowTime = LocalDateTime.parse(LogInAndTimeInput.getNowTime(), formatter);

            if (departureTime.isAfter(nowTime)) {
                long minutesBetween = Duration.between(LocalDateTime.now(), reserveTime).toMinutes();

                System.out.print("#" + (rowNum + 1) + " / ");
                System.out.print(tempTicket.line.lineNum + " / ");
                System.out.print(tempTicket.depTime + " / ");
                System.out.print(tempTicket.railIndices.get(0).fromStation.getStation() + " / ");
                System.out.print(tempTicket.calculateArrivalTime() + " / ");
                System.out.print(tempTicket.railIndices.get(tempTicket.railIndices.size() - 1).toStation.getStation());
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

            fileTempReserve.tempList = (ArrayList<TempTicket>) tempList.clone();
            fileReserve.reserveList = (ArrayList<Ticket>) reserveList.clone();
            fileTempReserve.update();
            fileReserve.update();

            for (TempTicket tempTicket : confirmedTempTicketList) {
                System.out.println(tempTicket.toString());
            }
        } while (flag == -1);
    }


    /**
     * <p>
     * 이 함수에서는 다음과 같은 역할을 순차적으로 수행합니다.<br>
     * 1. init2 함수에서 가예약취소, 예약취소를 선택할 수 있습니다.<br>
     * 2. 가예약 취소의 경우 {@code tempReserveCancel()} 함수 내부에서 진행삽니다.<br>
     * 3. 예약 취소의 경우 {@code ReserveCancel()} 함수 내부에서 진행됩니다.
     * </p>
     *
     * @author 변수혁
     */


    public void init2() {
        client = LogInAndTimeInput.getClient();
        System.out.println("가예약취소/예약취소를 선택하세요.");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        switch (input) {
            case "가예약취소":
                tempReserveCancel();
                break;
            case "예약취소":
                ReserveCancel();
                break;
            default:
                System.out.println("잘못된 입력입니다.");
        }
    }

    public void tempReserveCancel() {
        refreshFileData();

        //가예약 목록만 불러오기
        clientReservationList = fileReserve.getTicketListByClient(client);
        tempList = fileTempReserve.getTempList();

        //가에약 목록 없는 경우 별도 처리
        if (fileTempReserve.getTempList().isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        printClientInfo();
        removeExpiredTempTicket();

        if (clientTempReservationList.isEmpty()) {
            System.out.println("가예약된 기차표의 출발시각이 이미 지났으므로 자동 삭제되었습니다.");
            return;
        }

        int flag = 1;
        do {
            System.out.print("취소할 가예약을 입력하세요: ");
            Scanner inputScan = new Scanner(System.in);
            String[] inputArr = inputScan.nextLine().split(",");

            refreshFileData();

            switch (inputArr.length) {
                case 1:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = removeTempTicketByRowNum(inputArr);
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        clientTempReservationList = fileTempReserve.getTempTicketListByLineNum(inputArr[0], client);
                        for (TempTicket tempTicket : clientTempReservationList) {
                            try {
                                timeTableFile.increaseExtraSeat(inputArr[0], tempTicket.getFirstRailofTicket(), tempTicket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                            } catch (IOException | FileIntegrityException e) {
                                throw new RuntimeException(e);
                            }
                            cancelTempTicketList.add(tempTicket);
                            tempList.remove(tempTicket);
                        }
                    }
                    break;

                case 2:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = removeTempTicketByRowNum(inputArr, 2);
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        clientTempReservationList = fileTempReserve.getTempTicketListByLineNum(inputArr[0], client);
                        int num = Integer.parseInt(inputArr[1]);

                        if (num > clientTempReservationList.size()) {
                            System.out.println("입력하신 표의 개수가 많습니다. 가예약하신 표의 개수만큼 입력해주세요.");
                            flag = -1;
                        }

                        for (TempTicket tempTicket : clientTempReservationList) {
                            try {
                                //열차번호 일치하는 것 만 좌석 증가
                                //찾으면 바로 break
                                if (tempTicket.getLineNum().equals(inputArr[0])) {
                                    timeTableFile.increaseExtraSeat(inputArr[0], tempTicket.getFirstRailofTicket(), tempTicket.getLastRailofTicket(), num);
                                    break;
                                }
                            } catch (IOException | FileIntegrityException e) {
                                throw new RuntimeException(e);
                            }

                        }

                        for (int i = 0; i < num; i++) {
                            cancelTempTicketList.add(clientTempReservationList.get(i));
                            tempList.remove(clientTempReservationList.get(i));
                        }
                    } else if (inputArr[1].equals("출발")) {
                        try {
                            Station fromStation = new Station(inputArr[0]);
                            clientTempReservationList = fileTempReserve.getTempTicketListByfromStation(fromStation, client);

                            for (TempTicket tempTicket : clientTempReservationList) {
                                try {
                                    timeTableFile.increaseExtraSeat(inputArr[0], tempTicket.getFirstRailofTicket(), tempTicket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                                } catch (IOException | FileIntegrityException e) {
                                    throw new RuntimeException(e);
                                }
                                cancelTempTicketList.add(tempTicket);
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
                                try {
                                    timeTableFile.increaseExtraSeat(inputArr[0], tempTicket.getFirstRailofTicket(), tempTicket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                                } catch (IOException | FileIntegrityException e) {
                                    throw new RuntimeException(e);
                                }
                                cancelTempTicketList.add(tempTicket);
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
                        flag = removeTempTicketByRowNum(inputArr, 3);
                    }
                    break;

                case 4:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = removeTempTicketByRowNum(inputArr, 4);
                    } else if (inputArr[1].equals("출발")) {
                        try {
                            Station fromStation = new Station(inputArr[0]);
                            Station toStation = new Station(inputArr[2]);
                            clientTempReservationList = fileTempReserve.getTempTicketListByStation(fromStation, toStation, client);

                            for (TempTicket tempTicket : clientTempReservationList) {
                                try {
                                    timeTableFile.increaseExtraSeat(inputArr[0], tempTicket.getFirstRailofTicket(), tempTicket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                                } catch (IOException | FileIntegrityException e) {
                                    throw new RuntimeException(e);
                                }
                                cancelTempTicketList.add(tempTicket);
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
                    flag = removeTempTicketByRowNum(inputArr);
            }
            refreshFileData();

            for (TempTicket tempTicket : cancelTempTicketList) {
                System.out.println(tempTicket.toString());
            }
        } while (flag == -1);


    }


    public void ReserveCancel() {
        refreshFileData();

        clientReservationList = fileReserve.getTicketListByClient(client);
        reserveList = fileReserve.getReserveList();

        if (fileReserve.getReserveList().isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        printClientInfo();


        int rowNum = 0;
        for (Ticket ticket : clientReservationList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            LocalDateTime departureTime = LocalDateTime.parse(ticket.depTime, formatter);
            printTicketInfo(rowNum, ticket);
            rowNum++;
        }

        if (clientReservationList.isEmpty()) {
            System.out.println("예약된 기차표가 없습니다.");
            return;
        }

        int flag = 1;
        do {
            System.out.print("취소할 예약을 입력하세요: ");
            Scanner inputScan = new Scanner(System.in);
            String[] inputArr = inputScan.nextLine().split(",");

            fileTempReserve.repos();
            fileReserve.repos();

            switch (inputArr.length) {
                case 1:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = removeTicketByRowNum(inputArr);
                        //좌석수 추가
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        clientReservationList = fileReserve.getTicketListByLineNum(inputArr[0], client);
                        for (Ticket ticket : clientReservationList) {
                            try {
                                timeTableFile.increaseExtraSeat(inputArr[0], ticket.getFirstRailofTicket(), ticket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                            } catch (IOException | FileIntegrityException e) {
                                throw new RuntimeException(e);
                            }
                            cancelTicketList.add(ticket);
                            reserveList.remove(ticket);
                        }
                    }
                    break;

                case 2:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = removeTicketByRowNum(inputArr, 2);
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        clientReservationList = fileReserve.getTicketListByLineNum(inputArr[0], client);
                        int num = Integer.parseInt(inputArr[1]);

                        if (num > clientReservationList.size()) {
                            System.out.println("입력하신 표의 개수가 많습니다. 가예약하신 표의 개수만큼 입력해주세요.");
                            flag = -1;
                        }

                        for (Ticket ticket : clientReservationList) {
                            try {
                                //열차번호 일치하는 것 만 좌석 증가
                                //찾으면 바로 break
                                if (ticket.getLineNum().equals(inputArr[0])) {
                                    timeTableFile.increaseExtraSeat(inputArr[0], ticket.getFirstRailofTicket(), ticket.getLastRailofTicket(), num);
                                    break;
                                }
                            } catch (IOException | FileIntegrityException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        //좌석 삭제 작업 수행
                        for (int i = 0; i < num; i++) {
                            cancelTicketList.add(clientReservationList.get(i));
                            reserveList.remove(clientReservationList.get(i));
                        }


                    } else if (inputArr[1].equals("출발")) {
                        try {
                            Station fromStation = new Station(inputArr[0]);
                            clientReservationList = fileReserve.getTicketListByfromStation(fromStation, client);

                            for (Ticket ticket : clientReservationList) {
                                try {
                                    timeTableFile.increaseExtraSeat(inputArr[0], ticket.getFirstRailofTicket(), ticket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                                } catch (IOException | FileIntegrityException e) {
                                    throw new RuntimeException(e);
                                }
                                cancelTicketList.add(ticket);
                                reserveList.remove(ticket);
                            }
                        } catch (FileIntegrityException e) {
                            e.printStackTrace();
                            // 출발역 잘못 입력 시 에러 메시지 출력
                            flag = -1;
                        }
                    } else if (inputArr[1].equals("도착")) {
                        try {
                            Station toStation = new Station(inputArr[0]);
                            clientReservationList = fileReserve.getTicketListBytoStation(toStation, client);

                            for (Ticket ticket : clientReservationList) {
                                try {
                                    timeTableFile.increaseExtraSeat(inputArr[0], ticket.getFirstRailofTicket(), ticket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                                } catch (IOException | FileIntegrityException e) {
                                    throw new RuntimeException(e);
                                }
                                cancelTicketList.add(ticket);
                                reserveList.remove(ticket);
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
                        flag = removeTicketByRowNum(inputArr, 3);
                    }
                    break;

                case 4:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        flag = removeTicketByRowNum(inputArr, 4);
                    } else if (inputArr[1].equals("출발")) {
                        try {
                            Station fromStation = new Station(inputArr[0]);
                            Station toStation = new Station(inputArr[2]);
                            clientReservationList = fileReserve.getTicketListByStation(fromStation, toStation, client);

                            for (Ticket ticket : clientReservationList) {
                                try {
                                    timeTableFile.increaseExtraSeat(inputArr[0], ticket.getFirstRailofTicket(), ticket.getLastRailofTicket(), 1); //이거 맞는지 모르겠음요..
                                } catch (IOException | FileIntegrityException e) {
                                    throw new RuntimeException(e);
                                }
                                cancelTicketList.add(ticket);
                                reserveList.remove(ticket);
                            }
                        } catch (FileIntegrityException e) {
                            e.printStackTrace();
                            // 출발역 도착역 잘못 입력 시 에러 메시지 출력
                            flag = -1;
                        }
                    }
                    break;

                default:
                    flag = removeTicketByRowNum(inputArr);
            }

            //fileReserve.reserveList = (ArrayList<Ticket>) reserveList.clone();
            fileTempReserve.update();
            fileReserve.update();


            for (Ticket ticket : cancelTicketList) {
                System.out.println(ticket.toString());
                System.out.println("취소 수수료:" + calcCancelFee(ticket.depTime, ticket.arrivalTime, ticket.calculatePrice()));
                System.out.println("-".repeat(20));
            }


        } while (flag == -1);

    }


    public void refreshFileData() {
        fileTempReserve.repos();
        fileReserve.repos();
    }

    private void printClientInfo() {
        System.out.println(client.getPhoneNumber() + "/" + client.getName() + " 고객님의 예약정보입니다.");
        System.out.println("행 번호 / 노선 번호 / 출발 시각 / 출발 역 / 도착 시간 / 도착 역");
    }

    //printTicketInfo 오버로딩
    private void printTempTicketInfo(int rowNum, TempTicket tempTicket) {
        printTicketInfo(rowNum, tempTicket);
    }

    private void printTicketInfo(int rowNum, Ticket Ticket) {
        System.out.print("#" + rowNum + " / ");
        System.out.print(Ticket.line.lineNum + " / ");
        System.out.print(Ticket.depTime + " / ");
        System.out.print(Ticket.railIndices.get(0).fromStation.getStation() + " / ");
        System.out.print(Ticket.arrivalTime + " / ");
        System.out.print(Ticket.railIndices.get(Ticket.railIndices.size() - 1).toStation.getStation());
        System.out.println();

    }


    private void removeExpiredTempTicket() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        int rowNum = 0;
        for (TempTicket tempTicket : clientTempReservationList) {
            LocalDateTime departureTime = LocalDateTime.parse(tempTicket.depTime, formatter);
            LocalDateTime reserveTime = LocalDateTime.parse(tempTicket.getReserveTime(), formatter);

            if (departureTime.isAfter(LocalDateTime.now())) {
                long minutesBetween = Duration.between(LocalDateTime.now(), reserveTime).toMinutes();

                printTempTicketInfo(rowNum, tempTicket);
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
    }


    //legacyCode
//    public void removeRowsByTrainNumber(String csvFilePath, String trainNumber) {
//        removeRowsByTrainNumber(csvFilePath, trainNumber, Integer.MAX_VALUE);
//    }
//
//    public void removeRowsByTrainNumber(String csvFilePath, String trainNumber, int count) {
//        List<String[]> matchingRows = new ArrayList<>();
//        int removedCount = 0;
//
//        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] row = line.split(",");
//                if (row[2].equals(trainNumber)) {
//                    matchingRows.add(row);
//                    removedCount++;
//                    if (removedCount == count) {
//                        break; // 지정된 개수만큼 삭제하면 종료
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        // 파일 재작성: 매칭되는 행을 제외한 나머지 행을 파일에 씀
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {
//            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    String[] row = line.split(",");
//                    boolean found = false;
//                    for (String[] matchingRow : matchingRows) {
//                        if (row[2].equals(matchingRow[2])) {
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (!found) {
//                        bw.write(line);
//                        bw.newLine();
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    //열차 취소시 csv파일에서 삭제

//    public static void clearCSVContent(String fileName) {
//        try {
//            // FileWriter 객체를 생성할 때, 두 번째 매개변수로 false를 주어 파일의 내용을 덮어쓰게 함
//            FileWriter fw = new FileWriter(fileName, false);
//
//            // 파일을 빈 내용으로 덮어쓰기
//            fw.write("");
//
//            // FileWriter 닫기
//            fw.close();
//
//            System.out.println(fileName + "의 내용이 성공적으로 지워졌습니다.");
//        } catch (IOException e) {
//            // IO 예외 처리
//            e.printStackTrace();
//            System.out.println(fileName + "의 내용을 지우는 데 실패했습니다.");
//        }
//    }

    private int rowIndicesHandle(String[] inputArr) {
        int arrLength = inputArr.length;
        boolean patternFlag = true;
        for (int i = 0; i < arrLength; i++) {
            if (!(Pattern.matches("^\\#[1-9]$", inputArr[i]))) patternFlag = false;
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

    //rowIndicesHandle 과 비슷한 역할을 하나 오버라이딩 하기 애매해서 별도로 함수 구현
    private int removeTempTicketByRowNum(String[] inputArr, int seatCount) {
        int arrLength = inputArr.length;
        boolean patternFlag = true;
        for (String string : inputArr) {
            if (!(Pattern.matches("^\\#[1-9]$", string))) patternFlag = false;
        }

        if (!patternFlag) {
            return -1;
        }

        for (String s : inputArr) {
            int index = Integer.parseInt(s.replace("#", "")) - 1;

            if (index >= 0 && index < clientTempReservationList.size()) {
                var reserve = clientReservationList.get(index);
                try {
                    timeTableFile.increaseExtraSeat(reserve.getLineNum(), reserve.getFirstRailofTicket(), reserve.getLastRailofTicket(), seatCount);
                } catch (IOException | FileIntegrityException e) {
                    throw new RuntimeException(e);
                }
                cancelTempTicketList.add(clientTempReservationList.get(index)); //index 인지 i 인지
                tempList.remove(clientTempReservationList.get(index));
            }
        }

        return 1;
    }

    private int removeTempTicketByRowNum(String[] inputArr) {
        return removeTempTicketByRowNum(inputArr, 1);
    }

    private int removeTicketByRowNum(String[] inputArr, int seatCount) {
        int arrLength = inputArr.length;
        boolean patternFlag = true;
        for (String s : inputArr) {
            if (!(Pattern.matches("^\\#[1-9]$", s))) patternFlag = false;
        }

        if (!patternFlag) {
            return -1;
        }

        for (int i = 0; i < arrLength; i++) {
            int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

            if (index >= 0 && index < clientReservationList.size()) {
                try {
                    var reserve = clientReservationList.get(index);
                    timeTableFile.increaseExtraSeat(reserve.getLineNum(), reserve.getFirstRailofTicket(), reserve.getLastRailofTicket(), seatCount);
                    //이거 코드 너무 길긴한데 방법이 없네요..
                } catch (IOException | FileIntegrityException e) {
                    throw new RuntimeException(e);
                }
                cancelTicketList.add(clientReservationList.get(index));
                //reserve list 수정하고 setter로 reservlist 다시 설정
                //public 으로 fileReserve 안에 있는 reserveList 수정
                fileReserve.reserveList.remove(index);
            }
        }

        return 1;
    }

    private int removeTicketByRowNum(String[] inputArr) {
        return removeTicketByRowNum(inputArr, 1);

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
}