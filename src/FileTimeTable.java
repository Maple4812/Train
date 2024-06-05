import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class FileTimeTable implements FileInterface{
    private String fileName;
    private ArrayList<Line> lineList = new ArrayList<>();//timetable.csv의 한 줄에 저장된 정보를 각 줄 마다 Line 객체로 묶어 저장
    Scanner scan;

    public FileTimeTable(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }

    public ArrayList<Line> getLineList(){return this.lineList;}

    public Line getLine(String str){
        /*
            데이터 파일 최신화
         */
        try {
            repos();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        /*
            입력 받은 노선번호에 맞는 Line 반환
         */
        for(Line line : lineList){
            if(line.lineNum.equals(str)){
                return line;
            }
        }
        return null;
    }

    /*
    아래는 다른 파트에서 쓰는 부분
     */

    public Ticket getTicketByLineNum(String lineNum) {
        for (Ticket ticket : trainlist) {
            if (ticket.lineNum.equals(lineNum))
                return ticket;
        }
        return null;
    }

    public ArrayList<Ticket> getTicketByDepStation(String depStation) {
        ArrayList<Ticket> arrayList = new ArrayList<>();
        for (Ticket ticket : trainlist) {
            if (ticket.fromStation.getStation().equals(depStation))
                arrayList.add(ticket);
        }
        return arrayList;
    }

    public ArrayList<Ticket> getTicketByArrStation(String arrStation) {
        ArrayList<Ticket> arrayList = new ArrayList<>();
        for (Ticket ticket : trainlist) {
            if (ticket.toStation.getStation().equals(arrStation))
                arrayList.add(ticket);
        }
        return arrayList;
    }

    public ArrayList<Ticket> getTicketByDepArrStation(String depStation, String arrStation) {
        ArrayList<Ticket> arrayList = new ArrayList<>();
        for (Ticket ticket : trainlist) {
            if (ticket.fromStation.getStation().equals(depStation)) {
                if (ticket.toStation.getStation().equals(arrStation))
                    arrayList.add(ticket);
            }
        }
        return arrayList;
    }

    /*
        위는 다른 파트에서 쓰는 부분
     */

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        scan= new Scanner(new File(fileName));
        ArrayList<String> lineNumList=new ArrayList<>(); // 노선 번호 중복을 체크하기 위해 노선 번호만 저장 할 리스트
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Line line= new Line();

            /*
                데이터 파일이 노선 번호, 출발 시각, <운행 정보 인덱스, 여석> ... 형태를 갖는지 확인합니다.
                이때 strArr의 개수는 4 이상의 짝수
                (한 Line이 최대 20개? 의 구간만 지날 수 있다는 조건이 있었던거 같은데 확인 필요. 일단 현재는 4 이상 짝수이면 모두 만족)
             */
            if( !((strArr.length>=4) && (strArr.length % 2 == 0))) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }

            if(strArr[2].equals(strArr[4])){ // 부가 확인 항목 1: 출발역과 도착역이 같은 열차가 존재하는 경우
                throw new FileIntegrityException("오류: 출발역과 도착역이 같은 열차가 있습니다.");
            }
            
            /*
                ********************************************************
                이 부분은 기획서 수정이 필요해 보입니다. 기획서에 출발 시각과 도착 시각에 대한 규칙이 따로 정의되어 있지 않고
                단지 시각 형식만 따른다고 되어 있습니다.
                출발 시각과 도착 시각의 선후관계가 올바른지 무결성검사를 진행합니다.
                ********************************************************
             */
            // 출발 시각의 연도가 더 큰 경우
            if(Integer.parseInt(strArr[1].substring(0,4))>Integer.parseInt(strArr[3].substring(0,4))){
                throw new FileIntegrityException("오류: 도착 시각이 출발 시각보다 이릅니다.");
            }

            // 출발 시각의 월/일이 더 큰 경우
            if(Integer.parseInt(strArr[1].substring(4,8))>Integer.parseInt(strArr[3].substring(4,8))){
                throw new FileIntegrityException("오류: 도착 시각이 출발 시각보다 이릅니다.");
            }

            // 출발 시각의 시/분이 더 큰 경우
            if(Integer.parseInt(strArr[1].substring(8,12))>Integer.parseInt(strArr[3].substring(8,12))){
                throw new FileIntegrityException("오류: 도착 시각이 출발 시각보다 이릅니다.");
            }

            /*
                여석 수의 의미 규칙에 따라 여석 수가 전체 좌석 수보다 큰지 무결성 검사를 진행합니다.
                위에서 Seat.checkIntegrity()를 통해 여석 수, 전체 좌석 수의 무결성 검사를 이미 진행했기에
                추가적인 형식 검사 없이 Integer.parseInt()를 사용합니다.
             */
            if(Integer.parseInt(strArr[6])>Integer.parseInt(strArr[7])){
                throw new FileIntegrityException("오류: 여석 수가 전체 좌석 수 보다 큰 열차가 존재합니다.");
            }


            ticket.lineNum=strArr[0];
            ticket.depTime=strArr[1];
            ticket.fromStation=new Station(strArr[2]);
            ticket.arrivalTime=strArr[3];
            ticket.toStation=new Station(strArr[4]);
            ticket.price=new Price(strArr[5]);
            ticket.extraSeat=new Seat(strArr[6]);
            ticket.entireSeat=new Seat(strArr[7]);

            trainlist.add(ticket);

        }

        /*
            부가 확인 항목 2
            노선 번호가 같은 열차가 존재하는지 무결성 검사를 진행합니다.
            trainlist에 저장된 ticket 객체들의 노선 번호를 중복 없이 lineNumList에 저장하고 둘의 사이즈를 비교합니다.
            사이즈가 다르다면 중복된 노선 번호가 존재한다는 것이기 때문에 exception을 throw 합니다.
        */
        for (Ticket ticket : trainlist) {
            if (!lineNumList.contains(ticket.lineNum)) {
                lineNumList.add(ticket.lineNum);
            }
        }
        if(trainlist.size()!=lineNumList.size()){
            throw new FileIntegrityException("오류: 노선 번호가 같은 열차가 있습니다.");
        }

