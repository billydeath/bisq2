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

package bisq.wallets.bitcoind;

import bisq.wallets.AddressType;
import bisq.wallets.bitcoind.rpc.psbt.BitcoindPsbtOptions;
import bisq.wallets.bitcoind.rpc.psbt.BitcoindPsbtOutput;
import bisq.wallets.bitcoind.rpc.responses.*;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class BitcoindPsbtMultiSigIntegrationTests extends SharedBitcoindInstanceTests {

    @Test
    public void psbtMultiSigTest() throws MalformedURLException {
        regtestSetup.mineInitialRegtestBlocks();

        var aliceBackend = regtestSetup.createNewWallet("alice_wallet");
        var bobBackend = regtestSetup.createNewWallet("bob_wallet");
        var charlieBackend = regtestSetup.createNewWallet("charlie_wallet");

        String aliceAddress = aliceBackend.getNewAddress(AddressType.BECH32, "");
        String bobAddress = bobBackend.getNewAddress(AddressType.BECH32, "");
        String charlieAddress = charlieBackend.getNewAddress(AddressType.BECH32, "");

        BitcoindGetAddressInfoResponse aliceAddrInfo = aliceBackend.getAddressInfo(aliceAddress);
        BitcoindGetAddressInfoResponse bobAddrInfo = bobBackend.getAddressInfo(bobAddress);
        BitcoindGetAddressInfoResponse charlieAddrInfo = charlieBackend.getAddressInfo(charlieAddress);

        // Generate MultiSig Address
        var keys = new ArrayList<String>();
        keys.add(aliceAddrInfo.getPubkey());
        keys.add(bobAddrInfo.getPubkey());
        keys.add(charlieAddrInfo.getPubkey());

        BitcoindAddMultisigAddressResponse aliceMultiSigAddrResponse = aliceBackend.addMultiSigAddress(2, keys);
        BitcoindAddMultisigAddressResponse bobMultiSigAddrResponse = bobBackend.addMultiSigAddress(2, keys);
        BitcoindAddMultisigAddressResponse charlieMultiSigAddrResponse = charlieBackend.addMultiSigAddress(2, keys);

        aliceBackend.importAddress(aliceMultiSigAddrResponse.getAddress(), "");
        bobBackend.importAddress(bobMultiSigAddrResponse.getAddress(), "");
        charlieBackend.importAddress(charlieMultiSigAddrResponse.getAddress(), "");

        minerWallet.sendToAddress(aliceMultiSigAddrResponse.getAddress(), 5);
        regtestSetup.mineOneBlock();

        // Create PSBT (send to Alice without Alice)
        String aliceReceiveAddr = aliceBackend.getNewAddress(AddressType.BECH32, "");
        BitcoindPsbtOutput psbtOutput = new BitcoindPsbtOutput();
        psbtOutput.addOutput(aliceReceiveAddr, 4L);

        var psbtOptions = new BitcoindPsbtOptions(
                true,
                new int[]{0}
        );

        BitcoindWalletCreateFundedPsbtResponse createFundedPsbtResponse = bobBackend.walletCreateFundedPsbt(
                Collections.emptyList(),
                psbtOutput,
                0,
                psbtOptions
        );

        // Bob and Charlie sign the PSBT
        BitcoindWalletProcessPsbtResponse bobPsbtResponse = bobBackend.walletProcessPsbt(createFundedPsbtResponse.getPsbt());
        BitcoindWalletProcessPsbtResponse charliePsbtResponse = charlieBackend.walletProcessPsbt(bobPsbtResponse.getPsbt());

        // Finalize PSBT
        BitcoindFinalizePsbtResponse finalizePsbtResponse = daemon.finalizePsbt(charliePsbtResponse.getPsbt());
        assertThat(finalizePsbtResponse.isComplete())
                .isTrue();

        // Broadcast final transaction
        daemon.sendRawTransaction(finalizePsbtResponse.getHex());

        regtestSetup.mineOneBlock();
        assertThat(aliceBackend.getBalance())
                .isGreaterThan(3.9); // Not exactly 4.0 because of fees.
    }
}
