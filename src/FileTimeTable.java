import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class FileTimeTable implements FileInterface{
    private String fileName;
//    private FileWriter fw; // 예약 시 여석 수가 줄어드는 걸 csv 파일에 업데이트해야함
//    private PrintWriter writer;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    private ArrayList<Ticket> trainlist;
    Scanner scan = new Scanner(new File(fileName));

    public FileTimeTable(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }

    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Ticket ticket=new Ticket();
            if(strArr.length != 8) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            UserName.checkIntegrity(strArr[0]);  //노선번호 무결성 확인
            Time.checkIntegrity(strArr[1]);  //출발 시각 무결성 확인
            Station.checkIntegrity(strArr[2]);  //출발역 무결성 확인
            Time.checkIntegrity(strArr[3]);  //도착 시각 무결성 확인
            Station.checkIntegrity(strArr[4]);  //도착역 무결성 확인
//            가격.checkIntegrity(strArr[5]);  //가격 무결성 확인, Ticket 객체에서 노선 번호와 함께 무결성검사 할 것인지, 따로 price 객체를 만들지?
            Seat.checkIntegrity(strArr[6]);  //여석 수 무결성 확인
            Seat.checkIntegrity(strArr[7]);  //전체 좌석 수 무결성 확인

            if(strArr[2].equals(strArr[4])){ // 부가 확인 항목: 출발역과 도착역이 같은 열차가 존재하는 경우
                throw new FileIntegrityException("오류: 출발역과 도착역이 같은 열차가 있습니다.");
            }

            ticket.lineNum=strArr[0];
            ticket.depTime=strArr[1];
            ticket.fromStation=new Station(strArr[2]);
            ticket.arrivalTime=strArr[3];
            ticket.toStation=new Station(strArr[4]);
//            ticket.price=Integer.parseInt(strArr[5]);
            ticket.extraSeat=new Seat(strArr[6]);
            ticket.entireSeat=new Seat(strArr[7]);

            trainlist.add(ticket);

        }

    }
}
