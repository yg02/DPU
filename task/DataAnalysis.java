package task;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class DataAnalysis {

    public static int totalInstanceNum = 100;


    public double[] generateBestObjective(String resultRoot, String[] algorithmNames, int roundNum, int objectiveIndex, int timeIndex) {

        double[] bestObjective = new double[totalInstanceNum];

        for(int i = 0; i < totalInstanceNum; i++)
            bestObjective[i] = Double.MAX_VALUE;

        for(int j = 0; j < algorithmNames.length; j++) {
            double time = 0.0;
            double timeS = 0.0;
            try {
                String algorithmResultFile = resultRoot + algorithmNames[j] +".txt";
                BufferedReader reader=new BufferedReader(new FileReader(algorithmResultFile));
                for(int i = 0; i < totalInstanceNum; i++){
                    for(int k = 0; k < roundNum; k++) {
                        String info = reader.readLine();
                        String[] details = info.split("\t");
                        double objective = Double.valueOf(details[objectiveIndex]);
                        time += Double.valueOf(details[timeIndex]);
                        timeS += Double.valueOf(details[timeIndex]);
                        if(objective < bestObjective[i])
                            bestObjective[i] = objective;
                    }
                    if(i % 10 == 9){
                        time/=50;
                        System.out.println(i + ": " + time);
                        time = 0.0;
                    }
                }
                reader.close();
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }

            timeS /= 500.0;
            System.out.println(algorithmNames[j] + ": " + timeS);

        }
        return bestObjective;
    }


    public void OutpurResults(String[] algorithmNames, String[] printNames, String file, int roundNum) {

        String resultRoot = "results\\";//!!!!!!读取文件的位置

        double[] bestObjective = this.generateBestObjective(resultRoot, algorithmNames, roundNum, 3, 4);//4指的是目标函数值所在的那一列

//        for (int i = 0; i < totalInstanceNum; i++) {
//            System.out.println(bestObjective[i]);
//        }

        DecimalFormat format = new DecimalFormat("0.00000");

        Workbook wb = new XSSFWorkbook();
        Map<String, CellStyle> styles = createStyles(wb);
        Sheet sheet = wb.createSheet("results");
        //turn off gridlines
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);

        String[] titles={"Algorithms", "TotalTaskNumber", "InstanceIndex", "RoundIndex",
                "Objective", "RE", "Time"};
        //the header row: centered text in 48pt font
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(12.75f);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(styles.get("header"));
            sheet.setColumnWidth(i, 256*(titles[i].length()+3));
        }
        //freeze the first row
        sheet.createFreezePane(4, 1);
        Row row;
        Cell cell;

