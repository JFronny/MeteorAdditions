package io.gitlab.jfronny.meteoradditions.util;

public interface IServerFinderDoneListener {
    void onServerDone(ServerPinger pinger);
    void onServerFailed(ServerPinger pinger);
}
