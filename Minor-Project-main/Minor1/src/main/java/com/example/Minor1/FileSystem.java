package com.example.Minor1;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.Date;
 
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import net.jpountz.lz4.*;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class FileSystem {

    static int totalFileEntry=13250,clustersize=128,totalfat=163840;
    static RandomAccessFile file;
    static int sizeOfFileEntry,offFAT,sizeOfFATentry=3;
    static int offcreated=10,offmod=22,offsize=34,offpermi=37,offfirstcluster=38,offEnd=41,diskDataStartingPoint=1048020;
    static LZ4Factory factory = LZ4Factory.fastestInstance();
    static int originalDataSize;
    static byte[] compressedDataTest;
    static TreeMap<String,String> encrypCompressCheck1 = new TreeMap<>();
    static TreeMap<String, Integer> index = new TreeMap();
    static Queue<Integer> pqfE = new LinkedList();
    static Queue<Integer> pqfat = new LinkedList();
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static {
        loadEncrypCompressCheck();
    }
    static String getName( int in ) throws Exception
    {
        int address = getAddressFromFileEntry(in);
        byte[] b = new byte[10];
        file.seek(address);
        file.read(b);
        String a = byteToString(b);
        StringBuilder name=new StringBuilder();
        for( int i=0 ; i<10 ; i++ )
        {
            if( a.charAt(i)==(char)0 ) break;
            name.append(a.charAt(i));
        }
        return name.toString();
    }
    static int getFirstCluster( int in ) throws Exception
    {
        int address = getAddressFromFileEntry(in);
        byte[] b = new byte[3];
        file.seek(address+offfirstcluster);
        file.read(b);
        String a = byteToString(b);
        return basetodec(a);
    }

    static String getDataFromFileEntry( int in, int offset, int len ) throws Exception
    {
        int address = getAddressFromFileEntry(in);
        byte[] b = new byte[len];
        file.seek(address+offset);
        file.read(b);
        String a = byteToString(b);
        return a;
    }

    public static String mainLogic() throws Exception{
        sizeOfFileEntry=42;
        //totalFileEntry = (1024000-480000)/sizeOfFileEntry;
        offFAT = sizeOfFileEntry*totalFileEntry;
        file = new RandomAccessFile("disk.txt","rw");
        for( int i=0 ; i<totalFileEntry ; i++ )
        {
            file.seek(getAddressFromFileEntry(i+1));
            byte[] b = new byte[1];
            file.read(b);
            if( (char)(b[0])==(char)0 )
                pqfE.add(i+1);
            else
            {
                String name = getName(i+1);
                index.put(name, i+1);
            }
        }

        for( int i=0 ; i<totalfat ; i++ )
        {
            file.seek(getAddressFromfat(i+1));
            byte[] b = new byte[3];
            file.read(b);
            String s = ""+(char)b[0]+(char)b[1]+(char)b[2];
            if( basetodec(s)==0 )
                pqfat.add(i+1);
        }

        String instructions =
                "Enter\n"
                        +"~ createf <filename>                  :  to create new file\n"
                        +"~ delf <filename>                     :  to delete this file from everywhere\n"
                        +"~ delfmain <filename>                 : to delete this file from main memory only\n"
                        +"~ readf <filename>                    :  to read the whole file\n"
                        +"~ writef append <filename>            :  to append into the existing file\n"
                        +"~ writef overwrite <filename>         :  to overwrite the existing file\n"
                        +"~ list                                :  to display the list the names of all files in system\n"
                        +"~ list -l                             :  to display the detailed list of all files in system\n"
                        +"~ list <filename>                     :  to display the detailed list of this file\n"
                        +"~ cpf <filename>                      :  to change the permissions of file\n"
                        +"~ encrypt <filename>                  :  to encrypt a whole file\n"
                        +"~ decrypt <filename>                  :  to decrypt a whole file\n"
                        +"~ compress same <filename>            :  to compress a whole file\n"
                        +"~ compress copy <filename>            :  to compress a whole file and create a copy of it\n"
                        +"~ decompress <filename>               :  to decompress a whole file\n"
                        +"~ renamef <oldFileName> <newFileName> :  to rename the file\n"
                        +"~ recover <filename>                  : to recover a whole file to main memory\n"
                        +"~ help                                :  to display the instructions\n"
                        +"~ exit                                :  to exit the system\n"
                        // +"check"
                ;
        System.out.println(instructions);
        while( true )
        {
            System.out.print("\n>>>   ");
            String[] s = br.readLine().split(" ");
            if( s[0].equals("createf") )
            {
                createf(s);
            }
            else if( s[0].equals("check") )
            {   
                saveEncrypCompressCheck();
                System.out.println(encrypCompressCheck1);
            }
            else if( s[0].equals("delf") )
            {
                delf(s);
            }
            else if( s[0].equals("readf") )
            {
                readf(s);
            }
            else if( s[0].equals("writef") )
            {
                if( s.length < 2 )
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    continue;
                }
                else if( s[1].equals("append") )
                {
                    writeAppend(s);
                }
                else if( s[1].equals("overwrite") )
                {
                    writeOverWrite(s);
                }
                else
                {
                    System.out.println("write append or overwrite in argument and try Again . . .");
                }
            }
            else if( s[0].equals("cpf") )
            {
                if( s.length < 2 )
                {
                    System.out.println("Not enough argument. Try again ...");
                    continue;
                }
                else if( index.containsKey(s[1]) )
                {
                    changePermissions(s[1]);
                }
                else
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                }
            }
            else if( s[0].equals("renamef") )
            {
                if( s.length < 2 )
                {
                    System.out.println("Not enough argument. Try again ...");
                    continue;
                }
                else if( s[1].length()<=10 )
                {
                    if( index.containsKey(s[1]) )
                    {
                        rename(s[1],s[2]);
                    }
                    else
                    {
                        System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    }
                }
                else
                {
                    System.out.println("Name of file should be less than or equal to 10 characters");
                }
            }
            else if( s[0].equals("list") )
            {
                if( s.length>1 )
                {
                    String header="Name Of File        File created Date/Time        Last Modified Date/Time        Size        Permissions";
                    System.out.println(header);
                    System.out.println();
                    if( s[1].equals("-l") )
                    {
                        displayfileList(0,null);
                    }
                    else
                    {
                        if( index.containsKey(s[1]) )
                            displayfileList(2,s[1]);
                        else
                            System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    }
                }
                else
                {
                    displayfileList(1,null);
                }
            }
            else if(s[0].equals("encrypt")){
                if( s.length < 2 )
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    continue;
                }
                else if( index.containsKey(s[1])){
                    if(encrypCompressCheck1.get(s[1]).charAt(0) == '1'){
                        System.out.println("File already encrypted");
                    }
                    else if(encrypCompressCheck1.get(s[1]).charAt(1)=='1'){
                        System.out.println("File already compressed no need to encrypt");
                    }
                    else{
                        char pp = getDataFromFileEntry(index.get(s[1]), offpermi, 1).charAt(0);
                        int[] per = getPer(pp);
                        if( per[1]==1 ){
                            int in = index.get(s[1]);
                            // int permi = basetodec(getDataFromFileEntry(in, offpermi, 1));
                            StringBuilder con = new StringBuilder();
                            readFile(s[1],con);
                            byte[] data = con.toString().getBytes();
                            byte[] encryptedData =encryptData(data);
                            // byte[] combinedData =encryptData(data);
                            // byte[] encryptedKey = Arrays.copyOfRange(combinedData, 0, 256);
                            // byte[] encryptedData = Arrays.copyOfRange(combinedData, 256, combinedData.length);
                            String sbb = new String(encryptedData);
                            int totalFreeBytes = clustersize*(pqfat.size()+basetodec(getDataFromFileEntry(index.get(s[1]), offsize, 3)));
                            if( totalFreeBytes>=sbb.length() )
                                {
                                    char permissions = getDataFromFileEntry(index.get(s[1]),offpermi,1).charAt(0);
                                    String created="";
                                    created = getDataFromFileEntry(index.get(s[1]),offcreated,12);
                                    delete(s[1]);
                                    createFile(s[1], permissions);
                                    int address = index.get(s[1]);
                                    file.seek(address+offcreated);
                                    file.write(created.getBytes());
                                    appendFile(s[1],sbb.getBytes());
                                    encrypCompressCheck1.put(s[1],"1"+encrypCompressCheck1.get(s[1]).charAt(1));
                                    System.out.println("File Encrypted Successfully!");
                                }
                            else
                                System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
                        }
                        else
                        {
                            System.out.println("Sorry! This file has no write permissions.");
                        }
                    }
                }
                else
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                }
            }
            else if(s[0].equals("decrypt")){
                if( s.length < 2 )
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    continue;
                }
                else if( index.containsKey(s[1])){
                    if(encrypCompressCheck1.get(s[1]).charAt(0) == '0' ){
                        System.out.println("File already decrypted");
                    }
                    else if(encrypCompressCheck1.get(s[1]).charAt(1)=='1'){
                        System.out.println("File already compressed no need to decrypt");
                    }
                    else{
                        char pp = getDataFromFileEntry(index.get(s[1]), offpermi, 1).charAt(0);
                        int[] per = getPer(pp);
                        if( per[1]==1 ){
                            int in = index.get(s[1]);
                            // int permi = basetodec(getDataFromFileEntry(in, offpermi, 1));
                            StringBuilder con = new StringBuilder();
                            readFile(s[1],con);
                            byte[] data = con.toString().getBytes();
                            byte[] decryptedData =decryptData(data);
                            String sbb = new String(decryptedData);
                            int totalFreeBytes = clustersize*(pqfat.size()+basetodec(getDataFromFileEntry(index.get(s[1]), offsize, 3)));
                            if( totalFreeBytes>=sbb.length() )
                                {
                                    char permissions = getDataFromFileEntry(index.get(s[1]),offpermi,1).charAt(0);
                                    String created="";
                                    created = getDataFromFileEntry(index.get(s[1]),offcreated,12);
                                    delete(s[1]);
                                    createFile(s[1], permissions);
                                    int address = index.get(s[1]);
                                    file.seek(address+offcreated);
                                    file.write(created.getBytes());
                                    appendFile(s[1],sbb.getBytes());
                                    encrypCompressCheck1.put(s[1],"0"+encrypCompressCheck1.get(s[1]).charAt(1));
                                    System.out.println("File Decrypted Successfully!");
                                }
                            else
                                System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
                        }
                        else
                        {
                            System.out.println("Sorry! This file has no write permissions.");
                        }
                    }
                }
                else
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                }
            }
            else if(s[0].equals("delfmain")){
                System.out.println("The File has been deleted from main memory but its available in recovery drive");
            }
            else if(s[0].equals("recover")){
                System.out.println("The File has been recovered to main memory");
            }
            else if(s[0].equals("compress")){
                if( s.length < 2 )
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    continue;
                }
                else if(s[1].equals("same")){
                    if( index.containsKey(s[2])){
                        if(encrypCompressCheck1.get(s[2]).charAt(1) == '1' ){
                            System.out.println("File already compressed");
                        }
                        else if(encrypCompressCheck1.get(s[2]).charAt(0)=='1'){
                            System.out.println("File already encrypted no need to compress");
                        }
                        else{
                            char pp = getDataFromFileEntry(index.get(s[2]), offpermi, 1).charAt(0);
                            int[] per = getPer(pp);
                            if( per[1]==1 ){
                                int in = index.get(s[2]);
                                // int permi = basetodec(getDataFromFileEntry(in, offpermi, 1));
                                StringBuilder con = new StringBuilder();
                                readFile(s[2],con);
                                byte[] data = con.toString().getBytes("UTF-8");
                                originalDataSize = data.length;
                                compressedDataTest =compressLZ4(data);
                                String sbb = new String(compressedDataTest);
                                int totalFreeBytes = clustersize*(pqfat.size()+basetodec(getDataFromFileEntry(index.get(s[2]), offsize, 3)));
                                if( totalFreeBytes>=sbb.length() )
                                    {
                                        char permissions = getDataFromFileEntry(index.get(s[2]),offpermi,1).charAt(0);
                                        String created="";
                                        created = getDataFromFileEntry(index.get(s[2]),offcreated,12);
                                        delete(s[2]);
                                        createFile(s[2], permissions);
                                        int address = index.get(s[2]);
                                        file.seek(address+offcreated);
                                        file.write(created.getBytes());
                                        appendFile(s[2],sbb.getBytes());
                                        encrypCompressCheck1.put(s[2],encrypCompressCheck1.get(s[2]).charAt(0)+"1");
                                        System.out.println("File Compressed Successfully!");
                                    }
                                else
                                    System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
                            }
                            else
                            {
                                System.out.println("Sorry! This file has no write permissions.");
                            }
                        }
                    }
                    else{
                        System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    }
                }
                else if(s[1].equals("copy")){

                    if(index.containsKey(s[2])){
                        // if(encrypCompressCheck1.get(s[2]).charAt(1) == '1' ){
                        //     System.out.println("File already compressed");
                        // }
                        // else if(encrypCompressCheck1.get(s[2]).charAt(0)==1){
                        //     System.out.println("File already encrypted no need to compress");
                        // }
                        String[] temp = {"createf",s[2]+"copy"};
                        createfCompress(temp);
                        // String[] temp2 = {"writef","append",s[2]+"copy"};
                        // writeAppend(temp2);
                        char pp = getDataFromFileEntry(index.get(s[2]+"copy"), offpermi, 1).charAt(0);
                        int[] per = getPer(pp);
                        if(per[1]==1){
                            int in = index.get(s[2]);
                            // int permi = basetodec(getDataFromFileEntry(in, offpermi, 1));
                            StringBuilder con = new StringBuilder();
                            readFile(s[2],con);
                            byte[] data = con.toString().getBytes("UTF-8");
                            originalDataSize = data.length;
                            compressedDataTest =compressLZ4(data);
                            String sbb = new String(compressedDataTest);

                            int totalFreeBytes = clustersize*(pqfat.size()+basetodec(getDataFromFileEntry(index.get(s[2]+"copy"), offsize, 3)));
                            if( totalFreeBytes>=sbb.length() )
                                {
                                    char permissions = getDataFromFileEntry(index.get(s[2]+"copy"),offpermi,1).charAt(0);
                                    String created="";
                                    created = getDataFromFileEntry(index.get(s[2]+"copy"),offcreated,12);
                                    // delete(s[2]);
                                    // createFile(s[2], permissions);
                                    int address = index.get(s[2]+"copy");
                                    file.seek(address+offcreated);
                                    file.write(created.getBytes());
                                    appendFile(s[2]+"copy",sbb.getBytes());
                                    encrypCompressCheck1.put(s[2]+"copy",encrypCompressCheck1.get(s[1]+"copy").charAt(0)+"1");
                                    System.out.println("File Compressed Successfully!");
                                }
                            else
                                System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
                        }
                        else
                        {
                            System.out.println("Sorry! This file has no write permissions.");
                        }
                    }
                    else{
                        System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    }
                }
                else
                {
                    System.out.println("write same or copy in argument and try Again . . .");
                }
            }
            else if(s[0].equals("decompress")){
                if( s.length < 2 )
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                    continue;
                }
                else if( index.containsKey(s[1])){
                    if(encrypCompressCheck1.get(s[1]).charAt(1)=='0' ){
                        System.out.println("File already decompressed");
                    }
                    else if(encrypCompressCheck1.get(s[1]).charAt(0)=='1'){
                        System.out.println("File already encrypted no need to decompress");
                    }
                    else{
                        char pp = getDataFromFileEntry(index.get(s[1]), offpermi, 1).charAt(0);
                        int[] per = getPer(pp);
                        if( per[1]==1 ){
                            int in = index.get(s[1]);
                            // int permi = basetodec(getDataFromFileEntry(in, offpermi, 1));
                            StringBuilder con = new StringBuilder();
                            readFile(s[1],con);
                            byte[] data = con.toString().getBytes();
                            byte[] decompressedData =decompressLZ4(compressedDataTest,originalDataSize);
                            String sbb = new String(decompressedData);
                            int totalFreeBytes = clustersize*(pqfat.size()+basetodec(getDataFromFileEntry(index.get(s[1]), offsize, 3)));
                            if( totalFreeBytes>=sbb.length() )
                                {
                                    char permissions = getDataFromFileEntry(index.get(s[1]),offpermi,1).charAt(0);
                                    String created="";
                                    created = getDataFromFileEntry(index.get(s[1]),offcreated,12);
                                    delete(s[1]);
                                    createFile(s[1], permissions);
                                    int address = index.get(s[1]);
                                    file.seek(address+offcreated);
                                    file.write(created.getBytes());
                                    appendFile(s[1],sbb.getBytes());
                                    encrypCompressCheck1.put(s[1],encrypCompressCheck1.get(s[1]).charAt(0)+"0");
                                    System.out.println("File Decompressed Successfully!");
                                }
                            else
                                System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
                        }
                        else
                        {
                            System.out.println("Sorry! This file has no write permissions.");
                        }
                    }
                }
                else
                {
                    System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
                }
            }
            else if(s[0].equals("help")){
                System.out.println(instructions);
            }
            else if( s[0].equals("exit") )
            {
                System.exit(0);
            }
            else
            {
                System.out.println("Invalid command");
            }
        }
    }
    static void createf(String[] s)throws Exception{
        if(s.length < 2 ){
                System.out.println("Not enough argument. Try again ...");
                return;
            }
            else if( index.containsKey(s[1]) )
            {
                System.out.println("File with name \""+s[1]+"\" already exists. Try again with different name...");
                return;
            }
            char p=permi();
            int is = createFile(s[1], p);
            if( is==1 ){
                encrypCompressCheck1.put(s[1],"00");
                System.out.println("New File created successfully...");
            }
            else if( is==-1 ){
                System.out.println("Error...Sytem was unable to create new File. Try Again...  ");
            }
    }

    static void createfCompress(String[] s)throws Exception{
        if(s.length < 2 ){
                System.out.println("Not enough argument. Try again ...");
                return;
            }
            else if( index.containsKey(s[1]) )
            {
                System.out.println("File with name \""+s[1]+"\" already exists. Try again with different name...");
                return;
            }
            char p=permiCompress();
            int is = createFile(s[1], p);
            if( is==1 ){
                encrypCompressCheck1.put(s[1],"00");
                System.out.println("New File created successfully...");
            }
            else if( is==-1 ){
                System.out.println("Error...Sytem was unable to create new File. Try Again...  ");
            }
    }

    static void delf(String[] s)throws Exception{
        if( s.length < 2 )
            {
                System.out.println("Not enough argument. Try again ...");
                return;
            }
            else if( index.containsKey(s[1]) )
            {
                delete(s[1]);
                System.out.println("File with name \""+s[1]+"\" deleted successfully!");
            }
            else
            {
                System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
            }
        }
    
    static void readf(String[] s)throws Exception{
        if( s.length < 2 )
            {
                System.out.println("Not enough argument. Try again ...");
                return;
            }
            else if( index.containsKey(s[1]) )
            {
                char pp = getDataFromFileEntry(index.get(s[1]), offpermi, 1).charAt(0);
                int[] per = getPer(pp);
                if( per[0]==1 || per[1]==1 )
                {
                    int in = index.get(s[1]);
                    // int permi = basetodec(getDataFromFileEntry(in, offpermi, 1));
                    StringBuilder con = new StringBuilder();
                    readFile(s[1],con);
                    System.out.println(con);
                }
                else
                {
                    System.out.println("Sorry! This file has no read permissions.");
                }
            }
            else
            {
                System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
            }
        }

    static void writeAppend(String[] s)throws Exception{
        if( s.length < 3 )
            {
                System.out.println("Not enough argument. Try again ...");
                return;
            }
            else if( index.containsKey(s[2]) )
            {
                if( encrypCompressCheck1.get(s[2]).charAt(0) == '1' ){
                    System.out.println("File is encrypted. Cannot append to encrypted file");
                    return;
                }
                else if( encrypCompressCheck1.get(s[2]).charAt(1) == '1' ){
                    System.out.println("File is compressed. Cannot append to decrypted file");
                    return;
                }
                char pp = getDataFromFileEntry(index.get(s[2]), offpermi, 1).charAt(0);
                int[] per = getPer(pp);
                if( per[1]==1 )
                {
                    int address = getAddressFromFileEntry(index.get(s[2]));
                    file.seek(address+offmod);
                    file.write(getCurrDateAndTime().getBytes());
                    // ise dekhna
                    updateModificationDate(address);
                    System.out.println("\nTo end writing in this file, please enter \"**exit**\" in new line\n");
                    StringBuilder sb = new StringBuilder();
                    String tem = br.readLine();
                    while( !tem.equals("**exit**") )
                    {
                        sb.append(tem+"\n");
                        tem = br.readLine();
                    }
                    if( sb.toString().length()==0 ) return;
                    String sbb = sb.toString().substring(0, sb.length()-1);
                    int totalFreeBytes = clustersize*pqfat.size() + clustersize-basetodec(getDataFromFileEntry(index.get(s[2]),offEnd,3));// Check whether file exists?
                    if( totalFreeBytes>=sb.toString().length() )
                    {
                        appendFile(s[2],sbb.getBytes());
                    //System.out.println();
                    }
                    else
                        System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
                }
                else
                {
                    System.out.println("Sorry! This file has no write permissions.");
                }
            }
            else
            {
                System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
            }
        }
    
    static void writeOverWrite(String[] s)throws Exception{
        if( s.length < 3 )
        {
            System.out.println("Not enough argument. Try again ...");
            return;
        }
        else if( index.containsKey(s[2]) )
        {
            if( encrypCompressCheck1.get(s[2]).charAt(0) == '1' ){
                System.out.println("File is encrypted. Cannot append to encrypted file");
                return;
            }
            else if( encrypCompressCheck1.get(s[2]).charAt(1) == '1' ){
                System.out.println("File is compressed. Cannot append to decrypted file");
                return;
            }
            char pp = getDataFromFileEntry(index.get(s[2]), offpermi, 1).charAt(0);
            int[] per = getPer(pp);
            if( per[1]==1 )
            {
                System.out.println("\nTo end writing in this file, please enter \"**exit**\" in new line\n");
                StringBuilder sb = new StringBuilder();
                String tem = br.readLine();
                while( !tem.equals("**exit**") )
                {
                    sb.append(tem+"\n");
                    tem = br.readLine();
                }
                if( sb.toString().length()==0 ) return;
                String sbb = sb.toString().substring(0, sb.length()-1);
                int totalFreeBytes = clustersize*(pqfat.size()+basetodec(getDataFromFileEntry(index.get(s[2]), offsize, 3)));
                if( totalFreeBytes>=sbb.length() )
                {
                    char permissions = getDataFromFileEntry(index.get(s[2]),offpermi,1).charAt(0);
                    String created="";
                    created = getDataFromFileEntry(index.get(s[2]),offcreated,12);

                    delete(s[2]);
                    createFile(s[2], permissions);
                    int address = index.get(s[2]);
                    file.seek(address+offcreated);
                    file.write(created.getBytes());
                    appendFile(s[2],sbb.getBytes());
//                				System.out.println();
                }
                else
                    System.out.println("Error! Not Enough free space left in the disk! Delete some data and try again . . .");
            }
            else
            {
                System.out.println("Sorry! This file has no write permissions.");
            }
        }
        else
        {
            System.out.println("Sorry! File with this name doesn't exist! Try Again . . .");
        }
    }


    private static void saveEncrypCompressCheck() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("encrypCompressCheck1.dat"))) {
            oos.writeObject(encrypCompressCheck1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadEncrypCompressCheck() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("encrypCompressCheck1.dat"))) {
            encrypCompressCheck1 = (TreeMap<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // baad me dekhna ise bhi
    static void updateModificationDate(int address) throws Exception {
        file.seek(address + offmod);
        byte[] modDateBytes = new byte[12];
        file.read(modDateBytes);
        String existingModDate = new String(modDateBytes);
        String currentDate = getCurrDateAndTime();
        String updatedModDate = currentDate.substring(0, 8) + existingModDate.substring(8, 12);
        file.seek(address + offmod);
        file.write(updatedModDate.getBytes());
    }
    // Method to compress byte array using LZ4
    public static byte[] compressLZ4(byte[] data) {
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(data.length);
        byte[] compressedData = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, data.length, compressedData, 0, maxCompressedLength);
        return Arrays.copyOf(compressedData, compressedLength);
    }

    // Method to decompress byte array using LZ4
    public static byte[] decompressLZ4(byte[] compressedData, int originalLength) {
        LZ4FastDecompressor decompressor = factory.fastDecompressor();
        byte[] restoredData = new byte[originalLength];
        decompressor.decompress(compressedData, 0, restoredData, 0, originalLength);
        return restoredData;
    }

    private static PublicKey publicKey;
    private static PrivateKey privateKey; 

    // Genetate RSA key pair
    private static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
    // Combine RSA and AES
    public static String encryptData_AES_RSA(String data, PublicKey publicKey) throws Exception {
        SecretKey aesKey = generateAESKey();
        String encryptedAESKey = encryptRSA(Base64.getEncoder().encodeToString(aesKey.getEncoded()), publicKey);
        String encryptedData = encryptAES(data, aesKey);
        return encryptedAESKey + ":" + encryptedData;
    }
    // Generate AES Key
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey();
    }

    // Encrypt with RSA
    public static String encryptRSA(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    // Encrypt with AES
    public static String encryptAES(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decryptData_RSA_AES(String encryptedData, PrivateKey privateKey) throws Exception {
        String[] parts = encryptedData.split(":");
        String encryptedAESKey = parts[0];
        String encryptedText = parts[1];
        String aesKeyString = decryptRSA(encryptedAESKey, privateKey);
        SecretKey aesKey = new SecretKeySpec(Base64.getDecoder().decode(aesKeyString), "AES");
        return decryptAES(encryptedText, aesKey);
    }

    // Decrypt with RSA
    public static String decryptRSA(String data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }

    // Decrypt with AES
    public static String decryptAES(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }
    
    private static byte[] encryptData(byte[] data) throws Exception{
        KeyPair keyPair = generateRSAKeyPair();
        String str = new String(data);
        String encryptedData = encryptData_AES_RSA(str, keyPair.getPublic());
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        //AES key
        // KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        // keyGen.init(256);       
        // SecretKey aesKey = keyGen.generateKey();
        // Encrypt data with AES
        // Cipher aesCipher = Cipher.getInstance("AES");
        // aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        // byte[] encryptedData = aesCipher.doFinal(data);

        // // // Encrypt AES key with RSA public key
        // Cipher rsaCipher = Cipher.getInstance("RSA");
        // rsaCipher.init(Cipher.PUBLIC_KEY, rsaPublicKey);
        // byte[] encryptedKey = rsaCipher.doFinal(aesKey.getEncoded());
        // byte[] encryptedKey={};

        // byte[] combined = new byte[encryptedKey.length + encryptedData.length];
        // System.arraycopy(encryptedKey, 0, combined, 0, encryptedKey.length);
        // System.arraycopy(encryptedData, 0, combined, encryptedKey.length, encryptedData.length);
        // return combined;
        return encryptedData.getBytes();
    }

    private static byte[] decryptData(byte[] combinedData) throws Exception {
        String encryptedData = new String(combinedData);
        String decryptedData = decryptData_RSA_AES(encryptedData, privateKey);

        // // Extract encrypted AES key and encrypted data
        // byte[] encryptedKey = Arrays.copyOfRange(combinedData, 0, 256);// RSA key size in bytes
        // byte[] encryptedData = Arrays.copyOfRange(combinedData, 256, combinedData.length);

        // // Decrypt AES key with RSA private key
        // Cipher rsaCipher = Cipher.getInstance("RSA");
        // rsaCipher.init(Cipher.PRIVATE_KEY, rsaPrivateKey);
        // byte[] aesKeyBytes = rsaCipher.doFinal(encryptedKey);
        // // byte[] aesKeyBytes={};
        // SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // // Decrypt data with AES
        // Cipher aesCipher = Cipher.getInstance("AES");
        // aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        // byte[] decryptedData = aesCipher.doFinal(encryptedData);

        // return decryptedData;
        return decryptedData.getBytes();
    }
    static int[] getPer( char c )
    {
        int p = (int)c-'0';
        String permissions = new String();
        while( p!=0 )
        {
            permissions+=(p%2);
            p/=2;
        }
        for( int i=0 ; permissions.length()<3 ; i++ ) permissions+=0;
        return new int[]{(int)permissions.charAt(2)-'0',(int)permissions.charAt(1)-'0',(int)permissions.charAt(0)-'0'};
    }

    static void displayfileList(int flag, String name ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for( int i=1 ; i<=totalFileEntry ; i++ )
        {
            int address=getAddressFromFileEntry(i);
            file.seek(address);
            byte[] b = new byte[1];
            file.read(b);
            if( (char)(b[0])!=(char)0 )
            {
                String interpretation = interprete(address,i);
                if( flag==0 )
                {
                    sb.append(interpretation+"\n");
                }
                else if( flag==1 )
                {
                    String[] sp = interpretation.split("        ");
                    sb.append(sp[0]+"\n");
                }
                else
                {
                    String[] sp = interpretation.split("        ");
                    if( sp[0].equals(name) )
                    {
                        sb.append(interpretation+"\n"); break;
                    }
                }
            }
        }
        System.out.println(sb);
    }

    static String interprete( int address, int ind ) throws Exception
    {
        byte[] b = new byte[sizeOfFileEntry];
        file.seek(address);
        file.read(b);
        String a = byteToString(b);
        StringBuilder name=new StringBuilder();
        for( int i=0 ; i<10 ; i++ )
        {
            if( a.charAt(i)==(char)0 ) break;
            name.append(a.charAt(i));
        }
        // System.out.println(a);
        String Name = name.toString();
        while( Name.length()<20 ) Name+=" ";
        String created = getFormatted(a.substring(10,22));
        while( created.length()<30 ) created+=" ";
        String modified = getFormatted(a.substring(22,34));
        while( modified.length()<31 ) modified+=" ";
        String size = String.valueOf(128*basetodec(getDataFromFileEntry(ind, offsize, 3)));
        while( size.length()<12 ) size+=" ";
        int p = (int)getDataFromFileEntry(ind, offpermi, 1).charAt(0)-'0';
        StringBuilder permissions = new StringBuilder();
        while( p!=0 )
        {
            permissions.append(p%2);
            p/=2;
        }
        for( int i=0 ; permissions.length()<3 ; i++ ) permissions.append('0');
        return Name+created+modified+size+permissions.reverse();
    }

    static String getFormatted( String date1 )
    {
        // System.out.println(date1);
        if (date1.length() < 12) {
            throw new IllegalArgumentException("Date string is too short");
        }
        String res="";
        String[] month={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String day = date1.substring(0, 2);
        String mon = month[Integer.parseInt(date1.substring(2, 4))-1];
        String year = date1.substring(4, 8);
        String hour = date1.substring(8, 10);
        String minute = date1.substring(10, 12);
        res+=mon+" "+day+" "+year+" "+hour+":"+minute;
        //17 12 2024 04 28
        // String mon = month[Integer.parseInt(date1.substring(2, 4))];
        // res+=mon+" ";
        // res+=date1.substring(0,2)+" "+date1.substring(4,8)+" ";
        // res+=date1.substring(8,10)+":"+date1.substring(10,12);
        return res;
    }

    static void rename( String oldName, String newName ) throws IOException
    {
        int ind = index.get(oldName);
        index.remove(oldName);
        index.put(newName, ind);
        StringBuilder sb = new StringBuilder();
        for( int i=0 ; i<offcreated ; i++ )
            sb.append((char)0);
        int address = getAddressFromFileEntry(index.get(newName));
        file.seek(address);
        file.write(sb.toString().getBytes());
        file.seek(address);
        file.write(newName.getBytes());
    }

    static char permi() throws IOException
    {
        char temp;
        String p="";
        System.out.println("Read Only Permissions? y/n \t");
        temp=br.readLine().charAt(0);
        while( temp!='y' && temp!='n' )
        {
            System.out.println("Try Again...");
            System.out.println("Read Permissions? y/n \t");
            temp=br.readLine().charAt(0);
        }
        if( temp=='y' ) p+='1';
        else p+='0';

        System.out.println("Read/Write Permissions? y/n \t");
        temp=br.readLine().charAt(0);
        while( temp!='y' && temp!='n' )
        {
            System.out.println("Try Again...");
            System.out.println("Read/Write Permissions? y/n \t");
            temp=br.readLine().charAt(0);
        }
        if( temp=='y' ) p+='1';
        else p+='0';

        System.out.println("Execute Permissions? y/n\t");
        try {
            temp=br.readLine().charAt(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while( temp!='y' && temp!='n' )
        {
            System.out.println("Try Again...");
            System.out.println("Execute Permissions? y/n\t");
            temp=br.readLine().charAt(0);
        }
        if( temp=='y' ) p+='1';
        else p+='0';

        int p1 = 4*(p.charAt(0)-'0')+2*(p.charAt(1)-'0')+(p.charAt(2)-'0');
        return (char)(p1+'0');
    }

    static char permiCompress() throws IOException
    {
        char temp;
        String p="";
        temp='y';
        if( temp=='y' ) p+='1';
        else p+='0';
        temp='y';
        if( temp=='y' ) p+='1';
        else p+='0';
        temp='y';
        if( temp=='y' ) p+='1';
        else p+='0';
        int p1 = 4*(p.charAt(0)-'0')+2*(p.charAt(1)-'0')+(p.charAt(2)-'0');
        return (char)(p1+'0');
    }

    static void changePermissions( String name ) throws IOException
    {
        int ind = index.get(name);
        char p = permi();
        int address = getAddressFromFileEntry(ind);
        file.seek(address+offpermi);
        file.write((p+"").getBytes());
    }

    static void delete( String name ) throws Exception
    {
        int ind=index.get(name);
        index.remove(name);
        pqfE.add(ind);
        int address=getAddressFromFileEntry(ind);
        byte[] b = new byte[3];
        file.seek(address+offfirstcluster);
        file.read(b, 0, 3);
        String a = byteToString(b);
        int curr=basetodec(a);

        int next = getNextCluster(curr);
        file.seek(getAddressFromfat(curr));
        file.write(((char)(0)+""+(char)(0)+""+(char)(0)).getBytes());
        pqfat.add(curr);
        while( next!=(totalfat+1) )
        {
            curr=next;
            next = getNextCluster(next);
            file.seek(getAddressFromfat(curr));
            file.write(((char)(0)+""+(char)(0)+""+(char)(0)).getBytes());
            pqfat.add(curr);
        }
        file.seek(address);
        StringBuilder sb=new StringBuilder();
        for( int i=0 ; i<sizeOfFileEntry ; i++ )
            sb.append((char)0);
        file.write(sb.toString().getBytes());
    }

    static void appendFile(  String name, byte[] toBeAppended ) throws Exception
    {
        int ind = index.get(name);
        int firstCluster = basetodec(getDataFromFileEntry(ind, offfirstcluster, 3));
        int next = getNextCluster(firstCluster);
        int endOfFile = basetodec(getDataFromFileEntry(ind, offEnd, 1));
        if( next==(totalfat+1) )
        {
            int addressData = getDataAddress(firstCluster);
            file.seek(addressData+endOfFile);
            int noOfBytesToBeWritten = Math.min(clustersize-endOfFile, toBeAppended.length);
            file.write(toBeAppended, 0, noOfBytesToBeWritten);

            if( noOfBytesToBeWritten==toBeAppended.length )
            {
                setEndOfFile(ind, endOfFile+noOfBytesToBeWritten);
            }
            else
            {
                next = pqfat.poll();

                setSizeOfFile(ind,1,"add");

                setFATEntry(firstCluster, next);

                setFATEntry(next, totalfat+1);

                append( next, toBeAppended, noOfBytesToBeWritten, -1, ind );
            }
        }
        else
        {
            append( next, toBeAppended, 0, endOfFile, ind );
        }
    }

    static void append( int curr, byte[] toBeAppended, int offsetOfString, int endOfFile, int ind ) throws Exception
    {
        if( endOfFile<0 )
        {
            int addressData = getDataAddress(curr);
            int noOfBytesToBeWritten = Math.min(clustersize, toBeAppended.length-offsetOfString);
            file.seek(addressData);
            file.write(toBeAppended, offsetOfString, noOfBytesToBeWritten);
            offsetOfString+=noOfBytesToBeWritten;
            if( offsetOfString==toBeAppended.length )
            {
                setEndOfFile( ind, noOfBytesToBeWritten );
            }
            else
            {
                int next = pqfat.poll();
                setSizeOfFile(ind,1,"add");
                setFATEntry(curr,next);
                setFATEntry(next,totalfat+1);
                append( next, toBeAppended, offsetOfString, endOfFile, ind );
            }
        }
        else
        {
            int next = getNextCluster(curr);
            if( next==(totalfat+1) )
            {
                int addressData = getDataAddress(curr);
                file.seek(addressData+endOfFile);
                int noOfBytesToBeWritten = Math.min(clustersize-endOfFile, toBeAppended.length);
                file.write(toBeAppended, 0, noOfBytesToBeWritten);
                offsetOfString+=noOfBytesToBeWritten;
                if( offsetOfString==toBeAppended.length )
                {
                    setEndOfFile(ind, noOfBytesToBeWritten+endOfFile);
                }
                else
                {
                    next = pqfat.poll();
                    setSizeOfFile(ind,1,"add");
                    setFATEntry(curr, next);
                    setFATEntry(next, totalfat+1);
                    append(next,toBeAppended,noOfBytesToBeWritten,-1,ind);
                }
            }
            else
            {
                append(next,toBeAppended,offsetOfString,endOfFile,ind);
            }
        }
    }

    static void setSizeOfFile( int ind, int size, String op ) throws Exception
    {
        int address = getAddressFromFileEntry(ind);
        if( op.equals("add") )
        {
            int oldsize = basetodec(getDataFromFileEntry(ind, offsize, 3));
            file.seek(address+offsize);
            file.write(regulate(decTo128(oldsize+size), 3).getBytes());
        }
        else
        {
            file.write(regulate(decTo128(size), 3).getBytes());
        }
    }

    static void setFATEntry( int curr, int next ) throws Exception
    {
        int addressFAT = getAddressFromfat(curr);
        file.seek(addressFAT);
        file.write(regulate(decTo128(next),3).getBytes());
        return;
    }

    static void setEndOfFile( int ind, int endOfFile ) throws Exception
    {
        int address = getAddressFromFileEntry(ind);
        file.seek(address+offEnd);
        file.write(regulate(decTo128(endOfFile), 1).getBytes());
        return;
    }

    static void readFile( String name, StringBuilder con ) throws Exception
    {
        int ind = index.get(name);
        int firstCluster = basetodec(getDataFromFileEntry(ind,offfirstcluster,3));
        int endOfFile = basetodec(getDataFromFileEntry(ind, offEnd, 1));
        read( firstCluster, con, endOfFile );
    }

    static void read( int in, StringBuilder con, int endOfFile ) throws Exception
    {
        int next = getNextCluster(in);
        if( next!=(totalfat+1) )
        {
            byte[] b = new byte[clustersize];
            file.seek(getDataAddress(in));
            file.read(b);
            String a = byteToString(b);
            con.append(a);
            read( next, con, endOfFile );
        }
        else
        {
            if( endOfFile==0 ) return;
            byte[] b = new byte[endOfFile];
            file.seek(getDataAddress(in));
            file.read(b);
            String a = byteToString(b);
            con.append(a);
        }
    }

    static int getDataAddress( int clusterNo )
    {
        return diskDataStartingPoint+(clusterNo-1)*clustersize;
    }



    static int getNextCluster( int n ) throws Exception
    {
        int address1 = getAddressFromfat(n);
        byte[] b = new byte[3];
        file.seek(address1);
        file.read(b);
        String a = byteToString(b);
        return basetodec(a);
    }

    static int createFile( String name, char permissions ) throws Exception
    {

        if( name.length()>10 || (int)permissions>55 || pqfE.size()==0 || pqfat.size()==0 ) return -1;

        int first = pqfE.poll();
        index.put(name, first);
        int firstfat=pqfat.poll();
        int address=getAddressFromFileEntry(first);

        file.seek(address);
        file.write(name.getBytes());

        file.seek(address+offcreated);
        file.write(getCurrDateAndTime().getBytes());

        file.seek(address+offmod);
        file.write(getCurrDateAndTime().getBytes());

        file.seek(address+offsize);
        file.write(regulate(decTo128(1),3).getBytes());

        file.seek(address+offpermi);
        file.write((permissions+"").getBytes());

        file.seek(address+offfirstcluster);
        file.write(regulate(decTo128(firstfat),3).getBytes());

        setFATEntry(firstfat, totalfat+1);

        file.seek(address+offEnd);
        file.write(decTo128(0).getBytes());
        return 1;
    }

    static String byteToString( byte[] b )
    {
        String s="";
        for( int i=0 ; i<b.length ; i++ )
            s+=(char)b[i];
        return s;
    }

    static int getAddressFromFileEntry( int i )
    {
        return sizeOfFileEntry*(i-1);
    }

    static int getAddressFromfat( int i )
    {
        return offFAT+sizeOfFATentry*(i-1);
    }

    static String decTo128( int n )
    {
        StringBuilder sb = new StringBuilder();
        while( n>0 )
        {
            int rem = n%128;
            sb.append((char)rem);
            n/=128;
        }
        return sb.reverse().toString();
    }

    static int basetodec( String s128 )
    {
        int p=1;int res=0;
        for( int i=s128.length()-1 ; i>=0 ; i-- )
        {
            res += p*(int)(s128.charAt(i));
            p*=128;
        }
        return res;
    }

    static String regulate( String s, int n )
    {
        String res="";
        for( int i=0 ; i+s.length()<n ; i++ )
            res+=(char)0;
        res+=s;
        return res;
    }

    static String getCurrDateAndTime()
    {
        Date d = new Date();
        String[] dt = d.toString().split(" ");
        String res = "";
        TreeMap<String,String> tm = new TreeMap();
        tm.put("Jan", "01");    	
        tm.put("Feb", "02");    	
        tm.put("Mar", "03");    	
        tm.put("Apr", "04");
        tm.put("May", "05");    	
        tm.put("Jun", "06");    	
        tm.put("Jul", "07");    	
        tm.put("Aug", "08");
        tm.put("Sep", "09");    	
        tm.put("Oct", "10");    	
        tm.put("Nov", "11");    	
        tm.put("Dec", "12");
        res=dt[2]+tm.get(dt[1])+dt[5]+dt[3].charAt(0)+dt[3].charAt(1)+dt[3].charAt(3)+dt[3].charAt(4);
        // System.out.println(res);
        return res;
    }
}
