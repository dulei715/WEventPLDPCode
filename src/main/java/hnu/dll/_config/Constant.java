package hnu.dll._config;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.configure.XMLConfigure;
import cn.edu.dll.constant_values.ConstantValues;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Constant {
    public static String projectPath = System.getProperty("user.dir");
    public static String configPath;
    public static XMLConfigure xmlConfigure;
    public static String basicDatasetPath;

    public static String checkInFileName;
    public static String trajectoriesFileName;
    public static String tlnsFileName;
    public static String sinFileName;
    public static String logFileName;

    public static String checkInFilePath;
    public static String trajectoriesFilePath;
    public static String tlnsFilePath;
    public static String sinFilePath;
    public static String logFilePath;

    public static Integer MAX_BACKWARD_WINDOW_SIZE;

    public static Random[] randomArray;

    public static void initializeRandomArrayByRoundSize(Integer roundSize) {
        randomArray = new Random[roundSize];
        for (int i = 0; i < roundSize; i++) {
            randomArray[i] = new Random(i);
        }
    }


    static {

        initializeRandomArrayByRoundSize(10);

        configPath = StringUtil.join(ConstantValues.FILE_SPLIT, projectPath, "config", "parameter_config.xml");
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configPath = StringUtil.join(ConstantValues.FILE_SPLIT, projectPath, "deployment", "config", "parameter_config.xml");
            String pathA = configFile.getAbsolutePath();
            configFile = new File(configPath);
            if (!configFile.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Not find the parameter_config.xml").append(ConstantValues.LINE_SPLIT);
                stringBuilder.append("Neither path '").append(pathA).append("' nor path '").append(configFile.getAbsolutePath()).append("' ");
                stringBuilder.append("exists!");
                throw new RuntimeException(stringBuilder.toString());
            }
        }
        xmlConfigure = new XMLConfigure(configPath);
        basicDatasetPath = ConfigureUtils.getDatasetBasicPath();

        checkInFileName = ConfigureUtils.getDatasetFileName("checkIn");
        trajectoriesFileName = ConfigureUtils.getDatasetFileName("trajectories");
        tlnsFileName = ConfigureUtils.getDatasetFileName("tlns");
        sinFileName = ConfigureUtils.getDatasetFileName("sin");
        logFileName = ConfigureUtils.getDatasetFileName("log");

        checkInFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, checkInFileName);
        trajectoriesFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, trajectoriesFileName);
        tlnsFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, tlnsFileName);
        sinFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, sinFileName);
        logFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, logFileName);

        MAX_BACKWARD_WINDOW_SIZE = ConfigureUtils.getMaxWindowSize() + 1;



    }




    // constant for experiment result title
    public static final String MechanismName = "Name";
    public static final String BatchName = "Batch";
    public static final String BatchRealSize = "BatchSize";
    public static final String TimeCost = "Time Cost";
    public static final String PrivacyBudget = "Privacy Budget";
    public static final String WindowSize = "Window Size";
    public static final String MRE = "MRE"; // MeanRelativeError, 是所有相对误差的均值
    public static final String BRE = "BRE"; // BatchRelativeError, 是一个batch中的所有相对误差的总和
    public static final String MJSD = "MJSD";// MeanJSDivergence (AJSD), 是所有时刻JS散度的均值
    public static final String BJSD = "BJSD"; // BatchJSDivergence, 是一个batch中的所有JS散度的总和









    public static final Double PRIVACY_LOWER_BOUND = 0.1;
    public static final Double PRIVACY_UPPER_BOUND = 10D;
    public static final Integer WINDOW_LOWER_BOUND = 20;
    public static final Integer WINDOW_UPPER_BOUND = 100;



    public static Double MIN_UNION_PRIVACY_BUDGET = 0.1D;



    public static Double Sample_Ratio_For_Picture = 0.01;


    // for scheme name string
    /**
     * 用于对比的类的类名：
     * 0. NonPrivacyBudgetMechanism (NonPrivacyScheme)
     *
     * 1. LPMechanism
     *  (1) LDPPopulationDistribution (LPD)
     *  (2) LDPopulationAbsorption (LPA)
     *
     * 2. BaselinePLPMechanism
     *  (1) BaselinePLPDistribution (PLPD)
     *  (2) BaselinePLPAbsorption (PLPA)
     *
     * 3-1. AblateOPSMechanism
     *  (1) AblateOPSDistributionPlus (A-OPS-PLPD+)
     *  (2) AblateOPSAbsorptionPlus (A-OPS-PLPA+)
     *
     * 3-2. AblateRePerturbMechanism
     *  (1) AblateRePerturbDistributionPlus (A-RP-PLPD+)
     *  (2) AblateRePerturbAbsorptionPlus (A-RP-PLPA+)
     *
     * 4. EnhancedPLPMechanism
     *  (1) PLDPPopulationDistributionPlus (PLPD+)
     *  (2) PLDPPopulationAbsorptionPlus (PLPA+)
     *
     */
    public static final String NonPrivacySchemeName = "NonPrivacy-Scheme";
    public static final String LPDSchemeName = "LBD-Scheme";
    public static final String LPASchemeName = "LBA-Scheme";
    public static final String BasePLPDSchemeName = "PLPD-Basic-Scheme";
    public static final String BasePLPASchemeName = "PLPA-Basic-Scheme";
    public static final String AblateOPSPLPDSchemeName = "PLPD-Ablate-OPS-Scheme";
    public static final String AblateOPSPLPASchemeName = "PLPA-Ablate-OPS-Scheme";
    public static final String AblateRPPLPDSchemeName = "PLPD-Ablate-RP-Scheme";
    public static final String AblateRPPLPASchemeName = "PLPA-Ablate-RP-Scheme";

    public static final String EnhancedPLPDSchemeName = "PLPD-Plus-Scheme";
    public static final String EnhancedPLPASchemeName = "PLPA-Plus-Scheme";



    // for trajectory
    public static final Integer SampleTrajectoryGridSideLength = 300;
    //    public static final Integer TrajectorySamplingSize = 10000;
    public static final Integer TrajectorySamplingSize = 1000;
    //    public static final Integer TrajectorySamplingSize = 10;
    // 这里的轨迹长度是指轨迹中关注点的数量
    public static final Integer TrajectorySamplingLengthLowerBound = 2;
    public static final Integer TrajectorySamplingLengthUpperBound = 200;


    public static final Integer generatingTrajectorySizeFromSynthetic = TrajectorySamplingSize;

    public static final List<Integer> CandidateSectorSizeListForNYC = Arrays.asList(5, 10, 15, 20);


    /**
     *
     *  ------------------------------------------------------------------------------
     */

    public static final List<Double> CandidatePrivacyBudgetList = Arrays.asList(
            0.5, 1.0, 1.5, 2.0, 2.5
    );

    public static final List<Integer> CandidateWindowSizeList = Arrays.asList(
            10, 20, 30, 40, 50
    );


    public static void main(String[] args) {
//        System.out.println(configPath);
        String datasetBasicPath = ConfigureUtils.getDatasetBasicPath();
        System.out.println(datasetBasicPath);
    }
















}
