import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Line {
    static String REGEXP_PATTERN_LINE = "^[A-Z][0-9]{4}$"; //노선 번호 문법 규칙
    String lineNum, depTime, arrivalTime; //노선 번호, 출발 시각, 도착 시각
    LinkedHashMap<Rail, Integer> railList; // <운행 정보, 여석 수>

    public Line() {}

    public static void checkIntegrity(String str) throws FileIntegrityException{
        /*

         */
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
                    throw new FileIntegrityException("구간이 연결되지 않습니다.");
                }
            }
            i++;
        }
        
        return stationList;

    }

    /*
        충돌 무결성 검사용
        각 구간을 지나는 출발시간을 list로 반환
     */
    public ArrayList<String> getDeptimeList(){

    }

    /*
        중복 구간이 있는지 무결성 검사하는 용
     */
    public ArrayList<Integer> getRailIndecies(){

    }
}
