package yedi.examples.greeter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import yedi.core.BeanContainer;

public class Greeter {

    private String message;
    private Hello greeting;
    private int firstNumber;
    private int secondNumber;
    private byte someByte;
    private float someFloat;
    private Forecast forecast;
    private Calendar currentDate;
    private List<String> someStrings;

    /**
     * @param args
     */
    public static void main(String[] args) {
        BeanContainer container =  new BeanContainer("resources/yedi-config.yaml");
        Greeter greeter = container.getBean("greeter");
        greeter.greet();
    }

    private Greeter(byte someByte, float someFloat) {
        super();
        this.someByte = someByte;
        this.someFloat = someFloat;
    }

    private Greeter(byte someByte, float someFloat, Forecast forecast) {
        super();
        this.someByte = someByte;
        this.someFloat = someFloat;
        this.forecast = forecast;
    }

    public void greet() {
        System.out.println(message);
        System.out.println(greeting.sayHello());
        System.out.println(someStrings.get(0) + (firstNumber + secondNumber));
        System.out.println(someStrings.get(1) + currentDate.getTime());
        System.out.println("");
        System.out.println(someStrings.get(2));
        System.out.println("---------------------------");
        System.out.println("Temperature: " + forecast.getTemperature());
        System.out.println("Wind speed: " + forecast.getWindSpeed());
        System.out.println("Forecast: " + forecast.getDescription());
        System.out.println(someByte);
        System.out.println(someFloat);
    }

    public void setCurrentDate(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        this.currentDate = calendar;
    }



}
