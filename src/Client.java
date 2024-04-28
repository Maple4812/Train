public class Client {
    private String name;
    private String phoneNumber;

    public Client(String userName, String phoneNumber) {
        this.name = userName;
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {    //전화번호 메소드
        return this.phoneNumber;
    }

    public String getName() {    //이름 메소드
        return this.name;
    }
}
