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

    public void sendToMain() {    //메인함수로 각 메소드에 입력된 값 반환
        Main.receiveClientData(this.name, this.phoneNumber);
    }
}
