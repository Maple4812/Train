import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class FileTimeTable implements FileInterface{
    private String fileName;
    public FileRail railFile;
    private ArrayList<Line> lineList = new ArrayList<>();//timetable.csv의 한 줄에 저장된 정보를 각 줄 마다 Line 객체로 묶어 저장
    Scanner scan;
    private final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");

    public FileTimeTable(String fileName, FileInterface railFile) throws FileNotFoundException {
        this.fileName = fileName;
        this.railFile = (FileRail) railFile;
    }

    public ArrayList<Line> getLineList(){return this.lineList;}

    public Line getLine(String str) throws FileIntegrityException {
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

        // 2024-06-14 수정
        // 기존 return null 이었지만
        // 이렇게 되면 null 값을 들고다니는 Ticket 객체가 생겨서
        // 다른 곳에서 NullPointerException 발생
        // return null 대신 exception 을 throw 해서
        // TempReservation 클래스에서 try catch 로 잡아내는걸로 수정함
        throw new FileIntegrityException();
    }

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        lineList.clear();
        scan= new Scanner(new File(fileName));
        ArrayList<String> lineNumList=new ArrayList<>(); // 노선 번호 중복을 체크하기 위해 노선 번호만 저장 할 리스트
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Line line= new Line();

            /*
                데이터 파일이 노선 번호, 출발 시각, <운행 정보 인덱스, 여석> ... 형태를 갖는지 확인합니다.
                이때 strArr의 개수는 4 이상의 짝수
                (한 Line이 최대 20개? 의 구간만 지날 수 있다는 조건이 있었던거 같은데 확인 필요. 일단 현재는 4 이상 짝수이면 모두 만족)
                (이 부분 수정 시 아래 repos도 수정해야함)
             */
            if( !((strArr.length>=4) && (strArr.length % 2 == 0))) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }

             /*
                line 객체안에 파일에서 받아온 strArr 요소 추가 후 무결성 검사
                lineList.add
                부가 확인 항목 1 : 각 구간이 이어지는지 무결성 검사 : line.checkIntegrity
             */

            line.lineNum=strArr[0];
            line.depTime=strArr[1];
            line.railList=new LinkedHashMap<>();

            try{
                for(int i=1;i<strArr.length/2;i++){
                    // 존재하는 구간인지 체크
                    if(railFile.getRailByIndex(Integer.parseInt(strArr[2*i]))==null)
                        throw new FileIntegrityException("오류: 존재하지 않는 구간을 지나는 열차가 있습니다.");
                    // 중복 구간인지 체크
                    if(line.railList.containsKey(railFile.getRailByIndex(Integer.parseInt(strArr[2*i])))){
                        throw new FileIntegrityException("오류: 같은 구간을 2회 이상 지나는 열차가 있습니다.");
                    }
                    line.railList.put(railFile.getRailByIndex(Integer.parseInt(strArr[2*i])),Integer.parseInt(strArr[2*i+1]));
                }
            }catch (NumberFormatException e){
                System.out.println("마지막 데이터 요소 이후 쉼표 이외의 문자가 존재합니다.");
                System.exit(0);
            }

            line.checkIntegrity(strArr[0]);

            lineList.add(line);
        }

        /*
            부가 확인 항목 2
            노선 번호가 같은 열차가 존재하는지 무결성 검사를 진행합니다.
            linelist에 저장된 line 객체들의 노선 번호를 중복 없이 lineNumList에 저장하고 둘의 사이즈를 비교합니다.
            사이즈가 다르다면 중복된 노선 번호가 존재한다는 것이기 때문에 exception을 throw 합니다.
        */
        for (Line line : lineList) {
            if (!lineNumList.contains(line.lineNum)) {
                lineNumList.add(line.lineNum);
            }
        }
        if(lineList.size()!=lineNumList.size()){
            throw new FileIntegrityException("오류: 노선 번호가 같은 열차가 있습니다.");
        }

        /*
            부가 확인 항목 3
            5분이내에 같은 시각에 출발 또는 도착하는 열차가 있는지 확인합니다.
            두 개의 for문을 사용해 (인덱스가 같은 경우를 제외하고) 출발시간과 출발역 및 도착역이 같은 열차가 존재한다면 exception을 throw 합니다.
        */



        for (Rail rail : railFile.getRaillist()) {

            // 해당 인덱스의 출발 시각을 저장
            ArrayList<String> depArr = new ArrayList<>();

            for (Line line : lineList) {

                // 만약 라인에 인덱스가 존재한다면
                if (line.railList.containsKey(rail)) {
                    depArr.add(line.calculateDepTime(rail.railIndex));
                }
            }

            // 배열에서 저장된 요소를 각각비교하여 5분이내면 exception을 throw 합니다.

            for(int i = 0;i<depArr.size();i++){
                for(int j=i;j<depArr.size();j++){

                    long Date1;
                    long Date2;
                    try {

                        Date1 = FORMATTER.parse(depArr.get(i)).getTime();
                        Date2 = FORMATTER.parse(depArr.get(j)).getTime();

                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(Date2 - Date1 < 5 * 60 * 1000L && Date2 - Date1 > -5 * 60 * 1000L && i != j){
                        throw new FileIntegrityException("오류: 노선이 동시에 겹치는 열차가 있습니다.");
                    }
                }
            }


        }



    }

    public void reduceExtraSeat(String lineNum, int startIdx, int endIdx, int n) throws IOException, FileIntegrityException {
        repos();
        Line line = getLine(lineNum);
        scan = new Scanner(new File(fileName));

        ArrayList<String> strToWrite = new ArrayList<>();
        String newString = null;
        while(scan.hasNextLine()) {
            String record = scan.nextLine();
            String[] strArr = record.split(",");
            StringBuilder sb = new StringBuilder();
            boolean inRange = false;
            if (strArr[0].equals(lineNum)) {
                for (Map.Entry<Rail, Integer> entry: line.railList.entrySet()) {
                    Rail key = entry.getKey();
                    if(key.equals(railFile.getRailByIndex(startIdx))) {
                        inRange = true;
                    }
                    // startIdx와 endIdx 사이일 때
                    if (inRange) {
                        // n 만큼 감소
                        line.railList.put(key, entry.getValue() - n);
                    }
                    if (key.equals(railFile.getRailByIndex(endIdx))) {
                        break;
                    }
                }
                // 새로운 레코드 작성
                sb.append(strArr[0]);
                sb.append(",");
                sb.append(strArr[1]);
                sb.append(",");

                int currentIndex = 0;
                for(Map.Entry<Rail, Integer> entry: line.railList.entrySet()) {
                    currentIndex++;
                    sb.append(entry.getKey().railIndex);
                    sb.append(",");
                    sb.append((entry.getValue()));
                    if(currentIndex < line.railList.size()) {
                        sb.append(",");
                    }
                }
                newString = sb.toString();
            }
            else {
                strToWrite.add(record);
            }
        }
        // 기존의 파일 내용 삭제
        PrintWriter writer = new PrintWriter(new FileWriter(fileName, false));
        // lineNum 이외의 레코드(Line) 작성
        for (String s: strToWrite) {
            writer.println(s);
        }
        // 여석 수가 변화한 레코드(Line) 작성
        writer.println(newString);
        writer.close();
        scan.close();
    }

    public void repos() throws FileNotFoundException, FileIntegrityException {
        /*
            기존에 여석수 늘리거나 줄이는 경우, 또는 getLine을 사용하는 경우 repos를 통해 최신 데이터 파일을 받아옴
         */
        lineList.clear();
        // 아래 부분을 checkIntegrity로 퉁쳐도 될것 같긴 한데 일단 그대로 두겠습니다.
        scan = new Scanner(new File(fileName));
        lineList = new ArrayList<>();
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Line line=new Line();
            if( !((strArr.length>=4) && (strArr.length % 2 == 0))) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            line.lineNum=strArr[0];
            line.depTime=strArr[1];
            line.railList=new LinkedHashMap<>();

            for(int i=1;i<strArr.length/2;i++){
                line.railList.put(railFile.getRailByIndex(Integer.parseInt(strArr[2*i])),Integer.parseInt(strArr[2*i+1]));
            }

            line.checkIntegrity(strArr[0]);

            lineList.add(line);
        }

    }

    public void increaseExtraSeat(String lineNum, int startIdx, int endIdx, int n) throws IOException, FileIntegrityException {
        repos();
        Line line = getLine(lineNum);
        scan = new Scanner(new File(fileName));

        ArrayList<String> strToWrite = new ArrayList<>();
        String newString = null;
        while(scan.hasNextLine()) {
            String record = scan.nextLine();
            String[] strArr = record.split(",");
            StringBuilder sb = new StringBuilder();
            boolean inRange = false;
            if (strArr[0].equals(lineNum)) {
                for (Map.Entry<Rail, Integer> entry: line.railList.entrySet()) {
                    Rail key = entry.getKey();
                    if(key.equals(railFile.getRailByIndex(startIdx))) {
                        inRange = true;
                    }
                    // startIdx와 endIdx 사이일 때
                    if (inRange) {
                        // n 만큼 증가
                        line.railList.put(key, entry.getValue() + n);
                    }
                    if (key.equals(railFile.getRailByIndex(endIdx))) {
                        break;
                    }
                }
                // 새로운 레코드 작성
                sb.append(strArr[0]);
                sb.append(",");
                sb.append(strArr[1]);
                sb.append(",");

                int currentIndex = 0;
                for(Map.Entry<Rail, Integer> entry: line.railList.entrySet()) {
                    currentIndex++;
                    sb.append(entry.getKey().railIndex);
                    sb.append(",");
                    sb.append((entry.getValue()));
                    if(currentIndex < line.railList.size()) {
                        sb.append(",");
                    }
                }
                newString = sb.toString();
            }
            else {
                strToWrite.add(record);
            }
        }
        // 기존의 파일 내용 삭제
        PrintWriter writer = new PrintWriter(new FileWriter(fileName, false));
        // lineNum 이외의 레코드(Line) 작성
        for (String s: strToWrite) {
            writer.println(s);
        }
        // 여석 수가 변화한 레코드(Line) 작성
        writer.println(newString);
        writer.close();
        scan.close();
    }
}