//        String result_root = Helper.getConfigSettings("result_root", Algorithm.cfgFile);
        int rownum = 1;
        for(int j = 0; j < algorithmNames.length; j++) {
            try {
                String algorithmResultFile = resultRoot + algorithmNames[j] + ".txt";
                BufferedReader reader=new BufferedReader(new FileReader(algorithmResultFile));
//    			reader.readLine();

                for(int i = 0; i < totalInstanceNum; i++)
                    for(int k = 1; k <= roundNum; k++)
                    {
                        String info = reader.readLine();
                        String[] details = info.split("\t");

                        row = sheet.createRow(rownum++);
                        int colnum=0;
                        //AlgorithmName
                        cell = row.createCell(colnum++);
//    				cell.setCellValue(details[0]);
                        cell.setCellValue(printNames[j]);

                        //TotalTaskNum
                        cell = row.createCell(colnum++);
                        cell.setCellValue(details[0]);

                        //InstanceIndex
                        cell = row.createCell(colnum++);
                        cell.setCellValue(details[1]);

                        //RoundIndex
                        cell = row.createCell(colnum++);
                        cell.setCellValue(details[2]);


                        //Objective
                        cell = row.createCell(colnum++);
                        cell.setCellStyle(styles.get("input_double"));
                        cell.setCellValue(details[3]);


                        //RE
                        cell = row.createCell(colnum++);
                        cell.setCellStyle(styles.get("input_double"));
                        double objective = Double.valueOf(details[3]);
                        double RE = ((objective - bestObjective[i]) / bestObjective[i]) * 100;
                        cell.setCellValue(format.format(RE));

                        //time
                        cell = row.createCell(colnum++);
                        cell.setCellStyle(styles.get("input_double"));
                        cell.setCellValue(details[4]);

                    }
                reader.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        try
        {
            if(wb instanceof XSSFWorkbook)
                file += ".xlsx";
            else
                file += ".xls";
            FileOutputStream out = new FileOutputStream(resultRoot + file);
            wb.write(out);
            out.close();
            System.out.println(file + " completed");
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }


    }

    private  Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)14);
        titleFont.setFontName("Trebuchet MS");
        style = wb.createCellStyle();
        style.setFont(titleFont);
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        styles.put("title", style);

        Font headerFont = wb.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(headerFont);
        styles.put("header", style);

        Font itemFont = wb.createFont();
        itemFont.setFontHeightInPoints((short)9);
        itemFont.setFontName("Trebuchet MS");
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setFont(itemFont);
        styles.put("item_left", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        styles.put("item_right", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(CellStyle.BORDER_DOTTED);
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderLeft(CellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderTop(CellStyle.BORDER_DOTTED);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setDataFormat(wb.createDataFormat().getFormat("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)"));
        styles.put("input_$", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(CellStyle.BORDER_DOTTED);
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderLeft(CellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderTop(CellStyle.BORDER_DOTTED);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.000%"));
        styles.put("input_%", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(CellStyle.BORDER_DOTTED);
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderLeft(CellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderTop(CellStyle.BORDER_DOTTED);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("input_double", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(itemFont);
        style.setDataFormat(wb.createDataFormat().getFormat("m/d/yy"));
        styles.put("input_d", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(CellStyle.BORDER_DOTTED);
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderLeft(CellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderTop(CellStyle.BORDER_DOTTED);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setDataFormat(wb.createDataFormat().getFormat("$##,##0.00"));
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styles.put("formula_$", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(itemFont);
        style.setBorderRight(CellStyle.BORDER_DOTTED);
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderLeft(CellStyle.BORDER_DOTTED);
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBorderTop(CellStyle.BORDER_DOTTED);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setDataFormat(wb.createDataFormat().getFormat("0"));
        style.setBorderBottom(CellStyle.BORDER_DOTTED);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styles.put("formula_i", style);

        return styles;
    }

    private  CellStyle createBorderedStyle(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }

    public static void main(String[] args) {

//"Results_EDA", "Results_EDA_pure", "Results_EDA_tradition"
//"Results_70", "Results_72", "Results_74", "Results_76", "Results_78","Results_80"
//"EDA", "Results_CS", "Results_FA", "Results_GA", "Results_PSO"
//"EDA_X_a_82", "EDA_X_a_84","EDA_X_a_85", "EDA_X_a_86","EDA_X_a_87", "EDA_X_a_88", "EDA_X_a_90",
//"EDA_X_a_92", "EDA_X_a_94", "EDA_X_a_96", "EDA_X_a_98"
// "EDA_X_a_10", "EDA_X_a_20" , "EDA_X_a_30", "EDA_X_a_40","EDA_X_a_50", "EDA_X_a_60", "EDA_X_a_70" , "EDA_X_a_80", "EDA_X_a_90"
//"0.1", "0.2" , "0.3", "0.4", "0.5", "0.6", "0.7" , "0.8", "0.9"
//"EDA_X_a_70", "EDA_X_a_72","EDA_X_a_74", "EDA_X_a_76","EDA_X_a_78", "EDA_X_a_80",
//"EDA_X_a_82", "EDA_X_a_84","EDA_X_a_86", "EDA_X_a_88", "EDA_X_a_90"
// "0.70", "0.72","0.74", "0.76","0.78", "0.80",
//                "0.82", "0.84","0.86", "0.88", "0.90"
//"EDA_X_LJF", "EDA_X_SJF"


        String[] filetNames = {"EDA_X_Num110", "EDA_Num110", "FA_Num110", "GA_Num110", "PSO_Num110"
        };
        int l = filetNames.length;
        String[] algorithmNames = new String[l];
        for (int i = 0; i < l; i++)
            algorithmNames[i] = "Results_" + filetNames[i];

        String[] printNames = {"NEDA", "EDA", "FA", "GA", "PSO"};

        DataAnalysis analysis = new DataAnalysis();

        analysis.OutpurResults(algorithmNames, printNames,"Results_Compare_NeTime_Num110", 5);
    }

}
