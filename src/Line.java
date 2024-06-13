import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Line {
    static String REGEXP_PATTERN_LINE = "^[A-Z][0-9]{4}$"; //노선 번호 문법 규칙
    String lineNum, depTime; //노선 번호, 출발 시각
    LinkedHashMap<Rail, Integer> railList; // <운행 정보, 여석 수>
    private final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");

    public Line() {}

    // static 삭제했습니다.
    public void checkIntegrity(String str) throws FileIntegrityException{
        // 노선 번호 형식 확인
        if (!Pattern.matches(REGEXP_PATTERN_LINE, str)) {
            throw new FileIntegrityException("무결성 오류: 노선 번호가 올바른 형식이 아닙니다.");
        }

        // railList에서 같은 Rail 객체가 두 개 이상 존재하는지 확인
        ArrayList<Rail> railArrayList = new ArrayList<>(railList.keySet());
        for (int i = 0; i < railArrayList.size() - 1; i++) {
            for (int j = i + 1; j < railArrayList.size(); j++) {
                if (railArrayList.get(i).equals(railArrayList.get(j))) {
                    throw new FileIntegrityException("무결성 오류: railList에 동일한 Rail 객체가 두 개 이상 존재합니다.");
                }
            }
        }

        // railList의 운행 구간에서 선행하는 rail의 도착역과 바로 뒤의 rail의 출발역이 같지 않은지 확인
        // 정현: 이 부분 getStationList 사용하면 될 거 같아서 일단 주석으로 해두겠습니다.
//        for (int i = 0; i < railArrayList.size() - 1; i++) {
//            Rail currentRail = railArrayList.get(i);
//            Rail nextRail = railArrayList.get(i + 1);
//            if (!currentRail.toStation.equals(nextRail.fromStation)) {
//                throw new FileIntegrityException("무결성 오류: 선행하는 rail의 도착역과 바로 뒤의 rail의 출발역이 같지 않습니다.");
//            }
//        }
        getStationList(); //getStationList 내의 throw FileIntegrityException이 연결되는 구간인지 체크하기 때문

        // 각 rail의 인덱스가 rail.csv 파일 내에 존재하는지 확인
        // FileRail의 getRailByIndex가 static이 아니어서 오류가 생기는 것 같습니다.(추후 논의 필요)
        // 일단 try catch 문으로 묶어서 filerail 생성
        try{
            FileRail filerail = new FileRail("rail.csv");
            filerail.checkIntegrity(); //checkIntegrity를 통해 여기서 만든 filerail의 raillist를 생성
            for (Rail rail : railArrayList) {
                if (filerail.getRailByIndex(rail.railIndex) == null) {
                    throw new FileIntegrityException("무결성 오류: rail.csv 파일에 존재하지 않는 rail 인덱스가 포함되어 있습니다.");
                }
            }
        }catch (FileNotFoundException e){
            throw new FileIntegrityException("오류: rail.csv를 읽어오는중 문제 발생");
        }

    }


    /*
        1. public static으로 해야하는지? 만약 static으로 해야하면 설계문서 수정해야 할 듯
    */
    public ArrayList<Rail> slicing(String fromstation, String tostation) throws FileIntegrityException {
        //Ticket 객체로 변환 및 사용자가 입력한 출발 시각과의 비교는 CheckTimeTable 내에서 수행
        ArrayList<String> stationList = getStationList(); //이 Line이 지나는 역을 순서대로 반환
        ArrayList<Rail> slicedList = new ArrayList<>(); //return 할 arraylist

        /*
            입력 받은 출발역과 도착역이 이 Line에 존재하고, 선후관계도 일치하는 경우 Rail 객체의 list를 반환
         */
        if(stationList.contains(fromstation) && stationList.contains(tostation)
                && (stationList.indexOf(fromstation)<stationList.indexOf(tostation))){
            int startIdx = stationList.indexOf(fromstation);
            int endIdx = stationList.indexOf(tostation);
            int i = 0;
            for (Map.Entry<Rail, Integer> entry : railList.entrySet()){
                /*
                    서울-대전-대구-부산 (1/2/3)
                    slicing(대전,부산)
                    -> stationList : 서울, 대전, 대구, 부산
                    -> railList : (서울, 대전), (대전, 대구), (대구, 부산)
                    -> startIdx : 1 (대전)
                    -> endIdx : 3 (부산)
                    -> 우리가 가져와야하는 구간은 railList의 인덱스 1, 2에 해당하는 (대전, 대구), (대구, 부산)
                    -> 즉 railList의 startIdx부터 endIdx-1 까지 Rail 객체를 slicedList에 저장함
                 */
                if(i>=startIdx && i<=endIdx-1){
                    slicedList.add(entry.getKey());
                }
                i++;
            }
            return slicedList;
        }

        /*
            입력 받은 출발역과 도착역이 이 Line에 존재하지 않거나, 선후관계가 맞지 않으면 null을 반환
         */
        else{
            return null;
        }

    }

    public ArrayList<ArrayList<Rail>> NEWslicing(String fromstation, String tostation) throws FileIntegrityException{
        /*
            정현: 2024.06.11.02:24 완성...
            case : 서울-강릉-대전-서울-대전-대구-대전-부산-대전... 에서 서울~대전을 뽑아내는경우
                    0   1    2   3   4   5   6   7    8      [0,2] [0,4] [0,6] [0,8] [3,4] [3,6] [3,8]
                   그럼 출발역이랑 같은 역 정보가 들어있는 인덱스만 알아내서 각 인덱스별로 뒤에 대전 있는지 이중 for문 돌리면 되는거 아닌가?
                   서울:[0,3]
                   대전:[2,4,6,8]
                   stationList: 서울-강릉-대전-서울-대전-대구-대전-부산-대전
                   stationCNT: {서울=[0, 3], 강릉=[1], 대전=[2,4,6,8], 대구=[5], 부산=[7]}
         */
        ArrayList<String> stationList = getStationList(); //이 Line이 지나는 역을 순서대로 반환
        Map<String, Set<Integer>> stationCNT = new LinkedHashMap<>(); // 특정역을 몇번째로 지나는지 저장(위 예시 참고)
        ArrayList<ArrayList<Rail>> slicedList = new ArrayList<>(); // 이 메소드가 return 할 arraylist


        for (int i = 0; i < stationList.size(); i++) {
            String value = stationList.get(i);
            stationCNT.computeIfAbsent(value, k -> new LinkedHashSet<>())
                    .add(i); //computeIfAbsent는 Map에서 특정 키에 해당하는 값이 존재하지 않는다면 새로 만들어서 추가해줌. 이때 Key는 역 이름, value는 인덱스
        }

        if(stationCNT.containsKey(fromstation) && stationCNT.containsKey(tostation)){
            /*
                지나는 역 중에 fromstation과 동일한 출발역, tostation과 동일한 도착역이 모두 존재하는 경우
             */
            for(int startIdx : stationCNT.get(fromstation)){ //startIdx: 서울 [0,3]
                for(int endIdx : stationCNT.get(tostation)){ //endIdx: 대전 [2,4,6,8]
                    if(startIdx<endIdx){
                        /*
                            모든 fromstation과 동일한 출발역에 대해,
                            출발역보다 뒤에 tostation과 동일한 도착역이 존재한다면 slicing해서 반환할 arraylist에 저장
                         */
                        ArrayList<Rail> tempList = new ArrayList<>(); //임시 ArrayList
                        int i=0;
                        for (Map.Entry<Rail, Integer> entry : railList.entrySet()){
                            /*
                                startIdx=0 이고 endIdx=2인 경우
                                (서울, 강릉) 구간과 (강릉, 대전) 구간을 저장해야함 (즉 railList의 인덱스 0번과 1번을 저장해야함)
                                즉 startIdx부터 endIdx-1 까지의 Rail 정보를 저장
                             */
                            if(i>=startIdx && i<=endIdx-1){
                                tempList.add(entry.getKey());
                            }
                            i++;
                        }
                        slicedList.add(tempList); // 이 메소드가 return 할 slicedList에 저장
                    }
                }
            }
            /*
                fromstation과 일치하는 출발역 정보와 tostation과 일치하는 도착역 정보가 존재하지만 slicedList에 아무것도 저장되지 않은 경우,
                즉 startIdx>=endIdx인 경우들만 존재하는 경우 null을 반환한다.
                예) 대전-서울 을 지나는 Line에 대해 Line.slicing(서울, 대전)을 한 경우
             */
            if(slicedList.isEmpty()){
                return null;
            }
            return slicedList;
        }
        else{
            /*
                지나는 역 중 출발역, 도착역 중 한 가지라도 없는 경우 null 반환
             */
            return null;
        }
    }

    //인덱스에 따른 출발시각 반환
    public String calculateDepTime(int index){

        try {
            // 처음 출발 시각
            long depDate = FORMATTER.parse(depTime).getTime();

            // 해당하는 인덱스가 나올때까지 계속 더해줌
            for (Map.Entry<Rail, Integer> entry : railList.entrySet()) {
                if(Objects.equals(entry.getKey().railIndex, index)){
                    break;
                }
                depDate += Integer.parseInt(entry.getKey().duration) * 60 * 1000L;
            }
            return FORMATTER.format(new Date(depDate));

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    //인덱스에 따른 최소 여석 수 반환
    public int calculateSeat(int index1, int index2){
        int n = 0;
        int seat = 600;

        // 2024-06-14 수정
        // 부등호 방향 반대로 수정
        // 해당 구간들 중 여석의 최대값을 찾고 있었음
        for (Map.Entry<Rail, Integer> entry : railList.entrySet()) {
            if (n == 1) {
                if (seat > entry.getValue()) {
                    seat = entry.getValue();
                }
            } else if(Objects.equals(entry.getKey().railIndex, index1)){
                n = 1;
                seat = entry.getValue();
            } else if (Objects.equals(entry.getKey().railIndex, index2)){
                if (seat > entry.getValue()) {
                    seat = entry.getValue();
                }
                break;
            }
        }

        return seat;
    }

    /*
        이 함수는 다음 두 가지 기능이 가능합니다.
        1. 구간이 이어지는지 무결성 검사용으로 단독으로 사용 가능
        2. return 되는 arraylist를 받아 slicing에 활용 가능
     */
    public ArrayList<String> getStationList() throws FileIntegrityException {
        /*
            1. 저장된 Rail 정보가 없는 경우 null 반환
            일단 null 반환으로 해놓았는데, 오류를 throw해도 될거같습니다.
         */
        if(this.railList.size()<=0){
            return null;
        }

        ArrayList<String> stationList = new ArrayList<>(); //지나는 역을 저장할 list
        int i = 0; //반복변수

        for (Map.Entry<Rail, Integer> entry : railList.entrySet()) {// railList에 저장된 모든 Rail 객체에 대해 검사
            if (i == 0) {
                /*
                    i=0인 경우, Rail 객체에서 출발역 도착역이 동일한 경우를 이미 검사했으므로 추가적인 검사 X
                 */
                stationList.add(entry.getKey().fromStation.getStation()); //railList[0].출발역을 저장
                stationList.add(entry.getKey().toStation.getStation()); //railList[0].도착역을 저장
            }
            else{
                /*
                    전 구간의 도착역과 다음 구간의 출발역이 일치하는 경우
                    즉, 구간이 연결되는 경우라면,
                    출발역(전 구간의 도착역)은 이미 stationList에 저장되어 있으므로 도착역만 저장한다.
                 */
                if(entry.getKey().fromStation.getStation().equals(stationList.get(i))){
                    stationList.add(entry.getKey().toStation.getStation()); //도착역을 저장
                }
                /*
                    전 구간의 도착역과 다음 구간의 출발역이 일치하지 않는 다면,
                    구간이 연결되지 않은 것이므로 오류를 throw 합니다
                 */
                else{
                    throw new FileIntegrityException("무결성 오류: 선행하는 rail의 도착역과 바로 뒤의 rail의 출발역이 같지 않습니다.");
                }
            }
            i++;
        }

        return stationList;

    }

    /*
        충돌 무결성 검사용, 일단 쓸 일 없어서 주석 처리
        각 구간을 지나는 출발시간을 list로 반환
     */
//    public ArrayList<String> getDeptimeList(){
//
//    }

    /*
        중복 구간이 있는지 무결성 검사하는 용, 일단 쓸 일 없어서 주석 처리
     */
//    public ArrayList<Integer> getRailIndecies(){
//
//    }
}
