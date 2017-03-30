package sk.kukla.jurcisin.vincent.utils;

import static com.jayway.awaitility.Awaitility.await;

import akka.actor.ActorSystem;
import com.google.common.base.Preconditions;
import com.jayway.awaitility.Awaitility;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kukla.jurcisin.vincent.ClusterObjectWrapper;
import sk.kukla.jurcisin.vincent.actors.ClusterStateActor;
import sk.kukla.jurcisin.vincent.management.ClusterState;

/**
 * Created by vincent on 29.3.2017.
 */
public class TestUtils {

    private static Logger LOG = LoggerFactory.getLogger(TestUtils.class);
    private static int stoppedMemberOrdinal;

    protected static Map<Integer, ClusterObjectWrapper> CLUSTER_OBJECTS = new ConcurrentHashMap<>();

    public static void startCluster() {
        LOG.info("starting cluster ...");
        String address;
        for (int i=1; i<=3; i++) {
            initMember(i);
        }
        try {
            Thread.sleep(5000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitForLeaders(1, 15);
    }

    private static void initMember(int ordinal) {
        int port;
        port = 2550 + ordinal;
        // Override the configuration of the port
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                .withFallback(ConfigFactory.load());

        // Create an Akka system
        ActorSystem actorSystem = ActorSystem.create("ClusterSystem", config);

        // Create ClusterState and ClusterGovernor
        ClusterState clusterState = new ClusterState(actorSystem);
        actorSystem.actorOf(ClusterStateActor.props(clusterState));
        CLUSTER_OBJECTS.put(ordinal, new ClusterObjectWrapper(ordinal, actorSystem, clusterState));
    }

    public static void stopCluster() {
        LOG.info("cluster shutdown ...");
        for (ClusterObjectWrapper clusterObjectRegistry: CLUSTER_OBJECTS.values()) {
            clusterObjectRegistry.getActorSystem().shutdown();
        }

        LOG.info("waiting for cluster to shutdown !");
        await().atMost(15, TimeUnit.SECONDS).until(() ->
                CLUSTER_OBJECTS.values().stream().allMatch(c -> c.getActorSystem().isTerminated()));
        LOG.info("cluster stopped!");
    }

    public static void waitForLeaders(int numOfLeaders, int timeout) {
        Awaitility.await()
                .atMost(timeout, TimeUnit.SECONDS)
                .until(TestUtils::numberOfLeaders, Matchers.equalTo(numOfLeaders));

        List<ClusterObjectWrapper> collect = CLUSTER_OBJECTS.values().stream()
                .filter(objectWrapper -> objectWrapper.getClusterState().isLeader()).collect(Collectors.toList());
        LOG.info("[cluster] has {} leaders: {}", numOfLeaders, collect);
    }

    private static int numberOfLeaders() {
        int leadersCount = 0;
        for (ClusterObjectWrapper objectWrapper : CLUSTER_OBJECTS.values()) {
            if (objectWrapper.getClusterState().isLeader()) {
                leadersCount++;
            }
        }
        return leadersCount;
    }

    public static void stopLeaderMember() {
        int leaderOrdinal = 0;
        for (ClusterObjectWrapper objectWrapper : CLUSTER_OBJECTS.values()) {
            if (objectWrapper.getClusterState().isLeader()) {
                leaderOrdinal = objectWrapper.getOrdinal();
                break;
            }
        }
        Assert.assertNotEquals("no leader was found", 0, leaderOrdinal);
        stoppedMemberOrdinal = leaderOrdinal;
        CLUSTER_OBJECTS.get(leaderOrdinal).getClusterState().setLeader(false);
        shutdownActorSystem(leaderOrdinal);
        LOG.info("[cluster] shutdowned cluster member = {}", leaderOrdinal);
    }

    public static void stopNonLeaderMember() {
        int nonLeaderOrdinal = 0;
        for (ClusterObjectWrapper objectWrapper : CLUSTER_OBJECTS.values()) {
            if (!objectWrapper.getClusterState().isLeader()) {
                nonLeaderOrdinal = objectWrapper.getOrdinal();
                break;
            }
        }
        Assert.assertNotEquals("no nonleader was found", 0, nonLeaderOrdinal);
        stoppedMemberOrdinal = nonLeaderOrdinal;
        shutdownActorSystem(nonLeaderOrdinal);
        LOG.info("[cluster] shutdowned cluster member = {}", nonLeaderOrdinal);
    }

    public static void leaderIsDifferentThenKilled() {
        if (currentLeader().isPresent()) {
            Assert.assertNotEquals(stoppedMemberOrdinal, currentLeader().get().getOrdinal());
        } else {
            Assert.fail("doesn't exist leader");
        }
    }

    public static void startupStoppedMember() {
        initMember(stoppedMemberOrdinal);
    }

    private static void startupActorSystem(ActorSystem actorSystem) {
        throw new UnsupportedOperationException();
    }

    private static Optional<ClusterObjectWrapper> currentLeader() {
        Optional<ClusterObjectWrapper> first = CLUSTER_OBJECTS.values().stream()
                .filter(objectWrapper -> objectWrapper.getClusterState().isLeader())
                .findFirst();
        return first;
    }

    private static void shutdownActorSystem(int ordinal) {
        ClusterObjectWrapper objectWrapper = CLUSTER_OBJECTS.get(ordinal);
        Preconditions.checkNotNull(objectWrapper, "missing cluster object wrapper for ordinal "+ordinal);
        ActorSystem actorSystem = objectWrapper.getActorSystem();
        actorSystem.shutdown();
        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .until(actorSystem::isTerminated);
    }
}
