/**
 * Copyright (c) 2022-2023, Mybatis-Flex (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mybatisflex.core.audit;

import com.mybatisflex.core.FlexConsts;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.ParamNameResolver;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * 审计管理器，统一执行如何和配置入口
 */
public class AuditManager {

    private static boolean auditEnable = false;
    private static Clock clock = System::currentTimeMillis;
    private static MessageCreator messageCreator = new DefaultMessageCreator();
    private static MessageCollector messageCollector = new ScheduledMessageCollector();

    public static boolean isAuditEnable() {
        return auditEnable;
    }

    public static void setAuditEnable(boolean auditEnable) {
        AuditManager.auditEnable = auditEnable;
    }

    public static Clock getClock() {
        return clock;
    }

    public static void setClock(Clock clock) {
        AuditManager.clock = clock;
    }

    public static MessageCreator getMessageCreator() {
        return messageCreator;
    }

    public static void setMessageCreator(MessageCreator messageCreator) {
        AuditManager.messageCreator = messageCreator;
    }

    public static MessageCollector getMessageCollector() {
        return messageCollector;
    }

    public static void setMessageCollector(MessageCollector messageCollector) {
        AuditManager.messageCollector = messageCollector;
    }

    public static <T> T startAudit(AuditRunnable<T> supplier, BoundSql boundSql) throws SQLException {
        AuditMessage auditMessage = messageCreator.create();
        if (auditMessage == null) {
            return supplier.execute();
        }
        auditMessage.setQueryTime(clock.getTick());
        try {
            return supplier.execute();
        } finally {
            auditMessage.setElapsedTime(clock.getTick() - auditMessage.getQueryTime());
            auditMessage.setQuery(boundSql.getSql());
            Object parameter = boundSql.getParameterObject();


            /** parameter 的组装请查看 getNamedParams 方法
             * @see ParamNameResolver#getNamedParams(Object[])
             */
            if (parameter instanceof Map) {
                if (((Map<?, ?>) parameter).containsKey(FlexConsts.SQL_ARGS)) {
                    auditMessage.addParams(((Map<?, ?>) parameter).get(FlexConsts.SQL_ARGS));
                } else if (((Map<?, ?>) parameter).containsKey("collection")) {
                    Collection collection = (Collection) ((Map<?, ?>) parameter).get("collection");
                    auditMessage.addParams(collection.toArray());
                } else if (((Map<?, ?>) parameter).containsKey("array")) {
                    auditMessage.addParams(((Map<?, ?>) parameter).get("array"));
                } else {
                    for (int i = 1; i <= 100; i++) {
                        if (((Map<?, ?>) parameter).containsKey(ParamNameResolver.GENERIC_NAME_PREFIX + i)) {
                            auditMessage.addParams(((Map<?, ?>) parameter).get(ParamNameResolver.GENERIC_NAME_PREFIX + i));
                        }
                    }
                }
            }
            messageCollector.collect(auditMessage);
        }
    }


    @FunctionalInterface
    public interface AuditRunnable<T> {
        T execute() throws SQLException;
    }

}