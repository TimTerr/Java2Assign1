import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer{
    List<Movies> movies = new ArrayList<>();
    public MovieAnalyzer(String dataset_past) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dataset_past, Charset.forName("utf8")));
        String str;
        List<String> header = Stream.of(br.readLine().split(",")).collect(Collectors.toList());
        while((str = br.readLine()) != null){
            List<String> row;
            if((row = divide(str)) == null) break;
            else {
                Map<String, String> m = new HashMap<>();
                for (int i = 1; i < header.size(); i++) {
                    //if(row.get(i) == null) System.out.println("null");
                    m.put(header.get(i), row.get(i));
                }
                this.movies.add(new Movies(m));
            }
        }
//        for (Movies m: movies) {
//            System.out.println(movies.size());
//        }
//        FileInputStream input = new FileInputStream(dataset_past);
//        int n = 0;
//        ArrayList<Byte> m = new ArrayList<>();
//        while((n = input.read()) != 1){
//            m.add((byte)n);
//        }
//        System.out.println(m);
        //char[] chars = new char[Integer.MAX_VALUE];
        //long n = input.read(chars);
        //System.out.println(input);
        //System.out.println(0x10454);
    }
    public Map<Integer, Integer> getMovieCountByYear(){return null; }
    //这个用来分割字符串
    public List<String> divide (String str){
        StringBuilder sb = new StringBuilder(str);
        List<String> s = new ArrayList<>();
        for (int i = 0; i < sb.length(); i++) {
            int begin, end;
            if(sb.charAt(i) == '"'){
                begin = i;i++;
                while(sb.charAt(i)!='"') i++;
                end = i;
                s.add(sb.substring(begin, end+1));
                //System.out.println(sb.substring(begin, end+1));
            }
            else if(sb.charAt(i) == ','){
                if(i == sb.length()-1){
                    s = null;
                    break;
                }
                i++;begin = i;
                if(sb.charAt(i) == '"'){
                    i++;
                    while(sb.charAt(i)!='"') i++;
                    end = i;
                    s.add(sb.substring(begin, end+1));
                    //System.out.println(sb.substring(begin, end+1));
                    continue;
                }
                while(sb.charAt(i) != ',')i++;
                i--;end = i;
                s.add(sb.substring(begin, end+1));
                //System.out.println(sb.substring(begin, end+1));
            }
        }
        return s;
    }

    public static void main(String[] args) throws IOException{
        new MovieAnalyzer("resources/imdb_top_500.csv");
    }
}
class Movies{
    Map<String, String> map;
    public Movies(Map<String, String> m){
        this.map = m;
        //System.out.println(map);
    }
}