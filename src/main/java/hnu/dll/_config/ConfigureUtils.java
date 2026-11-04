package hnu.dll._config;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.pair.CombineTriple;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigureUtils {

//    static {
//        System.out.println(projectPath);
//        String path = StringUtil.join(ConstantValues.FILE_SPLIT, projectPath, "development", "config", "parameter_config.xml");
//        System.out.println(path);
//    }
    public static Map<String, Long> unitSlotMap;
    static {
        unitSlotMap = new HashMap<>();
        unitSlotMap.put("second", 1000L);
        unitSlotMap.put("minute", 60000L);
        unitSlotMap.put("hour", 3600000L);
        unitSlotMap.put("day", 86400000L);
    }

    public static <T> CombineTriple<String, T, List<T>> getIndependentData(String independentVariableName, String singleName, String varianceName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return Constant.xmlConfigure.getIndependentData(independentVariableName, singleName, varianceName);
    }

    public static List[] getGenerationPrivacyBudgetList() {
        Document document = Constant.xmlConfigure.getDocument();
        Element candidateSet = document.getRootElement().element("candidateSet");
        Element elementPrivacyBudget = (Element)candidateSet.selectNodes("./attribute[@name='GenPrivacyBudget']").get(0);
        String[] privacyBudgetValueStrArr = elementPrivacyBudget.element("value").getTextTrim().split(",");
        String[] privacyBudgetRatioStrArr = elementPrivacyBudget.element("ratio").getTextTrim().split(",");
        List<Double> valueList = new ArrayList<>(), ratioList = new ArrayList<>();
        for (int i = 0; i < privacyBudgetValueStrArr.length; i++) {
            valueList.add(Double.valueOf(privacyBudgetValueStrArr[i]));
            ratioList.add(Double.valueOf(privacyBudgetRatioStrArr[i]));
        }
        return new List[]{valueList, ratioList};
    }
    public static List[] getGenerationWindowSizeList() {
        Document document = Constant.xmlConfigure.getDocument();
        Element candidateSet = document.getRootElement().element("candidateSet");
        Element elementWindowSize = (Element)candidateSet.selectNodes("./attribute[@name='GenWindowSize']").get(0);
        String[] windowSizeValueStrArr = elementWindowSize.element("value").getTextTrim().split(",");
        String[] windowSizeRatioStrArr = elementWindowSize.element("ratio").getTextTrim().split(",");
        List<Integer> valueList = new ArrayList<>();
        List<Double> ratioList = new ArrayList<>();
        for (int i = 0; i < windowSizeValueStrArr.length; i++) {
            valueList.add(Integer.valueOf(windowSizeValueStrArr[i]));
            ratioList.add(Double.valueOf(windowSizeRatioStrArr[i]));
        }
        return new List[]{valueList, ratioList};
    }
//    @Deprecated
//    public static Double getPrivacyBudgetLowerBound() {
//        Document document = Constant.xmlConfigure.getDocument();
//        Element candidateSet = document.getRootElement().element("candidateSet");
//        Element elementPrivacyBudget = (Element)candidateSet.selectNodes("./attribute[@name='PrivacyBudgetLowerBound']").get(0);
//        String budgetLowerBoundString = elementPrivacyBudget.element("value").getTextTrim();
//        Double budgetLowerBound = Double.valueOf(budgetLowerBoundString);
//        return budgetLowerBound;
//    }
    public static Double getPrivacyBudgetUpperBound() {
        Document document = Constant.xmlConfigure.getDocument();
        Element candidateSet = document.getRootElement().element("candidateSet");
        Element elementPrivacyBudget = (Element)candidateSet.selectNodes("./attribute[@name='PrivacyBudgetUpperBound']").get(0);
        String budgetUpperBoundString = elementPrivacyBudget.element("value").getTextTrim();
        Double budgetUpperBound = Double.valueOf(budgetUpperBoundString);
        return budgetUpperBound;
    }
    public static Integer getWindowSizeLowerBound() {
        Document document = Constant.xmlConfigure.getDocument();
        Element candidateSet = document.getRootElement().element("candidateSet");
        Element elementWindowSize = (Element) candidateSet.selectNodes("./attribute[@name='WindowSizeLowerBound']").get(0);
        String windowSizeLowerBoundString = elementWindowSize.element("value").getTextTrim();
        Integer windowSizeLowerBound = Integer.valueOf(windowSizeLowerBoundString);
        return windowSizeLowerBound;
    }

    public static Double getBackwardPrivacyBudgetUpperBoundDifference() {
        Document document = Constant.xmlConfigure.getDocument();
        Element candidateSet = document.getRootElement().element("candidateSet");
        Element elementPrivacyBudget = (Element)candidateSet.selectNodes("./attribute[@name='BackwardPrivacyBudgetUpperBoundDifference']").get(0);
        String budgetUpperBoundString = elementPrivacyBudget.element("value").getTextTrim();
        Double budgetUpperBound = Double.valueOf(budgetUpperBoundString);
        return budgetUpperBound;
    }

    public static Double getBackwardPrivacyBudgetLowerBoundDifference() {
        Document document = Constant.xmlConfigure.getDocument();
        Element candidateSet = document.getRootElement().element("candidateSet");
        Element elementPrivacyBudget = (Element)candidateSet.selectNodes("./attribute[@name='BackwardPrivacyBudgetLowerBoundDifference']").get(0);
        String budgetLowerBoundString = elementPrivacyBudget.element("value").getTextTrim();
        Double budgetLowerBound = Double.valueOf(budgetLowerBoundString);
        return budgetLowerBound;
    }

    public static Integer getMaxWindowSize() {
        List[] dataListArray = getGenerationWindowSizeList();
        List<Integer> list = dataListArray[0];
        Integer maxWindowSize = BasicArrayUtil.getIntegerMaxValue(list);
        return maxWindowSize;
    }



    public static Integer getDefaultUserTypeSize() {
        Integer result;
        try {
            result = (Integer) Constant.xmlConfigure.getIndependentData("UserType", "default", "default").getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String getDatasetBasicPath() {
        Document document = Constant.xmlConfigure.getDocument();
        Element relativePathElement = (Element) document.selectNodes("//datasets/basicPath[@type='relative']").get(0);
        String relativePath = relativePathElement.getTextTrim(), absolutePath;
        relativePath = relativePath.replace(";", ConstantValues.FILE_SPLIT);
        absolutePath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.projectPath, relativePath);
        File datasetFile = new File(absolutePath);
        if (datasetFile.exists()) {
            return datasetFile.getAbsolutePath();
        }
        List<Element> elemnetList = document.selectNodes("//datasets/basicPath[@type='absolute']");
        List<String> testPathList = new ArrayList<>();
        for (Element element : elemnetList) {
            absolutePath = element.getTextTrim();
            absolutePath = absolutePath.replace(";", ConstantValues.FILE_SPLIT);
            testPathList.add(absolutePath);
            datasetFile = new File(absolutePath);
            if (datasetFile.exists()) {
                return datasetFile.getAbsolutePath();
            }
        }
        MyPrint.showList(testPathList, ConstantValues.LINE_SPLIT);
        throw new RuntimeException("No valid data set path!");
    }

    public static String getDatasetFileName(String tagName) {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element)document.selectNodes("//datasets/fileName[@name='" + tagName + "']").get(0);
        return element.getTextTrim();
    }


    public static Long getTimeInterval(String datasetName) {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element) document.selectNodes("//fileHandle[@name='" + datasetName + "']").get(0);
        Element subElement = (Element) element.selectNodes("timeSlot").get(0);
        Long unit = unitSlotMap.get(subElement.attributeValue("unit"));
        Long time = Long.valueOf(subElement.getTextTrim());
        return unit * time;
    }

    public static Integer getTrajectoryLongitudeSize() {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element) document.selectNodes("//fileHandle[@name='trajectories']").get(0);
        Element longitudeElement = (Element) element.selectNodes("longitudeSplitSize").get(0);
