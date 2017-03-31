package sk.kukla.jurcisin.vincent.management;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kukla.jurcisin.vincent.MemberInfo;
import sk.kukla.jurcisin.vincent.MemberStatus;

/**
 * Created by vincent on 29.3.2017.
 */
public class ClusterState {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterState.class);

    private final Map<Address, MemberInfo> members;
    private final ActorSystem actorSystem;
    private ActorRef selfActor;
    private boolean leader;

    public ClusterState(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.members = new ConcurrentHashMap<>();
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

    public Map<Address, MemberInfo> getMembers() {
        return members;
    }

    public void leaderChanged(final ClusterEvent.LeaderChanged message) {
        leader = message.getLeader().equals(Cluster.get(actorSystem).selfAddress());
        MemberInfo newLeaderMemberInfo = members.get(message.getLeader());
        Preconditions.checkNotNull(newLeaderMemberInfo, "member doesn't exists");
        newLeaderMemberInfo.setLeader(true);
        // old members leader attribute set to false
        members.forEach((address, memberInfo) -> {
            if (!newLeaderMemberInfo.getAddress().equals(memberInfo.getAddress())) {
                memberInfo.setLeader(false);
            }
        });
    }

    public void onClusterEvent(Address memberAddress, MemberStatus status) {
        try {
            members.put(memberAddress, new MemberInfo(memberAddress, status));
        } catch (ClassCastException ex) {
            LOG.error("event can't be casted to Class<ClusterEvent>");
        }
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("ClusterState, isLeader="+leader+"\n");
        for (MemberInfo memberInfo : members.values()) {
            output.append("     "+memberInfo+"\n");
        }
        return output.toString();
    }
}
