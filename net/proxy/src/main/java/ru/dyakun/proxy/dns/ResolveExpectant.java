package ru.dyakun.proxy.dns;

import java.net.InetAddress;

public interface ResolveExpectant {

    void onResolve(InetAddress address);

    void onException(Exception e);

}
