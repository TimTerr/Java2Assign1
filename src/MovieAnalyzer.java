import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {
    List<Movies> movies = new ArrayList<>();
    public MovieAnalyzer(String dataset_past) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dataset_past,
                Charset.forName("utf8")));
        String str;
        List<String> header = Stream.of(br.readLine().split(",")).collect(Collectors.toList());
        while ((str = br.readLine()) != null){
            List<String> row;
            row = divide(str);
            Map<String, String> m = new HashMap<>();
            for (int i = 1; i < header.size(); i++) {
                //if(row.get(i) == null) System.out.println("null");
                m.put(header.get(i), row.get(i));
            }
            this.movies.add(new Movies(m));
        }
    }

    /*****Solved*****/
    public Map<Integer, Integer> getMovieCountByYear() {
        Map<Integer, Integer> map = new TreeMap<>(Comparator.reverseOrder());
        movies.stream().collect(Collectors.groupingBy(Movies::getReleased_Year, Collectors.counting()))
                //filter过滤掉了小于等于0的非法输入
                .entrySet().stream().filter(e -> e.getKey() > 0).sorted(Map.Entry
                        .comparingByKey())
                .forEachOrdered(e -> map.put(e.getKey(), e.getValue().intValue()));
        //System.out.println(map);
        return map;
    }

    /*****Solved*****/
    public Map<String, Integer> getMovieCountByGenre() {
        List<String> genre = new ArrayList<>();
        movies.forEach(e -> genre.addAll(e.getGenreByList()));
        Map<String, Integer> map = new LinkedHashMap<>();
        genre.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry
                        .<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEachOrdered(e -> map.put(e.getKey(), e.getValue().intValue()));
        //System.out.println(map);
        return map;
    }

    /*****Solved*****/
    public Map<List<String>, Integer> getCoStarCount() {
        List<List<String>> coStars = new ArrayList<>();
        movies.forEach(e -> {
            List<String> subStars = e.getStars();
            for (int i = 0; i < subStars.size() - 1; i++) {
                int j = i + 1;
                while (j < (subStars).size()) {
                    List<String> l = new ArrayList<>();
                    l.add(subStars.get(i));
                    l.add(subStars.get(j));
                    coStars.add(l);
                    j++;
                }
            }
        });
        for (int i = 0; i < coStars.size() - 1; i++) {
            for (int j = i + 1; j < coStars.size(); j++) {
                List<String> s1 = coStars.get(i);
                List<String> s2 = coStars.get(j);
                if (s1.get(0).equals(s2.get(0)) && s1.get(1).equals(s2.get(1))) {
                    coStars.set(j, coStars.get(i));
                }
            }
        }
        Map<List<String>, Integer> map = new LinkedHashMap<>();
        coStars.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry
                        .<List<String>, Long>comparingByValue().reversed())
                .forEachOrdered(e -> map.put(e.getKey(), e.getValue().intValue()));
        //System.out.println(map);
        return map;
    }

    /*****Solved*****/
    public List<String> getTopMovies(int top_k, String by) {
        List<String> topMovies = new ArrayList<>();
        if (by.equals("runtime")) {
            movies.stream().filter(m -> m.getRuntime() > 0)
                    .sorted(Comparator.comparing(Movies::getRuntime).reversed()
                            .thenComparing(Movies::getSeries_Title))
                    .forEachOrdered(m -> topMovies.add(m.getSeries_Title()));
        } else if (by.equals("overview")) {
            movies.stream().filter(m -> m.getOverviewLength() > 0)
                    .sorted(Comparator.comparing(Movies::getOverviewLength).reversed()
                            .thenComparing(Movies::getSeries_Title))
                    .forEachOrdered(m -> topMovies.add(m.getSeries_Title()));
        }
        //System.out.println(topMovies.subList(0, top_k));
        return topMovies.subList(0, top_k);
    }

    /*****Solved*****/
    public List<String> getTopStars(int top_k, String by) {
        //用Map<String, List<Double/Long>>来存储每个star对应的所有ratings或gross
        Map<String, List<Double>> mRatings = new HashMap<>();
        Map<String, List<Long>> mGross = new HashMap<>();
        //之后用Map<String, Double>来记录每个star对应的average ratings或average gross
        Map<String, Double> map = new HashMap<>();
        List<String> topStars = new ArrayList<>();
        //List<String>存人名
        List<String> stars = new ArrayList<>();
        if (by.equals("rating")) {
            //List<Double>存ratings
            List<Double> ratings = new ArrayList<>();
            movies.stream().forEach(e -> {
                int num = e.getStars().size();
                stars.addAll(e.getStars());
                for (int i = 0; i < num; i++) {
                    ratings.add(e.getRating());
                }
            });
            for (int i = 0; i < stars.size(); i++) {
                //人名之前出现过，则把金额加入到list里
                if (mRatings.containsKey(stars.get(i))) {
                    mRatings.get(stars.get(i)).add(ratings.get(i));
                } else {
                    //如果没出现过，就新建List
                    List<Double> l = new ArrayList<>();
                    l.add(ratings.get(i));
                    mRatings.put(stars.get(i), l);
                }
            }
            mRatings.entrySet().stream().forEach(e -> {
                double n = 0;
                int count = 0;
                for (int i = 0; i < e.getValue().size(); i++) {
                    n += e.getValue().get(i);
                    if (e.getValue().get(i) == 0) {
                        count++;
                    }
                }
                //考虑分母为0（该演员所有输入均不符合的时候）
                if ((e.getValue().size() - count) != 0) {
                    map.put(e.getKey(), n / (e.getValue().size() - count));
                }
            });
        } else if (by.equals("gross")) {
            //List<Long>存gross
            List<Long> gross = new ArrayList<>();
            movies.stream().forEach(e -> {
                int num = e.getStars().size();
                stars.addAll(e.getStars());
                for (int i = 0; i < num; i++) {
                    gross.add(e.getGross());
                }
            });
            for (int i = 0; i < stars.size(); i++) {
                //人名之前出现过，则把金额加入到list里
                if (mGross.containsKey(stars.get(i))) {
                    mGross.get(stars.get(i)).add(gross.get(i));
                } else {
                    //如果没出现过，就新建List
                    List<Long> l = new ArrayList<>();
                    l.add(gross.get(i));
                    mGross.put(stars.get(i), l);
                }
            }
            mGross.entrySet().stream().forEach(e -> {
                double n = 0;
                int count = 0;
                for (int i = 0; i < e.getValue().size(); i++) {
                    n += e.getValue().get(i);
                    if (e.getValue().get(i) == 0) {
                        count++;
                    }
                }
                //考虑分母为0（该演员所有输入均不符合的时候）
                if ((e.getValue().size() - count) != 0) {
                    map.put(e.getKey(), n / (e.getValue().size() - count));
                }
            });
        }
        map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEachOrdered(e -> topStars.add(e.getKey()));
        //System.out.println(topStars.subList(0, top_k));
        return topStars.subList(0, top_k);
    }

    /*****Solved*****/
    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        //Map<String, List<Movies>>存储每个genre对应的所有电影
        Map<String, List<Movies>> genreMovies = new HashMap<>();
        movies.stream().forEach(e -> {
            List<String> genres = e.getGenreByList();
            for (int i = 0; i < genres.size(); i++) {
                //如果genreMovies含有这个genre,那么就添加movies进去
                if (genreMovies.containsKey(genres.get(i))) {
                    genreMovies.get(genres.get(i)).add(e);
                } else {
                    //如果没有，则新建一个List<Movies>
                    List<Movies> l = new ArrayList<>();
                    l.add(e);
                    genreMovies.put(genres.get(i), l);
                }
            }
        });
        //存完以后按照搜索查找
        List<String> list = new ArrayList<>();
        genreMovies.get(genre).stream().sorted(Comparator.comparing(Movies::getSeries_Title))
                .collect(Collectors.filtering(e -> (e.getRating() >= min_rating && e.getRuntime()
                        <= max_runtime && e.getRuntime() > 0), Collectors.toList()))
                .stream().forEachOrdered(e -> list.add(e.getSeries_Title()));
        //System.out.println(list);
        return list;
    }

    /*****这个用来分割字符串*****/
    public List<String> divide(String str) {
        StringBuilder sb = new StringBuilder(str);
        List<String> s = new ArrayList<>();
        //在每一行最后都加上一个逗号，这样所有数据每一段都以逗号结尾。
        sb.append(",");
        int begin = 0;
        int end;
        for (int i = 0; i < sb.length(); i++) {
            //如果有引号就要一直读到引号+逗号结束
            if (sb.charAt(i) == '"') {
                //此时的i是逗号的位置
                i = checkDoubleQuotationMarks(sb, i, 1);
                end = i - 1;
                //引号输出可以把前后引号去了,end是末尾引号的位置
                s.add(sb.substring(begin + 1, end));
                //begin等于逗号后面的位置
                begin = i + 1;
                //System.out.println(sb.substring(begin, end+1));
            } else if (sb.charAt(i) == ',') {
                //读到逗号结束
                end = i;
                s.add(sb.substring(begin, end));
                begin = i + 1;
            }
            //如果既不是引号也不是逗号，就不管
        }
        return s;
    }

    public int checkDoubleQuotationMarks(StringBuilder stringBuilder, int begin, int count) {
        //begin开始于引号出现处，count为单数时代表之前只识别了单数个引号；双数同理
        int end = begin;
        end++;
        //循环到出现引号或者到达文档尾
        while (stringBuilder.charAt(end) != '"' && end < stringBuilder.length() - 1) {
            end++;
        }
        //当第一次出现引号的时候，判断是否出现新的引用，注意读到末尾的情况
        if (end < stringBuilder.length() - 1) {
            //如果引号后面是逗号，count单数时表示此时识别到的引号为双数结尾，如果逗号出现则可以退出
            if (stringBuilder.charAt(end + 1) == ',') {
                if (count % 2 != 0) {
                    return end + 1;
                } else {
                    //如果count双数表示此时识别到的引号为单数开始，应该继续向下识别
                    return checkDoubleQuotationMarks(stringBuilder, end, count + 1);
                }
            } else if (stringBuilder.charAt(end + 1) == '"') {
                //如果引号后面还是是引号，如果count单数时表示最后的引号是单数开始，从此引号开始接着往下识别
                if (count % 2 != 0) {
                    return checkDoubleQuotationMarks(stringBuilder, end + 1, count + 2);
                } else {
                    //如果count双数表示最后的引号是双数结尾，这个时候引用应该结束了，但是为了以防万一我们还是从最后第二个引号开始重新读一下
                    return checkDoubleQuotationMarks(stringBuilder, end, count + 1);
                }
            } else {
                //如果引号后面是其他内容，说明还要继续读
                return checkDoubleQuotationMarks(stringBuilder, end, count + 1);
            }
        } else {
            //如果end出现在文档末尾了，直接返回end
            return end;
        }
    }
}

