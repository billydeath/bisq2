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

package bisq.network.p2p.services.data.storage.auth;

import bisq.common.encoding.Hex;
import bisq.network.p2p.services.data.RemoveDataRequest;
import bisq.network.p2p.services.data.storage.MetaData;
import bisq.security.DigestUtil;
import bisq.security.KeyGeneration;
import bisq.security.SignatureUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;

@Getter
@EqualsAndHashCode
@Slf4j
public class RemoveAuthenticatedDataRequest implements AuthenticatedDataRequest, RemoveDataRequest {

    public static RemoveAuthenticatedDataRequest from(AuthenticatedDataStorageService store, AuthenticatedPayload payload, KeyPair keyPair)
            throws GeneralSecurityException {
        byte[] hash = DigestUtil.hash(payload.serialize());
        byte[] signature = SignatureUtil.sign(hash, keyPair.getPrivate());
        int newSequenceNumber = store.getSequenceNumber(hash) + 1;

        return new RemoveAuthenticatedDataRequest(payload.getMetaData(), hash, keyPair.getPublic(), newSequenceNumber, signature);
    }

    protected final MetaData metaData;
    protected final byte[] hash;
    protected final byte[] ownerPublicKeyBytes; // 442 bytes
    @Nullable
    transient protected PublicKey ownerPublicKey;
    protected final int sequenceNumber;
    protected final byte[] signature;         // 47 bytes
    protected final long created;

    public RemoveAuthenticatedDataRequest(MetaData metaData,
                                          byte[] hash,
                                          PublicKey ownerPublicKey,
                                          int sequenceNumber,
                                          byte[] signature) {
        this(metaData,
                hash,
                ownerPublicKey.getEncoded(),
                ownerPublicKey,
                sequenceNumber,
                signature);
    }

    protected RemoveAuthenticatedDataRequest(MetaData metaData,
                                             byte[] hash,
                                             byte[] ownerPublicKeyBytes,
                                             PublicKey ownerPublicKey,
                                             int sequenceNumber,
                                             byte[] signature) {
        this.metaData = metaData;
        this.hash = hash;
        this.ownerPublicKeyBytes = ownerPublicKeyBytes;
        this.ownerPublicKey = ownerPublicKey;
        this.sequenceNumber = sequenceNumber;
        this.signature = signature;
        created = System.currentTimeMillis();
    }

    public boolean isSignatureInvalid() {
        try {
            if (ownerPublicKey == null) {
                ownerPublicKey = KeyGeneration.generatePublic(ownerPublicKeyBytes);
            }
            return !SignatureUtil.verify(hash, signature, ownerPublicKey);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isPublicKeyHashInvalid(AuthenticatedData entryFromMap) {
        try {
            return !Arrays.equals(entryFromMap.getHashOfPublicKey(), DigestUtil.hash(ownerPublicKeyBytes));
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isSequenceNrInvalid(long seqNumberFromMap) {
        return sequenceNumber <= seqNumberFromMap;
    }


    @Override
    public String toString() {
        return "RemoveAuthenticatedDataRequest{" +
                "\r\n     metaData=" + metaData +
                ",\r\n     hash=" + Hex.encode(hash) +
                ",\r\n     ownerPublicKeyBytes=" + Hex.encode(ownerPublicKeyBytes) +
                ",\r\n     ownerPublicKey=" + ownerPublicKey +
                ",\r\n     sequenceNumber=" + sequenceNumber +
                ",\r\n     signature=" + Hex.encode(signature) +
                ",\r\n     created=" + created +
                "\r\n}";
    }
}
