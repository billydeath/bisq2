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

package bisq.grpc;

import bisq.application.DefaultApplicationService;
import bisq.application.ApplicationVersion;
import bisq.grpc.proto.GetVersionGrpc;
import bisq.grpc.proto.GetVersionReply;
import bisq.grpc.proto.GetVersionRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrpcVersionService extends GetVersionGrpc.GetVersionImplBase {
    private static final Logger log = LoggerFactory.getLogger(GrpcVersionService.class);

    private final DefaultApplicationService applicationService;

    public GrpcVersionService(DefaultApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public void getVersion(GetVersionRequest req,
                           StreamObserver<GetVersionReply> responseObserver) {
        try {
            var reply = GetVersionReply.newBuilder()
                    .setVersion(ApplicationVersion.VERSION)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Throwable cause) {
            new GrpcExceptionHandler().handleException(log, cause, responseObserver);
        }
    }
}
