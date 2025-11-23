package uk.co.deloitte.banking.ahb.dtp.test.util.environment;

import io.micronaut.context.annotation.Value;
import org.junit.Assume;

import javax.inject.Singleton;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.NOTE;


@Singleton
public class EnvUtils {

    @Value("${test-env}")
    private Environments testEnv;

    @Deprecated
    public void ignoreTest() {
        NOTE("********** TEST Ignored ***********");
        Assume.assumeTrue(false);
    }

    @Deprecated
    public void ignoreTestInEnv(Environments env) {
        NOTE("********** TEST Not deployed in " + env + " and this environment is " + testEnv + "********");
        shouldSkip(env);
    }

    @Deprecated
    public void ignoreTestInEnv(Environments env, Environments env2) {
        NOTE("********** TEST Not deployed in " + env + " and" + env2 + " and this environment is "
                + testEnv + "********");
        shouldSkip(env);
        shouldSkip(env2);
    }


    @Deprecated
    public void ignoreTestInEnv(Environments env, Environments env2, Environments env3) {
        NOTE("********** TEST Not deployed in " + env + " and" + env2 + " and" + env3
                + " and this environment is " + testEnv + "********");

        shouldSkip(env);
        shouldSkip(env2);
        shouldSkip(env3);
    }



    public void ignoreTestInEnv(String reason, Environments env) {
        NOTE("********** TEST Not deployed in " + env + " and this environment is " + testEnv + "******** " +  reason);
        shouldSkip(env);
    }

    public void ignoreTestInEnv(String reason, Environments env, Environments env2) {
        NOTE("********** TEST Not deployed in " + env + " and" + env2 + " and this environment is "
                + testEnv + "********" + reason);
        shouldSkip(env);
        shouldSkip(env2);
    }

    public void ignoreTestInEnv(String reason, Environments env, Environments env2, Environments env3) {
        NOTE("********** TEST Not deployed in " + env + " and" + env2 + " and" + env3
                + " and this environment is " + testEnv + "********" + reason);

        shouldSkip(env);
        shouldSkip(env2);
        shouldSkip(env3);
    }




    private void shouldSkip(Environments env) {
        Assume.assumeTrue(!env.equals(Environments.ALL));
        Assume.assumeTrue(!testEnv.equals(env));
    }

    public void shouldSkipHps(Environments env) {
        Assume.assumeTrue(!env.equals(Environments.ALL));
        Assume.assumeTrue(!testEnv.equals(env));
        //remove commenting to disable cards tests
      //  Assume.assumeTrue(false);
    }

    public boolean isDev(){
        return this.testEnv.equals(Environments.DEV);
    }

    public boolean isCit(){
        return this.testEnv.equals(Environments.CIT);
    }
}
