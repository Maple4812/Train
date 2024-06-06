public class Rail {
    int railIndex; //운행 정보 인덱스
    Station fromStation, toStation; //출발역, 도착역
    String duration; // 소요 시간
    Price price; // 가격

    public Rail() {}

    /*
        1. 설계 문서에는 public static void checkIntegrity()로 되어 있고, 여기서 모든 인자와 경우를 검사한다고 되어 있음
        2. 여기서 설계 문서대로 작동하려면 int 인자를 추가해 railIndex만 checkIntegrity로 검사하고 나머지는 FileRail에서 따로 검사
     */
    public void checkIntegrity() throws FileIntegrityException{
        if (railIndex == 0) { //railIndex가 0인 경우
            throw new FileIntegrityException("무결성 오류: railIndex는 0이 될 수 없습니다.");
        }

        if (fromStation.equals(toStation)){ //출발역과 도착역이 동일한 경우
            throw new FileIntegrityException("무결성 오류: 출발역과 도착역이 동일합니다.");
        }

        //나머지 요소에 대해 무결성 검사
        Station.checkIntegrity(fromStation.getStation());
        Station.checkIntegrity(toStation.getStation());
        Price.checkIntegrity(price.getPrice());
        Time.checkIntegrity(duration);

    }


}
