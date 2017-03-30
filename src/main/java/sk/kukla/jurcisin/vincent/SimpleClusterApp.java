package sk.kukla.jurcisin.vincent;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import sk.kukla.jurcisin.vincent.actors.ClusterStateActor;
import sk.kukla.jurcisin.vincent.management.ClusterState;

public class SimpleClusterApp {

    public static void main(String[] args) {
        if (args.length == 0)
            startup(new String[] { "2551", "2552", "0" });
        else
            startup(args);
    }

    public static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                    .withFallback(ConfigFactory.load());
            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);
//            ActorRef pingActor = system.actorOf(Props.create(PingActor.class), "PingActor");
//            ActorRef pongActor = system.actorOf(Props.create(PongActor.class), "PongActor");
//            pingActor.tell(new PingMessage(), pongActor);
            system.actorOf(ClusterStateActor.props(new ClusterState(system)), "ClusterStateActor");
        }
    }
}
