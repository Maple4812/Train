public class PhoneNumber {
    String REGEXP_PATTERN_DASH = "[0-9]{3}-[0-9]{4}-[0-9]{4}";
    String REGEXP_PATTERN_NORMAL = "[0-9]{11}";
    String phoneNumber = "";

    public PhoneNumber(String input) throws Exception {
        if(input.matches(REGEXP_PATTERN_DASH)){
            String[] tempArr = input.split("-");
            StringBuilder strBuilder = new StringBuilder(phoneNumber);
            for(String s: tempArr){
                strBuilder.append(s);
            }
            phoneNumber = strBuilder.toString();
        } else if (input.matches(REGEXP_PATTERN_NORMAL)) {
            phoneNumber = input;
        } else {
            throw new Exception("잘못된 전화번호 형식입니다.");
        }
    }

    public void printPhoneNumber(){
        System.out.println(phoneNumber);
    }
}
