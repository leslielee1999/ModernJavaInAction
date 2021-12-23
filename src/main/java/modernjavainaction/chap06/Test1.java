package modernjavainaction.chap06;

import java.util.*;
import java.util.stream.Stream;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static modernjavainaction.chap06.Dish.menu;

public class Test1 {
    public static void main(String[] args) {
        //  更为常见的需求：传递其它 Collector 给 groupingBy 以对分组后的数据进行其它处理，而非又一个 groupingBy
        Map<Dish.Type, Long> typesCount = menu.stream().collect(
                groupingBy(Dish::getType, counting())
        );
        System.out.println(typesCount);

        //  注意：groupingBy(f)，实际上是 groupingBy(f, toList()) 的语法糖

    }

    /**
     * 对流进行 grouping
     *
     */

    /*  基本的分组需求 */
    public void testGrouping(){
        //  可使用方法引用的分组操作
        Map<Dish.Type, List<Dish>> dishesByType =
                menu.stream().collect(groupingBy(Dish::getType));
        System.out.println(dishesByType);

        //  复杂的分组需求，无提供相关方法，需通过 lambda 表达式来表达逻辑
        Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(
                groupingBy(dish -> {
                    if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                    else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                    else return CaloricLevel.FAT;
                }));
        System.out.println(dishesByCaloricLevel);
    }
    public enum CaloricLevel {DIET, NORMAL, FAT}

    /*  存在对分组的每个元素进行处理的需求 */
    public void handleGrouping(){
        //  对分组后每个组的元素进行
        //  1. 过滤
        //  先过滤掉不符合要求的元素，再进行分组
        Map<Dish.Type, List<Dish>> caloricDishesByType1 =
                menu.stream().filter(dish -> dish.getCalories() > 500).collect(groupingBy(Dish::getType));
        System.out.println(caloricDishesByType1);    //  {MEAT=[pork, beef], OTHER=[french fries, pizza]}

        //  先分组，再过滤（since java9）
        //  Map<Dish.Type, List<Dish>> caloricDishesByType2 =
        //          menu.stream().collect(groupingBy(Dish::getType, filtering(dish -> dish.getCalories() > 500, toList())));
        //  System.out.println(caloricDishesByType2);    //  {OTHER=[french fries, pizza], MEAT=[pork, beef], FISH=[]}

        //  2.  更为常用的对分组数据的处理操作：mapping
        //  这里得到的 value 是一个 List<String>
        Map<Dish.Type, List<String>> caloricDishesByType3 =
                menu.stream().collect(groupingBy(Dish::getType, mapping(Dish::getName, toList())));
        System.out.println(caloricDishesByType3);

        //  3. flatMapping（since java9）
        //  应用：可提取每个分类中的标签
        //  toSet：防止标签重复
        Map<String, List<String>> dishTags = new HashMap<>();
        dishTags.put("pork", asList("greasy", "salty"));
        dishTags.put("beef", asList("salty", "roasted"));
        dishTags.put("chicken", asList("fried", "crisp"));
        dishTags.put("french fries", asList("greasy", "fried"));
        dishTags.put("rice", asList("light", "natural"));
        dishTags.put("season fruit", asList("fresh", "natural"));
        dishTags.put("pizza", asList("tasty", "salty"));
        dishTags.put("prawns", asList("tasty", "roasted"));
        dishTags.put("salmon", asList("delicious", "fresh"));

        // Map<Dish.Type, Set<String>> dishNamesByType =
        //         menu.stream()
        //                 .collect(groupingBy(Dish::getType,
        //                         flatMapping(dish -> dishTags.get(dish.getName()).stream(),
        //                                 toSet())));
        //
        // System.out.println(dishNamesByType);
        // {MEAT=[salty, greasy, roasted, fried, crisp], FISH=[roasted, tasty, fresh,
        //         delicious], OTHER=[salty, greasy, natural, light, tasty, fresh, fried]}
    }

    /*  多级分类  */
    public void multilevelGrouping(){
        Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = menu.stream().collect(
                groupingBy(Dish::getType,
                        groupingBy(dish -> {
                            if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                            else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                            else return CaloricLevel.FAT;
                        }))
        );
        System.out.println(dishesByTypeCaloricLevel);
        //  {OTHER={NORMAL=[french fries, pizza], DIET=[rice, season fruit]}, FISH={NORMAL=[salmon], DIET=[prawns]}, MEAT={FAT=[pork], NORMAL=[beef], DIET=[chicken]}}
    }

    //  -----------------------------------------------------------------------------------------------

    /**
     * 对流进行 summarize
     * <br/><br/>
     * 😲 提醒：要使用流操作时，点进去看看源代码是否有初始值，否则需要考虑使用 Optional 容器来保存结果，防止 NPE 的出现
     */

