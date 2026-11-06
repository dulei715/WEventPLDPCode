package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import hnu.dll.metric.Measurement;
import hnu.dll.utils.BasicUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes shift-specific group-level AMSE and AJSD without changing the
 * existing result.txt schema used by the original experiments.
 */
public final class GroupShiftMetricUtils {

    private static final ThreadLocal<BatchContext> BATCH_CONTEXT = new ThreadLocal<>();
    private static final Map<String, Map<Integer, List<List<Double>>>> TRUTH_CACHE =
            new ConcurrentHashMap<>();

    private GroupShiftMetricUtils() {
    }

    public static void setBatchContext(String basicPath, int segmentID,
                                       List<Integer> timestampList, int totalUserSize) {
        if (!GroupShiftUtils.isEnabled()) {
            BATCH_CONTEXT.remove();
            return;
        }
        BATCH_CONTEXT.set(new BatchContext(basicPath, segmentID,
                new ArrayList<>(timestampList), totalUserSize));
    }

    public static void clearBatchContext() {
        BATCH_CONTEXT.remove();
    }

    public static int parseTimestamp(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }
        if (name.startsWith("timestamp_")) {
            name = name.substring("timestamp_".length());
        }
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException ignored) {
            String digits = name.replaceAll("[^0-9-]", "");
            if (digits.isEmpty() || "-".equals(digits)) {
                throw new IllegalArgumentException("Cannot parse timestamp from " + file);
            }
            return Integer.parseInt(digits);
        }
    }

    public static List<Map<Integer, Double>> copyGroupEstimationList(
            List<Map<Integer, Double>> source) {
        if (source == null) {
            return null;
        }
        List<Map<Integer, Double>> result = new ArrayList<>(source.size());
        for (Map<Integer, Double> map : source) {
            result.add(map == null ? null : new HashMap<>(map));
        }
        return result;
    }

    public static void recordBatch(String methodName, int batchID,
                                   List<Map<Integer, Double>> globalPublicationList,
                                   List<List<Map<Integer, Double>>> groupEstimationBatchList,
                                   List<Set<Integer>> groupUserIndexSetList,
                                   List<Integer> domainIndexList) {
        if (!GroupShiftUtils.isEnabled()) {
            return;
        }
        BatchContext context = BATCH_CONTEXT.get();
        if (context == null || globalPublicationList == null) {
            return;
        }
        Map<Integer, List<List<Double>>> truthByTimestamp = loadTruth(context.basicPath);
        List<GroupShiftUtils.GroupSpec> groupSpecList = GroupShiftUtils.getGroupSpecList();
        int groupCount = groupSpecList.size();
        String safeMethodName = methodName.replaceAll("[^A-Za-z0-9_.-]", "_");
        File outputFile = new File(context.basicPath, "group_shift_metrics/" + safeMethodName
                + "/segment_" + context.segmentID + "/batch_" + batchID + ".txt");
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create group metric directory " + parent);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("timestamp,weighted_amse,weighted_ajsd,group0_amse,group0_ajsd");
            for (int groupIndex = 1; groupIndex < groupCount; groupIndex++) {
                writer.write(",group" + groupIndex + "_amse,group" + groupIndex + "_ajsd");
            }
            writer.newLine();
            int rowCount = Math.min(globalPublicationList.size(), context.timestampList.size());
            for (int batchIndex = 0; batchIndex < rowCount; batchIndex++) {
                int timestamp = context.timestampList.get(batchIndex);
                List<List<Double>> truthList = truthByTimestamp.get(timestamp);
                if (truthList == null || truthList.size() != groupCount) {
                    continue;
                }
                List<Map<Integer, Double>> groupEstimateList = null;
                if (groupEstimationBatchList != null && batchIndex < groupEstimationBatchList.size()) {
                    groupEstimateList = groupEstimationBatchList.get(batchIndex);
                }
                List<Map<Integer, Double>> alignedEstimateList = alignGroupEstimates(
                        groupEstimateList, groupUserIndexSetList, globalPublicationList.get(batchIndex),
                        groupCount, context.totalUserSize);

                double weightedAmse = 0D;
                double weightedAjsd = 0D;
                List<Double> amseList = new ArrayList<>(groupCount);
                List<Double> ajsdList = new ArrayList<>(groupCount);
                for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
                    List<Double> truth = truthList.get(groupIndex);
                    Map<Integer, Double> estimateMap = alignedEstimateList.get(groupIndex);
                    List<Double> estimate = BasicUtils.toSortedListByKeys(
                            BasicUtils.normalizeValues(estimateMap), domainIndexList, 0D);
                    double amse = Measurement.get2NormSquareError(truth, estimate);
                    double ajsd = Measurement.getJSDivergence(truth, estimate);
                    amseList.add(amse);
                    ajsdList.add(ajsd);
                    weightedAmse += groupSpecList.get(groupIndex).getRatio() * amse;
                    weightedAjsd += groupSpecList.get(groupIndex).getRatio() * ajsd;
                }
                writer.write(Integer.toString(timestamp));
                writer.write("," + weightedAmse + "," + weightedAjsd + ","
                        + amseList.get(0) + "," + ajsdList.get(0));
                for (int groupIndex = 1; groupIndex < groupCount; groupIndex++) {
                    writer.write("," + amseList.get(groupIndex) + "," + ajsdList.get(groupIndex));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write group shift metrics", e);
        }
    }

    private static List<Map<Integer, Double>> alignGroupEstimates(
            List<Map<Integer, Double>> groupEstimateList,
            List<Set<Integer>> groupUserIndexSetList,
            Map<Integer, Double> globalEstimate,
            int groupCount, int totalUserSize) {
        List<Map<Integer, Double>> result = new ArrayList<>(groupCount);
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            result.add(globalEstimate == null ? new LinkedHashMap<Integer, Double>()
                    : new HashMap<>(globalEstimate));
        }
        if (groupEstimateList == null || groupUserIndexSetList == null
                || groupEstimateList.size() != groupUserIndexSetList.size()) {
            return result;
        }
        for (int groupIndex = 0; groupIndex < groupEstimateList.size(); groupIndex++) {
            Set<Integer> userIndexSet = groupUserIndexSetList.get(groupIndex);
            Map<Integer, Double> estimate = groupEstimateList.get(groupIndex);
            if (userIndexSet == null || userIndexSet.isEmpty() || estimate == null) {
                continue;
            }
            int firstUserIndex = Integer.MAX_VALUE;
            for (Integer userIndex : userIndexSet) {
                if (userIndex != null && userIndex < firstUserIndex) {
                    firstUserIndex = userIndex;
                }
            }
            if (firstUserIndex == Integer.MAX_VALUE) {
                continue;
            }
            int shiftGroupIndex = GroupShiftUtils.getGroupIndex(firstUserIndex, totalUserSize);
            if (shiftGroupIndex >= 0 && shiftGroupIndex < groupCount) {
                result.set(shiftGroupIndex, new HashMap<>(estimate));
            }
        }
        return result;
    }

    private static Map<Integer, List<List<Double>>> loadTruth(String basicPath) {
        Map<Integer, List<List<Double>>> cached = TRUTH_CACHE.get(basicPath);
        if (cached != null) {
            return cached;
        }
        File truthFile = new File(basicPath, "basic_info/group_shift_truth.txt");
        Map<Integer, List<List<Double>>> result = new HashMap<>();
        if (!truthFile.isFile()) {
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(truthFile))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length < 4) {
                    continue;
                }
                int timestamp = Integer.parseInt(fields[0]);
                int groupIndex = Integer.parseInt(fields[1]);
                List<Double> distribution = new ArrayList<>(fields.length - 3);
                for (int i = 3; i < fields.length; i++) {
                    distribution.add(Double.parseDouble(fields[i]));
                }
                List<List<Double>> groupTruth = result.computeIfAbsent(timestamp,
                        key -> new ArrayList<>());
                while (groupTruth.size() <= groupIndex) {
                    groupTruth.add(null);
                }
                groupTruth.set(groupIndex, distribution);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read group shift truth " + truthFile, e);
        }
        TRUTH_CACHE.put(basicPath, result);
        return result;
    }

    private static class BatchContext {
        private final String basicPath;
        private final int segmentID;
        private final List<Integer> timestampList;
        private final int totalUserSize;

        private BatchContext(String basicPath, int segmentID,
                             List<Integer> timestampList, int totalUserSize) {
            this.basicPath = basicPath;
            this.segmentID = segmentID;
            this.timestampList = timestampList;
            this.totalUserSize = totalUserSize;
        }
    }
}
