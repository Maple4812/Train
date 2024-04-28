import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class LogInAndTimeInput {
    private static String nowComputerTime;
    private static String nowTime;
    private static Client client;

    public static void setNowComputerTime(String time) {
        nowComputerTime = time;
    }

    public static String getNowComputerTime() {
        return nowComputerTime;
    }

    public static void setNowTime(String time) {
        nowTime = time;
    }

    public static String getNowTime() {
        return nowTime;
    }

    public static Client getClient() {
        return client;
    }

    public void init() {
        Scanner scanner = new Scanner(System.in);
        boolean isValidInput = false;
        while (!isValidInput) {
            System.out.println("사용자 이름과 전화번호를 입력하세요 (이름,전화번호):");
            String input = scanner.nextLine();
            String[] userInfo = input.split(",");
            if (userInfo.length != 2) {
                System.out.println("잘못된 입력입니다. 사용자 이름과 전화번호를 쉼표로 구분하여 입력하세요.");
                continue;
            }
            String userName = userInfo[0];
            String phoneNumber = userInfo[1];

            // 이름 예외 처리
            try {
                UserName.checkIntegrity(userName); // 이름이 조건을 충족하는지 확인
            } catch (FileIntegrityException e) {
                System.out.println("이름 형식이 올바르지 않습니다. 사용자 이름은 길이가 1 이상인 한글로만 이루어진 문자열이어야 합니다.");
                continue;
            }

            // 전화번호 예외 처리
            try {
                PhoneNumber.checkIntegrity(phoneNumber); // 전화번호가 조건을 충족하는지 확인
            } catch (FileIntegrityException e) {
                System.out.println("전화번호 형식이 올바르지 않습니다. 전화번호는 '010'으로 시작하고 총 11자리의 숫자여야 합니다.");
                continue;
            }

            client = new Client(userName, phoneNumber);

            // 입력값이 모두 유효한 경우에만 반복문 탈출
            isValidInput = true;
        }

        // TempReserve의 타이머가 꺼져있는 경우
        if (!TempReservation.isTimerOn()) {
            setNowComputerTime(getTime());
            System.out.println("현재 시간을 입력하세요 (yyyyMMddHHmm 형식):");
            String time = scanner.nextLine();
            setNowTime(time);
        } else { // TempReserve의 타이머가 켜져있는 경우
            setNowComputerTime(getTime());
            setNowTime(TempReservation.getNewTime());
        }

        // FileUserInfo에 클라이언트 정보 저장
        FileUserInfo fileUserInfo = new FileUserInfo("userinfo.csv");
        fileUserInfo.write(client.getName(), client.getPhoneNumber());
    }

    public void repos(String file) {
        // repos 메소드의 내용은 그대로 유지
    }

    private static String getTime() {
        // 현재 시간을 yyyyMMddHHmm 형식으로 반환
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        return formatter.format(date);
    }
}
