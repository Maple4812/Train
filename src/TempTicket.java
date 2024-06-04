public class TempTicket extends Ticket{
    private String reserveComputerTime;
    private String reserveTime;

    public TempTicket(){}

    public String getReserveComputerTime() {
        return reserveComputerTime;
    }

    public String getReserveTime() {
        return reserveTime;
    }

    public int getFirstRailofTempTicket(){
        return railIndices.get(0).railIndex;
    }

    public int getLastRailofTempTicket(){
        return railIndices.get(railIndices.size() - 1).railIndex;
    }

    public void setReserveComputerTime(String reserveComputerTime) {
        this.reserveComputerTime = reserveComputerTime;
    }

    public void setReserveTime(String reserveTime) {
        this.reserveTime = reserveTime;
    }
}
