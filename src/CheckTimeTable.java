import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckTimeTable {

    public FileTimeTable timeTableFile;
    public FileRail railFile;
    public String[] inputArr;

    // 사용자가 앞에서 입력한 시간을 받아옴
    String thistime;

    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmm");
    Date currentdate = new Date();
    Date inputdate = new Date();
    Date Depdate = new Date();

    public CheckTimeTable(FileInterface timeTableFile,FileInterface railFile) {
        this.timeTableFile = (FileTimeTable) timeTableFile;
        this.railFile = (FileRail) railFile;
    }

    public int init() throws IOException, FileIntegrityException, ParseException {
        System.out.print("운행하는 역 : ");
        // 운행하는 역 출력
        // 리스트를 만들어 운행하는 모든 역을 중복 없이 출력
        List<String> STlist = new ArrayList<>();
        /*
            출발역을 중복 제거하여 역 리스트에 추가
         */
        for (int i = 0; i < railFile.getRaillist().size(); i++) {
            if(!STlist.contains(railFile.getRaillist().get(i).fromStation.getStation())){
                STlist.add(railFile.getRaillist().get(i).fromStation.getStation());
            }
        }
        /*
            도착역을 중복 제거하여 역 리스트에 추가
         */
        for (int i = 0; i < railFile.getRaillist().size(); i++) {
            if(!STlist.contains(railFile.getRaillist().get(i).toStation.getStation())){
                STlist.add(railFile.getRaillist().get(i).toStation.getStation());
            }
        }

        /*
            역 리스트를 출력
         */
        for(int i=0;i<STlist.size();i++){
            System.out.print(STlist.get(i));
            if(i!=STlist.size()-1){
                System.out.print(", ");
            }
        }
        System.out.println();


        while(true) {
            System.out.println("원하시는 출발역, 도착역, 출발 시각을 차례대로 입력해주세요.(출발역,도착역,출발 시각)");
            System.out.print("-> ");
            LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
            TempReservation.removeTimeOutReserve();

            // 시간표 조회 : 사용자로부터 출발역, 도착역, 출발 시각 입력받음
            Scanner scan = new Scanner(System.in, "UTF-8");
            String input = scan.nextLine();
            /*
                Q를 입력 받은 경우 메인 메뉴로 돌아감
             */
            if (input.equals("Q")) {
                LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
                return 0;
            }
            inputArr = input.split(",");


            // 요소가 3개가 아닐 시 재입력
            if (inputArr.length != 3) {
                System.out.println("잘못된 입력입니다.");
                continue;
            }

            // 각 요소가 문법형식에 맞는지 확인
            try {
                Station.checkIntegrity(inputArr[0]);
                Station.checkIntegrity(inputArr[1]);
                Time.checkIntegrity(inputArr[2]);

            } catch (FileIntegrityException e) {
                System.out.println("잘못된 입력입니다.(문법오류) ");
                continue;
            }

            Station station1 = new Station(inputArr[0]);
            inputArr[0] = station1.getStation();
            Station station2 = new Station(inputArr[1]);
            inputArr[1] = station2.getStation();

            // 출발역과 도착역이 같을경우
            if (inputArr[0].equals(inputArr[1])) {
                System.out.println("역 설정을 다시 해주세요.");
                continue;
            }

            // 출발역 또는 도착역이 목록에 없을경우
            if (!STlist.contains(inputArr[0]) || !STlist.contains(inputArr[1])) {
                System.out.println("해당 역이 존재하지 않습니다. 다시 입력해 주세요.");
                continue;
            }

            // 검색한 시각이 현재 시각보다 전인지 확인
            thistime = LogInAndTimeInput.getNowTime();

            inputdate = dtFormat.parse(inputArr[2]);
            currentdate = dtFormat.parse(thistime);

            long diff = inputdate.getTime() - currentdate.getTime();

            if (diff < 0) {
                System.out.println("출발시간을 확인해주세요 현재 시각은 " + thistime.substring(8, 10) + ":" + thistime.substring(10, 12) + "입니다");
                continue;
            }

            //검색한 시간이 현재 시각 보다 한달 후 인지 확인

            if (diff > (30 * 24 * 60 * 60 * 1000L)) {
                System.out.println("한 달 이내의 열차만 예매할 수 있습니다");
                continue;
            }


            // 조건을 모두 만족시 다음으로 이동


            // 검색에 부합하는 기차 정보 출력(새로 작성)
            int n = 0;
            for (int i = 0; i < timeTableFile.getLineList().size(); i++) {
                if (timeTableFile.getLineList().get(i).slicing(inputArr[0], inputArr[1]) != null) {

                    //티켓 객체 임시 생성
                    Ticket ticket = new Ticket();
                    ticket.railIndices = timeTableFile.getLineList().get(i).slicing(inputArr[0], inputArr[1]);
                    ticket.line = timeTableFile.getLineList().get(i);
                    ticket.depTime = timeTableFile.getLineList().get(i).caculateDeptime(inputArr[0], inputArr[1]); // caculateDeptime : 역입력 시 출발 시각 반환 함수

                    //검색 시간으로 부터 30분 이내로 출발 시간이 차이나는 기차만 출력
                    Depdate = dtFormat.parse(ticket.depTime);
                    diff = inputdate.getTime() - Depdate.getTime();
                    if ((diff < (30 * 60 * 1000)) && (diff > (-30 * 60 * 1000))) {
                        if (n == 0){System.out.println("노선 번호 / 출발 시각 / 출발 역 / 도착 시각 / 도착 역 / (여석 수 / 전체 좌석 수)");}
                        n++;
                        printTicket(ticket);
                    }
                }
            }
            if (n == 0) {
                System.out.println("검색에 해당하는 열차가 없습니다!");
                continue;
            }
            break;

            /* 검색에 부합하는 기차 정보 출력(기존)
            int n = 0;
            for (int i = 0; i < timeTableFile.getTrainlist().size(); i++) {
                if (timeTableFile.getTrainlist().get(i).fromStation.getStation().equals(inputArr[0])) {
                    if (timeTableFile.getTrainlist().get(i).toStation.getStation().equals(inputArr[1])) {

                        //검색 시간으로 부터 30분 이내로 출발 시간이 차이나는 기차만 출력
                        Depdate = dtFormat.parse(timeTableFile.getTrainlist().get(i).depTime);
                        diff = inputdate.getTime() - Depdate.getTime();
                        if ((diff < (30 * 60 * 1000)) && (diff > (-30 * 60 * 1000))) {
                            n++;
                            System.out.println("노선 번호 / 출발 시각 / 출발 역 / 도착 시각 / 도착 역 / (여석 수 / 전체 좌석 수)");
                            System.out.print(timeTableFile.getTrainlist().get(i).lineNum);
                            System.out.print("  ");
                            System.out.print(timeTableFile.getTrainlist().get(i).depTime);
                            System.out.print("  ");
                            System.out.print(timeTableFile.getTrainlist().get(i).fromStation.getStation());
                            System.out.print("  ");
                            System.out.print(timeTableFile.getTrainlist().get(i).arrivalTime);
                            System.out.print("  ");
                            System.out.print(timeTableFile.getTrainlist().get(i).toStation.getStation());
                            System.out.print("  ");
                            System.out.print(timeTableFile.getTrainlist().get(i).extraSeat.getSeat());
                            System.out.print("/");
                            System.out.println(timeTableFile.getTrainlist().get(i).entireSeat.getSeat());
                        }

                    }
                }

            }
            if (n == 0) {
                System.out.println("검색에 해당하는 열차가 없습니다!");
                continue;
            }
            break;
        */
        }




        // 기차 정보 출력이후 예약 메뉴 진입 여부
        while(true) {
            System.out.println("예약 메뉴로 넘어가겠습니까?");
            System.out.print("-> ");
            LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
            TempReservation.removeTimeOutReserve();

            Scanner scan = new Scanner(System.in, "UTF-8");
            String input = scan.nextLine();

            switch (input) {
                case "Yes":
                case "Y":
                case "O":
                case "네":
                    // 1을 반환하여 main.java 에서 예약메뉴로 이동
                    LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
                    return 1;
                case "No":
                case "N":
                case "X":
                case "아니오":
                case "아니요":
                    // 다시 메인 메뉴로 이동
                    LogInAndTimeInput.setNowTime(TempReservation.timeRenewal());
                    return 0;
                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
            }

        }

    }

    public void repos(String file){

    }

    // 티켓 객체의 가격과 도착시간을 계산 후 출력
    public void printTicket(Ticket ticket){

    }
}


