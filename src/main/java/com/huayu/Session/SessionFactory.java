package com.huayu.Session;

/**
 * 会话工厂
 */
public abstract class SessionFactory {

    private static Session session = new SessionMemoryImpl();

    public static Session getSession() {
        return session;
    }
}
