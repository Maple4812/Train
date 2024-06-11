import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Ticket {
    String reserveTime; // 예약 시각
    Line line;
    Client client; // 예약 사용자 정보
    String depTime, arrivalTime; //출발 시각, 도착 시각
    ArrayList<Rail> railIndices = new ArrayList<>();
    Price price; //가격
    private final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");

    public Ticket(){}

    //노선 번호 무결성 검사
    public void checkIntegrity() throws FileIntegrityException {
        // 1. railIndices 안에 있는 Rail 객체들의 나열이 실제 존재하는 노선 정보인지 확인

        // line 안에 해당 Rail 객체들의 나열이 존재하는지 확인하려면 우선 line 안에 LinkedHashMap 을 가져와야한다.
        // 해당 Rail 객체들을 ArrayList 에 저장하자. 어짜피 값을 변경하는 것은 아니기에 깊은 복사는 필요없다.
        ArrayList<Rail> lineIndices = new ArrayList<>();
        for(Rail r : line.railList.keySet()){
            lineIndices.add(r);
        }

        // 일치하는 Rail 객체의 수
        // 결과적으로 "mathchingSize == railIndices 의 길이" 이면 무결성 검사 통과이다.
        int mathchingSize = 0;
        boolean isMatched = false;
        // line 객체의 리스트를 돌면서...
        for(int i=0; i<lineIndices.size(); i++){
            // 동시에 railIndices 를 돈다.
            for(int j=0; j<railIndices.size();) {
                // 결과적으로 같지 않으면 오류!!
                if(mathchingSize == railIndices.size()) {
                    isMatched = true;
                    throw new FileIntegrityException();
                }
                // Array out of bounds 방지
                if(j >= railIndices.size())
                    break;
                // Rail 객체가 같으면 matchingSize 를 올려준다.
                if (lineIndices.get(i + j).railIndex == railIndices.get(j).railIndex) {
                    j++;
                    mathchingSize++;
                }
                // 다르면 처음부터 다시 맞춰나가니까 matchingSize 를 0 으로 초기화
                else {
                    mathchingSize = 0;
                    break;
                }
            }
        }
        if(!isMatched){
            System.out.println("입력한 노선 정보에 해당하는 노선이 없습니다.");
            throw new FileIntegrityException();
        }

        // 2. railIndices 안에 있는 Rail 객체들이 연결된 구간들인지 확인

        // railIndices 를 돌며 이전 Rail 의 도착역과 현재 Rail 의 출발역이 같은지 확인
        // 맨 처음 도착역 저장
//        String beforeStationName = railIndices.get(0).fromStation.getStation();
//        // 그 다음 역부터 방문
//        for(int i=1; i<railIndices.size(); i++){
//            // 이전 Rail 의 도착역과 다음 Rail 의 출발역의 이름이 다르면 에러!!
//            if(!beforeStationName.equals(railIndices.get(i).toStation.getStation()))
//                throw new FileIntegrityException();
//        }
        // 잘 넘어가면 성공!
    }

    public void checkIntegrityAboutTime(String nowTime) throws FileIntegrityException{
        // 시간과 관련된 무결성 검사
        // 현재 시각보다 이전 시각의 티켓을 구매하려 한다면 에러를 발생시키는 메서드

        // 현재 시각을 파라미터로 받아 무결성 검사...
        // 티켓의 출발 시각은 depTime 이용
        // 하지만 이때도 마찬가지로 depTime = NULL 이라면 해당 무결성 검사는 의미가 없음
        // 따라서 depTime 을 먼저 수정해주고 나서 해당 무결성 검사 메서드를 수행해야함

        try {
            long nowDate = FORMATTER.parse(nowTime).getTime();
            long depDate = FORMATTER.parse(depTime).getTime();

            // 현재 시각이 출발 시각보다 뒤 라면 에러 발생!!
            if(nowDate > depDate)
                throw new FileIntegrityException();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public int calculatePrice(){
        int total = 0;
        for(Rail r : railIndices){
            total += Integer.parseInt(r.price.getPrice());
        }

        return total;
    }

    public String calculateArrivalTime(){
        // 이 클래스의 depTime 은 해당 클래스의 객체를 만들면서 추가해야만 함
        // line 클래스에서 출발역에서 출발하는 시각을 받아와서 저장해놨다는 가정 하에 해당 메서드를 작성함!!
        // 만약 depTime = NULL 이면 작동하지 않음

        try {
            // 출발 시각을 long 타입으로 받아놓고...
            long depDate = FORMATTER.parse(depTime).getTime();
            // Rail 객체들을 돌면서 시간을 더함
            for(Rail r : railIndices){
                /*
                    duration만 형식이 달라서 parsing error 발생
                    임시로 처리해 두었습니다 - 정현
                 */
                String dur;
                int t= Integer.parseInt(r.duration);
                int h=t/60;
                int m=t%60;
                if(h<10 && m<10){
                    dur = "00000000"+"0"+h+"0"+m;
                }
                else if(h<10 && m>=10){
                    dur = "00000000"+"0"+h+m;
                }
                else if(h>=10 && m<10){
                    dur = "00000000"+h+"0"+m;
                }
                else{
                    dur = "00000000"+h+m;
                }
//                depDate += FORMATTER.parse(r.duration).getTime();
                depDate += FORMATTER.parse(dur).getTime();
                /*

                 */
            }
            // return
            return FORMATTER.format(new Date(depDate));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public int getFirstRailofTicket(){
        return railIndices.get(0).railIndex;
    }

    public int getLastRailofTicket(){
        return railIndices.get(railIndices.size() - 1).railIndex;
    }

    // Rail 객체들의 Rail index 들을 / 를 기준으로 하는 String 으로 return 해줌
    public String getRailIndicesToString(){
        String returnStr = "";
        for(int i=0; i<railIndices.size() - 1; i++){
            returnStr += String.valueOf(railIndices.get(i).railIndex) + "/";
        }
        returnStr += String.valueOf(railIndices.get(railIndices.size() - 1).railIndex);
        return returnStr;
    }

    //Ticket 정보 출력하기 위한 toString
    @Override
    public String toString() {
        return this.getLineNum() + " " + depTime + " " + railIndices.get(0).fromStation.toString() + " " + arrivalTime + " " + railIndices.get(railIndices.size()-1).toStation.toString();
    }

    public String getReserveTime(){return reserveTime;}

    public void setReserveTime(String reserveTime){this.reserveTime = reserveTime;}

    //lineNum getter
    public String getLineNum(){return line.lineNum;}
}
