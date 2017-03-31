package sk.kukla.jurcisin.vincent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sk.kukla.jurcisin.vincent.utils.TestUtils;

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
        TestUtils.printOverview();
        TestUtils.stopCluster();
    }

    @Test
    public void shutdownNonLeaderMember_test() throws Exception {
        TestUtils.stopNonLeaderMember();
        TestUtils.waitForLeaders(1, 15);
        TestUtils.leaderIsDifferentThenKilled();
    }

    @Test
    public void shutdownLeaderMember_test() throws Exception {
        TestUtils.stopLeaderMember();
        TestUtils.waitForLeaders(1, 10);
        TestUtils.leaderIsDifferentThenKilled();
    }

    @Test
    public void rejoinNonLeaderMember_test() throws Exception {
        TestUtils.stopNonLeaderMember();
        TestUtils.waitForLeaders(1, 15);
        TestUtils.leaderIsDifferentThenKilled();
        TestUtils.startupStoppedMember();
        TestUtils.waitForLeaders(1, 15);
    }

    @Test
    public void rejoinLeaderMember_test() throws Exception {
        TestUtils.stopLeaderMember();
        TestUtils.waitForLeaders(1, 15);
        TestUtils.leaderIsDifferentThenKilled();
        TestUtils.startupStoppedMember();
        TestUtils.waitForLeaders(1, 15);
    }
}
