package sk.kukla.jurcisin.vincent;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import sk.kukla.jurcisin.vincent.management.ClusterState;

/**
 * Created by vincent on 29.3.2017.
 */
public class ClusterObjectWrapper {

    private final int ordinal;
    private final ActorSystem actorSystem;
    private final ClusterState clusterState;
    private final ActorRef clusterStaterActor;
    private final Address address;

    public ClusterObjectWrapper(final int ordinal, final ActorSystem actorSystem, final ClusterState clusterState,
            ActorRef clusterStaterActor) {
        this.ordinal = ordinal;
        this.actorSystem = actorSystem;
        this.clusterState = clusterState;
        this.clusterStaterActor = clusterStaterActor;
        this.address = Cluster.get(actorSystem).selfAddress();
    }

    public int getOrdinal() {
        return ordinal;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ClusterState getClusterState() {
        return clusterState;
    }

    public Address getAddress() {
        return address;
    }

    public ActorRef getClusterStaterActor() {
        return clusterStaterActor;
    }

    @Override
    public String toString() {
        return "ClusterObjectWrapper{" +
                "ordinal=" + ordinal +
                ", address=" + address +
                ", clusterState=" + clusterState +
                '}';
    }
}
