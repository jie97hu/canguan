package cn.abcyun.canguan.common.util;

import java.util.UUID;

public final class IdUtils {

    private IdUtils() {
    }

    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
