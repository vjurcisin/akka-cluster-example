package sk.kukla.jurcisin.vincent.management;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vincent on 29.3.2017.
 */
public class ClusterState {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterState.class);

    private final ActorSystem actorSystem;
    private ActorRef selfActor;
    private boolean leader;

    public ClusterState(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public ActorRef getSelfActor() {
        return selfActor;
    }

    public void setSelfActor(final ActorRef selfActor) {
        this.selfActor = selfActor;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(final boolean leader) {
        this.leader = leader;
    }

    public void leaderChanged(final ClusterEvent.LeaderChanged message) {
        if (message.getLeader().equals(Cluster.get(actorSystem).selfAddress())) {
            leader = true;
        } else {
            leader = false;
        }
    }
}
