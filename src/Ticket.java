import java.util.regex.Pattern;

public class Ticket {
    static String REGEXP_PATTERN_LINE = "^[A-Z][0-9]{4}$"; // 노선번호 정규표현식
    String lineNum, depTime, arrivalTime; //노선번호, 출발 시각, 도착 시각
    Station fromStation, toStation; // 출발역, 도착역
    Price price; //가격
    Seat extraSeat, entireSeat; //여석 수, 전체 좌석 수

    public Ticket(){}

    //노선 번호 무결성 검사
    public static void checkIntegrity(String str) throws FileIntegrityException {
        if(!Pattern.matches(REGEXP_PATTERN_LINE, str)) {
            throw new FileIntegrityException("노선번호 무결성 오류");
        }
    }

    //Ticket 정보 출력하기 위한 toString

    @Override
    public String toString() {
        return lineNum + " " + depTime + " " + fromStation.getStation() + " " + arrivalTime + " " + toStation.getStation();
    }
}
