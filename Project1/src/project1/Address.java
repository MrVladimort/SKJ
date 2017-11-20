package project1;

class Address {
    String ip;
    int port;

    Address(String addres){
        String [] arg = addres.split(":");
        this.ip = arg[0];
        this.port = Integer.parseInt(arg[1]);
    }

    Address(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return this.ip+":"+this.port;
    }
}