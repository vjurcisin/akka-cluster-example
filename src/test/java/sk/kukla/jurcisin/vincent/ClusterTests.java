package sk.kukla.jurcisin.vincent;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sk.kukla.jurcisin.vincent.actors.PingActor;
import sk.kukla.jurcisin.vincent.actors.SimpleActor;
import sk.kukla.jurcisin.vincent.utils.TestUtils;
import sk.kukla.jurcisin.vincent.actors.PongActor;

/**
 * Created by vincent on 27.3.2017.
 */
public class ClusterTests {

    @Before
    public void setUp() throws Exception {
        TestUtils.startCluster();
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.stopCluster();
    }

    @Test
    public void shutdownNonLeaderMember_test() throws Exception {
        TestUtils.shutdownNonLeaderMember();
        TestUtils.waitForLeaders(1, 15);
        TestUtils.leaderIsDifferentThenKilled();
    }

    @Test
    public void shutdownLeaderMember_test() throws Exception {
        TestUtils.shutdownLeaderMember();
        TestUtils.waitForLeaders(1, 10);
        TestUtils.leaderIsDifferentThenKilled();
    }

    @Test
    public void rejoinNonLeaderMember_test() throws Exception {

    }

    @Test
    public void rejoinLeaderMember_test() throws Exception {

    }

    private static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                    .withFallback(ConfigFactory.load());
            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);
            ActorRef pingActor = system.actorOf(Props.create(PingActor.class), "PingActor");
            ActorRef pongActor = system.actorOf(Props.create(PongActor.class), "PongActor");
            system.actorOf(Props.create(SimpleActor.class), "SimpleActor");
//            pingActor.tell(new PingMessage(), pongActor);
        }
    }
}
