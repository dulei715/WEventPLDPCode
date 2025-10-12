package hnu.dll.schemes.basic_scheme;

import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes._scheme_utils.BooleanStreamDataElementUtils;
import hnu.dll.structure.stream_data.StreamCountData;
import hnu.dll.structure.stream_data.StreamDataElement;

import java.util.List;
import java.util.TreeMap;

public class NonPrivacyMechanism extends Mechanism {
    protected int currentTime;
    protected StreamCountData lastReleaseCountMap;
    public NonPrivacyMechanism(List<String> dataTypeList) {
        this.currentTime = -1;
        this.lastReleaseCountMap = new StreamCountData(this.currentTime, dataTypeList);
    }

    public StreamCountData getReleaseCountMap() {
        return this.lastReleaseCountMap;
    }

    public boolean updateNextPublicationResult(List<StreamDataElement<Boolean>> nextDataElementList) {
        ++this.currentTime;
        TreeMap<String, Integer> sampleCountMap = BooleanStreamDataElementUtils.getCountByGivenElementType(true, nextDataElementList);
        this.lastReleaseCountMap = new StreamCountData(this.currentTime, sampleCountMap);
        return true;
    }
}
