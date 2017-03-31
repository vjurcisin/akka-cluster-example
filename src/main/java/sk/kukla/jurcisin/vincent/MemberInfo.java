package sk.kukla.jurcisin.vincent;

import akka.actor.Address;
import akka.cluster.ClusterEvent;

/**
 * Created by vincent on 31.3.2017.
 */
public class MemberInfo {

    private final Address address;
    private final MemberStatus lastStatus;

    private boolean leader;

    public MemberInfo(final Address address, final MemberStatus lastEvent) {
        this.address = address;
        this.lastStatus = lastEvent;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(final boolean leader) {
        this.leader = leader;
    }

    public MemberStatus getLastStatus() {
        return lastStatus;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "MemberInfo{" +
                "address=" + address +
                ", lastStatus=" + lastStatus +
                ", leader=" + leader +
                '}';
    }
}
