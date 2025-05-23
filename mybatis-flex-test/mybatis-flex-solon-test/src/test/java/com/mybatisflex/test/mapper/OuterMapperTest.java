/*
 *  Copyright (c) 2022-2025, Mybatis-Flex (fuhai999@gmail.com).
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mybatisflex.test.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.test.entity.Outer;
import com.mybatisflex.test.entity.table.InnerTableDef;
import com.mybatisflex.test.entity.table.OuterTableDef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonTest;

import static com.mybatisflex.test.entity.table.InnerTableDef.INNER;
import static com.mybatisflex.test.entity.table.OuterTableDef.OUTER;

/**
 * @author 王帅
 * @since 2023-07-01
 */
@SolonTest
public class OuterMapperTest {

    @Inject
    private OuterMapper outerMapper;

    @Inject
    private InnerMapper innerMapper;

    @Test
    void testInsert() {
        Outer outer = new Outer();
        outer.setName("outer 01");
        int result = outerMapper.insertSelective(outer);
        Assertions.assertEquals(result,1);
    }

    @Test
    void testSelect() {
        OuterTableDef outer = OUTER.as("o");
        InnerTableDef inner = INNER.as("i");
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(outer.ID,
                outer.NAME,
                inner.ID,
                inner.TYPE)
            .from(outer)
            .leftJoin(inner).on(inner.ID.eq(2))
            .limit(1);
        Outer outer1 = outerMapper.selectOneByQuery(queryWrapper);
        System.out.println(outer1);
    }

}
