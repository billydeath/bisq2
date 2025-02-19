package bisq.network.p2p.node.transport;

import bisq.common.util.NetworkUtils;
import bisq.i2p.I2pClient;
import bisq.network.NetworkService;
import bisq.network.p2p.node.Address;
import bisq.network.p2p.node.ConnectionException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.io.File.separator;

// Start I2P
// Enable SAM at http://127.0.0.1:7657/configclients
// Takes about 1-2 minutes until its ready
@Slf4j
public class I2PTransport implements Transport {
    private final String i2pDirPath;
    private I2pClient i2pClient;
    private boolean initializeCalled;
    private String sessionId;

    public I2PTransport(Config config) {
        i2pDirPath = config.baseDir() + separator + "i2p";
        log.info("I2PTransport using i2pDirPath: {}", i2pDirPath);
    }


    @Override
    public Boolean initialize() {
        if (initializeCalled) {
            return true;
        }
        initializeCalled = true;
        log.debug("Initialize");
        i2pClient = I2pClient.getI2pClient(i2pDirPath);
        return true;
    }


    @Override
    public ServerSocketResult getServerSocket(int port, String nodeId) {
        log.debug("Create serverSocket");
        try {
            sessionId = nodeId + port;
            ServerSocket serverSocket = i2pClient.getServerSocket(sessionId, NetworkUtils.findFreeSystemPort());
            String destination = i2pClient.getMyDestination(sessionId);
            // Port is irrelevant for I2P
            Address address = new Address(destination, port);
            log.debug("ServerSocket created. SessionId={}, destination={}", sessionId, destination);
            return new ServerSocketResult(nodeId, serverSocket, address);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ConnectionException(exception);
        }
    }

    @Override
    public Socket getSocket(Address address) throws IOException {
        try {
            //todo check usage of sessionId
            log.debug("Create new Socket to {} with sessionId={}", address, sessionId);
            long ts = System.currentTimeMillis();
            Socket socket = i2pClient.getSocket(address.getHost(), sessionId);
            log.info("I2P socket to {} created. Took {} ms", address, System.currentTimeMillis() - ts);
            return socket;
        } catch (IOException exception) {
            log.error(exception.toString(), exception);
            throw exception;
        }
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        initializeCalled = false;
        if (i2pClient == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(i2pClient::shutdown, NetworkService.NETWORK_IO_POOL);
    }

    @Override
    public Optional<Address> getServerAddress(String serverId) {
        try {
            //todo
            String myDestination = i2pClient.getMyDestination(sessionId);
            return Optional.of(new Address(myDestination, -1));
        } catch (IOException exception) {
            log.error(exception.toString(), exception);
            return Optional.empty();
        }
    }
}
