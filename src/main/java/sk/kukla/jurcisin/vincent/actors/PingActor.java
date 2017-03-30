package sk.kukla.jurcisin.vincent.actors;

import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kukla.jurcisin.vincent.messages.PingMessage;
import sk.kukla.jurcisin.vincent.messages.PongMessage;

/**
 * Created by vincent on 27.3.2017.
 */
public class PingActor extends UntypedActor {

    public static final String NAME = "ping-actor";

    private static final Logger LOG = LoggerFactory.getLogger(PingActor.class);

    private Cluster cluster;
    private final Address selfAddress;
    private int count;

    public PingActor() {
        this.cluster = Cluster.get(getContext().system());
        this.selfAddress = cluster.selfAddress();
        this.count = 0;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof PingMessage) {
            count++;
            LOG.info("Ping message, count={}", count);
            getSender().tell(new PongMessage(), getSelf());
        } else if (message instanceof PongMessage) {
            LOG.info("Pong message");
        } else {
            unhandled(message);
        }
    }

    @Override
    public void preStart() throws Exception {
        LOG.info("[akka member: {}] preStart", selfAddress);
    }

    @Override
    public void postStop() throws Exception {
        LOG.info("[akka member: {}] postStop", selfAddress);
        cluster.unsubscribe(getSelf());
    }

    public int getCount() {
        return count;
    }
}
