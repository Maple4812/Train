import java.io.*;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileTempReserve implements FileInterface {
    private String fileName;
    private FileWriter fw;
    private PrintWriter writer;
    private final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");
    private ArrayList<TempTicket> tempList;

    public ArrayList<TempTicket> getTempList() {
        return tempList;
    }

    public FileTempReserve(String fileName) {
        this.fileName = fileName;
    }

    Scanner scan;

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        scan = new Scanner(new File(fileName));
        while (scan.hasNextLine()) {
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다은 split
            if (strArr.length != 7) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            UserName.checkIntegrity(strArr[0]);  //사용자 이름 무결성 확인
            PhoneNumber.checkIntegrity(strArr[1]);  //사용자 전화번호 무결성 확인
            Time.checkIntegrity(strArr[3]);  //출발 시각 무결성 확인
            Time.checkIntegrity(strArr[4]);  //예약 시각 무결성 확인
            Time.checkIntegrity(strArr[5]);  //예약 컴퓨터 시각 무결성 확인

            // 일회용의 Rail, Line 객체를 만들어 무결성 확인

            Line l = new Line();
            FileRail fileRail = new FileRail("rail.csv");
            fileRail.checkIntegrity();
            String[] rl = strArr[6].split("/");
            LinkedHashMap<Rail, Integer> map = new LinkedHashMap<>();

            for (String s : rl) {
                fileRail.checkIntegrity();
                Rail rail = fileRail.getRailByIndex(Integer.parseInt(s));
                rail.checkIntegrity();
                map.put(rail, 0);
            }

            l.railList = map;
            l.checkIntegrity(strArr[2]);
        }
    }

    // 수정 !!
    public void repos() {
        try {
            checkIntegrity();
            tempList = new ArrayList<>();
            scan = new Scanner(new File(fileName));
            while (scan.hasNextLine()) {
                String[] strArr = scan.nextLine().split(",");
//                ArrayList<String> list = new ArrayList<>(Arrays.asList(strArr)); // 6개의 인자를 String 형태로 가진 ArrayList (named: list)
//                tempList.add(list); // 위에서 생성한 ArrayList를 tempList에 append한다

                // 저장을 위한 TempTicket 클래스 생성
                TempTicket ticket = new TempTicket();

                // cilent 부분 채우기
                // strArr[0] : 사용자 이름
                // strArr[1] : 전화번호
                ticket.client = new Client(strArr[0], strArr[1]);

                // Line 객체를 받아오기 위해 어쩔 수 없이 FileTimeTable 객체와 FileRail 객체 생성.. 다른 좋은 방법이 있을 수도...
                FileRail rail = new FileRail("rail.csv");
                FileTimeTable table = new FileTimeTable("timeTable.csv", rail);
                // strArr[2] : 노선번호
                ticket.line = table.getLine(strArr[2]);

                // 출발시각
                // strArr[3] : String type 출발 시각
                ticket.depTime = strArr[3];

                // 예약시각
                ticket.setReserveTime(strArr[4]);

                // 예약 컴퓨터 시각
                ticket.setReserveComputerTime(strArr[5]);

                // 노선정보
                // RailIndex 에 맞는 Rail 객체를 받아오기 위해 FileRail 객체 생성,,,
                FileRail fileRail = new FileRail("rail.csv");

                // strArr[6] : 노선정보
                String[] railIndices = strArr[6].split("/");
                ArrayList<Rail> temp = new ArrayList<>();
                for (int i = 0; i < railIndices.length; i++) {
                    // getRailByIndex 를 통해 노선정보인덱스에 해당하는 Rail 객체를 받아온다.
                    temp.add(fileRail.getRailByIndex(Integer.parseInt(railIndices[i])));
                }
                ticket.railIndices = temp;

                // 만들어진 ticket 을 TempList 에 저장
                tempList.add(ticket);
            }
        } catch (FileNotFoundException | FileIntegrityException e) {
            e.printStackTrace();
        }
    }

    // 폐기... 기존 String 으로만 하던 방식이었음
