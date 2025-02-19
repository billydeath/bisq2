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

package bisq.wallets.bitcoind.rpc.responses;

import bisq.wallets.model.Utxo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BitcoindListUnspentResponseEntry implements Utxo {
    @JsonProperty("txid")
    private String txId;
    private int vout;
    private String address;
    private String label;
    private String scriptPubKey;
    private double amount;
    private int confirmations;
    private String redeemScript;
    private String witnessScript;
    private boolean spendable;
    private boolean solvable;
    private boolean reused;
    private String desc;
    private boolean safe;
}
