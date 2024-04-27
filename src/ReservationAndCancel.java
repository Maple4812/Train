import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReservationAndCancel {
    /**
     * repos로 파일들을 불러와서 tempReservation, Reservation 에 ArrayList 형태로 저장
     * @author 변수혁
     */
    private FileTempReserve fileTempReserve;
    private FileReserve fileReserve;
    //private FileTimeTable fileTimeTable;
    private ArrayList<String[]> tempReservationList;
    private ArrayList<String[]> reservationList;

    public ReservationAndCancel(FileInterface fileTempReserve, FileInterface fileReserve) {
        this.fileReserve = (FileReserve) fileReserve;
        this.fileTempReserve = (FileTempReserve) fileTempReserve;
    }

    public void init() {
        Client client = new Client();
        String clientName = client.getName();
        String clientPhoneNumber = client.getPhoneNumber();
        this.tempReservationList = new ArrayList<>(readCsv(((FileTempReserve) fileTempReserve).getFileName(), 6)); // 파일 형식에 따라 개수 수정 필요
        this.reservationList = new ArrayList<>(readCsv(((FileReserve) fileReserve).getFileName(), 4)); // 파일 형식에 따라 개수 수정 필요
        System.out.println("가예약 및 예약 취소 메뉴입니다.");
        //Client 객체 생성 및 출력(loginAndTimeInput에서 받아옴)
        System.out.println("홍길동" + " / " + clientPhoneNumber + "님의 예약 정보입니다.");
        System.out.println("행 번호 / 노선 번호 / 출발 시간 / 출발 역 / 도착 시간 / 도착 역");
        if(!tempReservationList.isEmpty()) {
            printReserveList(tempReservationList);
        }
        if(!reservationList.isEmpty()) {
            printReserveList(reservationList);
        }

    }


    public void printReserveList(ArrayList<String[]> reservationList) {
        if (reservationList.isEmpty()) {
            System.out.println("예약 정보가 없습니다.");
            return;
        }
        Client client = new Client();
        String clientName = "홍길동";

        System.out.println(clientName + "님이 예약한 열차 목록:");
        int i = 1;

        for (String[] row : reservationList) {
            if (row[0].equals(clientName)) {
                System.out.println("#" + (i) + " "+String.join(" / ", row));
                i++;
            }
        }
    }

    // 각 줄마다 String배열에 저장후 각 배열을 ArrayList에 저장
    public List<String[]> readCsv(String csvFile, int rowLength) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            if (br.ready()) { // BOM 제거 과정
                br.mark(1);
                if (br.read() != 0xFEFF) {
                    br.reset(); // BOM 없음
                }
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                if (row.length == rowLength) { // 파일 형식에 따라 개수 수정 필요
                    data.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    // 취소 입력 구현
    // 취소 수수료 계산

}
