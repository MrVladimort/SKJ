package project2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Test {
    public static void main(String args[]) throws Exception {
        String msg = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nisl purus in mollis nunc sed id semper. Posuere urna nec tincidunt praesent. Eu scelerisque felis imperdiet proin fermentum. Gravida arcu ac tortor dignissim. Sed id semper risus in hendrerit. Ut morbi tincidunt augue interdum velit. Est sit amet facilisis magna etiam tempor. Nisl nunc mi ipsum faucibus vitae aliquet. Tellus cras adipiscing enim eu turpis egestas. Auctor augue mauris augue neque. Sagittis purus sit amet volutpat consequat mauris. Ac tortor vitae purus faucibus ornare suspendisse sed nisi lacus. Scelerisque varius morbi enim nunc faucibus a pellentesque. Non tellus orci ac auctor. Pretium nibh ipsum consequat nisl. Nisl purus in mollis nunc sed id semper risus in. Magna fringilla urna porttitor rhoncus dolor purus non. Facilisi nullam vehicula ipsum a arcu. Aliquam id diam maecenas ultricies mi eget mauris pharetra et. Pellentesque adipiscing commodo elit at imperdiet dui accumsan sit. Egestas purus viverra accumsan in nisl nisi scelerisque eu ultrices. Phasellus vestibulum lorem sed risus ultricies tristique nulla aliquet. Faucibus et molestie ac feugiat sed lectus vestibulum mattis ullamcorper. Sagittis id consectetur purus ut faucibus pulvinar elementum. Mauris pellentesque pulvinar pellentesque habitant. Ut pharetra sit amet aliquam id. Tortor consequat id porta nibh venenatis cras sed. Nulla pellentesque dignissim enim sit amet venenatis. Gravida dictum fusce ut placerat orci nulla. Sed libero enim sed faucibus turpis in eu mi bibendum. Sit amet tellus cras adipiscing. Cursus risus at ultrices mi. Faucibus interdum posuere lorem ipsum dolor sit amet consectetur. In aliquam sem fringilla ut morbi tincidunt augue. Elementum nisi quis eleifend quam adipiscing vitae proin sagittis. Libero nunc consequat interdum varius sit. Velit euismod in pellentesque massa. Molestie a iaculis at erat pellentesque adipiscing. Ut tellus elementum sagittis vitae et leo duis ut diam. Sed ullamcorper morbi tincidunt ornare. Enim nec dui nunc mattis enim ut tellus elementum sagittis. Lacus suspendisse faucibus interdum posuere lorem ipsum. Vivamus arcu felis bibendum ut. Felis eget nunc lobortis mattis aliquam faucibus purus in massa. Libero nunc consequat interdum varius sit amet mattis vulputate enim. Nibh praesent tristique magna sit. Praesent semper feugiat nibh sed pulvinar proin gravida hendrerit. Id faucibus nisl tincidunt eget nullam non nisi est. Etiam tempor orci eu lobortis elementum.";
        byte [] all = msg.getBytes();

        int length = 0;
        while (all.length > length) {
            byte[] sendData = new byte[256];

            for (int i = 0; i < sendData.length && all.length > length; i++) {
                sendData[i] = all[length++];
            }
            System.out.println(length);

            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8080);

            clientSocket.send(sendPacket);
            clientSocket.close();
        }
    }
}
