package com.lianmed.filetool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;



public class FileData {


    //閸掓稑缂撻弬鍥︽婢剁懓寮烽弬鍥︽
    public static File createFile(String Folder, String fileName) throws IOException {
        File file = new File(Folder);
        if (!file.exists()) {
            try {
                //閹稿鍙庨幐鍥х暰閻ㄥ嫯鐭惧鍕灡瀵ょ儤鏋冩禒璺恒仚
                file.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File dir = new File(Folder + fileName);
        if (!dir.exists()) {
            try {
                //閸︺劍瀵氱�规氨娈戦弬鍥︽婢堕�涜厬閸掓稑缂撻弬鍥︽
                dir.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dir;
    }


    public static File writeToFile(String Folder, String fileName, byte[] data, int offset, int length) {
        File file = null;
        OutputStream output = null;
        try {
            file = createFile(Folder, fileName);
            output = new FileOutputStream(file);
            output.write(data, offset, length);
            output.flush();
            //Log.i(TAG, "write success, len = " + file.length());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void writeToFileEnd(String Folder, String fileName,  ArrayList<byte[]> pcmList) {
        RandomAccessFile rf = null;
        try {

            File file = createFile(Folder, fileName);
            rf = new RandomAccessFile(file, "rw");
            rf.seek(file.length());


            for (int i = 0; i < pcmList.size(); i++) {
                byte[] temp = pcmList.get(i);
                int len = temp.length;
                rf.write(temp, 0, len);
            }

//            for (byte[] d : pcmList) {
//                rf.write(d, 0, d.length);
//            }

//            rf.write(data, 0, length);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rf != null) {
                    rf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void writeToFile02(String Folder, String fileName, byte[] data, long offset, int length) {
        RandomAccessFile rf = null;
        try {
            File file = createFile(Folder, fileName);
            rf = new RandomAccessFile(file, "rw");
            rf.seek(offset);
            rf.write(data, 0, length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //LogDevice.d("Audio offline data error = " + e.getMessage()  );
            e.printStackTrace();
        } finally {
            try {
                if (rf != null) {
                    rf.close();
                }
            } catch (IOException e) {
                //LogDevice.d("Audio offline data error = " + e.getMessage()  );
                e.printStackTrace();
            }
        }
    }


    public static synchronized void writePcmOfflineDataToFile(String Folder, String fileName, byte[] data, long offset, int length) {
        RandomAccessFile rf = null;
        try {
            File file = createFile(Folder, fileName);
            rf = new RandomAccessFile(file, "rw");
            rf.seek(offset);
            rf.write(data, 0, length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //LogDevice.d("Audio offline data error = " + e.getMessage()  );
            e.printStackTrace();
        } finally {
            try {
                if (rf != null) {
                    rf.close();
                }
            } catch (IOException e) {
                //LogDevice.d("Audio offline data error = " + e.getMessage()  );
                e.printStackTrace();
            }
        }
    }


    public static byte[] readFromFile(File file, int offset, int length) {
        byte[] buffer = new byte[length];
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            input.read(buffer, offset, length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    public static int[] readFHRfromFile(String filepath) {
        File file = null;
        Long len;
        int rount;
        int data[];
        byte[] file_data;
        try {
            file = new File(filepath);
            len = file.length();
//            Log.i(TAG, "len = " + len + ", file is exist = " + file.exists());
            //if (LocalBuildConfig.getInstance().SingleHomeBluetooth) {

                if (len.intValue() < 16) {
                    return null;
                }
                file_data = readFromFile(file, 0, len.intValue());
                rount = (int) (len / 16);
                data = new int[rount];
                for (int i = 0; i < rount; i++) {
                    data[i] = file_data[i * 16] & 0xff;
                }
                return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] readFMPfromfile(String localpath, String documentid) {
        File file = null;
        Long len;
        int rount;
        int data[];
        byte[] file_data;
        try {
            file = new File(localpath, documentid + ".fetal");
            len = file.length();
            //Log.i(TAG, "len = " + len);
            if (len.intValue() < 16) {
                return null;
            }
            file_data = readFromFile(file, 0, len.intValue());
            rount = (int) (len / 16);
            data = new int[rount];
            for (int i = 0; i < rount; i++) {
                data[i] = file_data[i * 16 + 1] & 0xff + 51;
            }
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] readGainfromfile(String localpath, String documentid) {
        File file = null;
        Long len;
        int rount;
        int data[];
        byte[] file_data;
        try {
            file = new File(localpath, documentid + ".fetal");
            len = file.length();
            //Log.i(TAG, "len = " + len);
            if (len.intValue() < 16) {
                return null;
            }
            file_data = readFromFile(file, 0, len.intValue());
            rount = (int) (len / 16);
            data = new int[rount];
            for (int i = 0; i < rount; i++) {
                data[i] = Math.abs((int) ((file_data[i * 16 + 2] & 0xff) | ((file_data[i * 16 + 3] & 0xff) << 8)));
                data[i] = data[i] / 25 + 51;
            }
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] readTOCOfromFile(String filepath) {
        File file = null;
        Long len;
        int rount;
        int data[];
        byte[] file_data;
        try {
            file = new File(filepath);
            len = file.length();
            //if (LocalBuildConfig.getInstance().SingleHomeBluetooth) {
            if (len.intValue() < 16) {
                 return null;
              }
            file_data = readFromFile(file, 0, len.intValue());
            rount = (int) (len / 16);
            data = new int[rount];
            for (int i = 0; i < rount; i++) {
                 data[i] = (file_data[i * 16 + 5] & 0xff) > 100 ? (-1) : (file_data[i *16 + 5] & 0xff);
             }
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] readFMfromFile(String localpath, String documentid) {
        File file = null;
        Long len;
        int rount;
        int data[];
        byte[] file_data;
        try {
            file = new File(localpath, documentid + ".fetal");
            len = file.length();
            //if (LocalBuildConfig.getInstance().SingleHomeBluetooth) {
             if (len.intValue() < 16) {
                  return null;
               }
             file_data = readFromFile(file, 0, len.intValue());
             rount = (int) (len / 16);
             data = new int[rount];
             for (int i = 0; i < rount; i++) {
                if ((file_data[i * 16 + 4] & 0xff) == 128) {
                      data[i] = 1;
                  } else {
                	  data[i] = 0;
                  }
              }
                return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static int[] readFMToArrayfromFile(String localpath, String documentid) {
        File file = null;
        Long len;
        int rount;
        int data[];
        byte[] file_data;

        List<Integer> lists = new ArrayList<>();
        try {
            file = new File(localpath, documentid + ".fetal");
            len = file.length();

                if (len.intValue() < 16) {
                    return null;
                }
                file_data = readFromFile(file, 0, len.intValue());
                rount = (int) (len / 16);

                for (int i = 0; i < rount; i++) {
                    if ((file_data[i * 16 + 4] & 0xff) == 128) {
                        lists.add(i);
                    }
                }

                data = new int[lists.size()];
                for(int i = 0;i<lists.size();i++){
                    data[i] = lists.get(i);
                }

                return data;
          
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 鐠囪褰囬懗搴″姽濞茶濮╅崶鎯у斧婵鏆熼幑锟�
     * @param localpath
     * @param documentid
     * @return
     */
    public static int[] readFmpBasedataToArrayfromFile(String filepath) {
        File file = null;
        Long len;
        int data[];
        byte[] file_data;

        try {
            file = new File(filepath);
            len = file.length();
            if (len.intValue() == 0) {
                return null;
            }
            data = new int[len.intValue()];
            file_data = readFromFile(file, 0, len.intValue());
            for (int i = 0; i < len; i++) {
                data[i] = file_data[i] & 0xff;
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static byte[] arrayToByte(List<byte[]> lists) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (byte[] b:lists) {
                bos.write(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();

    }

    /*
    public static int[] readFMToArrayfromFMcalculate(ArrayList<Integer> FHR, ArrayList<Integer> TOCO, ArrayList<Integer> FmpBase) {
        List<Integer> fm = FMcalculate.getInstance().calculate(FHR, TOCO, FmpBase);
        if (fm != null) {
            int[] fm_data = FMcalculate.getInstance().fm_ListToArray(fm);
            return fm_data;
        } else {
            return null;
        }
    }
	*/


    public static ArrayList<Integer> intArray_to_listArray(int[] data) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            list.add(data[i]);
        }
        return list;
    }


}
