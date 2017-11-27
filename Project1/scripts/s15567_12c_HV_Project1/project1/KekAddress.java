package project1;

public class KekAddress {
    String ip;
    int port;

    KekAddress(String addres){
        String [] arg = addres.split(":");
        this.ip = arg[0];
        this.port = Integer.parseInt(arg[1]);
    }

    KekAddress(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return this.ip+":"+this.port;
    }
}