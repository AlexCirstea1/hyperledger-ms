//package ro.cloud.security.hyperledger.hyperledger.config;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.*;
//
//import java.io.BufferedReader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.PrivateKey;
//import java.security.cert.X509Certificate;
//import java.util.stream.Stream;
//import org.hyperledger.fabric.gateway.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class HyperledgerConfigTest {
//
//    private HyperledgerConfig config;
//
//    @Value("${fabric.wallet.path}")
//    private String walletPath;
//
//    @Value("${fabric.user-name}")
//    private String userName;
//
//    @Value("${fabric.network.config}")
//    private String networkConfigPath;
//
//    @Value("${fabric.channel-name}")
//    private String channelName;
//
//    @Value("${fabric.contract-name}")
//    private String contractName;
//
//    @BeforeEach
//    void setUp() {
//        config = new HyperledgerConfig();
//        config.setWalletPath(walletPath);
//        config.setUserName(userName);
//        config.setNetworkConfigPath(networkConfigPath);
//        config.setChannelName(channelName);
//        config.setContractName(contractName);
//    }
//
//    @Test
//    void gatewayBean_shouldLoadIdentity_and_buildGateway() throws Exception {
//        // mocks for all the static calls inside gateway()
//        try (MockedStatic<Wallets> walletsMock = mockStatic(Wallets.class);
//                MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
//                MockedStatic<Files> filesMock = mockStatic(Files.class);
//                MockedStatic<Identities> idsMock = mockStatic(Identities.class);
//                MockedStatic<Gateway> gatewayStatic = mockStatic(Gateway.class)) {
//
//            // Mock Paths.get(walletPath, "msp") directly
//            Path mspDir = mock(Path.class);
//            pathsMock.when(() -> Paths.get(walletPath, "msp")).thenReturn(mspDir);
//
//            // 2) read cert
//            Path signcerts = mock(Path.class);
//            when(mspDir.resolve("signcerts")).thenReturn(signcerts);
//            Path certPem = mock(Path.class);
//            when(signcerts.resolve("cert.pem")).thenReturn(certPem);
//            BufferedReader certReader = mock(BufferedReader.class);
//            filesMock.when(() -> Files.newBufferedReader(certPem)).thenReturn(certReader);
//            X509Certificate cert = mock(X509Certificate.class);
//            idsMock.when(() -> Identities.readX509Certificate(certReader)).thenReturn(cert);
//
//            // 3) read private key
//            Path keystore = mock(Path.class);
//            when(mspDir.resolve("keystore")).thenReturn(keystore);
//            Path keyFile = mock(Path.class);
//            filesMock.when(() -> Files.list(keystore)).thenReturn(Stream.of(keyFile));
//            BufferedReader keyReader = mock(BufferedReader.class);
//            filesMock.when(() -> Files.newBufferedReader(keyFile)).thenReturn(keyReader);
//            PrivateKey pkey = mock(PrivateKey.class);
//            idsMock.when(() -> Identities.readPrivateKey(keyReader)).thenReturn(pkey);
//
//            // 4) new in‑memory wallet + import identity
//            Wallet wallet = mock(Wallet.class);
//            walletsMock.when(Wallets::newInMemoryWallet).thenReturn(wallet);
//            X509Identity id = mock(X509Identity.class);
//            idsMock.when(() -> Identities.newX509Identity("Org1MSP", cert, pkey))
//                    .thenReturn(id);
//
//            // 5) stub Gateway builder
//            Path networkCfg = mock(Path.class);
//            pathsMock.when(() -> Paths.get(networkConfigPath)).thenReturn(networkCfg);
//
//            Gateway.Builder builder = mock(Gateway.Builder.class);
//            Gateway gw = mock(Gateway.class);
//
//            gatewayStatic.when(Gateway::createBuilder).thenReturn(builder);
//            when(builder.identity(wallet, userName)).thenReturn(builder);
//            when(builder.networkConfig(networkCfg)).thenReturn(builder);
//            when(builder.discovery(true)).thenReturn(builder);
//            when(builder.connect()).thenReturn(gw);
//
//            // ——— ACT ———
//            Gateway result = config.gateway();
//
//            // ——— VERIFY ———
//            assertNotNull(result);
//            // wallet creation + import
//            walletsMock.verify(Wallets::newInMemoryWallet);
//            verify(wallet).put(userName, id);
//            // gateway builder chain
//            gatewayStatic.verify(Gateway::createBuilder);
//            verify(builder).identity(wallet, userName);
//            verify(builder).networkConfig(networkCfg);
//            verify(builder).discovery(true);
//            verify(builder).connect();
//        }
//    }
//
//    @Test
//    void networkBean_shouldReturnNetwork() {
//        Gateway gw = mock(Gateway.class);
//        Network net = mock(Network.class);
//        when(gw.getNetwork(channelName)).thenReturn(net);
//
//        Network result = config.network(gw);
//
//        assertNotNull(result);
//        verify(gw).getNetwork(channelName);
//    }
//
//    @Test
//    void eventContractBean_shouldReturnContract() {
//        Network net = mock(Network.class);
//        Contract cc = mock(Contract.class);
//        when(net.getContract(contractName)).thenReturn(cc);
//
//        Contract result = config.eventContract(net);
//
//        assertNotNull(result);
//        verify(net).getContract(contractName);
//    }
//}
