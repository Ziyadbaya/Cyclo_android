package com.dev.cyclo;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * This class write log message on Android LogCat but also in application documents
 */
public class Logger {

    // as every logger
    public enum Severity{Verbose, Debug, Info, Warn, Error}

    private static File file; //the in with log will be store
    private FileWriter fw; // every instance of logger manage a FileWriter
    private final String className;

    private final static int maxDailLog = 7;

    private static final Comparator<File> comparatorFile = (o1, o2) -> (int) (o1.lastModified() - o2.lastModified());

    public Logger(Class<?> localClass) {
        if(file == null){
            newLogger();
        }
        className = localClass.getName();
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);
        }
        catch (IOException e){
            Log.e("[Logger]", e.getMessage());
        }
    }

    private String getDate(){
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ", Locale.FRANCE);
        Date date = new Date();
        return format.format(date);
    }

    private String getClassName(String className){
        String[] split = className.split("\\.");
        return split[split.length - 2] + "." + split[split.length - 1] + " ";
    }

    private String getTag(String tag){
        return " [" + tag + "] ";
    }

    private String getFormatedString(Severity severity, String string){
        String out = getDate();
        out += getClassName(className);
        out += severity.toString().toUpperCase(Locale.ROOT);
        out += " ";
        out += string;
        out += '\n';
        return out;
    }

    private String getFormatedString(Severity severity, String string, String tag){
        String out = getDate();
        out += getClassName(className);
        out += severity.toString().toUpperCase(Locale.ROOT);
        out += getTag(tag);
        out += string;
        out += '\n';
        return out;
    }


    /**
     * @param severity Verbose, Debug, Info, Warn, Error
     * @param string the message to log
     * @param tag a tag to group messages with regex
     */
    public void log(Severity severity, String string, String tag){
        String out = getFormatedString(severity, string, tag);
        writeAndLog(severity, string, out, tag);
    }


    /**
     * @param severity Verbose, Debug, Info, Warn, Error
     * @param string the message to log
     */
    public void log(Severity severity, String string){
        String out = getFormatedString(severity, string);
        writeAndLog(severity, string, out, className);
    }

    private void writeAndLog(Severity severity, String string, String out, String tag) {

        try{
            fw.append(out);
            fw.flush();
        }
        catch (NullPointerException | IOException writeError){ //if the fileWriter doesn't already exist
            try {
                fw = new FileWriter(file.getAbsoluteFile(), true);
                fw.append(out);
                fw.flush();
            }
            catch (IOException fwError){
                Log.e("[Logger]", fwError.getMessage());
            }
            Log.e("[Logger]", writeError.getMessage());
        }

        switch (severity){
            case Info:Log.i(tag, string);break;
            case Warn:Log.w(tag, string);break;
            case Debug:Log.d(tag, string);break;
            case Error:Log.e(tag, string);break;
            case Verbose:Log.v(tag, string);break;
            default:Log.wtf(tag, string);break;
        }
    }


    /**
     * @param directory to delete
     * @return true if the directory has been deleted, false otherwise
     */
    private static boolean deleteDirectory(File directory){
        File[] files = directory.listFiles();
        if(files != null){
            for (File file : files) {
                if(file.isDirectory()){
                    deleteDirectory(file);
                }
                else{
                    if (file.delete()) {
                        Log.v("logger", "Delete :" + file);
                    }
                    else{
                        Log.wtf("logger", "impossible to delete :" + file);
                    }
                }
            }
        }
        if(directory.delete()){
            Log.v("[Logger]", "Delete : " + directory);
            return true;
        }
        else {
            Log.wtf("[Logger]", "Cannot delete : " + directory);
            return false;
        }
    }


    /**
     * this method setting up a new log file
     * it also ensure that there are no more than maxDailLog dailyLog in the logs file
     */
    public static void newLogger(){
        String path = Main.path;

        //creating logs file on first launch
        File log = new File(path, "logs");
        path += "/logs";
        if(!log.exists()) { //if log file doesn't exist
            System.out.println("logs file doesn't exist");
            if (!log.mkdirs()) { // creating a new log file
                System.out.println("Cannot create log file");
            }
        }
        else { //if the logs file already exists
            File[] files = log.listFiles();
            if(files == null){
                Log.wtf("[Logger]", "logs file empty");
            }
            else { //if there are already dailyLogs
                Supplier<Stream<File>> fileStream = () -> Arrays.stream(files).sorted(comparatorFile); //this contains dailyLog files
                if (fileStream.get().count() > maxDailLog){ //if there are to much dailyLogs
                    if(fileStream.get().findFirst().isPresent()){
                        if(deleteDirectory(fileStream.get().findFirst().get())){
                            Log.v("[logger]", "deleted");
                        }
                        else {
                            Log.wtf("[logger]", "cannot delete :" + fileStream.get().findFirst().get());
                        }
                    }
                    else{
                        Log.wtf("[Logger]", "There are more than 7 files in logs but cannot suppress the oldest one");
                    }
                }
            }

        }

        //About the log file of the current day
        DateFormat dayFormat = new SimpleDateFormat("MM-dd", Locale.FRANCE);
        Date date = new Date();
        File dayFile = new File(path, dayFormat.format(date));
        if(!dayFile.exists()){//if the dailyLog file doesn't exists
            System.out.println("log file of the day doesn't exist");
            //creating the LogFile corresponding to the present day
            if (!dayFile.mkdirs()) { // creating a new log file
                System.out.println("Cannot create the log file of the day");
            }
        }
        path += "/" + dayFormat.format(date);

        DateFormat minFormat = new SimpleDateFormat("HH-mm", Locale.FRANCE);
        Date min = new Date();

        file = new File(path, minFormat.format(min) + ".txt");
        Log.w("[new Log File]", file.getAbsolutePath());
        try {
            if(!file.createNewFile()){
                System.out.println("Try to create a log file that already exists");
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
