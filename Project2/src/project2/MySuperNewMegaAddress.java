package project2;

public class MySuperNewMegaAddress {
    String ip;
    int port;

    MySuperNewMegaAddress(String address){
        String [] arg = address.split(":");
        this.ip = arg[0];
        this.port = Integer.parseInt(arg[1]);
    }

    MySuperNewMegaAddress(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return this.ip+":"+this.port;
    }
}