class Movies {
    Map<String, String> map;
    String Series_Title;
    String Certificate;
    String Runtime;
    String Genre;
    String Overview;
    String Meta_score;
    String Director;
    String Star1;
    String Star2;
    String Star3;
    String Star4;
    String No_of_Votes;
    String Gross;
    int Released_Year;
    float IMDB_Rating;

    public Movies(Map<String, String> m) {
        this.map = m;
        this.Series_Title = map.get("Series_Title");
        //0表示错误的数据
        this.Released_Year = map.get("Released_Year").equals("") ? 0
                : Integer.parseInt(map.get("Released_Year"));
        this.Certificate = map.get("Certificate");
        this.Runtime = map.get("Runtime").equals("") ? "0 min" : map.get("Runtime");
        this.Genre = map.get("Genre");
        this.IMDB_Rating = map.get("IMDB_Rating").equals("") ? 0
                : Float.parseFloat(map.get("IMDB_Rating"));
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

    public String getSeries_Title() {
        return this.Series_Title;
    }

    public int getReleased_Year() {
        return this.Released_Year;
    }

    public List<String> getGenreByList() {
        StringBuilder stringBuilder = new StringBuilder(this.Genre);
        stringBuilder.append(",");
        int begin = 0;
        int end;
        List<String> s = new ArrayList<>();
        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == ',') {
                end = i;
                s.add(stringBuilder.substring(begin, end).replaceAll(" ", ""));
                begin = i + 1;
            }
        }
        return s;
    }

    public List<String> getStars() {
        List<String> l = new ArrayList<>();
        l.add(this.Star1);
        l.add(this.Star2);
        l.add(this.Star3);
        l.add(this.Star4);
        Collections.sort(l);
        return l;
    }

    public int getRuntime() {
        StringBuilder rt = new StringBuilder(this.Runtime);
        rt.delete(rt.length() - 4, rt.length());
        return Integer.parseInt(rt.substring(0));
    }

    public int getOverviewLength() {
        return this.Overview.length();
    }

    public double getRating() {
        return this.IMDB_Rating;
    }

    public long getGross() {
        StringBuilder stringBuilder = new StringBuilder(this.Gross);
        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == ',') {
                stringBuilder.delete(i, i + 1);
            }
        }
        //System.out.println(Long.parseLong(stringBuilder.substring(0)));
        if (stringBuilder.length() == 0) {
            return 0;
        } else {
            return Long.parseLong(stringBuilder.substring(0));
        }
    }
}