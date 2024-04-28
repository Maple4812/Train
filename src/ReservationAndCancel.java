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
    private static final double FEE_RATE_FREE = 0.0;
    private static final double FEE_RATE_5_PERCENT = 0.05;
    private static final double FEE_RATE_10_PERCENT = 0.10;
    private static final double FEE_RATE_15_PERCENT = 0.15;
    private static final double FEE_RATE_40_PERCENT = 0.40;
    private static final double FEE_RATE_70_PERCENT = 0.70;
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
        System.out.println("가예약 확정 또는 예약취소를 입력해주세요: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        scanner.close();

        if (input.equals("가예약 확정")) {
            init1();
        } else if (input.equals("예약취소")) {
            init2();
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

        for (ArrayList<String> tempReserve : fileTempReserve.getTempList()) {
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

        int flag = 1;
        do {
            System.out.println("확정할 가예약을 입력하세요: ");
            Scanner inputScan = new Scanner(System.in);
            String[] inputArr = inputScan.nextLine().split(",");
            inputScan.close();

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

        }
        if (!clientReservationList.isEmpty()) {
            System.out.println(clientName + " / " + clientPhoneNumber + "님의 예약 정보입니다.");
            System.out.println("행 번호 / 노선 번호 / 출발 시간 / 출발 역 / 도착 시간 / 도착 역");
            printReserveList(clientReservationList);
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
            String trainNum = row[0];
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

    public int calcCancelFee(String departureTime){
        String nowTime = LogInAndTimeInput.getNowTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime depTime = LocalDateTime.parse(departureTime, formatter);
        LocalDateTime currentTime = LocalDateTime.parse(nowTime, formatter);

        long minutesDifference = java.time.Duration.between(currentTime, depTime).toMinutes();

        int cancellationFee = 0;
        if (minutesDifference <= 20) {
            cancellationFee = (int) (FEE_RATE_15_PERCENT * 100);
        } else if (minutesDifference <= 60) {
            cancellationFee = (int) (FEE_RATE_40_PERCENT * 100);
        } else if (minutesDifference < 180) {
            cancellationFee = (int) (FEE_RATE_70_PERCENT * 100);
        } else if (minutesDifference < 1440) {
            cancellationFee = (int) (FEE_RATE_5_PERCENT * 100);
        } else if (minutesDifference < 43200) {
            cancellationFee = (int) (FEE_RATE_FREE * 100);
        } else if (minutesDifference < 518400) {
            cancellationFee = (int) (FEE_RATE_10_PERCENT * 100);
        }
        return cancellationFee;
    }

    public void makeCancelList(){
        //TODO 입력 받아온 정보로 취소 열차들 어레이리스트에 저장
    }

    public void printCancelInfo(ArrayList<Ticket> ticketList){
        int i = 0;
        for (Ticket ticket : ticketList) {
            String ticketInfo = ticket.toString();
            System.out.println("#" + (i++) + " " + ticketInfo + calcCancelFee(ticket.depTime));
        }
    }

    // 취소 입력 구현


    public static void removeRowsByTrainNumber(String csvFilePath, String trainNumber) {
        List<String[]> nonMatchingRows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                if (!row[2].equals(trainNumber)) {

                    nonMatchingRows.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFilePath))) {
            for (String[] row : nonMatchingRows) {
                bw.write(String.join(",", row));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //열차 취소시 csv파일에서 삭제
}
