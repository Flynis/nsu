package ru.dyakun.proxy;

import java.nio.channels.SelectionKey;

public record ChangeOpReq(SelectionKey key, int ops) { }
