import Netta.Connection.Server.SingleClientServer;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

/**
 * Created by Austin on 2/3/2017.
 */
public class SingleClientServerTest {

    SingleClientServer s;

    class SingleClientServerMock extends SingleClientServer {
        public SingleClientServerMock(int port) throws NoSuchAlgorithmException {
            super(port);
        }
    }

    @Test
    public void initializeTest() throws NoSuchAlgorithmException {
        s = new SingleClientServerMock(99999);
    }
}
