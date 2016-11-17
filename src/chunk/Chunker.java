package chunk;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Chunker {

    int chunkSize;
    public Chunker() {
        this.chunkSize=50;
    }
    public Chunker(int chunkSize) {
        this.chunkSize=chunkSize;
    }
    public void enCode(InputStream is,OutputStream os) throws IOException {
        int r;
        byte[] buf = new byte[chunkSize];
        do{
            r=is.read(buf,0,buf.length);
            byte[] head = (Integer.toHexString(r)+"\r\n").getBytes();
            if(r>0){
                os.write(head);
                os.write(buf,0,r);
                os.write(("\r\n").getBytes());
            }
        }while (r>0);
        os.write(("0\r\n\r\n").getBytes());
        System.out.println("File chunked");
    }

    public void deCode(InputStream is,OutputStream os) throws IOException {

//  если знать что приходящий файл chunked и у него должен быть chunkSize, то можно сделать так:
         int r;
         int chunkINT;// размер блока на который разбивается файл
//берём максимально взможный размер числа INT для определеня размера блока(chunksize),то есть 8 байт и + 2 байта(\r\n)
              byte[] buf = new byte[10];
              is.read(buf,0,buf.length);
// определяем размер блоков (chunksize) прерводим в INT
        chunkINT = ChunkInt(buf);

// получаем сколько байт занимает  размер блока(chunksize)+конец строки(\r\n)
        byte[] headsz = (Integer.toHexString(chunkINT)+"\r\n").getBytes();
        int headsize = headsz.length;

// определяем размер следующих блоков для скачивания из стрима Заголовок+chunksize+конец строки(\r\n)
         int tempsize = headsize+chunkINT+2;
         byte[]temp = new byte[tempsize];

// дописываем остаток раннее скачанного кусочка (строка 80)
// тут есть варианты-(это при условии что массив buf =10) если  tempsize<buf, то вариант1  если нет вариант 2

        if(buf.length-headsize-chunkINT<=0){
//вариант 2
            os.write(buf,headsize, buf.length-headsize );
            // дописываем остаток первой записи до полного размера массива temp
            int u=tempsize-buf.length;// сколько байт недостаёт
            buf=new byte[u];

            is.read(buf,0,u);
            os.write(buf,0,u-2);
        }else
/*вариант1 */
        {
            os.write(buf, headsize, chunkINT);
            int u=buf.length-headsize-chunkINT;// на сколько байт в buf не вычитано до завершения полного блока
            switch (u){
                case 1: //6
                    buf = new byte[u];
                    is.read(buf,0,u);
                    os.write(buf,0,u-1);
                    break;
                case 2: //  длинна блока -5
                    // первый блок записан полностью можно начинать с нового
                    break;
                case 3: //длинна блока -4
                    is.read(buf,0,buf.length-2);
                    os.write(buf,2,chunkINT);
                    break;
                case 4: //длинна блока -3
                    is.read(buf,0,buf.length-4);
                    os.write(buf,1,chunkINT);
                    break;
                case 5: //длинна блока -2
                    is.read(buf,0,buf.length-6);
                    os.write(buf,0,chunkINT);
                    break;
                case 6: //длинна блока -1
                    os.write(buf,9,1);
                    is.read(buf,0,buf.length-2);
                    os.write(buf,5,chunkINT);
                    break;
                default:
            }
        }
// пишем весь файл скачивая полным определённым размером массива temp
        do{
           r = is.read(temp, 0, temp.length);
               if (r > 0) {
                   // сколько записать из последнего куска с учётом - "0\r\n\r\n"
                   int z=ChunkInt(temp);//количество байтов из файла предназначенных записи без концов строки и прочего
                    if(z<chunkINT) {// в этом случае понятно что это последний кусок
                       os.write(temp,headsize,z);
/*и есть вероятность когда при определённом chunksize и коком-то размере файла запись "хвостика"+"0\r\n\r\n" происходит
таким образом, что при декодировании несколько байт не помещаются в temp.length и в потоке остаются байты 13\10 13\10,
тогда при попытке получить chunkINT для этого блока имеем ошибку NumberFormatException так как в этом блоке-остатке
нет headsize, поэтому делаем брэйк */
                        break;
                    }else {
                       os.write(temp, headsize, chunkINT );
                   }
               }
        }while (r>0);
        System.out.println("File DEchunked");
    }
// определяем размер блоков (chunksize) прерводим в INT
    private int ChunkInt(byte[]a){
        for (int i = 0; i <a.length ; i++) {
            if (a[i] == (byte) (13) && (a[i + 1] == (byte) (10))) {
                String chunk = new String(a, 0, i);
                String f= "0x"+chunk;
              int  chkINT = Integer.decode(f);
                return chkINT;
            }
        }
        return 0;
    }
/*
Ещё вариант  - Сразу скачиваю весь файл в массив, потом с ним работаю
*/
    public  void deCode2(InputStream is,OutputStream os) throws IOException {
        ByteArrayOutputStream bos= new ByteArrayOutputStream();
        int r, len;
        do{
            len=is.available();
            byte  buf[]= new byte[len];
            r=is.read(buf,0,buf.length);
            if(r>0)
               try {
                    bos.write(buf);
                    }catch (IOException e){  }
        }while (r>0);
// весь файл здесь
        byte[] res= bos.toByteArray();
// получаю размер полных блоков
        int chunkInt = ChunkInt(res);
        byte[] headsz = (Integer.toHexString(chunkInt)+"\r\n").getBytes();
        int headsize = headsz.length;
// сколько полных блоков в файле
        int bit=(res.length-5)/(chunkInt+2+headsize);
// с какой позиции начинать писать в файл
        int k=headsize;
        for (int i = 0; i <bit; i++) {
            os.write(res,(k),chunkInt);
            k=k+chunkInt+2+headsize;// передвигаемся к месту следующего начала записи
        }
// дописываю хвостик
            byte[] ost =Arrays.copyOfRange(res,k-headsize,res.length);
// определяю длинну хвостика
            int rest = ChunkInt(ost);
            os.write(res,k,rest);
        System.out.println("File DEchunked");
    }

}
