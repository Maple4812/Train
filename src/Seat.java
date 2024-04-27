import java.util.regex.Pattern;

public class Seat {
    /*
        여석 수는 0이 될 수 있지만 전체 좌석 수는 0이 될 수 없습니다.
        '1~999'와 '0' 두 개의 패턴으로 나누어 무결성검사를 진행합니다.
    */
    private static final String REGEXP_PATTERN_NORMAL="^[1-9][0-9]{0,2}$";
    private static final String REGEXP_PATTERN_ZERO="^0$";
    String seat;

    public Seat(String seat) throws FileIntegrityException{
        checkIntegrity(seat);
        this.seat = seat;
    }

    public static void checkIntegrity(String str) throws FileIntegrityException {
        if(!Pattern.matches(REGEXP_PATTERN_NORMAL, str)) {
            if(!Pattern.matches(REGEXP_PATTERN_ZERO,str)) {
                throw new FileIntegrityException("좌석 수 무결성 오류");
            }
        }
    }

    public void reduceSeat(int n) {
        int temp = Integer.parseInt(seat);
        for (int i = 0; i < n; i++) {
            temp--;
        }
        this.seat = Integer.toString(temp);
    }

    public int getSeat() {
        return Integer.parseInt(seat);
    }
}
