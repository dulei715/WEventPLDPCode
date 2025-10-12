package hnu.dll._config;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.CombineTriple;
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
        Element elementPrivacyBudget = (Element)candidateSet.selectNodes("./version_2/attribute[@name='GenPrivacyBudget']").get(0);
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
        Element elementWindowSize = (Element)candidateSet.selectNodes("./version_2/attribute[@name='GenWindowSize']").get(0);
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
