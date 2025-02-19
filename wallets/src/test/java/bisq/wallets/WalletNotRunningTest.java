/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.wallets;

import bisq.common.util.FileUtils;
import bisq.common.util.NetworkUtils;
import bisq.wallets.bitcoind.rpc.BitcoindDaemon;
import bisq.wallets.rpc.RpcClient;
import bisq.wallets.rpc.RpcClientFactory;
import bisq.wallets.rpc.RpcConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WalletNotRunningTest {
    @Test
    void notRunningTest() throws IOException {
        int freePort = NetworkUtils.findFreeSystemPort();
        Path tempDirPath = FileUtils.createTempDir();
        Path walletPath = tempDirPath.resolve("wallet");

        RpcConfig rpcConfig = new RpcConfig.Builder()
                .networkType(NetworkType.REGTEST)
                .hostname("127.0.0.1")
                .user("bisq")
                .password("bisq")
                .port(freePort)
                .walletPath(walletPath)
                .build();

        RpcClient rpcClient = RpcClientFactory.create(rpcConfig);
        var minerChainBackend = new BitcoindDaemon(rpcClient);

        assertThatThrownBy(minerChainBackend::listWallets)
                .hasCauseInstanceOf(ConnectException.class);
    }
}
