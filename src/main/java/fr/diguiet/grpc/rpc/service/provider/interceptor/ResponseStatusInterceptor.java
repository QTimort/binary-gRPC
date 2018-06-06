package fr.diguiet.grpc.rpc.service.provider.interceptor;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * Class intercept the message and call back the listener on success or cancel
 * @see ServerInterceptor
 */
public class ResponseStatusInterceptor implements ServerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ResponseStatusInterceptor.class);
    private ICallBack callback;

    /**
     * Create and return a new Interceptor instance
     * @return a new Interceptor instance
     */
    public static ServerInterceptor newInterceptor(final ICallBack callback) {
        return (new ResponseStatusInterceptor(callback));
    }

    /**
     * New ResponseStatusInterceptor
     * @param callback the listener to call
     */
    private ResponseStatusInterceptor(final ICallBack callback) {
        Objects.requireNonNull(callback);
        this.callback = callback;
    }

    /**
     * Callback interface
     */
    public interface ICallBack {
        /**
         * Called when message is successfully sent to the client
         * @param clientIp the client ip
         * @param message the request message
         */
        void onResponseReceive(final SocketAddress clientIp, final GeneratedMessageV3 message);

        /**
         * Called when message is cancel and not sent the client
         * @param clientIp the client ip
         * @param message the request message
         */
        void onResponseCancel(final SocketAddress clientIp, final GeneratedMessageV3 message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
                                                                 final Metadata headers,
                                                                 final ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener<ReqT>() {
            private final SocketAddress clientIp = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
            private GeneratedMessageV3 lastMessage = null;

            /**
             * Called when response not sent
             */
            @Override
            public void onCancel() {
                ResponseStatusInterceptor.logger.warn("Message to " + this.clientIp + " has been canceled (" + this.lastMessage + ")");
                super.onCancel();
                ResponseStatusInterceptor.this.callback.onResponseCancel(this.clientIp, this.lastMessage);
            }

            /**
             * Called when response is sent
             */
            @Override
            public void onComplete() {
                super.onComplete();
                ResponseStatusInterceptor.this.callback.onResponseReceive(this.clientIp, this.lastMessage);
            }

            /**
             * Called when we receive a request
             * @param message
             */
            @Override
            public void onMessage(ReqT message) {
                super.onMessage(message);
                if (message instanceof GeneratedMessageV3)
                    this.lastMessage = (GeneratedMessageV3) message;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected ServerCall.Listener<ReqT> delegate() {
                return delegate;
            }
        };
    }
}
