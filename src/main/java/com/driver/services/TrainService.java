package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        //step 1. create list of trains
        List<Train>trains=new ArrayList<>();

        // make train object
        Train train = new Train();

        //sabse pehle seat set kar de
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        List<Station> list = trainEntryDto.getStationRoute();
        String route = "";

        for (int i=0; i<list.size(); i++) {
            if(i == list.size()-1)
                route += list.get(i);  // agar last me pahuch jaye to simpley add karna
            else
                route += list.get(i) + ",";    // or agar last tak nahi pahucta hai to "," add karna hai
        }
        train.setRoute(route);

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        trains.add(train);  // train add to list
        return trainRepository.save(train).getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket>ticketList = train.getBookedTickets();  //kitna ticket book hua hai
        String []trainRoot = train.getRoute().split(",");

        HashMap<String,Integer> map = new HashMap<>();
        for(int i=0; i<trainRoot.length; i++){
            map.put(trainRoot[i],i);
        }
        if(!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString())||!map.containsKey(seatAvailabilityEntryDto.getToStation().toString())){
            return 0;  // agar map train ke route me staion ata hi nahi hai return 0;
        }

        int ticketBookedByPassenger = 0;
        for(Ticket ticket:ticketList){
            ticketBookedByPassenger += ticket.getPassengersList().size();   // passenger count
        }

        int avilableSeat = train.getNoOfSeats()-ticketBookedByPassenger;
        for(Ticket t:ticketList){
            String fromStation = t.getFromStation().toString();
            String toStation = t.getToStation().toString();
            if(map.get(seatAvailabilityEntryDto.getToStation().toString()) <= map.get(fromStation)){
                avilableSeat++;
            }
            else if (map.get(seatAvailabilityEntryDto.getFromStation().toString()) >= map.get(toStation)){
                avilableSeat++;
            }
        }
        return avilableSeat+2;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.


//        int numberOfPeople;
//        Train train = trainRepository.findById(trainId).get();
//        String route = train.getRoute();
//        String arr[]= route.split(",");
//        for (int i=0; i<route.length(); i++) {
//            if(arr[i].equals(station)) {
//               return numberOfPeople = train.getBookedTickets().size();
//            }
//        }
//        throw new Exception("Train is not passing from this station");

        //We need to find out the number of people who will be boarding a train from a particular station
        Train train = trainRepository.findById(trainId).get();
        String reqStation = station.toString();
        String arr[] = train.getRoute().split(",");
        boolean found = false;

        for(String s:arr){
            if(s.equals(reqStation)){
                found = true;
                break;
            }
        }
        //if the trainId is not passing through that station

        if(found == false){
            throw new Exception("Train is not passing from this station");
        }

        int noOfPassengers = 0;
        //throw new Exception("Train is not passing from this station");
        List<Ticket>ticketList= train.getBookedTickets();
        for(Ticket ticket:ticketList){
            if(ticket.getFromStation().toString().equals(reqStation)){
                noOfPassengers+=ticket.getPassengersList().size();
            }
        }

        return noOfPassengers;

    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();
        int age = Integer.MIN_VALUE;
        if(train.getBookedTickets().size() == 0)return 0;

        List<Ticket>ticketList = train.getBookedTickets();
        for(Ticket ticket:ticketList){
            List<Passenger>passengers = ticket.getPassengersList();
            for(Passenger passenger:passengers){
                age = Math.max(age,passenger.getAge());
            }
        }
        return age;

    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> TrainList = new ArrayList<>();

        //get list of train which is pass through given station
        List<Train> trains = trainRepository.findAll();
        for(Train train : trains){
            String route = train.getRoute();
            String[] routeArray = route.split(",");
            for(int i=0; i<routeArray.length; i++){
                if(Objects.equals(routeArray[i], String.valueOf(station))){
                    int startTimeInMin = (startTime.getHour() * 60) + startTime.getMinute();
                    int lastTimeInMin = (endTime.getHour() * 60) + endTime.getMinute();

                    int departureTimeInMin = (train.getDepartureTime().getHour() * 60) + train.getDepartureTime().getMinute();
                    int reachingTimeInMin  = departureTimeInMin + (i * 60);
                    if(reachingTimeInMin >= startTimeInMin && reachingTimeInMin<=lastTimeInMin)
                        TrainList.add(train.getTrainId());
                }
            }
        }
        return TrainList;

    }

}
