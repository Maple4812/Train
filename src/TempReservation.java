import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TempReservation {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    private static FileTempReserve tempReserveFile;
    private FileUserInfo userInfoFile;
    private FileReserve reserveFile;
    private FileTimeTable timeTableFile;

    public TempReservation(FileInterface userInfoFile, FileInterface reserveFile, FileInterface tempReserveFile, FileInterface timeTableFile)
    {
        this.tempReserveFile = (FileTempReserve) tempReserveFile;
        this.userInfoFile = (FileUserInfo) userInfoFile;
        this.reserveFile = (FileReserve) reserveFile;
        this.timeTableFile = (FileTimeTable) timeTableFile;
    }
    public void init() {
    }

    public static boolean isTimerOn() {
        return tempReserveFile.isTimeOn();
    }

    public static String timeRenewal() {
        /*
        기능 :
            LogInAndTimeInput 에 저장되어있는 컴퓨터 시각과 함수가 호출된 시각의 차이를 구하고
            기존에 저장되어 있던 시각과 더해서 새로운 시각을 return
        */

        /*
        변수 :
            @param savedNowComputerDate: 저장되어있는 현재의 컴퓨터 시각
            @param nowComputerDate: 현재 컴퓨터 시각
            @param savedNowDate: 저장되어있는 현재 시각
            @param time1: 저장되어있는 현재의 컴퓨터 시각의 getTime 버젼
            @param time2: 현재의 컴퓨터 시각의 getTime 버젼
            @param nowTime: 저장되어있는 현재 시각의 getTime 버젼
            @param diff: 지나간 시간
         */

        Date savedNowComputerDate = formatter.parse(LogInAndTimeInput.getNowComputerTime());
        Date nowComputerDate = new Date();
        Date savedNowDate = formatter.parse(LogInAndTimeInput.getNowTime());

        long time1 = savedNowComputerDate.getTime();
        long time2 = nowComputerDate.getTime();
        long nowTime = savedNowDate.getTime();

        long diff = time2 - time1;
        nowTime += diff;

        return formatter.format(new Date(nowTime));
    }

    public static String getNewTime() {
        // 기능 : 타이머가 켜진경우의 새로운 현재 시각을 return 해준다.
        return tempReserveFile.getNewTime();
    }

    public static void removeTimeOutReserve()
    {
        // 기능 : 5분이 지난 예약들을 현재 시각을 기준으로 해서 삭제해준다.
        tempReserveFile.removeTimeOutReserve();
    }
}