//        Element latitudeElement = (Element) element.selectNodes("latitudeSplitSize").get(0);
        return Integer.valueOf(longitudeElement.getTextTrim());
    }
    public static Integer getTrajectoryLatitudeSize() {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element) document.selectNodes("//fileHandle[@name='trajectories']").get(0);
        Element latitudeElement = (Element) element.selectNodes("latitudeSplitSize").get(0);
        return Integer.valueOf(latitudeElement.getTextTrim());
    }

    public static List<Double> getIndependentPrivacyBudgetList(String varianceName) {
        Document document = Constant.xmlConfigure.getDocument();;
        Element element = (Element) document.selectNodes("//independentVariables/attribute[@name='PrivacyBudget']/variance[@name='" + varianceName + "']").get(0);
        String textTrim = element.getTextTrim();
        String[] strArr = textTrim.split(",");
        List<Double> result = new ArrayList<>(strArr.length);
        for (String str : strArr) {
            result.add(Double.valueOf(str));
        }
        return result;
    }

    public static Double getIndependentSinglePrivacyBudget(String varianceName) {
        Document document = Constant.xmlConfigure.getDocument();;
        Element element = (Element) document.selectNodes("//independentVariables/attribute[@name='PrivacyBudget']/single[@name='" + varianceName + "']").get(0);
        String textTrim = element.getTextTrim();
        return Double.valueOf(textTrim);
    }

    public static List<Integer> getIndependentWindowSizeList(String varianceName) {
        Document document = Constant.xmlConfigure.getDocument();;
        Element element = (Element) document.selectNodes("//independentVariables/attribute[@name='WindowSize']/variance[@name='" + varianceName + "']").get(0);
        String textTrim = element.getTextTrim();
        String[] strArr = textTrim.split(",");
        List<Integer> result = new ArrayList<>(strArr.length);
        for (String str : strArr) {
            result.add(Integer.valueOf(str));
        }
        return result;
    }
    public static Integer getIndependentSingleWindowSize(String varianceName) {
        Document document = Constant.xmlConfigure.getDocument();;
        Element element = (Element) document.selectNodes("//independentVariables/attribute[@name='WindowSize']/single[@name='" + varianceName + "']").get(0);
        String textTrim = element.getTextTrim();
        return Integer.valueOf(textTrim);
    }

    /**
     * 当前实验使用的配置标签，对应 parameter_config.xml 中 independentVariables 下各 attribute 的 name。
     * 通过 JVM 参数 -Dexp.tag=xxx 指定，未指定时为 "default"。
     */
    public static String getExperimentTag() {
        String tag = System.getProperty("exp.tag");
        if (tag == null || tag.trim().isEmpty()) {
            return "default";
        }
        return tag.trim();
    }

    /**
     * 实验结果的输出根目录（相对于数据集根目录），优先级：
     * 1) JVM 参数 -Dexp.result.dir（维度可扩展性实验用它把结果写到独立目录，
     *    避免与主实验的结果混在同一个 1.main_ldp_result 下）；
     * 2) 默认值 "../1.main_ldp_result"（与改造前完全一致）。
     *
     * @return 结果输出根目录的相对路径
     */
    public static String getResultOutputDirName() {
        String override = System.getProperty("exp.result.dir");
        if (override == null || override.trim().isEmpty()) {
            return "../1.main_ldp_result";
        }
        return override.trim();
    }

    /**
     * 读取指定标签下的取值域大小列表 d（逗号分隔，如 "2,5,10,25,50"）。
     * 未配置该标签时返回 null。
     */
    public static List<Integer> getIndependentDomainSizeList(String tagName) {
        try {
            Document document = Constant.xmlConfigure.getDocument();
            Element element = (Element) document.selectNodes(
                    "//independentVariables/attribute[@name='DomainSize']/single[@name='" + tagName + "']").get(0);
            List<Integer> result = new ArrayList<>();
            for (String item : element.getTextTrim().split(",")) {
                String temp = item.trim();
                if (!temp.isEmpty()) {
                    result.add(Integer.valueOf(temp));
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 当前实验实际使用的取值域大小 d，优先级：
     * 1) JVM 参数 -Dexp.domain（维度实验循环时指定"当前跑哪一个 d"）；
     * 2) 标签对应列表的第一个值；
     * 3) 都没有则返回 null，由调用方回退到 fileHandle 中的 locationTypeSize。
     */
    public static Integer getIndependentSingleDomainSize(String tagName) {
        String override = System.getProperty("exp.domain");
        if (override != null && !override.trim().isEmpty()) {
            try {
                return Integer.valueOf(override.trim());
            } catch (NumberFormatException ignored) {
                // 解析失败时继续走配置
            }
        }
        List<Integer> domainSizeList = getIndependentDomainSizeList(tagName);
        if (domainSizeList == null || domainSizeList.isEmpty()) {
            return null;
        }
        return domainSizeList.get(0);
    }

    /**
     * 当前实验实际使用的用户数（对应 independentVariables 中的 UserSize），优先级：
     * 1) 当前标签（-Dexp.tag）下的 UserSize；
     * 2) 都没有则返回 null，由调用方回退到 fileHandle 中的 userSize。
     *
     * 之所以不读 fileHandle：userSize 是数据集级别的配置，主实验与维度实验共用，
     * 直接在 fileHandle 上改会连带影响主实验。这里让维度实验可以按标签单独指定。
     *
     * @param tagName 实验标签，如 "default" / "fixed"
     * @return 用户数；未配置时返回 null
     */
    public static Integer getIndependentSingleUserSize(String tagName) {
        String override = System.getProperty("exp.user.size");
        if (override != null && !override.trim().isEmpty()) {
            try {
                return Integer.valueOf(override.trim());
            } catch (NumberFormatException ignored) {
                // 解析失败时继续走配置
            }
        }
        try {
            Document document = Constant.xmlConfigure.getDocument();
            Element element = (Element) document.selectNodes(
                    "//independentVariables/attribute[@name='UserSize']/single[@name='" + tagName + "']").get(0);
            String textTrim = element.getTextTrim();
            if (textTrim.isEmpty()) {
                return null;
            }
            return Integer.valueOf(textTrim);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 读取指定标签下的时刻数 timeStampSize（对应 independentVariables 中的 TimeStampSize）。
     * 之所以不读 fileHandle：维度实验需要"用户数放大 N 倍、时刻数同比缩小 N 倍"，
     * 使总数据量（userSize × timeStampSize）保持不变，同时把采样量提上去；
     * 直接在 fileHandle 上改会连带影响主实验。
     *
     * @param tagName 实验标签，如 "default" / "fixed"
     * @return 时刻数；未配置时返回 null
     */
    public static Integer getIndependentSingleTimeStampSize(String tagName) {
        String override = System.getProperty("exp.timestamp.size");
        if (override != null && !override.trim().isEmpty()) {
            try {
                return Integer.valueOf(override.trim());
            } catch (NumberFormatException ignored) {
                // 解析失败时继续走配置
            }
        }
        try {
            Document document = Constant.xmlConfigure.getDocument();
            Element element = (Element) document.selectNodes(
                    "//independentVariables/attribute[@name='TimeStampSize']/single[@name='" + tagName + "']").get(0);
            String textTrim = element.getTextTrim();
            if (textTrim.isEmpty()) {
                return null;
            }
            return Integer.valueOf(textTrim);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 读取指定标签下的 Zipf 指数 alpha（对应 independentVariables 中的 ZipfAlpha）。
     * 未配置时返回 0，表示"除主状态外其余状态均分"（均匀分布）。
     */
    public static Double getIndependentSingleZipfAlpha(String tagName) {
        try {
            Document document = Constant.xmlConfigure.getDocument();
            Element element = (Element) document.selectNodes(
                    "//independentVariables/attribute[@name='ZipfAlpha']/single[@name='" + tagName + "']").get(0);
            return Double.valueOf(element.getTextTrim());
        } catch (Exception e) {
            return 0D;
        }
    }

    /**
     * 当前实验是否启用"隐私参数与数据分布相关"的合成数据模式。
     */
    public static boolean isGroupShiftExperiment() {
        return "shift".equals(getExperimentTag());
    }

    /**
     * 读取独立变量下某个属性的单值配置。
     * 这里专门给 group shift 实验的附加配置使用，未配置时返回 null，
     * 这样旧实验不需要增加任何配置也不会受到影响。
     */
    private static String getIndependentSingleText(String attributeName, String tagName) {
        try {
            Document document = Constant.xmlConfigure.getDocument();
            Element element = (Element) document.selectNodes(
                    "//independentVariables/attribute[@name='" + attributeName + "']/single[@name='" + tagName + "']").get(0);
            String textTrim = element.getTextTrim();
            if (textTrim.isEmpty()) {
                return null;
            }
            return textTrim;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * group shift 中两组主状态概率的绝对偏移量 δ。
     * 可以通过 -Dexp.shift 覆盖配置文件中的默认值。
     */
    public static Double getGroupShiftProbabilityDelta() {
        String override = System.getProperty("exp.shift");
        String value = (override == null || override.trim().isEmpty())
                ? getIndependentSingleText("GroupShiftProbabilityDelta", "shift")
                : override.trim();
        Double result = value == null ? 0D : Double.valueOf(value);
        if (result < 0D || result > 1D) {
            throw new IllegalArgumentException("group shift probability delta must be in [0, 1], but got " + result);
        }
        return result;
    }

    /**
     * group shift 的相关方向：positive 表示高隐私组使用 p(t)+δ，
     * reverse 表示高隐私组使用 p(t)-δ。
     */
    public static String getGroupShiftDirection() {
        String override = System.getProperty("exp.shift.direction");
        String result = (override == null || override.trim().isEmpty())
                ? getIndependentSingleText("GroupShiftDirection", "shift")
                : override.trim();
        if (result == null || result.isEmpty()) {
            return "positive";
        }
        if (!"reverse".equalsIgnoreCase(result) && !"positive".equalsIgnoreCase(result)) {
            throw new IllegalArgumentException("group shift direction must be positive or reverse, but got " + result);
        }
        return result.toLowerCase();
    }

    public static Double getGroupShiftHighPrivacyRatio() {
        String value = getIndependentSingleText("GroupShiftHighPrivacyRatio", "shift");
        Double result = value == null ? 0.5D : Double.valueOf(value);
        if (result < 0D || result > 1D) {
            throw new IllegalArgumentException("high privacy group ratio must be in [0, 1], but got " + result);
        }
        return result;
    }

    public static Double getGroupShiftHighPrivacyBudget() {
        String value = getIndependentSingleText("GroupShiftHighPrivacyBudget", "shift");
        return value == null ? 0.5D : Double.valueOf(value);
    }

    public static Integer getGroupShiftHighPrivacyWindowSize() {
        String value = getIndependentSingleText("GroupShiftHighPrivacyWindowSize", "shift");
        return value == null ? 200 : Integer.valueOf(value);
    }

    public static Double getGroupShiftLowPrivacyBudget() {
        String value = getIndependentSingleText("GroupShiftLowPrivacyBudget", "shift");
        return value == null ? 2.5D : Double.valueOf(value);
    }

    public static Integer getGroupShiftLowPrivacyWindowSize() {
        String value = getIndependentSingleText("GroupShiftLowPrivacyWindowSize", "shift");
        return value == null ? 40 : Integer.valueOf(value);
    }

    public static List<Double> getIndependentUserRatioList(String varianceName) {
        Document document = Constant.xmlConfigure.getDocument();;
        Element element = (Element) document.selectNodes("//independentVariables/attribute[@name='TwoFixedUserRatio']/variance[@name='" + varianceName + "']").get(0);
        String textTrim = element.getTextTrim();
        String[] strArr = textTrim.split(",");
        List<Double> result = new ArrayList<>(strArr.length);
        for (String str : strArr) {
            result.add(Double.valueOf(str));
        }
        return result;
    }

    public static String getFileHandleInfo(String datasetName, String subTagName) {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element) document.selectNodes("//datasets/fileHandle[@name='" + datasetName + "']/" + subTagName).get(0);
        String textTrim = element.getTextTrim();
        return textTrim;
    }

    public static Double[] getTwoFixedPrivacyBudget() {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element) document.selectNodes("//attribute[@name='TwoFixedPrivacyBudget']/value[@name='default']").get(0);
        String[] budgetStrArr = element.getTextTrim().split(",");
        return new Double[]{Double.valueOf(budgetStrArr[0]), Double.valueOf(budgetStrArr[1])};
    }
    public static Integer[] getTwoFixedWindowSize() {
        Document document = Constant.xmlConfigure.getDocument();
        Element element = (Element) document.selectNodes("//attribute[@name='TwoFixedWindowSize']/value[@name='default']").get(0);
        String[] budgetStrArr = element.getTextTrim().split(",");
        return new Integer[]{Integer.valueOf(budgetStrArr[0]), Integer.valueOf(budgetStrArr[1])};
    }


    public static void main0(String[] args) {
        Double privacyBudgetUpperBound = ConfigureUtils.getPrivacyBudgetUpperBound();
        System.out.println(privacyBudgetUpperBound);
        Integer windowSizeLowerBound = ConfigureUtils.getWindowSizeLowerBound();
        System.out.println(windowSizeLowerBound);
        Double backUpperBound = ConfigureUtils.getBackwardPrivacyBudgetUpperBoundDifference();
        System.out.println(backUpperBound);
        Double backLowerBound = ConfigureUtils.getBackwardPrivacyBudgetLowerBoundDifference();
        System.out.println(backLowerBound);
    }

    public static void main1(String[] args) {
//        String datasetBasicPath = ConfigureUtils.getDatasetBasicPath();
//        System.out.println(datasetBasicPath);
//        String fileName = ConfigureUtils.getDatasetFileName("trajectories");
//        String fileName = ConfigureUtils.getDatasetFileName("checkIn");
//        System.out.println(fileName);
        System.out.println(Constant.projectPath);
    }

    public static void main2(String[] args) {
        Long timeStamp = getTimeInterval("checkIn");
        System.out.println(timeStamp);
    }

    public static void main3(String[] args) {
        Integer longitudeSize = getTrajectoryLongitudeSize();
        Integer latitudeSize = getTrajectoryLatitudeSize();
        System.out.println(longitudeSize);
        System.out.println(latitudeSize);
    }

    public static void main4(String[] args) {
        List<Double> result = getIndependentPrivacyBudgetList("default");
        MyPrint.showList(result);
    }

    public static void main5(String[] args) {
//        List[] result = getGenerationPrivacyBudgetList();
        List[] result = getGenerationWindowSizeList();
        MyPrint.showList(result[0]);
        MyPrint.showList(result[1]);
    }

    public static void main(String[] args) {
        Double[] budgetArr = getTwoFixedPrivacyBudget();
        MyPrint.showArray(budgetArr, "; ");
        Integer[] wSizeArr = getTwoFixedWindowSize();
        MyPrint.showArray(wSizeArr, "; ");
    }
}
