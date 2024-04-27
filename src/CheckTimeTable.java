import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckTimeTable {


    public FileTimeTable timeTableFile;
    public String[] inputArr;

    // 임시 시간!! 지우게 될것
    String thistime = "202111230830";


    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmm");
    Date currentdate = new Date();
    Date inputdate = new Date();
    Date Depdate = new Date();

    public CheckTimeTable(FileTimeTable timeTableFile) {
        this.timeTableFile = (FileTimeTable) timeTableFile;
    }

    public int init() throws IOException, FileIntegrityException, ParseException {
        System.out.print("운행하는 역 : ");
        // 운행하는 역 출력
        // 리스트를 만들어 운행하는 모든 역을 중복 없이 출력
        List<String> STlist = new ArrayList<>();
        /*
            출발역을 중복 제거하여 역 리스트에 추가
         */
        for (int i = 0; i < timeTableFile.getTrainlist().size(); i++) {
            if(!STlist.contains(timeTableFile.getTrainlist().get(i).fromStation.getStation())){
                STlist.add(timeTableFile.getTrainlist().get(i).fromStation.getStation());
            }
        }
        /*
            도착역을 중복 제거하여 역 리스트에 추가
         */
        for (int i = 0; i < timeTableFile.getTrainlist().size(); i++) {
            if(!STlist.contains(timeTableFile.getTrainlist().get(i).toStation.getStation())){
                STlist.add(timeTableFile.getTrainlist().get(i).toStation.getStation());
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

            System.out.println("원하시는 출발역, 도착역, 출발 시각을 차례대로 입력해주세요.");
            System.out.print("-> ");

            // 시간표 조회 : 사용자로부터 출발역, 도착역, 출발 시각 입력받음
            Scanner scan = new Scanner(System.in, "UTF-8");
            String input = scan.nextLine();
            inputArr = input.split(",");

            Station station1 = new Station(inputArr[0]);
            inputArr[0] = station1.getStation();
            Station station2 = new Station(inputArr[1]);
            inputArr[1] = station2.getStation();
            /*
                Q를 입력 받은 경우 메인 메뉴로 돌아감
             */
            if(inputArr.length == 1 && inputArr[0].equals("Q")){
                return 0;
            }

            // 요소가 3개가 아닐 시 재입력
            if (inputArr.length != 3) {
                System.out.println("잘못된 입력입니다.(인자수가 틀림) ");
                continue;
            }

            // 각 요소가 문법형식에 맞는지 확인
            try {
                Station.checkIntegrity(inputArr[0]);
                Station.checkIntegrity(inputArr[1]);
                Time.checkIntegrity(inputArr[2]);

            }catch (FileIntegrityException e) {
                System.out.println("잘못된 입력입니다.(문법오류) ");
                continue;
            }

            // 출발역과 도착역이 같을경우
            if(inputArr[0].equals(inputArr[1])) {
                System.out.println("역 설정을 다시 해주세요.");
                continue;
            }

            // 출발역 또는 도착역이 목록에 없을경우
            if(!STlist.contains(inputArr[0]) || !STlist.contains(inputArr[1])) {
                System.out.println("해당 역이 존재하지 않습니다. 다시 입력해 주세요.");
                continue;
            }

            // 검색한 시각이 현재 시각보다 전인지 확인

            inputdate = dtFormat.parse(inputArr[2]);
            currentdate = dtFormat.parse(thistime);

            long diff = inputdate.getTime() - currentdate.getTime();

            if(diff < 0) {
                System.out.println("출발시간을 확인해주세요 현재 시각은 " + thistime.substring(8,10) + ":" + thistime.substring(10,12) + "입니다");
                continue;
            }

            //검색한 시간이 현재 시각 보다 한달 후 인지 확인

            if (diff > (30 * 24 * 60 * 60 * 1000L)){
                System.out.println("한 달 이내의 열차만 예매할 수 있습니다");
                continue;
            }



            // 조건을 모두 만족시 다음으로 이동
            break;
        } // while문 종료


        // 검색에 부합하는 기차 정보 출력
        System.out.println("노선 번호 / 출발 시각 / 출발 역 / 도착 시각 / 도착 역 / (여석 수 / 전체 좌석 수)");
        for (int i = 0; i < timeTableFile.getTrainlist().size(); i++) {
            if (timeTableFile.getTrainlist().get(i).fromStation.getStation().equals(inputArr[0])) {
                if (timeTableFile.getTrainlist().get(i).toStation.getStation().equals(inputArr[1])) {

                    //검색 시간으로 부터 30분 이내로 출발 시간이 차이나는 기차만 출력
                    Depdate = dtFormat.parse(timeTableFile.getTrainlist().get(i).depTime);
                    long diff = inputdate.getTime() - Depdate.getTime();
                    if((diff < (30 * 60 * 1000)) && (diff > (-30 * 60 * 1000))){
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

        // 기차 정보 출력이후 예약 메뉴 진입 여부
        while(true) {
            System.out.println("예약 메뉴로 넘어가겠습니까?");
            System.out.print("-> ");

            Scanner scan = new Scanner(System.in, "UTF-8");
            String input = scan.nextLine();

            switch (input) {
                case "Yes":
                case "y":
                case "O":
                case "네":
                    // 1을 반환하여 main.java 에서 예약메뉴로 이동
                    return 1;
                case "No":
                case "N":
                case "X":
                case "아니오":
                case "아니요":
                    // 다시 메인 메뉴로 이동
                    return 0;
                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
            }

        }

    }

    public void repos(String file){

    }
}
