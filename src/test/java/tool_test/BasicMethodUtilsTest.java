package tool_test;

import hnu.dll.schemes.main_scheme._0_proposed_tools.BasicMethodUtils;
import hnu.dll.structure.personalized_data.AbstractPrivacyGroup;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BasicMethodUtilsTest {
    @Test
    public void constructDominatingGroupsTest() {
        /*
         * 构造 4 个 group:
         * D: (w=2, ε=1.0)
         * A: (w=3, ε=0.5)
         * C: (w=5, ε=0.8)
         * B: (w=5, ε=0.3)
         *
         * 排序后(w升序, ε降序): D, A, C, B
         *
         * source 条件: w_r <= w_H && ε_r >= ε_H
         * D → []          (第一个，无 source)
         * A → [D]         (D: w=2<=3, ε=1.0>=0.5)
         * C → [D]         (D: w=2<=5, ε=1.0>=0.8; A: ε=0.5<0.8 不满足)
         * B → [A, C, D]   (A: w=3<=5, ε=0.5>=0.3; C: w=5<=5, ε=0.8>=0.3; D: w=2<=5, ε=1.0>=0.3)
         */
        AbstractPrivacyGroup groupD = new PrivacyGroupTriple(2, 1.0, 100);
        AbstractPrivacyGroup groupA = new PrivacyGroupTriple(3, 0.5, 200);
        AbstractPrivacyGroup groupC = new PrivacyGroupTriple(5, 0.8, 150);
        AbstractPrivacyGroup groupB = new PrivacyGroupTriple(5, 0.3, 300);

        List<AbstractPrivacyGroup> groupList = new ArrayList<>();
        groupList.add(groupA);
        groupList.add(groupB);
        groupList.add(groupC);
        groupList.add(groupD);

        Map<AbstractPrivacyGroup, List<AbstractPrivacyGroup>> result =
                BasicMethodUtils.constructDominatingGroups(groupList);

        // D: 无 source
        assertTrue("D 的 sourceGroups 应为空", result.get(groupD).isEmpty());

        // A: source = [D]
        assertEquals("A 应有 1 个 source", 1, result.get(groupA).size());
        assertTrue("A 的 source 应包含 D", result.get(groupA).contains(groupD));

        // C: source = [D]
        assertEquals("C 应有 1 个 source", 1, result.get(groupC).size());
        assertTrue("C 的 source 应包含 D", result.get(groupC).contains(groupD));
        assertFalse("C 的 source 不应包含 A", result.get(groupC).contains(groupA));

        // B: source = [A, C, D]
        assertEquals("B 应有 3 个 source", 3, result.get(groupB).size());
        assertTrue("B 的 source 应包含 A", result.get(groupB).contains(groupA));
        assertTrue("B 的 source 应包含 C", result.get(groupB).contains(groupC));
        assertTrue("B 的 source 应包含 D", result.get(groupB).contains(groupD));
    }

    @Test
    public void constructDominatingGroupsNoDominanceTest() {
        /*
         * 所有 group 的 ε 相同，w 递增
         * 排序后每个 group 的 source 都包含前面所有 group
         */
        AbstractPrivacyGroup g1 = new PrivacyGroupTriple(2, 0.5, 100);
        AbstractPrivacyGroup g2 = new PrivacyGroupTriple(3, 0.5, 100);
        AbstractPrivacyGroup g3 = new PrivacyGroupTriple(4, 0.5, 100);

        List<AbstractPrivacyGroup> groupList = new ArrayList<>();
        groupList.add(g3);
        groupList.add(g1);
        groupList.add(g2);

        Map<AbstractPrivacyGroup, List<AbstractPrivacyGroup>> result =
                BasicMethodUtils.constructDominatingGroups(groupList);

        assertTrue("g1 无 source", result.get(g1).isEmpty());
        assertEquals("g2 有 1 个 source", 1, result.get(g2).size());
        assertTrue(result.get(g2).contains(g1));
        assertEquals("g3 有 2 个 source", 2, result.get(g3).size());
        assertTrue(result.get(g3).contains(g1));
        assertTrue(result.get(g3).contains(g2));
    }

    private List<AbstractPrivacyGroup> buildTestGroups() {
        List<AbstractPrivacyGroup> list = new ArrayList<>();
        list.add(new PrivacyGroupTriple(2, 1.0, 100));  // idx0
        list.add(new PrivacyGroupTriple(3, 0.5, 200));  // idx1
        list.add(new PrivacyGroupTriple(5, 0.8, 150));  // idx2
        list.add(new PrivacyGroupTriple(5, 0.3, 300));  // idx3
        return list;
    }

    @Test
    public void constructDominatingGroupsTest2() {
        List<AbstractPrivacyGroup> groupList = buildTestGroups();

        Map<AbstractPrivacyGroup, List<AbstractPrivacyGroup>> result =
                BasicMethodUtils.constructDominatingGroups(groupList);

        AbstractPrivacyGroup g0 = groupList.get(0);
        AbstractPrivacyGroup g1 = groupList.get(1);
        AbstractPrivacyGroup g2 = groupList.get(2);
        AbstractPrivacyGroup g3 = groupList.get(3);

        assertTrue(result.get(g0).isEmpty());
        assertEquals(1, result.get(g1).size());
        assertTrue(result.get(g1).contains(g0));
        assertEquals(1, result.get(g2).size());
        assertTrue(result.get(g2).contains(g0));
        assertFalse(result.get(g2).contains(g1));
        assertEquals(3, result.get(g3).size());
        assertTrue(result.get(g3).contains(g0));
        assertTrue(result.get(g3).contains(g1));
        assertTrue(result.get(g3).contains(g2));
    }

    @Test
    public void constructDominatingGroupsAsIndexTest() {
        List<AbstractPrivacyGroup> groupList = buildTestGroups();

        Map<Integer, List<Integer>> indexResult =
                BasicMethodUtils.constructDominatingGroupsAsIndex(groupList);

        // 与对象版本交叉验证
        Map<AbstractPrivacyGroup, List<AbstractPrivacyGroup>> objResult =
                BasicMethodUtils.constructDominatingGroups(groupList);

        assertEquals(objResult.size(), indexResult.size());

        for (int i = 0; i < groupList.size(); i++) {
            List<Integer> indexSources = indexResult.get(i);
            List<AbstractPrivacyGroup> objSources = objResult.get(groupList.get(i));
            assertEquals("idx" + i + " source 数量不一致", objSources.size(), indexSources.size());
            for (Integer idx : indexSources) {
                assertTrue("idx" + i + " 应包含 " + idx, objSources.contains(groupList.get(idx)));
            }
        }

        // 精确断言
        assertTrue(indexResult.get(0).isEmpty());
        assertEquals(1, indexResult.get(1).size());
        assertTrue(indexResult.get(1).contains(0));
        assertEquals(1, indexResult.get(2).size());
        assertTrue(indexResult.get(2).contains(0));
        assertFalse(indexResult.get(2).contains(1));
        assertEquals(3, indexResult.get(3).size());
        assertTrue(indexResult.get(3).contains(0));
        assertTrue(indexResult.get(3).contains(1));
        assertTrue(indexResult.get(3).contains(2));
    }

}
