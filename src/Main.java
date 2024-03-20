import java.util.Scanner;

public class Main {
    static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        System.out.print("현재 날짜와 시간을 입력해주세요: ");
        Date currentDate = new Date(scan.next());
        currentDate.printDate();
    }
}
