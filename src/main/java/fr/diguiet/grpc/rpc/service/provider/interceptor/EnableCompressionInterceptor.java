package fr.diguiet.grpc.rpc.service.provider.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * Currently grpc-java doesn't return compressed responses, even if the client
 * has sent a compressed payload. This turns on gzip compression for all responses.
 * @see ServerInterceptor
 */
public class EnableCompressionInterceptor implements ServerInterceptor {

    /**
     * Create and return a new Interceptor instance
     * @return a new Interceptor instance
     */
    public static ServerInterceptor newInterceptor() {
        return (new EnableCompressionInterceptor());
    }

    /**
     * New EnableCompressionInterceptor
     */
    private EnableCompressionInterceptor() {

    }

    /**
     * Intercept call to unable compression
     * {@inheritDoc}
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
                                                                 final Metadata headers,
                                                                 final ServerCallHandler<ReqT, RespT> next) {
        call.setCompression("gzip");
        return (next.startCall(call, headers));
    }
}
