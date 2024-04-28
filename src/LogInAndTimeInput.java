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

        // 이전에 입력된 값들을 삭제하는 코드 추가
        clearPreviousData("UserInfo.csv");
        
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
                System.out.println("이름 형식이 올바르지 않습니다.");
                continue;
            }

            // 전화번호 예외 처리
            try {
                PhoneNumber.checkIntegrity(phoneNumber); // 전화번호가 조건을 충족하는지 확인
            } catch (FileIntegrityException e) {
                System.out.println("전화번호 형식이 올바르지 않습니다.");
                continue;
            }

            client = new Client(userName, phoneNumber);

            // 입력값이 모두 유효한 경우에만 반복문 탈출
            isValidInput = true;
        }

        // TempReserve의 타이머가 꺼져있는 경우
        if (!TempReservation.isTimerOn()) {
            setNowComputerTime(getTime());
            String time = inputTime(scanner);
            if (time == null) return; // // TempReserve의 타이머가 켜져있는 경우
            setNowTime(time);
        } else {
            setNowComputerTime(getTime());
            setNowTime(TempReservation.getNewTime());
        }
    }

    private String inputTime(Scanner scanner) {
    System.out.println("현재 시간을 입력하세요 (yyyyMMddHHmm 형식):");
    String time = scanner.nextLine();
    try {
        // 입력된 값이 12자리의 숫자인지 확인
        if (time.matches("\\d{12}")) {
            return time;
        } else {
            throw new FileIntegrityException("시간 형식이 올바르지 않습니다.");
        }
    } catch (FileIntegrityException e) {
        System.out.println(e.getMessage());
        return null; // 예외 처리된 경우 null 반환
    }
}


    public void repos(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            String line;
            boolean found = false; // 클라이언트 정보가 이미 파일에 있는지 여부를 나타내는 플래그
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(client.getName()) && parts[1].equals(client.getPhoneNumber())) {
                    found = true;
                    break;
                }
            }
            if (!found) { // 클라이언트 정보가 파일에 없는 경우
                bw.write(client.getName() + "," + client.getPhoneNumber() + "," + nowComputerTime + "," + nowTime + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTime() {
        // 현재 시간을 yyyyMMddHHmm 형식으로 반환
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        return formatter.format(date);
    }

    //이전 입력값들 삭제하는 코드 추가가
    private void clearPreviousData(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("이전 데이터 삭제 완료: " + fileName);
            }
        }
    }
    
}
