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

package bisq.wallets.rpc.call;

import lombok.Getter;

public abstract class RpcCall<T, R> {
    @Getter
    protected final T request;

    public RpcCall(T request) {
        this.request = request;
    }

    public abstract String getRpcMethodName();

    public abstract boolean isResponseValid(R response);

    public abstract Class<R> getRpcResponseClass();
}
