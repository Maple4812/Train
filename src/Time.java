import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Time { //이 클래스는 "아마도" integrity 확인을 위한 용도로만 쓸 예정입니다
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    public static void checkIntegrity(String str) throws FileIntegrityException {
        boolean isValidDateTime = isValidGregorianDateTime(str);
        if(!isValidDateTime || str.length() != 12) {
            throw new FileIntegrityException("시각 무결성 오류");
        }
    }

    private static boolean isValidGregorianDateTime(String dateTimeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        dateFormat.setLenient(false); // 엄격한 파싱 설정

        try {
            Date date = dateFormat.parse(dateTimeString);
            return date != null; // null이 아니면 유효한 그레고리력 시각으로 간주
        } catch (ParseException e) {
            return false; // ParseException이 발생하면 올바르지 않은 시각으로 간주
        }
    }
}
