package hnu.dll.dataset.real.datasetB.spetial_tools;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.io.write.BasicWrite;
import hnu.dll.dataset.real.datasetB.basic_struct.CheckInBean;
import hnu.dll.dataset.real.datasetB.basic_struct.CityBean;
import hnu.dll.dataset.real.datasetB.basic_struct.POIBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckInBeanUtils {
    public static void transformToCountry(String inputSuperFilePath, String outputSuperFilePath, String outputFileName) {
        String checkInPath = StringUtil.join(ConstantValues.FILE_SPLIT, inputSuperFilePath, "dataset_TIST2015_Checkins.txt");
        String poiPath = StringUtil.join(ConstantValues.FILE_SPLIT, inputSuperFilePath, "dataset_TIST2015_POIs.txt");
        String cityPath = StringUtil.join(ConstantValues.FILE_SPLIT, inputSuperFilePath, "dataset_TIST2015_Cities.txt");
        Map<String, String> countryCodeNameMap = new HashMap<>();
        BasicRead basicRead = new BasicRead();
        CityBean tempCityBean;
        POIBean tempPOIBean;
        CheckInBean tempCheckInBean;
        String tempCountryName, tempVenueID, tempCountryCode;

        basicRead.startReading(cityPath);
        List<String> cityData = basicRead.readAllWithoutLineNumberRecordInFile();
        for (String cityDatum : cityData) {
            tempCityBean = CityBean.toBean(CheckInStringTool.recordSplit(cityDatum));
            countryCodeNameMap.put(tempCityBean.getCountryCode(), tempCityBean.getCountryName());
        }
        basicRead.endReading();

        Map<String, String> venueIDCountryMap = new HashMap<>();
        basicRead.startReading(poiPath);
        List<String> poiData = basicRead.readAllWithoutLineNumberRecordInFile();
        for (String poiDatum : poiData) {
            tempPOIBean = POIBean.toBean(CheckInStringTool.recordSplit(poiDatum));
            tempVenueID = tempPOIBean.getVenueID();
            tempCountryCode = tempPOIBean.getCountryCode();
            venueIDCountryMap.put(tempVenueID, countryCodeNameMap.get(tempCountryCode));
        }
        basicRead.endReading();

        List<String> result = new ArrayList<>();
        basicRead.startReading(checkInPath);
        List<String> checkInRecordList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        String tempString;
        for (String record : checkInRecordList) {
            tempCheckInBean = CheckInBean.toBean(CheckInStringTool.recordSplit(record));
            tempCountryName = venueIDCountryMap.get(tempCheckInBean.getVenueID());
            tempString = StringUtil.join(",", tempCheckInBean.getUserID(), tempCountryName, tempCheckInBean.getUtcTime());
            result.add(tempString);
        }

        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputSuperFilePath, outputFileName);
        BasicWrite basicWrite = new BasicWrite();
        basicWrite.startWriting(outputPath);
        basicWrite.writeStringListWithoutSize(result);
        basicWrite.endWriting();

    }
    public static void transformSplitFilesToCountry(String inputSuperFilePath, String splitDirectory, String outputSuperFilePath) {
        String checkInPath = StringUtil.join(ConstantValues.FILE_SPLIT, inputSuperFilePath, "dataset_TIST2015_Checkins.txt");
        String poiPath = StringUtil.join(ConstantValues.FILE_SPLIT, inputSuperFilePath, "dataset_TIST2015_POIs.txt");
        String cityPath = StringUtil.join(ConstantValues.FILE_SPLIT, inputSuperFilePath, "dataset_TIST2015_Cities.txt");
        Map<String, String> countryCodeNameMap = new HashMap<>();
        BasicRead basicRead = new BasicRead();
        CityBean tempCityBean;
        POIBean tempPOIBean;
        CheckInBean tempCheckInBean;
        String tempCountryName, tempVenueID, tempCountryCode;

        basicRead.startReading(cityPath);
        List<String> cityData = basicRead.readAllWithoutLineNumberRecordInFile();
        for (String cityDatum : cityData) {
            tempCityBean = CityBean.toBean(CheckInStringTool.recordSplit(cityDatum));
            countryCodeNameMap.put(tempCityBean.getCountryCode(), tempCityBean.getCountryName());
        }
        basicRead.endReading();

        Map<String, String> venueIDCountryMap = new HashMap<>();
        basicRead.startReading(poiPath);
        List<String> poiData = basicRead.readAllWithoutLineNumberRecordInFile();
        for (String poiDatum : poiData) {
            tempPOIBean = POIBean.toBean(CheckInStringTool.recordSplit(poiDatum));
            tempVenueID = tempPOIBean.getVenueID();
            tempCountryCode = tempPOIBean.getCountryCode();
            venueIDCountryMap.put(tempVenueID, countryCodeNameMap.get(tempCountryCode));
        }
        basicRead.endReading();

        List<String> result = new ArrayList<>();


        File splitFileDirectory = new File(splitDirectory);
        String[] splitFileNameArray = splitFileDirectory.list();
        String tempAbsoluteInputPath, tempAbsoluteOutputPath;
        List<String> tempCheckInRecordList;
        String tempString;
        BasicWrite basicWrite = new BasicWrite();
        for (String fileName : splitFileNameArray) {
            tempAbsoluteInputPath = StringUtil.join(ConstantValues.FILE_SPLIT, splitFileDirectory, fileName);
            tempAbsoluteOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputSuperFilePath, fileName);
            basicRead.startReading(tempAbsoluteInputPath);
            tempCheckInRecordList = basicRead.readAllWithoutLineNumberRecordInFile();
            basicRead.endReading();
            result.clear();
            for (String record : tempCheckInRecordList) {
                try {
                    tempCheckInBean = CheckInBean.toBean(CheckInStringTool.recordSplit(record));
                } catch (Exception exception) {
                    System.out.println("It is error for record: '" + record + "' to transform to checkInBean. We ignore this record!");
                    continue;
                }
                tempCountryName = venueIDCountryMap.get(tempCheckInBean.getVenueID());
                tempString = StringUtil.join(",", tempCheckInBean.getUserID(), tempCountryName, tempCheckInBean.getUtcTime());
                result.add(tempString);
            }
            basicWrite.startWriting(tempAbsoluteOutputPath);
            basicWrite.writeStringListWithoutSize(result);
            basicWrite.endWriting();
        }




    }

    public static Map<String, Integer> getUserRecordSize(String dataDirectory) {
        File directory = new File(dataDirectory);
        File[] dataFileArray = directory.listFiles();
        BasicRead basicRead = new BasicRead();
        List<String> dataList;
        String[] tempSplitRecord;
        String tempUserID;
        Integer tempSize;
        Map<String, Integer> resultMap = new HashMap<>();
        for (File file : dataFileArray) {
            basicRead.startReading(file.getAbsolutePath());
            System.out.println("Read file " + file.getName() + " ...");
            dataList = basicRead.readAllWithoutLineNumberRecordInFile();
            for (String record : dataList) {
                tempSplitRecord = record.split(",");
                tempSize = resultMap.getOrDefault(tempSplitRecord[0], 0);
                ++tempSize;
                resultMap.put(tempSplitRecord[0], tempSize);
            }
            basicRead.endReading();
        }
        return resultMap;
    }

    public static void main(String[] args) {
        String dataDirectory = "E:\\1.学习\\4.数据集\\3.dataset_for_dynamic_w_event\\0_dataset\\CheckIn_dataset_TIST2015\\join";
        Map<String, Integer> result = getUserRecordSize(dataDirectory);
//        MyPrint.showMap(result);
        System.out.println(result.size());
    }
}
