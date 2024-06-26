import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class FileReserve implements FileInterface{
    Scanner scan;
    String fileName;
    FileWriter fw;
    PrintWriter writer;
    public FileReserve(String fileName) {
        this.fileName = fileName;
    }
    public ArrayList<Ticket> reserveList = new ArrayList<>();

    public ArrayList<Ticket> getReserveList() {
        return reserveList;
    }

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        scan = new Scanner(new File(fileName));
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다은 split
            if(strArr.length != 6) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            UserName.checkIntegrity(strArr[0]);  //사용자 이름 무결성 확인
            PhoneNumber.checkIntegrity(strArr[1]);  //전화번호 무결성 확인
            Time.checkIntegrity(strArr[3]);  //출발 시각 무결성 확인
            Time.checkIntegrity(strArr[4]);  //예약 시각 무결성 확인

            // 일회용의 Rail, Line 객체를 만들어 무결성 확인

            Line l = new Line();
            FileRail fileRail = new FileRail("rail.csv");
            fileRail.checkIntegrity();
            String[] rl = strArr[5].split("/");
            LinkedHashMap<Rail, Integer> map = new LinkedHashMap<>();

            for (String s : rl) {
                Rail rail = fileRail.getRailByIndex(Integer.parseInt(s));
                rail.checkIntegrity();
                map.put(rail, 0);
            }

            l.railList = map;
            l.checkIntegrity(strArr[2]);
        }
    }

    public void repos(){
        try {
            checkIntegrity();
            reserveList = new ArrayList<>();
            scan = new Scanner(new File(fileName));
            while(scan.hasNextLine()) {
                String[] strArr = scan.nextLine().split(",");
                Ticket ticket = new Ticket();

                // cilent 부분 채우기
                // strArr[0] : 사용자 이름
                // strArr[1] : 전화번호
                ticket.client = new Client(strArr[0], strArr[1]);

                // Line 객체를 받아오기 위해 어쩔 수 없이 FileTimeTable 객체 생성.. 다른 좋은 방법이 있을 수도...
                FileRail rail = new FileRail("rail.csv");
                rail.checkIntegrity();
                FileTimeTable table = new FileTimeTable("timeTable.csv", rail);
                table.checkIntegrity();
                // strArr[2] : 노선번호
                ticket.line = table.getLine(strArr[2]);

                // strArr[3] : 출발시각
                ticket.depTime = strArr[3];

                // 예약시각
                // strArr[4] : String type 에약 시각
                ticket.reserveTime = strArr[4];

                // 노선정보
                // RailIndex 에 맞는 Rail 객체를 받아오기 위해 FileRail 객체 생성,,,
                FileRail fileRail = new FileRail("rail.csv");
                fileRail.checkIntegrity();

                // strArr[5] : 노선정보
                String[] railIndices = strArr[5].split("/");

                // 출발시각
                // strArr[3] : String type 출발 시각
                ticket.depTime = strArr[3];
                ticket.arrivalTime = ticket.calculateArrivalTime();

                ArrayList<Rail> temp = new ArrayList<>();
                for(int i=0; i<railIndices.length; i++){
                    // getRailByIndex 를 통해 노선정보인덱스에 해당하는 Rail 객체를 받아온다.
                    temp.add(fileRail.getRailByIndex(Integer.parseInt(railIndices[i])));
                }
                ticket.railIndices = temp;
                ticket.calculateArrivalTime();

                // 만들어진 ticket 을 TempList 에 저장
                reserveList.add(ticket);
            }
        } catch (FileNotFoundException | FileIntegrityException e) {
            e.printStackTrace();
        }
    }

//    public void write(String userName, String phoneNumber, String lineNum, String startTime) {
//        File file = new File(fileName);
//        try {
//            fw = new FileWriter(file, true);
//            writer = new PrintWriter(fw);
//            String str = userName + "," + phoneNumber + "," + lineNum + "," + startTime;
//            writer.println(str);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    //ReservationAndCancel에서 사용자별 예약정보를 출력하기 위해 이 항목에서 fileName을 get 하기 위해 만든 getter입니다.

    public String getFileName() {
        return fileName;
    }

    public int findByLineNum(String userName, String lineNum) {
        int index = 0;
        for (Ticket t : reserveList) {
            if (t.client.getName().equals(userName)) {
                if (t.line.lineNum.equals(lineNum))
                    return index;
            }
            index++;
        }
        return -1;
    }
    // ArrayList 에 적혀있는 내용을 파일에 덮어쓰기 합니다.
    // 추가!!!
    public void update(){
        File file = new File(fileName);
        try {
            fw = new FileWriter(file, false);
            writer = new PrintWriter(fw);
            for(Ticket t : reserveList){
                String str = t.client.getName() + "," + t.client.getPhoneNumber() + "," + t.line.lineNum + "," +
                        t.depTime + "," + LogInAndTimeInput.getNowTime() + "," + t.getRailIndicesToString();
                writer.println(str);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // repos 까지 수행해준다.
        repos();
    }

    public void addTicket(Ticket ticket) {
        this.reserveList.add(ticket);
    }

    // 아래 5개의 메서드들은 특정 파라미터를 통해 특정될 수 있는 예약 티켓들을 return 해주는 함수들
    // 추가!!
    public ArrayList<Ticket> getTicketListByClient(Client c){
        ArrayList<Ticket> returnTicket = new ArrayList<>();
        for(Ticket t : reserveList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<Ticket> getTicketListByLineNum(String lineNum, Client c){
        ArrayList<Ticket> returnTicket = new ArrayList<>();
        for(Ticket t : reserveList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && lineNum.equals(t.line.lineNum))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<Ticket> getTicketListByfromStation(Station fromStation, Client c){
        ArrayList<Ticket> returnTicket = new ArrayList<>();
        for(Ticket t : reserveList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && fromStation.getStation().equals(t.railIndices.get(0).fromStation.getStation()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<Ticket> getTicketListBytoStation (Station toStation, Client c){
        ArrayList<Ticket> returnTicket = new ArrayList<>();
        for(Ticket t : reserveList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && toStation.getStation().equals(t.railIndices.get(t.railIndices.size() - 1).toStation.getStation()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public ArrayList<Ticket> getTicketListByStation (Station fromStation, Station toStation, Client c){
        ArrayList<Ticket> returnTicket = new ArrayList<>();
        for(Ticket t : reserveList){
            if(t.client.getPhoneNumber().equals(c.getPhoneNumber()) && t.client.getName().equals(c.getName())
                    && fromStation.getStation().equals(t.railIndices.get(0).fromStation.getStation())
                    && toStation.getStation().equals(t.railIndices.get(t.railIndices.size() - 1).toStation.getStation()))
                returnTicket.add(t);
        }

        return returnTicket;
    }

    public void write(Ticket Ticket){
        File file = new File(fileName);
        try {
            // 하나하나 추가하는것이기 때문에 덮어쓰기를 허용하지 않는다. append = true
            fw = new FileWriter(file, true);
            writer = new PrintWriter(fw);
            String str = Ticket.client.getName() + "," + Ticket.client.getPhoneNumber() + "," + Ticket.line.lineNum + "," +
                    Ticket.depTime + "," + Ticket.getReserveTime() + "," + Ticket.getRailIndicesToString();
            writer.println(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // repos 까지 수행해준다.
        repos();
    }
}
