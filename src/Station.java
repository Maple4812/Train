import java.util.regex.Pattern;

public class Station {
    private static final String REGEXP_PATTERN_NORMAL = "^[가-힣]*역$"; //xx역 형식
    private static final String REGEXP_PATTERN_WITHOUT = "^[가-힣]*$"; //xx 형식
    private static final String REGEXP_PATTERN_DOUBLE = "^[가-힣]*역역$"; //xx역역 형식 (방지하기 위함)
    String station;

    public Station(String str) throws FileIntegrityException {
        checkIntegrity(str);
        if(str.matches(REGEXP_PATTERN_WITHOUT) && !Pattern.matches(REGEXP_PATTERN_NORMAL, str)){
            str += "역";  //xx에 '역' 추가
            this.station = str;
        }
        else{
            this.station = str;
        }
    }

    public static void checkIntegrity(String str) throws FileIntegrityException {
        if(!Pattern.matches(REGEXP_PATTERN_NORMAL, str) || !Pattern.matches(REGEXP_PATTERN_WITHOUT, str)) {
            if(Pattern.matches(REGEXP_PATTERN_DOUBLE, str)) {
                throw new FileIntegrityException("역명 무결성 오류");
            }
        }

    }

    public String getStation() {
        return station;
    }
}
