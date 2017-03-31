package sk.kukla.jurcisin.vincent.actors;

import akka.actor.Address;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.japi.Creator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kukla.jurcisin.vincent.MemberStatus;
import sk.kukla.jurcisin.vincent.management.ClusterState;

/**
 * Created by vincent on 29.3.2017.
 */
public class ClusterStateActor extends UntypedActor {

    public static final String NAME = "cluster-state-actor";

    private static final Logger LOG = LoggerFactory.getLogger(ClusterStateActor.class);

    private final Cluster cluster;
    private final ClusterState clusterState;
    private final Address selfAddress;

    private ClusterStateActor(ClusterState clusterState) {
        this.cluster = Cluster.get(getContext().system());
        this.selfAddress = cluster.remotePathOf(self()).address();
        this.clusterState = clusterState;
    }

    public static Props props(final ClusterState clusterState) {
        return Props.create(new Creator<ClusterStateActor>() {
            private static final long serialVersionUID = 1L;

            public ClusterStateActor create() throws Exception {
                return new ClusterStateActor(clusterState);
            }
        });
    }

    public void onReceive(final Object message) throws Throwable {
        if (message instanceof ClusterEvent.MemberJoined) {
            LOG.info("[member: {}] ClusterEvent.MemberJoined", selfAddress);
            clusterState.onClusterEvent(((ClusterEvent.MemberJoined) message).member().address(), MemberStatus.JOINED);
        } else if (message instanceof ClusterEvent.MemberRemoved) {
            LOG.info("[member: {}] ClusterEvent.MemberRemoved", selfAddress);
            clusterState
                    .onClusterEvent(((ClusterEvent.MemberRemoved) message).member().address(), MemberStatus.REMOVED);
        } else if (message instanceof ClusterEvent.UnreachableMember) {
            LOG.info("[member: {}] ClusterEvent.UnreachableMember", selfAddress);
            clusterState.onClusterEvent(((ClusterEvent.UnreachableMember) message).member().address(),
                    MemberStatus.UNREACHABLE);
        } else if (message instanceof ClusterEvent.MemberLeft) {
            LOG.info("[member: {}] ClusterEvent.MemberLeft", selfAddress);
            clusterState.onClusterEvent(((ClusterEvent.MemberLeft) message).member().address(), MemberStatus.LEFT);
        } else if (message instanceof ClusterEvent.MemberUp) {
            LOG.info("[member: {}] ClusterEvent.MemberUp", selfAddress);
            clusterState.onClusterEvent(((ClusterEvent.MemberUp) message).member().address(), MemberStatus.UP);
        } else if (message instanceof ClusterEvent.CurrentClusterState) {
            ClusterEvent.CurrentClusterState clusterState = (ClusterEvent.CurrentClusterState) message;
            LOG.info("[cluster] ClusterEvent.CurrentClusterState: {}", clusterState);
        } else if (message instanceof ClusterEvent.LeaderChanged) {
            LOG.info("[akka member: {}] ClusterEvent.LeaderChanged, new leader: {}", selfAddress,
                    ((ClusterEvent.LeaderChanged) message).getLeader());
            clusterState.leaderChanged((ClusterEvent.LeaderChanged) message);
        } else {
            LOG.warn("[member: {}] Unhandled message: {}", selfAddress, message);
            unhandled(message);
        }
    }

    @Override
    public void preStart() throws Exception {
        LOG.info("[akka member: {}] preStart", selfAddress);
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsSnapshot(), ClusterEvent.LeaderChanged.class,
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    @Override
    public void postStop() throws Exception {
        LOG.info("[akka member: {}] postStop", selfAddress);
        cluster.unsubscribe(getSelf());
    }
}
