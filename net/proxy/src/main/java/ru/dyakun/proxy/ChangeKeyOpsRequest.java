package ru.dyakun.proxy;

import java.nio.channels.SelectionKey;

public record ChangeKeyOpsRequest(SelectionKey key, int ops) { }
