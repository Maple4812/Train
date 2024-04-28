import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ReservationAndCancel {
    /**
     * repos로 파일들을 불러와서 tempReservation, Reservation 에 ArrayList 형태로 저장
     *
     * @author 변수혁
     */
    private FileTempReserve fileTempReserve;
    private FileReserve fileReserve;
    private ArrayList<String[]> clientTempReservationList;
    private ArrayList<String[]> clientReservationList;
    private FileTimeTable timeTableFile;
    String clientName;
    String clientPhoneNumber;

    public ReservationAndCancel(FileInterface fileTempReserve, FileInterface fileReserve, FileInterface timeTableFile) {
        this.fileReserve = (FileReserve) fileReserve;
        this.fileTempReserve = (FileTempReserve) fileTempReserve;
        this.timeTableFile = (FileTimeTable) timeTableFile;
    }

    public void init() throws IOException {
        while(true) {
            System.out.println("가예약 확정 또는 예약취소를 입력해주세요: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equals("가예약 확정")) {
                init1();
                break;
            } else if (input.equals("예약취소")) {
                init2();
                break;
            } else if (input.equals("Q")){
                System.out.println("메인 프롬프트로 돌아갑니다");
                return;
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    public void init1() throws IOException {
        fileTempReserve.repos();
        ArrayList<ArrayList<String>> tempReserves = new ArrayList<>();
        ArrayList<Integer> tempReserveIndexArrayList = new ArrayList<>();
        Client loginClient = LogInAndTimeInput.getClient();

        if (fileTempReserve.getTempList().isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }

        System.out.println(loginClient.getPhoneNumber() + "/" + loginClient.getName() + " 고객님의 예약정보입니다.");
        System.out.println("행 번호 / 노선 번호 / 출발 시각 / 출발 역 / 도착 시간 / 도착 역");

        int tempIndex = 0;
        int rowNum = 1;
        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            LocalDateTime departureTime = LocalDateTime.parse(tempReserve.get(3), formatter);
            LocalDateTime reserveTime = LocalDateTime.parse(tempReserve.get(4), formatter);

            if (tempReserve.get(0).equals(loginClient.getName()) && departureTime.isAfter(LocalDateTime.now())) {
                long minutesBetween = Duration.between(LocalDateTime.now(), reserveTime).toMinutes();

                System.out.print("#" + rowNum + " / ");
                System.out.print(tempReserve.get(2) + " / ");
                System.out.print(tempReserve.get(3) + " / ");
                System.out.print(timeTableFile.getTicket(tempReserve.get(2)).fromStation.getStation() + " / ");
                System.out.print(timeTableFile.getTicket(tempReserve.get(2)).arrivalTime + " / ");
                System.out.print(timeTableFile.getTicket(tempReserve.get(2)).toStation.getStation());
                System.out.println();
                if (minutesBetween > 20) {
                    System.out.println("- 20분이 지나 삭제되었습니다.");
                } else {
                    tempReserves.add(tempReserve);
                    tempReserveIndexArrayList.add(tempIndex);
                }
                System.out.println();
                rowNum++;
            }

            tempIndex++;
        }

        if (tempReserves.isEmpty()) {
            System.out.println("가예약된 기차표의 출발시각이 이미 지났으므로 자동 삭제되었습니다.");
            return;
        }

        int flag = 1;
        do {
            System.out.println("확정할 가예약을 입력하세요: ");
            Scanner inputScan = new Scanner(System.in);
            String[] inputArr = inputScan.nextLine().split(",");

            ArrayList<Ticket> confirmedTicketArrayList = new ArrayList<>();

            switch (inputArr.length) {
                case 1:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        int index = Integer.parseInt(inputArr[0].replace("#", "")) - 1;

                        if (index >= 0 && index < tempReserves.size()) {
                            fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                            confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                            new FileOutputStream(fileTempReserve.getFileName()).close();
                            fileTempReserve.getTempList().remove(tempReserveIndexArrayList.get(index));
                            for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                                fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                            }
                        }
                    } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        int index = 0;
                        while (index != -1) {
                            index = fileTempReserve.findByLineNum(loginClient.getName(), inputArr[0]);
                            fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), inputArr[0], timeTableFile.getTicket(inputArr[0]).depTime);
                            confirmedTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                            fileTempReserve.getTempList().remove(index);
                        }

                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    }
                    break;

                case 2:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();
                        for (int i = 0; i < 2; i++) {
                            int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                            if (index >= 0 && index < tempReserves.size()) {
                                fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                                confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                                fileTempReserve.getTempList().remove(fileTempReserve.findByLineNum(loginClient.getName(), tempReserves.get(i).get(2)));
                            }
                        }
                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
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

                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        for (int i = 0; i < num; i++) {
                            fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), inputArr[0], timeTableFile.getTicket(inputArr[0]).depTime);
                            confirmedTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
                            fileTempReserve.getTempList().remove(fileTempReserve.findByLineNum(loginClient.getName(), inputArr[0]));
                        }

                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    } else if (inputArr[1].equals("출발")) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepStation(inputArr[0]+"역");

                        int index = -1;
                        for (Ticket ticket : ticketArrayList) {
                            index = fileTempReserve.findByLineNum(loginClient.getName(), ticket.lineNum);
                            if (index != -1) {
                                fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
                                confirmedTicketArrayList.add(ticket);
                                fileTempReserve.getTempList().remove(index);
                            }
                        }

                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    } else if (inputArr[1].equals("도착")) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByArrStation(inputArr[0]+"역");

                        int index = -1;
                        for (Ticket ticket : ticketArrayList) {
                            index = fileTempReserve.findByLineNum(loginClient.getName(), ticket.lineNum);
                            if (index != -1) {
                                fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
                                confirmedTicketArrayList.add(ticket);
                                fileTempReserve.getTempList().remove(index);
                            }
                        }

                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    }
                    break;

                case 3:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        for (int i = 0; i < 3; i++) {
                            int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                            if (index >= 0 && index < tempReserves.size()) {
                                fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                                confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                                fileTempReserve.getTempList().remove(fileTempReserve.findByLineNum(loginClient.getName(), tempReserves.get(i).get(2)));
                            }
                        }
                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    }
                    break;

                case 4:
                    if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        for (int i = 0; i < 4; i++) {
                            int index = Integer.parseInt(inputArr[i].replace("#", "")) - 1;

                            if (index >= 0 && index < tempReserves.size()) {
                                fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), tempReserves.get(index).get(2), tempReserves.get(index).get(3));
                                confirmedTicketArrayList.add(timeTableFile.getTicket(tempReserves.get(index).get(2)));
                                fileTempReserve.getTempList().remove(fileTempReserve.findByLineNum(loginClient.getName(), tempReserves.get(i).get(2)));
                            }
                        }
                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    } else if (inputArr[1].equals("출발")) {
                        new FileOutputStream(fileTempReserve.getFileName()).close();

                        ArrayList<Ticket> ticketArrayList = timeTableFile.getTicketByDepArrStation(inputArr[0]+"역", inputArr[2]+"역");

                        int index = -1;
                        for (Ticket ticket : ticketArrayList) {
                            index = fileTempReserve.findByLineNum(loginClient.getName(), ticket.lineNum);
                            if (index != -1) {
                                fileReserve.write(loginClient.getName(), loginClient.getPhoneNumber(), ticket.lineNum, ticket.depTime);
                                confirmedTicketArrayList.add(ticket);
                                fileTempReserve.getTempList().remove(index);
                            }
                        }

                        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
                            fileTempReserve.write(tempReserve.get(0), tempReserve.get(1), tempReserve.get(2), tempReserve.get(3), tempReserve.get(4), tempReserve.get(5));
                        }
                    }
                    break;

                default:
                    flag = -1;
            }
            TempReservation.printConfirmedTickets(confirmedTicketArrayList);
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
            try {
                tempCancelTicketList = makeTempCancelList();
            } catch (Exception e) {
                System.out.println("입력값이 잘못되었습니다.");
            }
            printCancelInfo(tempCancelTicketList);

        }else {
            System.out.println("가예약 정보가 없습니다.");
        }
        if (!clientReservationList.isEmpty()) {
            System.out.println(clientName + " / " + clientPhoneNumber + "님의 예약 정보입니다.");
            System.out.println("행 번호 / 노선 번호 / 출발 시간 / 출발 역 / 도착 시간 / 도착 역");
            printReserveList(clientReservationList);
            ArrayList<Ticket> CancelTicketList = new ArrayList<>();
            try {
                CancelTicketList = makeCancelList();
            } catch (Exception e) {
                System.out.println("입력값이 잘못되었습니다.");
            }
            printCancelInfo(CancelTicketList);
        }else{
            System.out.println("예약 정보가 없습니다.");
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
            String ticketInfo = timeTableFile.getTicket(trainNum).toString();
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
            return (int) Math.round(0.05*ticketPrice);
        } else if (minutesDifference >= 0) { // 출발 3시간 이내
            return (int) Math.round(0.1*ticketPrice);
        } else if (minutesDifference >= -20) { // 출발 20분 이후 ~ 출발 60분 미만
            return (int) Math.round(0.15*ticketPrice);
        } else if (minutesDifference >= -60) { // 출발 60분 경과 후 ~ 출발 3시간 이내
            return (int) Math.round(0.4*ticketPrice);
        } else if (arrTimeIscurrentTime >= 0) { // 출발 3시간 경과 후 ~ 도착
            return (int) Math.round(0.7*ticketPrice);
        } else {
            return ticketPrice;
        }
    }


    public void printCancelInfo(ArrayList<Ticket> ticketList) {
        for (Ticket ticket : ticketList) {
            System.out.println("취소 열차 정보:");
            System.out.println("노선 번호: " + ticket.lineNum);
            System.out.println("출발 시각: " + ticket.depTime);
            System.out.println("출발 역: " + ticket.fromStation.getStation());
            System.out.println("도착 시각: " + ticket.arrivalTime);
            System.out.println("도착 역: " + ticket.toStation.getStation());
            System.out.println("취소 수수료: " + calcCancelFee(ticket.depTime, ticket.arrivalTime, Integer.parseInt(ticket.price.getPrice())) + "원");
            System.out.println("-".repeat(20));
        }
    }

    // 취소 입력 구현

    public int countStringInArrayList(ArrayList<String[]> arrayList, String searchString) {
        int count = 0;

        // ArrayList를 반복하면서 각 String[] 객체를 확인합니다.
        for (String[] arr : arrayList) {
            // 현재 String[] 객체에 searchString가 포함되어 있는지 확인합니다.
            for (String str : arr) {
                if (str.contains(searchString)) {
                    count++; // 포함되어 있다면 count를 증가시킵니다.
                    break; // searchString가 발견되면 더 이상 현재 String[] 객체를 검사하지 않고 다음으로 넘어갑니다.
                }
            }
        }

        return count;
    }

    public ArrayList<Ticket> makeTempCancelList() throws Exception {
        System.out.println("RSVD Cancel: ");
        Scanner inputScan = new Scanner(System.in);
        String[] inputArr = inputScan.nextLine().split(",");

        ArrayList<Ticket> cancelTicketArrayList = new ArrayList<>();

        switch (inputArr.length) {
            case 1:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    int index = Integer.parseInt(inputArr[0].replace("#", "")) - 1;

                    if (index >= 0 && index < clientTempReservationList.size()) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(clientTempReservationList.get(index)[2]));
                        removeRowsByTrainNumber(fileTempReserve.getFileName(), clientTempReservationList.get(index)[2],1);

                    }
                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int count = countStringInArrayList(clientTempReservationList, inputArr[0]);
                    for (int i = 0; i < count; i++) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
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
    public ArrayList<Ticket> makeCancelList() throws Exception {
        System.out.println("RSVD Cancel: ");
        Scanner inputScan = new Scanner(System.in);
        String[] inputArr = inputScan.nextLine().split(",");

        ArrayList<Ticket> cancelTicketArrayList = new ArrayList<>();

        switch (inputArr.length) {
            case 1:
                if (Pattern.matches("^\\#[1-9]$", inputArr[0])) {
                    int index = Integer.parseInt(inputArr[0].replace("#", "")) - 1;

                    if (index >= 0 && index < clientReservationList.size()) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(clientReservationList.get(index)[2]));
                        removeRowsByTrainNumber(fileReserve.getFileName(), clientReservationList.get(index)[2],1);

                    }
                } else if (Pattern.matches("^[A-Z][0-9]{4}$", inputArr[0])) {
                    int count = countStringInArrayList(clientReservationList, inputArr[0]);
                    for (int i = 0; i < count; i++) {
                        cancelTicketArrayList.add(timeTableFile.getTicket(inputArr[0]));
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


    public void removeRowsByTrainNumber(String csvFilePath, String trainNumber){
        removeRowsByTrainNumber(csvFilePath, trainNumber, Integer.MAX_VALUE);
    }

    public void removeRowsByTrainNumber(String csvFilePath, String trainNumber, int count) {
        List<String[]> rows = new ArrayList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                rows.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int removedCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length > 2 && row[2].equals(trainNumber)) {
                rows.remove(i);
                removedCount++;
                i--; // Adjust index due to removal
                if (removedCount == count) {
                    break;
                }
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {
            for (String[] row : rows) {
                bw.write(String.join(",", row));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isExist(String trainNumber, ArrayList<String[]> reservationList) {
        for (String[] row : reservationList) {
            if (row[2].equals(trainNumber)) {
                return true;
            }
        }
        return false;
    }
}
