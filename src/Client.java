public class Client {
    private String name;
    private String phoneNumber;

    public Client(String userName, String phoneNumber) {
        this.name = userName;
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getName() {
        return this.name;
    }
}
