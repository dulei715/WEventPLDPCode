package hnu.dll.schemes.compare_scheme._0_non_privacy;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.structure.HistoryPopulationQueue;

import java.util.*;

public abstract class NonPrivacyMechanism extends Mechanism {

    protected int currentTime;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;


    protected HistoryPopulationQueue samplingSubMechanismHistoryQueue;
    protected HistoryPopulationQueue publicationSubMechanismHistoryQueue;

    List<Integer> calculationPopulationIndexList;
    List<Integer> publicationPopulationIndexList;


    public NonPrivacyMechanism(Set<String> dataTypeSet) {
        this.currentTime = -1;
        this.domainSize = dataTypeSet.size();
        this.domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.domainSize - 1);
    }



    public CombinePair<Boolean, Map<Integer, Double>> updateNextPublicationResult(List<Integer> nextDataIndexList) {

        ++this.currentTime;
        Boolean flag = true;
        LinkedHashMap<Integer, Double> estimation = BasicArrayUtil.getUniqueListWithStatisticList(nextDataIndexList);
        return new CombinePair<>(flag, estimation);
    }



    public String getSimpleName() {
        return "Non-Privacy";
    }

}
