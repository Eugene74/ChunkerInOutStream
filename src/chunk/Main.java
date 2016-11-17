package chunk;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        File file=new File("C:\\Users\\Admin\\Desktop\\я\\каталог1 тренировочный\\chunkdechunk.txt");
        File file2=new File("C:\\Users\\Admin\\Desktop\\я\\каталог1 тренировочный\\chunk.txt");
        File file3=new File("C:\\Users\\Admin\\Desktop\\я\\каталог1 тренировочный\\dechunk.txt");

       int chunkSize=2;
        Chunker chunker = new Chunker(chunkSize);

        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file2);
        InputStream is = fis;
        OutputStream os = fos;
        chunker.enCode(is,os);
        os.flush();

        is = new FileInputStream(file2);
        os = new FileOutputStream(file3);
        //вариант 1 : пошаговая запись- скачал в буфер кусок - записал в файл нужную часть
       // chunker.deCode(is,os);
        // ИЛИ
        // вариант 2 : через полное скачивание в массив и потом работа с ним
        chunker.deCode2(is, os);
        os.flush();


    }

}
