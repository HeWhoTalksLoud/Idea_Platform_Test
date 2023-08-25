package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        final String fileName = "tickets.json";
        final String city1 = "Владивосток";
        final String city2 = "Тель-Авив";

        TicketList ticketList;
        List<Ticket> tickets;
        try {
            ticketList = mapper.readValue(new File(fileName), TicketList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tickets = ticketList.getTickets();

        // Минимальное время полета между городами Владивосток и Тель-Авив
        // для каждого авиаперевозчика
        Predicate<Ticket> areRequiredCities =
                t -> (t.getOriginName().equals(city1) && t.getDestinationName().equals(city2)) ||
                        (t.getDestinationName().equals(city1) && t.getOriginName().equals(city2));

        List<Ticket> filteredTicketsList = tickets.stream()
                .filter(areRequiredCities).toList();

        Map<String, List<Ticket>> filteredTicketsByCarrier = filteredTicketsList.stream()
                .collect(Collectors.groupingBy(Ticket::getCarrier));

        System.out.printf("Мин. время полета между городами %s и %s по перевозчикам:\n",
                city1, city2);

        filteredTicketsByCarrier.forEach((key, value) -> System.out.printf("%s: %s\n", key,
                minutesToHoursMinutesString((value.stream()
                        .map(Ticket::flightDuration)
                        .min(Comparator.comparingInt(o -> o)).orElse(0)))));

        // Разница между средней ценой и медианой для полета
        // между городами Владивосток и Тель-Авив
        double avgPrice = filteredTicketsList.stream()
                        .mapToDouble(Ticket::getPrice)
                        .average().orElse(0);
        System.out.printf("Средняя цена полета между городами %s и %s: %.2f\n", city1, city2, avgPrice);
        double medianPrice = median(filteredTicketsList.stream()
                .map(Ticket::getPrice)
                .toList());
        System.out.printf("Медианная цена полета между городами %s и %s: %.2f\n", city1, city2, medianPrice);
        System.out.printf("Разница между средней и медианной ценой: %.2f\n",
                Math.abs(avgPrice - medianPrice));
    }

    private static double median(List<Integer> list) {
        int size = list.size();
        return list.stream()
                .mapToDouble(value -> value)
                .sorted()
                .skip((size - 1) /2)
                .limit(2 - size % 2)
                .average()
                .orElse(Double.NaN);
    }

    private static String minutesToHoursMinutesString(int minutes) {
        int hours = minutes / 60;
        int minutesRemaining = minutes % 60;
        StringBuilder result = new StringBuilder();
        if (hours != 0) result.append(hours).append(" ч. ");
        if (minutesRemaining != 0) result.append(minutesRemaining).append( "мин.");

        return result.toString().trim();
    }


}