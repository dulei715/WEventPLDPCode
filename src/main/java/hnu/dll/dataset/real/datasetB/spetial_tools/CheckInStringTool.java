package hnu.dll.dataset.real.datasetB.spetial_tools;

import cn.edu.dll.basic.NumberUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CheckInStringTool {
    @Deprecated
    public static String[] citySplitOld(String record) {
        String[] tempResult = record.split("[\\s]+");
        int numberStartIndex;
        String tempString;
        List<String> resultList = new ArrayList<>();
        for (numberStartIndex = 0; numberStartIndex < tempResult.length && !NumberUtil.isNumber(tempResult[numberStartIndex]); ++numberStartIndex);
        tempString = StringUtil.join(" ", tempResult, 0, numberStartIndex - 1);
        resultList.add(tempString);
        int rightIndex = numberStartIndex + 4;
        for (int i = numberStartIndex; i < rightIndex; i++) {
            resultList.add(tempResult[i]);
        }
        tempString = StringUtil.join(" ", tempResult, rightIndex);
        resultList.add(tempString);
        return resultList.toArray(new String[0]);
    }
    public static String[] recordSplit(String record) {
        String[] tempResult = record.split("\\t");
        return tempResult;
    }

    public static String toRFC1132(String data) {
        String[] dataArray = data.split(" ");

//         Tue, 3 Jun 2008 11:05:30 GMT
//        String dataString = "Tue Apr 03 18:00:06 +0000 2012";
        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(dataArray[0]).append(", ");
        stringBuilder.append(dataArray[0]).append(", ");
        stringBuilder.append(dataArray[2]).append(" ");
        stringBuilder.append(dataArray[1]).append(" ");
//        if (dataArray.length <= 5) {
//            System.out.println("error!");
//        }
        stringBuilder.append(dataArray[5]).append(" ");
        stringBuilder.append(dataArray[3]).append(" ");
        stringBuilder.append("GMT");
        return stringBuilder.toString();
    }
    public static String toZonedDateTime(String data) {
        String formatString = toRFC1132(data);
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(formatString, formatter);
        return zonedDateTime.toString();
    }
    public static long toTimestamp(String data) {
        String formatString = toRFC1132(data);
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        Instant instant = Instant.from(formatter.parse(formatString));
        return instant.toEpochMilli();
    }



    public static void main(String[] args) {
//        String record = "Cuiaba\t-15.615000\t-56.093004\tBR\tBrazil\tProvincial capital";
        String record = "Saint Paul\t45.020997\t-93.065004\tUS\tUnited States\tProvincial capital";
//        System.out.println(record);
        String[] resultArray = recordSplit(record);
        MyPrint.showArray(resultArray, ConstantValues.LINE_SPLIT);
    }
}
