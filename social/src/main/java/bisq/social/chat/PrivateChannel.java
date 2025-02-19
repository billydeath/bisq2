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

package bisq.social.chat;

import bisq.security.DigestUtil;
import bisq.social.user.ChatUser;
import bisq.social.user.UserNameGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PrivateChannel extends Channel<PrivateChatMessage> {
    private final ChatUser peer;
    private final ChatIdentity chatIdentity;

    public PrivateChannel(String id, ChatUser peer, ChatIdentity chatIdentity) {
        super(id);
        this.peer = peer;
        this.chatIdentity = chatIdentity;
    }

    public String getChannelName() {
        return UserNameGenerator.fromHash(DigestUtil.hash(peer.networkId().getPubKey().publicKey().getEncoded()));
    }
}