/*
    부가 확인 항목 3
    출발시간과 출발역 및 도착역이 같은 열차가 존재하는지 무결성 검사를 진행합니다.
    두 개의 for문을 사용해 (인덱스가 같은 경우를 제외하고) 출발시간과 출발역 및 도착역이 같은 열차가 존재한다면 exception을 throw 합니다.
*/
        for(int i=0;i<trainlist.size();i++){
            for(int j=0;j< trainlist.size();j++){
                if(i!=j){
                    if(     trainlist.get(i).depTime.equals(trainlist.get(j).depTime)
                            && trainlist.get(i).fromStation.getStation().equals(trainlist.get(j).fromStation.getStation())
                            && trainlist.get(i).toStation.getStation().equals(trainlist.get(j).toStation.getStation())
                    ){
                        throw new FileIntegrityException("오류: 출발시간과 출발역 및 도착역이 같은 열차가 있습니다.");
                    }
                }
            }
        }

    }

    public void reduceExtraSeat(String lineNum, int n) throws IOException {
        repos();
        Ticket ticket = getTicket(lineNum);
        ticket.extraSeat.reduceSeat(n);
        scan = new Scanner(new File(fileName));

        PrintWriter tempWriter = new PrintWriter(new FileWriter("temp.csv"));  // 파일의 내용을 지우고 수정하는 데 있어서 파일 삭제가 불가피했습니다.
        String newString = null;
        while(scan.hasNextLine()) {
            String[] strArr = scan.nextLine().split(",");
            StringBuilder newStr = new StringBuilder();
            if (strArr[0].equals(lineNum)) {
                int seatTemp = Integer.parseInt(strArr[6]);
                seatTemp = seatTemp - n;
                for (int i = 0; i < strArr.length; i++) {
                    if(i < strArr.length - 1) {
                        if (i == 6) {
                            newStr.append(seatTemp).append(",");
                        }
                        else {
                            newStr.append(strArr[i]).append(",");
                        }
                    }
                    else {
                        newStr.append(strArr[i]);
                    }
                }
                newString = newStr.toString();
            }
            else {
                for (int i = 0; i < strArr.length; i++) {
                    if(i < strArr.length - 1) {
                        newStr.append(strArr[i]).append(",");
                    }
                    else {
                        newStr.append(strArr[i]);
                    }
                }
                tempWriter.println(newStr);
            }
        }
        tempWriter.println(newString);
        tempWriter.close();

        File originalFile = new File(fileName);
        File tempFile = new File("temp.csv");
        Scanner scan = new Scanner(tempFile);

        PrintWriter writer = new PrintWriter(new FileWriter(originalFile));
        while(scan.hasNextLine()) {
            writer.println(scan.nextLine());
        }
        scan.close();
        writer.close();
    }

    public void repos() throws FileNotFoundException {
        lineList.clear();
        scan = new Scanner(new File("timeTable.csv"));
        lineList = new ArrayList<>();
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Line line=new Line();

            /*
                strArr[0]이 '노선 번호'의 문법 규칙을 만족하는 지 검사
                문법 규칙을 만족한다면 line.lineNum에 할당
                만족하지 않으면 FileIntegrityException을 throw
             */
            line.lineNum=strArr[0]; //노선 번호
            /*
                strArr[1]이 '시각' 문법 규칙을 만족하는지 검사
                문법 규칙을 만족한다면 값을 할당
                만족하지 않으면 FileIntegrityException을 throw
             */
            line.depTime=strArr[1]; //출발 시각
            try {
                ticket.fromStation=new Station(strArr[2]);
                ticket.arrivalTime=strArr[3];
                ticket.toStation=new Station(strArr[4]);
                ticket.price=new Price(strArr[5]);
                ticket.extraSeat=new Seat(strArr[6]);
                ticket.entireSeat=new Seat(strArr[7]);
            } catch (FileIntegrityException e) {
                throw new RuntimeException(e);
            }
            trainlist.add(ticket);
        }
    }

    public void increaseExtraSeat(String lineNum, int n) throws IOException {
        repos();
        Ticket ticket = getTicket(lineNum);
        ticket.extraSeat.reduceSeat(n);
        scan = new Scanner(new File(fileName));

        PrintWriter tempWriter = new PrintWriter(new FileWriter("temp.csv"));  // 파일의 내용을 지우고 수정하는 데 있어서 파일 삭제가 불가피했습니다.
        String newString = null;
        while(scan.hasNextLine()) {
            String[] strArr = scan.nextLine().split(",");
            StringBuilder newStr = new StringBuilder();
            if (strArr[0].equals(lineNum)) {
                int seatTemp = Integer.parseInt(strArr[6]);
                seatTemp = seatTemp + n;
                for (int i = 0; i < strArr.length; i++) {
                    if(i < strArr.length - 1) {
                        if (i == 6) {
                            newStr.append(seatTemp).append(",");
                        }
                        else {
                            newStr.append(strArr[i]).append(",");
                        }
                    }
                    else {
                        newStr.append(strArr[i]);
                    }
                }
                newString = newStr.toString();
            }
            else {
                for (int i = 0; i < strArr.length; i++) {
                    if(i < strArr.length - 1) {
                        newStr.append(strArr[i]).append(",");
                    }
                    else {
                        newStr.append(strArr[i]);
                    }
                }
                tempWriter.println(newStr);
            }
        }
        tempWriter.println(newString);
        tempWriter.close();

        File originalFile = new File(fileName);
        File tempFile = new File("temp.csv");
        Scanner scan = new Scanner(tempFile);

        PrintWriter writer = new PrintWriter(new FileWriter(originalFile));
        while(scan.hasNextLine()) {
            writer.println(scan.nextLine());
        }
        scan.close();
        writer.close();
    }
}
