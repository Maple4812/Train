import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class FileRail implements FileInterface{
    private String fileName;
    private ArrayList<Rail> raillist = new ArrayList<>();
    Scanner scan;

    public FileRail(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }

    public ArrayList<Rail> getRaillist(){
        return this.raillist;
    }

    public Rail getRailByIndex(int index) {
        /*
            입력 받은 index 값과 동일한 운행 정보 인덱스를 갖는 Rail 객체가 존재하면 반환
         */
        for(Rail rail : raillist){
            if(rail.railIndex == index){
                return rail;
            }
        }

        /*
            존재하지 않는 경우 null을 반환
         */
        return null;
    }

    /*
        사용자가 예약할 때 구간 정보를 보여주는 용.
        예약 팀이 부탁
     */
    public void printRail(){
        /*
            FileRail을 railIndex를 기준으로 양수는 오름 차순으로, 음수는 내림 차순으로, 양수가 음수 보다 먼저 오도록 정렬
         */
        Comparator<Rail> FileRailComparator = new Comparator<Rail>() {

            @Override
            public int compare(Rail r1, Rail r2) {
                int o1 = r1.railIndex;
                int o2 = r2.railIndex;
                if(o1>=0 && o2>=0){
                    return o1-o2;
                }else if (o1 < 0 && o2 < 0){
                    return o2-o1;
                }else if (o1 >= 0){
                    return -1;
                }else{
                    return 1;
                }
            }
        };
        Collections.sort(raillist, FileRailComparator);

        /*
            각 Rail 객체와 그 정보들을 출력
         */
        System.out.println("다음은 운행 정보 인덱스와 출발역, 도착역입니다.");
        for(Rail rail : raillist){
            String str=Integer.toString(rail.railIndex);
            str+=" : ";
            str+=rail.fromStation.getStation();
            str+=" -> ";
            str+=rail.toStation.getStation();
            System.out.println(str);
        }
    }



    @Override
    public void checkIntegrity() throws FileNotFoundException, FileIntegrityException {
        raillist.clear();
        scan= new Scanner(new File(fileName));
        while(scan.hasNextLine()){
            String[] strArr = scan.nextLine().split(","); //한 줄 읽어온 다음 split
            Rail rail=new Rail(); //읽어온 줄의 정보를 저장할 Rail 객체

            /*
                이 부분에 csv에 저장된 운행 정보 인덱스가 숫자가 아닌 경우, NumberFormatException을 발생시킬지 말지 정해야함
             */
            /*
                레코드의 요소 개수가 5인지 검사 (인덱스, 출발역, 도착역, 소요 시간, 가격)
             */
            if(strArr.length!=5){
                throw new FileIntegrityException("오류: Rail.csv 파일 인자 개수가 5개가 아닙니다.");
            }

            /*
                운행 정보 인덱스의 형식 검사 (2차 기획서 내용 기반)
                1. 운행 정보 인덱스는 0이 될 수 없다.
                -> Rail 객체의 checkIntegrity에서 실행하므로 여기서 따로 처리하지 않고, while문 뒷부분에 rail.checkIntegrity를 실행
             */
//            if(Integer.parseInt(strArr[0])==0){
//                throw new FileIntegrityException("오류: 노선 번호가 0인 열차가 존재합니다.");
//            }

            rail.railIndex=Integer.parseInt(strArr[0]);

            /*
                출발역과 도착역의 형식 검사
                1. '역' 형식을 만족하는지 검사
                2. 출발역과 도착역이 같은 경우는 Rail 객체의 checkIntegrity에서 검사
             */
            rail.fromStation = new Station(strArr[1]); //Station 객체 생성 시 Station.checkIntegrity가 자동 실행 되는 점 활용
            rail.toStation = new Station(strArr[2]);

            /*
                소요 시간 형식 검사
                1. 0보다 크고 300 이하인 정수
             */
            if(Integer.parseInt(strArr[3])<=0 || Integer.parseInt(strArr[3])>300){
                throw new FileIntegrityException("오류: 소요 시간의 형식이 잘못 되었습니다.");
            }
            rail.duration = strArr[3];

            /*
                가격 형식 검사
             */
            rail.price = new Price(strArr[4]); //Price 객체 생성 시 Price.checkIntegrity가 자동 실행 되는 점 활용

            rail.checkIntegrity(); //
            raillist.add(rail);
        }

        /*
            <그 외 추가 항목들 모두 이 아래에서 검사>
            동일한 운행 정보 인덱스를 갖는 Rail이 존재하는지 검사
         */
        ArrayList<Integer> idxList=new ArrayList<>(); //운행 정보 인덱스를 중복 없이 저장하는 list. 무결성 검사용
        for(Rail rail : raillist){
            if(!idxList.contains(rail.railIndex)){ // 중복이 아닌 인덱스만 저장
                idxList.add(rail.railIndex);
            }
        }
        if(idxList.size()!=raillist.size()){
            throw new FileIntegrityException("오류: 중복되는 운행 정보 인덱스가 존재합니다.");
        }

        /*
                반대되는 운행 정보에 대한 무결성 검사
                예) 운행 정보 인덱스가 1인 구간인 경우, 인덱스가 -1인 구간이 raillist에 존재하는지 검사
         */
        for(Rail rail : raillist){

            if(getRailByIndex(-rail.railIndex)==null){
                throw new FileIntegrityException("오류: 반대되는 운행 정보가 존재하지 않습니다.");
            }
            if(!rail.fromStation.getStation().equals(getRailByIndex(-rail.railIndex).toStation.getStation())){
                throw new FileIntegrityException("오류: 반대되는 운행 정보와 역 정보가 일치하지 않습니다.");
            }
            if(!rail.toStation.getStation().equals(getRailByIndex(-rail.railIndex).fromStation.getStation())){
                throw new FileIntegrityException("오류: 반대되는 운행 정보와 역 정보가 일치하지 않습니다.");
            }
            if(!rail.duration.equals(getRailByIndex(-rail.railIndex).duration)){
                throw new FileIntegrityException("오류: 반대되는 운행 정보와 소요 시간 정보가 일치하지 않습니다.");
            }
            if(!rail.price.getPrice().equals(getRailByIndex(-rail.railIndex).price.getPrice())){
                throw new FileIntegrityException("오류: 반대되는 운행 정보와 가격 정보가 일치하지 않습니다.");
            }

        }
    }
}