//    public void write(String userName, String phoneNumber, String lineNum, String startTime, String endTime, String endComputerTime){
//        // 이 함수에서 추가적인 규칙 검사는 이루어지지 않습니다. 해당 함수에 입력되는 모든 인자는 이미 검사를 받았다고 가정합니다.
//        File file = new File(fileName);
//        try {
//            fw = new FileWriter(file, false);
//            writer = new PrintWriter(fw);
//            String str = userName + "," + phoneNumber + "," + lineNum + "," + startTime + "," + endTime + "," + endComputerTime;
//            writer.println(str);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public boolean isTimerOn() {
        repos();
        // 리스트가 비어있으면 timer 가 false
        return !tempList.isEmpty();
    }

    public String getNewTime() {
        /*
        기능 :
            tempList 에 저장되어있는 예약 시간중 가장 최근에 가까운 시간을 String 의 형태로 return
            formatter.parse 를 할 때 생길 수 있는 ParseException 을 throws 한다.
            해당 메소드는 timer 가 true 일 때, LogInAndTimeInput 클래서에서 처음 시간을 받는 경우에만 사용된다.
        */

        /*
        변수 :
            @param lastestDate: 가장 최근 예약날짜, 1970년 1월 1일 0시 0분 0초로 초기화한다.
            @param lastestComputerDate: 가장 최근 예약컴퓨터날짜, 1970년 1월 1일 0시 0분 0초로 초기화한다.
            @param reserveDate: record 들의 예약 날짜를 담는 변수
            @param reserveComputerDate: record 들의 예약 컴퓨터 날짜를 담는 변수
            @param SavedNowComputerDate: UserInfo 에 저장되어있는 컴퓨터시각, 타이머가 켜진 경우에만 함수가 호출되니
                                                타이머가 켜진상태에서 프로그램을 사용자가 킨 순간의 컴퓨터시각이 된다.
         */
        Date lastestDate = new Date(0);
        Date lastestComputerDate = new Date(0);

        try {
            // parsing 할 때 생길 수 있는 error 때문에 try catch 문 안에서 수행해준다.
            // 하지만 parsing 할 수 없으면 애초에 무결성 검사에서 걸러지기 때문에
            // error 가 catch 되는 경우는 없다.

            for (TempTicket t : tempList) {
                Date reserveDate = FORMATTER.parse(t.getReserveTime()); // 수정 !!
                Date reserveComputerDate = FORMATTER.parse(t.getReserveComputerTime()); // 수정 !!
                if (lastestDate.before(reserveDate)) {
                    lastestDate = reserveDate;
                    lastestComputerDate = reserveComputerDate;
                }
            }

            Date SavedNowComputerDate = FORMATTER.parse(LogInAndTimeInput.getNowComputerTime());

            // 프로그램을 킨 순간의 컴퓨터 시각과 예약된 가장 최근의 컴퓨터시각을 비교하기 위해 .getTime() 을 호출한다.
            long time1 = SavedNowComputerDate.getTime();
            long time2 = lastestComputerDate.getTime();

            // 시각의 차이를 구한다. 이는 지나간 시각이다.
            long timeDiff = time1 - time2;

            // 가장 최근 예약시각에 지나간 시각을 더하기 위해 .getTime() 을 호출한다.
            long time3 = lastestDate.getTime();

            // 가장 최근 예약시각 + 지나간 시각
            lastestDate = new Date(timeDiff + time3);

            // 12자리 문자열의 형태로 변환 후 return 한다.
            return FORMATTER.format(lastestDate);
        } catch (ParseException ignore) {
            // 만약 프로그램 동작 도중 데이터의 변환으로 인해 parsing error 가 발생한다면
            // 시각은 새롭게 고쳐지지 않고 현재 시각은 변경되지 않는다.
            System.out.println("ParsingError!");
        }

        return null;
    }

    // 수정!!!
    // 기존 String 으로만 하던 방식에서 TempTicket 을 사용하는 방식으로...
    public void removeTimeOutReserve() {
        // 기능 : 5분이 지난 예약들을 삭제해준다.
        update();
        // 타이머가 꺼진 경우 수행하지 않는다.
        if (tempList.isEmpty()) {
            return;
        }

        try {
            // 저장되어있는 현재 시각을 불러온다
            Date savedNowDate = FORMATTER.parse(LogInAndTimeInput.getNowTime());
            long NowTime = savedNowDate.getTime();

            for (TempTicket t : tempList) {
                // 저장된 예약 시각들을 받는다.
                Date reserveDate = FORMATTER.parse(t.getReserveTime());
                long reserveTime = reserveDate.getTime();

                // 현재시각과 예약시각의 차이를 분 단위로 구한다.
                long diff = (NowTime - reserveTime) / (1000 * 60);
                if (diff > 4) {
                    // 차이가 5 분 보다 크다면 삭제한다.
                    FileRail rail = new FileRail("rail.csv");
                    FileTimeTable table = new FileTimeTable("timeTable.csv", rail);
                    table.increaseExtraSeat(t.line.lineNum, t.getFirstRailofTicket(), t.getLastRailofTicket(), 1);
                    // ArrayList 상에서만 지우고 update 해주면 되니까...
                    // removeLineByTime(record.get(4));
                    tempList.remove(t);
                    System.out.println("시간이 지나 가예약이 삭제되었습니다!");
                }
            }
            // update 안에 repos 까지 넣어서 사용
            // 앞서 ArrayList 만 삭제했으니 파일에도 적용해야함
            update();
        } catch (ParseException ignored) {
            // 만약 프로그램 동작 도중 데이터의 변환으로 인해 parsing error 가 발생한다면
            // 예약들은 새로고침되지 않는다.
            System.out.println("ParsingError!");
        } catch (IOException | FileIntegrityException e) {
            throw new RuntimeException(e);
        }
    }

    //ReservationAndCancel에서 사용자별 예약정보를 출력하기 위해 이 항목에서 fileName을 get 하기 위해 만든 getter입니다.

    public String getFileName() {
        return fileName;
    }

