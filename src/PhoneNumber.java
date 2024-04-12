import java.util.regex.Pattern;

public class PhoneNumber {
    private static final String REGEXP_PATTERN_NORMAL = "010[0-9]{8}"; //010xxxxxxxx 형식
    String phoneNumber;

    public PhoneNumber(String str) throws FileIntegrityException {
        checkIntegrity(str);
        this.phoneNumber = str;
    }

    public static void checkIntegrity(String str) throws FileIntegrityException {
        if(!Pattern.matches(REGEXP_PATTERN_NORMAL, str)) {
            throw new FileIntegrityException("전화번호 무결성 오류");
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
