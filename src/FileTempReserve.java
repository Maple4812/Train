import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class FileTempReserve implements FileInterface{
    private String fileName;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    private ArrayList<ArrayList<String>> tempList;
    public FileTempReserve(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }
    Scanner scan = new Scanner(new File(fileName));
    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다은 split
            if(strArr.length != 6) {
                throw new FileIntegrityException("무결성 오류: 파일에 인자의 개수가 옳지 않은 레코드가 존재합니다.");
            }
            UserName.checkIntegrity(strArr[0]);  //사용자 이름 무결성 확인
            PhoneNumber.checkIntegrity(strArr[1]);  //사용자 이름 무결성 확인
            Ticket.checkIntegrity(strArr[2]);  //사용자 이름 무결성 확인
            Time.checkIntegrity(strArr[3]);  //출발 시각 무결성 확인
            Time.checkIntegrity(strArr[4]);  //예약 시각 무결성 확인
            Time.checkIntegrity(strArr[5]);  //예약 컴퓨터 시각 무결성 확인
        }
    }

    private void repos(){}

    public void write(){}

    public boolean isTimeOn()
    {
        // 리스트가 비어있으면 timer 가 false
        return !tempList.isEmpty();
    }

    public String getNewTime() {
        /*
        기능 :
            tempList 에 저장되어있는 예약 시간중 가장 최근에 가까운 시간을 String 의 형태로 return
            formatter.parse 를 할 때 생길 수 있는 ParseException 을 throws 한다.
            해당 메소드는 timer 가 true 일 때, LogInAndTimeInput 클래서에서 처음 시간을 받는 경우에만 사용된다.
        */

        /*
        변수 :
            @param lastestDate: 가장 최근 예약날짜, 1970년 1월 1일 0시 0분 0초로 초기화한다.
            @param lastestComputerDate: 가장 최근 예약컴퓨터날짜, 1970년 1월 1일 0시 0분 0초로 초기화한다.
            @param reserveDate: record 들의 예약 날짜를 담는 변수
            @param reserveComputerDate: record 들의 예약 컴퓨터 날짜를 담는 변수
            @param SavedNowComputerDate: UserInfo 에 저장되어있는 컴퓨터시각, 타이머가 켜진 경우에만 함수가 호출되니
                                                타이머가 켜진상태에서 프로그램을 사용자가 킨 순간의 컴퓨터시각이 된다.
         */
        Date lastestDate = new Date(0);
        Date lastestComputerDate = new Date(0);

        try {
            // parsing 할 때 생길 수 있는 error 때문에 try catch 문 안에서 수행해준다.
            // 하지만 parsing 할 수 없으면 애초에 무결성 검사에서 걸러지기 때문에
            // error 가 catch 되는 경우는 없다.

            for(ArrayList<String> record : tempList)
            {
                Date reserveDate = formatter.parse(record.get(4));
                Date reserveComputerDate = formatter.parse(record.get(5));
                if(lastestDate.before(reserveDate))
                {
                    lastestDate = reserveDate;
                    lastestComputerDate = reserveComputerDate;
                }
            }

            Date SavedNowComputerDate = formatter.parse(LogInAndTimeInput.getNowComputerTime());

            // 프로그램을 킨 순간의 컴퓨터 시각과 예약된 가장 최근의 컴퓨터시각을 비교하기 위해 .getTime() 을 호출한다.
            long time1 = SavedNowComputerDate.getTime();
            long time2 = lastestComputerDate.getTime();

            // 시각의 차이를 구한다. 이는 지나간 시각이다.
            long timeDiff = time1 - time2;

            // 가장 최근 예약시각에 지나간 시각을 더하기 위해 .getTime() 을 호출한다.
            long time3 = lastestDate.getTime();

            // 가장 최근 예약시각 + 지나간 시각
            lastestDate = new Date(timeDiff + time3);

            // 12자리 문자열의 형태로 변환 후 return 한다.
            return formatter.format(lastestDate);
        }catch (ParseException ignore)
        {
            // 만약 프로그램 동작 도중 데이터의 변환으로 인해 parsing error 가 발생한다면
            // 시각은 새롭게 고쳐지지 않고 현재 시각은 변경되지 않는다.
            System.out.println("ParsingError!");
        }

        return null;
    }

    public void removeTimeOutReserve() {
        // 기능 : 5분이 지난 예약들을 삭제해준다.

        // 타이머가 꺼진 경우 수행하지 않는다.
        if(tempList.isEmpty())
            return;

        // 저장되어있는 현재 시각을 불러온다
        Date savedNowDate = formatter.parse(LogInAndTimeInput.getNowTime());
        long NowTime = savedNowDate.getTime();

        try{
            for(ArrayList<String> record: tempList)
            {
                // 저장된 예약 시각들을 받는다.
                Date reserveDate = formatter.parse(record.get(4));
                long reserveTime = reserveDate.getTime();

                // 현재시각과 예약시각의 차이를 분 단위로 구한다.
                long diff = (NowTime - reserveTime) / (1000 * 60);
                if(diff > 5)
                    // 차이가 5 분 보다 크다면 삭제한다.
                    tempList.remove(record);
            }
        }catch (ParseException ignored){
            // 만약 프로그램 동작 도중 데이터의 변환으로 인해 parsing error 가 발생한다면
            // 예약들은 새로고침되지 않는다.
            System.out.println("ParsingError!");
        }
    }
}
