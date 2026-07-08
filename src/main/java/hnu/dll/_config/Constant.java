package hnu.dll._config;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.configure.XMLConfigure;
import cn.edu.dll.constant_values.ConstantValues;

import java.io.File;
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

    public static String CheckInFilePath;
    public static String TrajectoriesFilePath;
    public static String TLNSFilePath;
    public static String SinFilePath;
    public static String LogFilePath;

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

        CheckInFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, checkInFileName);
        TrajectoriesFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, trajectoriesFileName);
        TLNSFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, tlnsFileName);
        SinFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, sinFileName);
        LogFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDatasetPath, logFileName);

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
     * 2. DirectPLPMechanism
     *  (1) DirectPLPDistribution (Direct-PLPD)
     *  (2) DirectPLPAbsorption (Direct-PLPA)
     *
     *
     * 3. BasicPLPMechanism (只加了OPS)
     *  (1) BasicPLPDistribution (Basic-PLPD)
     *  (2) BasicPLPAbsorption (Basic-PLPA)
     *
     *
     * 4-1. AblateBAPUEPLPMechanism (只加了GAOPS)
     *  (1) AblateBAPUEPLPDistribution (Ablate-BAPUE-PLPD)
     *  (2) AblateBAPUEPLPAbsorption (Ablate-BAPUE-PLPA)
     *
     * 4-2. AblateGAOPSPLPMechanism (只加了BAPUE)
     *  (1) AblateGAOPSPLPDistribution (Ablate-GAOPS-PLPD)
     *  (3) AblateGAOPSPLPAbsorption (Ablate-GAOPS-PLPA)
     *
     *
     * 5. AdvancedPLPMechanism
     *  (1) PLDPPopulationDistributionPlus (PLPD+)
     *  (2) PLDPPopulationAbsorptionPlus (PLPA+)
     *
     */
    public static final String NonPrivacySchemeName = "NonPrivacy-Scheme";
    public static final String LPDSchemeName = "LBD-Scheme"; // 应该是LPD-Scheme
    public static final String LPASchemeName = "LBA-Scheme"; // 应该是LPA-Scheme
    public static final String DirectPLPDSchemeName = "PLPD-Direct-Scheme";
    public static final String DirectPLPASchemeName = "PLPA-Direct-Scheme";
//    public static final String AblateOPSPLPDSchemeName = "PLPD-Ablate-OPS-Scheme";
//    public static final String AblateOPSPLPASchemeName = "PLPA-Ablate-OPS-Scheme";
    public static final String BasicPLPDSchemeName = "PLPD-basic-Scheme";
    public static final String BasicPLPASchemeName = "PLPA-basic-Scheme";

    public static final String UnionPLPDSchemeName = "PLPD-Union-Scheme";
    public static final String UnionPLPASchemeName = "PLPA-Union-Scheme";

    public static final String AblateBAPUEPLPDSchemeName = "PLPD-Ablate-BAPUE-Scheme";
    public static final String AblateBAPUEPLPASchemeName = "PLPA-Ablate-BAPUE-Scheme";
    public static final String AblateGAOPSPLPDSchemeName = "PLPD-Ablate-GAOPS-Scheme";
    public static final String AblateGAOPSPLPASchemeName = "PLPA-Ablate-GAOPS-Scheme";

    public static final String AdvancedPLPDSchemeName = "PLPD-Plus-Scheme";
    public static final String AdvancedPLPASchemeName = "PLPA-Plus-Scheme";







    /**
     *
     *  ------------------------------------------------------------------------------
     */

//    public static final List<Double> CandidatePrivacyBudgetList = Arrays.asList(
//            0.5, 1.0, 1.5, 2.0, 2.5
//    );
//
//    public static final List<Integer> CandidateWindowSizeList = Arrays.asList(
//            10, 20, 30, 40, 50
//    );

    /**
     * population distribution 类 methods 独有的参数
     */
    public static final Integer PopulationLowerBound = 10;

    /**
     * file directories
     *
     */
    public static final String GroupParameterDirectoryName = "group_generated_parameters";
    public static final String PersonalizedParameterFileName = "userParameterFile.txt";
    public static final String UserToIndexFileName = "user_to_Index.txt";
    public static final String LocationToIndexFileName = "location_to_Index.txt";

    public static final String GroupOutputDirName = "group_output";


    /**
     * for different datasets
     */
    public static final String TrajectoryLocationFileName = "cell.txt";
    public static final String CheckInLocationFileName = "country.txt";

    public static final String TLNSLocationFileName = "status.txt";
    public static final String SinLocationFileName = "status.txt";
    public static final String LogLocationFileName = "status.txt";

    public static void main(String[] args) {
//        System.out.println(configPath);
        String datasetBasicPath = ConfigureUtils.getDatasetBasicPath();
        System.out.println(datasetBasicPath);
    }
















}
