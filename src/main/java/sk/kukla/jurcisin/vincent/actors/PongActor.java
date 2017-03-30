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
public class PongActor extends UntypedActor {

    public static final String NAME = "pong-actor";

    private static final Logger LOG = LoggerFactory.getLogger(PongActor.class);

    private final Cluster cluster;
    private final Address selfAddress;
    private int count;

    private PongActor() {
        this.cluster = Cluster.get(getContext().system());
        this.selfAddress = cluster.selfAddress();
        this.count = 0;
    }

    public void onReceive(final Object message) throws Throwable {
        if (message instanceof PingMessage) {
            LOG.info("Ping message");
        } else if (message instanceof PongMessage) {
            count++;
            LOG.info("Pong message, count={}", count);
            getSender().tell(new PingMessage(), getSelf());
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
