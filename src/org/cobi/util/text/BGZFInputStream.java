/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class BGZFInputStream {

    static final int BUF_SIZE = 1 * 1024 * 1024;

    File input;
    int threadNum = 1;
    long pos[];
    RandomAccessFile raf;
    byte[] buf = new byte[BUF_SIZE];
    boolean notBGZF = false;
    int intFileFormat = -1;
    public BZSpider[] spider;

    public int getThreadNum() {
        return threadNum;
    }

    public BGZFInputStream(String strFile, int n_thread) {
        this.input = new File(strFile);
        this.threadNum = n_thread;
        pos = new long[n_thread + 1];

        if (strFile.endsWith(".gz")) {
            intFileFormat = 0;
        } else {
            intFileFormat = 1;
        }

    }

    public void adjustPos() throws IOException {
        for (int i = 0; i < threadNum; i++) {
            pos[i] = (input.length() / threadNum * i);
        }
        pos[pos.length - 1] = input.length();
        if (intFileFormat == 1) {
            return;
        }

        raf = new RandomAccessFile(input.getCanonicalFile(), "r");
        for (int i = 1; i < threadNum; i++) {
            raf.seek(pos[i]);
            boolean boolNoFound = true;
            int n_buf = -1;//if n_buf>0,we can consider this file is not bgzf format, but the gzip format. 
            do {
                raf.read(buf);
                n_buf++;
                if (n_buf > 0) {
                    notBGZF = true;
                    threadNum = 1;
                    pos[1] = pos[pos.length - 1];
                   // System.out.println("The file is gzip-format, not bgzip-format!");
                    break;
                }
                for (int id = 0; id < buf.length - 1; id++) {
                    if (buf[id] == 31 && buf[id + 1] == -117 && buf[id + 2] == 8 && buf[id + 3] == 4) { //This should be used unsigned number or others. 
                        pos[i] += (id + n_buf * buf.length);
                        boolNoFound = false;
                        break;
                    }
                }
            } while (boolNoFound);
            if (notBGZF) {
                break;
            }
        }
        raf.close();
        //For file with small size and many threads. 
        for (int i = 0; i < (threadNum - 1); i++) {
            if (pos[i] == pos[i + 1]) {
                threadNum = 1;
                pos[1] = pos[pos.length - 1];
            }
        }
    }

    public void creatSpider() throws IOException {
        spider = new BZSpider[this.threadNum];
        for (int i = 0; i < this.threadNum; i++) {
            spider[i] = new BZSpider(i, intFileFormat, this.pos[i], this.pos[i + 1], '\t');
            // System.out.println("BZSpider" + i + ": created!");
        }
    }

    public class BZSpider {

        int spiderID;
        int intFormat;
        long longRemainSize = -1;
        InputStream inputStream;

        int intRead = -1;
        byte[] bytBuffer = new byte[BUF_SIZE];
        int intLineStart = 0;
        int intCurrPos = 0;
        int intTempBufferLengthACC = 0;
        byte[] bytTempBuffer = new byte[BUF_SIZE];
        byte[] bytStartLine = null;
        byte[] bytEndLine = null;
        byte bytDelimiter;

        public BZSpider(int spiderID, int intFormat, long longStart, long longEnd, char chrDelimiter) throws IOException {
            longRemainSize = (longEnd - longStart);
            this.spiderID = spiderID;
            this.intFormat = intFormat;
            //Plan one           
//            RandomAccessFile raf=new RandomAccessFile(fleInput.getCanonicalFile(), "r");
//            raf.seek(longStart);
//            input=new GZIPInputStream(Channels.newInputStream(raf.getChannel()),BUF_SIZE);
//            this.bytDelimiter=(byte)chrDelimiter;
//            if(this.spiderID!=0)    bytStartLine=this.readLine(new int[3000]);

            //Plan two. 
//            RandomAccessFile raf=new RandomAccessFile(fleInput.getCanonicalFile(), "r");
//            MappedByteBuffer mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, longStart,longRemainSize); 
//            for(int i=0;i<300;i++)   System.out.print(mbb.get());
//            System.out.println();
            //Plan Three. 
//            InputStream is = new BufferedInputStream(new FileInputStream(input));
//            LimitInputStream cis = new LimitInputStream(is, longEnd);
//            cis.skip(longStart);
//            ReadableByteChannel inChannel = Channels.newChannel(cis);
//            input= new GZIPInputStream(Channels.newInputStream(inChannel));
//            input=new GZIPInputStream(cis);
            if (intFormat == 0) {
                InputStream is = new BufferedInputStream(new FileInputStream(input));
                LimitInputStream cis = new LimitInputStream(is, longEnd);
                cis.skip(longStart);
                inputStream = new GZIPInputStream(cis);
            } else {
                InputStream is = new BufferedInputStream(new FileInputStream(input));
                LimitInputStream cis = new LimitInputStream(is, longEnd);
                cis.skip(longStart);
                inputStream = cis;
            }

            if (this.spiderID != 0) {
                bytStartLine = this.readLine();
                spider[this.spiderID - 1].bytEndLine = bytStartLine;
                // System.out.println(new String(bytStartLine));
            }
            bytDelimiter = (byte) chrDelimiter;
        }

        public void closeInputStream() throws IOException {
            inputStream.close();
        }

        public synchronized byte[] readLine(int intDelPos[]) throws IOException {
            int intDelPosMarker = 0;
            intDelPos[intDelPosMarker] = 0;
            int len = intDelPos.length;
            do {
                if (intRead == -1) {
                    intRead = (inputStream.read(bytBuffer));
                    if (intRead == -1) {
                        //The end of the block is not a complete line. 
                        if (intTempBufferLengthACC != 0) {
                            bytBuffer = new byte[intTempBufferLengthACC + (bytEndLine == null ? 0 : bytEndLine.length) + 1];
                            System.arraycopy(bytTempBuffer, 0, bytBuffer, 0, intTempBufferLengthACC);
                            if (bytEndLine != null) {
                                System.arraycopy(this.bytEndLine, 0, bytBuffer, intTempBufferLengthACC, this.bytEndLine.length);
                            }
                            bytBuffer[this.bytBuffer.length - 1] = (byte) '\n';
                            intTempBufferLengthACC = 0;
                            intDelPosMarker = 0;
                        } else {
                            return null;
                        }
                    }
                }

                intLineStart = intCurrPos;
                while (intCurrPos != intRead) {
                    if (bytBuffer[intCurrPos] == bytDelimiter) {
                        ++intDelPosMarker;
                        if (intDelPosMarker < len) {
                            intDelPos[intDelPosMarker] = intCurrPos - intLineStart + intTempBufferLengthACC;
                        }
                    } else if (bytBuffer[intCurrPos] == 10) {
                        //parse the line. 
                        int intLineLength = intCurrPos - intLineStart;//don't contaion \n
                        byte[] bytLine = null;
                        if (intTempBufferLengthACC != 0) {
                            // bytLine = new byte[intTempBufferLengthACC + intLineLength];
                            //System.arraycopy(bytTempBuffer, 0, bytLine, 0, intTempBufferLengthACC);                        
                            bytLine = Arrays.copyOfRange(bytTempBuffer, 0, intTempBufferLengthACC + intLineLength);
                            System.arraycopy(bytBuffer, intLineStart, bytLine, intTempBufferLengthACC, intLineLength);
                            intTempBufferLengthACC = 0;
                        } else {
                            // bytLine = new byte[intLineLength];
                            // System.arraycopy(bytBuffer, intLineStart, bytLine, 0, intLineLength);
                            bytLine = Arrays.copyOfRange(bytBuffer, intLineStart, intCurrPos);
                        }
                        intCurrPos++;
                        if (intCurrPos == intRead) {
                            intRead = -1;
                            intCurrPos = 0;
                        }

                        ++intDelPosMarker;
                        if (intDelPosMarker < len) {
                            //return 13 new line 10
                            //the new line or return line symbol is not included
                            if (intCurrPos > 1 && bytBuffer[intCurrPos - 2] == 13) {
                                intDelPos[intDelPosMarker] = bytLine.length - 1;
                            } else {
                                intDelPos[intDelPosMarker] = bytLine.length;
                            }
                            intDelPos[0] = intDelPosMarker;
                        } else {
                            intDelPos[0] = len;
                        }

                        return bytLine;
                    }
                    intCurrPos++;
                }

                //The buffer ends with imcomplete line. 
                int intTempBufferLength = intCurrPos - intLineStart;
                if ((bytTempBuffer.length - intTempBufferLengthACC) < intTempBufferLength) {
                    bytTempBuffer = Arrays.copyOf(bytTempBuffer, bytTempBuffer.length + intTempBufferLength * 2);
                }
                System.arraycopy(bytBuffer, intLineStart, bytTempBuffer, intTempBufferLengthACC, intTempBufferLength);
                intTempBufferLengthACC += intTempBufferLength;
                intRead = -1;
                intCurrPos = 0;
            } while (intTempBufferLengthACC != 0);

            return null;
        }

        public synchronized byte[] readLine() throws IOException {
            do {
                if (intRead == -1) {
                    intRead = (inputStream.read(bytBuffer));
                    if (intRead == -1) {
                        //The end of the block is not a complete line. 
                        if (intTempBufferLengthACC != 0) {
                            bytBuffer = new byte[intTempBufferLengthACC + (bytEndLine == null ? 0 : bytEndLine.length) + 1];
                            System.arraycopy(bytTempBuffer, 0, bytBuffer, 0, intTempBufferLengthACC);
                            if (bytEndLine != null) {
                                System.arraycopy(this.bytEndLine, 0, bytBuffer, intTempBufferLengthACC, this.bytEndLine.length);
                            }
                            bytBuffer[this.bytBuffer.length - 1] = (byte) '\n';
                            intTempBufferLengthACC = 0;
                        } else {
                            return null;
                        }
                    }
                }

                intLineStart = intCurrPos;
                while (intCurrPos != intRead) {
                    if (bytBuffer[intCurrPos] == 10) {
                        //parse the line. 
                        int intLineLength = intCurrPos - intLineStart;//don't contaion \n
                        byte[] bytLine = null;
                        if (intTempBufferLengthACC != 0) {
                            // bytLine = new byte[intTempBufferLengthACC + intLineLength];
                            //System.arraycopy(bytTempBuffer, 0, bytLine, 0, intTempBufferLengthACC);                        
                            bytLine = Arrays.copyOfRange(bytTempBuffer, 0, intTempBufferLengthACC + intLineLength);
                            System.arraycopy(bytBuffer, intLineStart, bytLine, intTempBufferLengthACC, intLineLength);
                            intTempBufferLengthACC = 0;
                        } else {
                            // bytLine = new byte[intLineLength];
                            // System.arraycopy(bytBuffer, intLineStart, bytLine, 0, intLineLength);
                            bytLine = Arrays.copyOfRange(bytBuffer, intLineStart, intCurrPos);
                        }
                        intCurrPos++;
                        if (intCurrPos == intRead) {
                            intRead = -1;
                            intCurrPos = 0;
                        }
                        return bytLine;
                    }
                    intCurrPos++;
                }

                //The buffer ends with imcomplete line. 
                int intTempBufferLength = intCurrPos - intLineStart;
                if ((bytTempBuffer.length - intTempBufferLengthACC) < intTempBufferLength) {
                    bytTempBuffer = Arrays.copyOf(bytTempBuffer, bytTempBuffer.length + intTempBufferLength * 2);
                }
                System.arraycopy(bytBuffer, intLineStart, bytTempBuffer, intTempBufferLengthACC, intTempBufferLength);
                intTempBufferLengthACC += intTempBufferLength;
                intRead = -1;
                intCurrPos = 0;
            } while (intTempBufferLengthACC != 0);

            return null;
        }

    }

    public static void main(String[] args) throws IOException {
//        String strFile="D:\\01WORK\\KGGseq\\testdata\\1000Genome\\ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf";
        String strFile = "E:\\home\\mxli\\MyJava\\kggseq3\\1kgafr.20150813.vcf.gz";
        BGZFInputStream bf = new BGZFInputStream(strFile, 4);
        int[] intDelPos = new int[3000];
        bf.adjustPos();
        bf.creatSpider();

        int intLine = 0;
        for (int i = 0; i < bf.threadNum; i++) {
            byte[] bytTemp = bf.spider[i].readLine(intDelPos);
            while (bytTemp != null) {
//            System.out.println(intLine+++": "+new String(bytTemp));               
                bytTemp = bf.spider[i].readLine(intDelPos);
                intLine++;
            }
        }
        System.out.println(intLine);
    }
}
