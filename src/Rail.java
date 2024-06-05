public class Rail {
    int railIndex; //운행 정보 인덱스
    Station fromStation, toStation; //출발역, 도착역
    String duration; // 소요 시간
    Price price; // 가격

    public Rail(int railIndex, Station fromStation, Station toStation, String duration, Price price) {
        this.railIndex = railIndex;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.duration = duration;
        this.price = price;
    }

    /*
        1. 설계 문서에는 public static void checkIntegrity()로 되어 있고, 여기서 모든 인자와 경우를 검사한다고 되어 있음
        2. 여기서 설계 문서대로 작동하려면 int 인자를 추가해 railIndex만 checkIntegrity로 검사하고 나머지는 FileRail에서 따로 검사
     */
    public static void checkIntegrity() throws FileIntegrityException{


    }


}
