import java.util.regex.Pattern;

public class UserName{
    private static final String REGEXP_PATTERN_NAME = "[가-힣]{1,}"; //길이가 1 이상인 한글로만 이루어진 문자열
    String name;
    UserName(String str) {
        name = str;
    }

    public static void checkIntegrity(String str) throws FileIntegrityException {
        if(!Pattern.matches(REGEXP_PATTERN_NAME, str)) {
            throw new FileIntegrityException("사용자 이름 무결성 오류");
        }
    }

    public String getUserName() {
        return this.name;
    }
}
