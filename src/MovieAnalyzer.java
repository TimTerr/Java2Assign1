import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer{
    List<Movies> movies = new ArrayList<>();
    public MovieAnalyzer(String dataset_past) throws IOException {
        int count = 0;
        BufferedReader br = new BufferedReader(new FileReader(dataset_past, Charset.forName("utf8")));
        String str;
        List<String> header = Stream.of(br.readLine().split(",")).collect(Collectors.toList());count++;
        //System.out.println(header);
        while((str = br.readLine()) != null){
            //System.out.println(str);
            List<String> row;count++;
            //如果有空缺问题（逗号结尾）
            if((row = divide(str)) == null) {
                //System.out.println(count);
                continue;
            }
            else {
                Map<String, String> m = new HashMap<>();
                for (int i = 1; i < header.size(); i++) {
                    //if(row.get(i) == null) System.out.println("null");
                    m.put(header.get(i), row.get(i));
                }
                this.movies.add(new Movies(m));
            }
        }
    }
    public Map<Integer, Integer> getMovieCountByYear(){
        Map<Integer, Integer> map = new TreeMap<>(Comparator.reverseOrder());
        movies.stream().collect(Collectors.groupingBy(Movies::getReleased_Year, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(e-> map.put(e.getKey(), e.getValue().intValue()));

        //System.out.println(map);
        return map;
    }
    /*****这个用来分割字符串*****/
    public List<String> divide (String str){
        StringBuilder sb = new StringBuilder(str);
        List<String> s = new ArrayList<>();
        for (int i = 0; i < sb.length(); i++) {
            int begin, end;
            //如果有引号就要一直读到引号+逗号结束结束
            if(sb.charAt(i) == '"'){
                begin = i;
                i = checkDoubleQuotationMarks(sb, i, 1);
                end = i;
                s.add(sb.substring(begin, end+1));
                System.out.println(sb.substring(begin, end+1));
            }
            //如果是逗号，读到下一个逗号结束（注意逗号后面是否有引号）
            else if(sb.charAt(i) == ','){
                if(i != sb.length()-1){
                    i++;
                    //如果逗号后面引号开头
                    if(sb.charAt(i) == '"'){
                        begin = i;
                        i = checkDoubleQuotationMarks(sb, i, 1);
                        end = i;
                        s.add(sb.substring(begin, end+1));
                        System.out.println(sb.substring(begin, end+1));
                    }
                    //如果没有引号，后面也不会出现引号了，正常读
                    else{
                        begin = i;
                        while(sb.charAt(i) != ',')i++;
                        i--;end = i;
                        s.add(sb.substring(begin, end+1));
                        System.out.println(sb.substring(begin, end+1));
                    }
                }
                //如果逗号在结尾说明内容是错误的
                else {
                    s = null;
                    break;
                }
            }
        }
        return s;
    }
    //begin开始于引号出现处
    public int checkDoubleQuotationMarks(StringBuilder stringBuilder, int begin, int count){
        //count为单数时代表之前只识别了单数个引号；双数同理
        int end = begin;
        end++;
        //循环到出现引号或者到达文档尾
        while (stringBuilder.charAt(end) != '"' && end < stringBuilder.length()-1) end++;
        //当第一次出现引号的时候，判断是否出现新的引用，注意读到末尾的情况
        if(end < stringBuilder.length()-1){
            //如果引号后面是逗号，count单数时表示此时识别到的引号为双数结尾，如果逗号出现则可以退出
            if(stringBuilder.charAt(end+1) == ','){
                if (count%2 != 0) {
                    return end;
                }
                //如果count双数表示此时识别到的引号为单数开始，应该继续向下识别
                else{
                    return checkDoubleQuotationMarks(stringBuilder, end, count+1);
                }
            }
            //如果引号后面还是是引号，如果count单数时表示最后的引号是单数开始，从此引号开始接着往下识别
            else if(stringBuilder.charAt(end+1) == '"'){
                if(count%2 != 0) {
                    return checkDoubleQuotationMarks(stringBuilder, end+1, count+2);
                }
                //如果count双数表示最后的引号是双数结尾，这个时候引用应该结束了，但是为了以防万一我们还是从最后第二个引号开始重新读一下
                else{
                    return checkDoubleQuotationMarks(stringBuilder, end, count+1);
                }
            }
            //如果引号后面是其他内容，说明还要继续读
            else {
                return checkDoubleQuotationMarks(stringBuilder, end, count+1);
            }
        }
        //如果end出现在文档末尾了，直接返回end
        else return end;
    }

    public static void main(String[] args) throws IOException{
        new MovieAnalyzer("resources/imdb_top_500.csv").getMovieCountByYear();
    }
}
class Movies{
    Map<String, String> map;
    String Series_Title, Certificate, Runtime, Genre,  Overview, Meta_score, Director, Star1, Star2, Star3, Star4, No_of_Votes, Gross;
    int Released_Year;
    float IMDB_Rating;
    public Movies(Map<String, String> m){
        this.map = m;
        this.Series_Title = map.get("Series_Title");
        this.Released_Year = Integer.parseInt(map.get("Released_Year"));
        this.Certificate = map.get("Certificate");
        this.Runtime = map.get("Runtime");
        this.Genre = map.get("Genre");
        this.IMDB_Rating = Float.parseFloat(map.get("IMDB_Rating"));
        this.Overview = map.get("Overview");
        this.Meta_score = map.get("Meta_score");
        this.Director = map.get("Director");
        this.Star1 = map.get("Star1");
        this.Star2 = map.get("Star2");
        this.Star3 = map.get("Star3");
        this.Star4 = map.get("Star4");
        this.No_of_Votes = map.get("No_of_Votes");
        this.Gross = map.get("Gross");
    }
    public int getReleased_Year(){
        return this.Released_Year;
    }
}