//    public int findByLineNum(String userName, String lineNum) {
//        int index = 0;
//        for (ArrayList<String> tempReserve : tempList) {
//            if (tempReserve.get(0).equals(userName)) {
//                if (tempReserve.get(2).equals(lineNum))
//                    return index;
//            }
//            index++;
//        }
//        return -1;
//    }

//    public void removeLineByTime(String time) throws IOException {
//        ArrayList<String> lineList = new ArrayList<>();
//        Scanner scan = new Scanner(new File(fileName));
//        while(scan.hasNextLine()) {
//            String[] strArr = scan.nextLine().split(",");
//            StringBuilder tempStr = new StringBuilder();
//            if(!strArr[4].equals(time)) {
//                for (int i = 0; i < strArr.length; i++) {
//                    if(i < strArr.length - 1) {
//                        tempStr.append(strArr[i]).append(",");
//                    }
//                    else {
//                        tempStr.append(strArr[i]);
//                    }
//                }
//                lineList.add(tempStr.toString());
//            }
//        }
//        PrintWriter writer = new PrintWriter(new FileWriter(fileName));
//        for (String s: lineList) {
//            writer.println(s);
//        }
//        writer.close();
//    }

    // ArrayList 에 적혀있는 내용을 파일에 덮어쓰기 합니다.
    // 추가!!!
    public void update() {
        File file = new File(fileName);
        try {
            fw = new FileWriter(file, false);
            writer = new PrintWriter(fw);
            for (TempTicket t : tempList) {
                String str = t.client.getName() + "," + t.client.getPhoneNumber() + "," + t.line.lineNum + "," +
                        t.depTime + "," + t.getReserveTime() + "," + t.getReserveComputerTime() + "," + t.getRailIndicesToString();
                writer.println(str);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // repos 까지 수행해준다.
        repos();
    }

    // 하나하나씩 파일에 저장하고 싶을 때 사용
    // 추가!!
    public void write(TempTicket tempTicket){
        File file = new File(fileName);
        try {
            // 하나하나 추가하는것이기 때문에 덮어쓰기를 허용하지 않는다. append = true
            fw = new FileWriter(file, true);
            writer = new PrintWriter(fw);
            String str = tempTicket.client.getName() + "," + tempTicket.client.getPhoneNumber() + "," + tempTicket.line.lineNum + "," +
                    tempTicket.depTime + "," + tempTicket.getReserveTime() + "," + tempTicket.getReserveComputerTime() + "," + tempTicket.getRailIndicesToString();
            writer.println(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // repos 까지 수행해준다.
        repos();
    }

    public void addTempTicket(TempTicket ticket) {
        this.tempList.add(ticket);
    }

    // 아래 5개의 메서드들은 특정 파라미터를 통해 특정될 수 있는 예약 티켓들을 return 해주는 함수들
    // 추가!!
    public ArrayList<TempTicket> getTempTicketListByClient(Client c){
        ArrayList<TempTicket> returnTicket = new ArrayList<>();
        for(TempTicket t : tempList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<TempTicket> getTempTicketListByLineNum(String lineNum, Client c){
        ArrayList<TempTicket> returnTicket = new ArrayList<>();
        for(TempTicket t : tempList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && lineNum.equals(t.line.lineNum))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<TempTicket> getTempTicketListByfromStation(Station fromStation, Client c){
        ArrayList<TempTicket> returnTicket = new ArrayList<>();
        for(TempTicket t : tempList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && fromStation.getStation().equals(t.railIndices.get(0).fromStation.getStation()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<TempTicket> getTempTicketListBytoStation (Station toStation, Client c){
        ArrayList<TempTicket> returnTicket = new ArrayList<>();
        for(TempTicket t : tempList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && toStation.getStation().equals(t.railIndices.get(t.railIndices.size() - 1).toStation.getStation()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<TempTicket> getTempTicketListByStation (Station fromStation, Station toStation, Client c){
        ArrayList<TempTicket> returnTicket = new ArrayList<>();
        for(TempTicket t : tempList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && fromStation.getStation().equals(t.railIndices.get(0).fromStation.getStation())
                    && toStation.getStation().equals(t.railIndices.get(t.railIndices.size() - 1).toStation.getStation()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    // tempList 를 return 받을 수 있는 메서드
    // 추가!!
    public ArrayList<TempTicket> getTempTicket(){return tempList;};
}
