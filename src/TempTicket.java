public class TempTicket extends Ticket{
    private String reserveComputerTime;

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

    public void setReserveComputerTime(String reserveComputerTime) {
        this.reserveComputerTime = reserveComputerTime;
    }
}