    /*  查找流中的最大值或最小值 */
    public void findMaxOrMinInStream() {
        //  需要提供一个比较器
        Comparator<Dish> dishCaloriesComparator = Comparator.comparing(Dish::getCalories);
        //  查找最大：maxBy
        //  查找最小：minBy
        //  传入参数：自定义比较器
        Optional<Dish> mostCalorieDish = menu
                .stream()
                .collect(maxBy(dishCaloriesComparator));
    }

    /*  计算流中的累加和、平均数  */
    public void calSumOrAvg() {
        //  根据要进行处理的属性类型的不同，存在 summmingInt、summingLong 和 summingDouble 三种类型的累加操作
        int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
        //  同样的，存在 averagingInt、averagingLong 和 averagingDouble 三种不同类型的求平均数操作
        double avgCalories = menu.stream().collect(averagingInt(Dish::getCalories));
    }

    /*  一次操作，返回流中的元素个数、最大值、最小值、累加和、平均数  */
    public void summarizeTest() {
        //  同样的，存在 summarizingLong、summarizingDouble
        //  同样的，存在 LongSummaryStatistics、DoubleSummaryStatistics
        IntSummaryStatistics menuStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
    }

    /*  对流中某个 String 类型的属性进行连接  */
    public void joiningTest() {
        //  内部原理：使用 StringBuilder 进行拼接
        String shortMenu = menu.stream().map(Dish::getName).collect(joining());
        System.out.println(shortMenu);
        //  对元素进行分割符的连接
        String shortMenuV2 = menu.stream().map(Dish::getName).collect(joining(", "));
        System.out.println(shortMenuV2);
    }

    /*  广义的规约操作  */
    public void reductionTest() {
        //  参数 1 ：唯一标识/流中无元素时的返回值（此时也称 reducing 为恒等函数，指将输入参数作为返回值的函数）
        //  参数 2 ：属性映射方法
        //  参数 3 ：流操作
        //  返回值：收集器
        int totalCalories = menu.stream().collect(reducing(
                0, Dish::getCalories, (i, j) -> i + j
        ));
        System.out.println(totalCalories);
        //  ps：java.util.stream.Collectors.counting 方法，也是利用这个三参数的方法，将流元素映射为 1 ，然后相加，从而得到流元素个数

        //  单参数的 reducing 没有初始值，故可能返回空
        Optional<Dish> mostCalorieDish = menu
                .stream()
                .collect(reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));
        System.out.println(mostCalorieDish);
    }

    /*  初识：collect 与 reduce 的区别 */
    public void collectVSreduce() {
        //  reduce 虽然能实现和 collect 相同的功能，但二者存在不同，前者会创建一个新的容器去覆盖原有的，后者则是直接去修改原有容器的值
        //  所以 reduce 是线程不安全的，必须得保证同一时间点只能分配一个新的 List，所以它的性能会差于 collect（一个是分配对象多，另一个是无法并发）（针对于规约操作，且容器的值可变的情况）

        //  用 reduce 实现 collect.toList() 的功能
        Stream<Integer> stream = asList(1, 2, 3, 4, 5, 6).stream();
        List<Integer> list = stream.reduce(new ArrayList<Integer>(), (List<Integer> l, Integer e) -> {
            l.add(e);
            return l;
        }, (List<Integer> l1, List<Integer> l2) -> {
            l1.addAll(l2);
            return l1;
        });
        System.out.println(list);
    }

    /*  函数式编程中，会出现有多种不同的方式实现同一结果的情况   */
    public void testCollection() {
        //  根据要进行处理的属性类型的不同，存在 summmingInt、summingLong 和 summingDouble 三种类型的累加操作
        int totalCalories1 = menu.stream().collect(summingInt(Dish::getCalories));

        //  用方法引用替代重复的 lambda 表达式：简洁
        int totalCalories2 = menu.stream().collect(reducing(0,
                Dish::getCalories,
                Integer::sum));

        //  这里的 get，实际上是指从 Optional 容器里提取出值（要这样操作，首先你得保证 reduce 后一定是有值，不为空的，所以最好还是通过 orElse 或 orElseGet 来提取值最安全）
        int totalCalories3 = menu.stream().map(Dish::getCalories).reduce(Integer::sum).get();

        //  直接转为 IntStream 来得到最终结果
        int totalCalories4 = menu.stream().mapToInt(Dish::getCalories).sum();

        //  既然有这么多种方式，选择哪种最好呢，总不能每样都来一遍吧，这样可读性和通用性也不好，所以最好就选性能好，且简洁明了的即可，像此处就选择 mapToInt 最好了
        //  只有理清楚每种方式的具体实现和使用方式，才能进行比较并得到结论
    }
}
