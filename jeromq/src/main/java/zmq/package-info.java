/**
 * Provides low-level bindings for 0MQ.
 * <br/>
 * This is the java equivalent of <a href="https://github.com/zeromq/libzmq">libzmq project</a>.
 * <p/>
 * The ZeroMQ lightweight messaging kernel is a library which extends the standard socket interfaces
 * with features traditionally provided by specialised messaging middleware products.
 * ZeroMQ sockets provide an abstraction of asynchronous message queues,
 * multiple messaging patterns, message filtering (subscriptions),
 * seamless access to multiple transport protocols and more.
 * <p/>
 * All subpackages should be considered internal, with the exception of {@link zmq.msg}.
 * <p/>
 * Within this package, only {@link zmq.ZMQ}, {@link zmq.Ctx}, {@link zmq.SocketBase} and {@link zmq.Msg} should be used.
 */
package zmq;
