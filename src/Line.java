import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Line {
    static String REGEXP_PATTERN_LINE = "^[A-Z][0-9]{4}$"; //노선 번호 문법 규칙
    String lineNum, depTime, arrivalTime; //노선 번호, 출발 시각, 도착 시각
    LinkedHashMap<Rail, Integer> railList; // <운행 정보, 여석 수>

    public Line() {}

    public static void checkIntegrity(String str) throws FileIntegrityException{

    }


    /*
        1. public static으로 해야하는지? 만약 static으로 해야하면 설계문서 수정해야 할 듯
    */
    public ArrayList<Rail> slicing(String fromstation, String tostation){
        //1. 데이터 파일의 각 줄을 Line 객체로 저장한 lineList를 받아옴
        //2. 각 Line이 지나는 Rail에 대해, fromstation과 일치하는 rail.fromStation이 존재하는지 체크
        //3. 각 Line이 지나는 Rail에 대해, tostation과 일치하는 rail.toStation이 존재하는지 체크
        //4. 2, 3번을 만족한다면 railList 내에서 3번 Rail 객체가 2번 Rail 객체보다 뒤에 있는지 체크
        //5. 4번까지 만족한다면 2번 Rail부터 3번 Rail까지의 모든 Rail 객체를 ArrayList로 반환
        //6. Ticket 객체로 변환 및 사용자가 입력한 출발 시각과의 비교는 CheckTimeTable 내에서 수행
        return null;
    }
}
