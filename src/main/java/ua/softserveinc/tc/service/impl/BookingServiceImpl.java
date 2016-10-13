package ua.softserveinc.tc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.softserveinc.tc.constants.DateConstants;
import ua.softserveinc.tc.dao.BookingDao;
import ua.softserveinc.tc.dao.ChildDao;
import ua.softserveinc.tc.dao.RoomDao;
import ua.softserveinc.tc.dao.UserDao;
import ua.softserveinc.tc.dto.BookingDto;
import ua.softserveinc.tc.entity.Booking;
import ua.softserveinc.tc.entity.BookingState;
import ua.softserveinc.tc.entity.Room;
import ua.softserveinc.tc.entity.User;
import ua.softserveinc.tc.service.BookingService;
import ua.softserveinc.tc.service.RateService;
import ua.softserveinc.tc.service.RoomService;
import ua.softserveinc.tc.util.DateUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static ua.softserveinc.tc.dto.BookingDto.getBookingDto;
import static ua.softserveinc.tc.util.DateUtil.toDateAndTime;

@Service
public class BookingServiceImpl extends BaseServiceImpl<Booking> implements BookingService {

    @Autowired
    private BookingDao bookingDao;

    @Autowired
    private RateService rateService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private ChildDao childDao;

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, BookingState... bookingStates) {
        return getBookings(startDate, endDate, null, null, bookingStates);
    }

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, User user, BookingState... bookingStates) {
        return getBookings(startDate, endDate, user, null, bookingStates);
    }

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, Room room, BookingState... bookingStates) {
        return getBookings(startDate, endDate, null, room, bookingStates);
    }

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, User user, Room room, BookingState... bookingStates) {
        return bookingDao.getBookings(startDate, endDate, user, room, bookingStates);
    }

    @Override
    public void calculateAndSetDuration(Booking booking) {
        long difference = booking.getBookingEndTime().getTime() -
                booking.getBookingStartTime().getTime();

        booking.setDuration(difference);
    }

    @Override
    public void calculateAndSetSum(Booking booking) {
        calculateAndSetDuration(booking);
        Long sum = rateService.calculateBookingCost(booking);
        booking.setSum(sum);
        booking.setBookingState(BookingState.COMPLETED);
        bookingDao.update(booking);
    }

    @Override
    public Long getSumTotal(List<Booking> bookings) {
        return bookings.stream()
                .mapToLong(Booking::getSum)
                .sum();
    }

    @Override
    public Map<User, Long> generateAReport(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(Booking::getUser,
                        Collectors.summingLong(Booking::getSum)));
    }

    @Override
    public Map<Room, Long> generateStatistics(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(Booking::getRoom,
                        Collectors.summingLong(Booking::getSum)));
    }

    @Override
    public Booking confirmBookingStartTime(BookingDto bookingDto) {
        Booking booking = findById(bookingDto.getId());
        Date date = replaceBookingTime(booking, bookingDto.getStartTime());
        booking.setBookingStartTime(date);
        resetSumAndDuration(booking);
        return booking;
    }

    @Override
    public Booking confirmBookingEndTime(BookingDto bookingDto) {
        Booking booking = findById(bookingDto.getId());
        Date date = replaceBookingTime(booking, bookingDto.getEndTime());
        booking.setBookingEndTime(date);
        calculateAndSetSum(booking);
        return booking;
    }




    private void resetSumAndDuration(Booking booking) {
        booking.setDuration(0L);
        booking.setSum(0L);
        booking.setBookingState(BookingState.CALCULATE_SUM);
    }

    @Override
    public Date replaceBookingTime(Booking booking, String time) {
        DateFormat dfDate = new SimpleDateFormat(DateConstants.SHORT_DATE_FORMAT);
        String dateString = dfDate.format(booking.getBookingStartTime()) + " " + time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDateAndTime(dateString));
        return calendar.getTime();
    }

    public Boolean checkForDuplicateBooking(List<BookingDto> listDto) {
        for (BookingDto x : listDto) {
            if (checkForDuplicateBookingSingle(x)) return true;
        }
        return false;
    }
    public Boolean checkForDuplicateBookingSingle(BookingDto bookingDto) {
        User user = userDao.findById(bookingDto.getUserId());
        Room room = roomDao.findById(bookingDto.getRoomId());

            for (Booking y : bookingDao.getBookingsByUserAndRoom(user, room)) {

                if (y.getBookingEndTime().after(new Date()) && (y.getBookingState() != BookingState.CANCELLED)) {

                    if ((y.getRecurrentId()!=bookingDto.getRecurrentId())&&(!(!(DateUtil.toDateISOFormat(bookingDto.getEndTime()).after(y.getBookingStartTime()))
                            || !(DateUtil.toDateISOFormat(bookingDto.getStartTime()).before(y.getBookingEndTime())))
                            && bookingDto.getKidId().equals(y.getChild().getId()))) {
                        return true;
                    }
                }

            }
        return false;
    }



    @Override
    public List<BookingDto> persistBookingsFromDtoAndSetId(List<BookingDto> listDTO) {
        BookingDto bdto = listDTO.get(0);

        int available = roomService.getAvailableSpaceForPeriod(
                bdto.getDateStartTime(),
                bdto.getDateEndTime(),
                bdto.getRoom());

        if (available >= listDTO.size()) {
            listDTO.forEach(bookingDTO -> {
                Booking booking = bookingDTO.getBookingObject();
                booking.setSum(0L);
                booking.setDuration(0L);
                bookingDao.create(booking);
                bookingDTO.setId(booking.getIdBook());
            });
            return listDTO;
        } else {
            return Collections.emptyList();
        }
    }

    public List<BookingDto> getAllBookingsByUserAndRoom(Long idUser, Long idRoom) {
        User user = userDao.findById(idUser);
        Room room = roomDao.findById(idRoom);
        return getBookings(null, null, user, room, BookingState.BOOKED)
                .stream()
                .map(BookingDto::new)
                .collect(Collectors.toList());
    }

    public Long getMaxRecurrentId() {
        return bookingDao.getMaxRecurrentId();
    }


    public List<BookingDto> makeRecurrentBookings(List<BookingDto> bookingDtos) {
        /**
         * All recurrent bookings have the same date,
         * this method use date only from first element in list
         */

        //  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        String dateStart = bookingDtos.get(0).getStartTime();
        String dateEnd = bookingDtos.get(0).getEndTime();

        Date dateForRecurrentStart = DateUtil.toDateISOFormat(dateStart);
        Date dateForRecurrentEnd = DateUtil.toDateISOFormat(dateEnd);

        Map<String, Integer> daysOFWeek = new HashMap<>();
        daysOFWeek.put("Sun", Calendar.SUNDAY);
        daysOFWeek.put("Mon", Calendar.MONDAY);
        daysOFWeek.put("Tue", Calendar.TUESDAY);
        daysOFWeek.put("Wed", Calendar.WEDNESDAY);
        daysOFWeek.put("Thu", Calendar.THURSDAY);
        daysOFWeek.put("Fri", Calendar.FRIDAY);
        daysOFWeek.put("Sat", Calendar.SATURDAY);

        Calendar calendar1 = Calendar.getInstance();

        Calendar calendarEndTime = Calendar.getInstance();
        calendarEndTime.setTime(dateForRecurrentEnd);

        Calendar calendarStartTime = Calendar.getInstance();
        calendarStartTime.setTime(dateForRecurrentStart);

        String[] days = bookingDtos.get(0).getDaysOfWeek().split(" ");
        Long newRecID = bookingDao.getMaxRecurrentId() + 1;

        /**
         * This code make Map for bookings (using children) and their new recurrent ID
         */
        Map<Long, Long> recurrentMap = new HashMap<>(bookingDtos.size());

        for (BookingDto bookingDto : bookingDtos) {
            recurrentMap.put(bookingDto.getKidId(), newRecID);
            newRecID++;
        }

        Room room = roomDao.findById(bookingDtos.get(0).getRoomId());

        List<BookingDto> newRecurrentBooking = new LinkedList<>();

        while (dateForRecurrentEnd.getTime() > calendarStartTime.getTimeInMillis()) {
            for (int i = 0; i < days.length; i++) {

                List<BookingDto> dailyBookings = new LinkedList<>();

                calendarStartTime.set(Calendar.DAY_OF_WEEK, daysOFWeek.get(days[i]));

                if (dateForRecurrentEnd.getTime() < calendarStartTime.getTimeInMillis()) break;
                if (dateForRecurrentStart.getTime() > calendarStartTime.getTimeInMillis()) continue;


                for (int j = 0; j < bookingDtos.size(); j++) {
                    Booking booking = new Booking();

                    booking.setBookingStartTime(calendarStartTime.getTime());


                    calendar1.setTime(calendarStartTime.getTime());
                    calendar1.set(Calendar.HOUR_OF_DAY, calendarEndTime.get(Calendar.HOUR_OF_DAY));
                    calendar1.set(Calendar.MINUTE, calendarEndTime.get(Calendar.MINUTE));

                    booking.setBookingEndTime(calendar1.getTime());

                    booking.setRecurrentId(recurrentMap.get(bookingDtos.get(j).getKidId()));
                    booking.setRoom(room);
                    booking.setChild(childDao.findById(bookingDtos.get(j).getKidId()));
                    booking.setComment(bookingDtos.get(j).getComment());
                    booking.setDuration(new Long(0));

                    booking.setUser(userDao.findById(bookingDtos.get(j).getUserId()));

                    //FIXME: fix this 'buf'
                    BookingDto buf = booking.getDto();
                    buf.setRoom(room);
                    buf.setRoomId(room.getId());

                    buf.setDateStartTime(DateUtil.toDateISOFormat(DateUtil.toIsoString(calendarStartTime.getTime())));
                    buf.setDateEndTime(DateUtil.toDateISOFormat(DateUtil.toIsoString(calendar1.getTime())));

                    buf.setUser(userDao.findById(bookingDtos.get(j).getUserId()));
                    buf.setBookingState(BookingState.BOOKED);
                    buf.setChild(childDao.findById(buf.getIdChild()));
                    dailyBookings.add(buf);
                }
                newRecurrentBooking.addAll(dailyBookings);
            }
            calendarStartTime.add(Calendar.WEEK_OF_YEAR, 1);
            calendarStartTime.set(Calendar.DAY_OF_WEEK, daysOFWeek.get("Mon"));
        }
        if (newRecurrentBooking.isEmpty()) return newRecurrentBooking;
        return persistBookingsFromDtoAndSetId(newRecurrentBooking);
    }

    public List<BookingDto> recurrentDtoToList (BookingDto recurrentBookingDto){
        String dateStart = recurrentBookingDto.getStartTime();
        String dateEnd = recurrentBookingDto.getEndTime();
        Date dateForRecurrentStart = DateUtil.toDateISOFormat(dateStart);
        Date dateForRecurrentEnd = DateUtil.toDateISOFormat(dateEnd);


        Calendar calendarEndTime = Calendar.getInstance();
        calendarEndTime.setTime(dateForRecurrentEnd);

        Calendar calendarStartTime = Calendar.getInstance();
        calendarStartTime.setTime(dateForRecurrentStart);

        ArrayList<Integer> weekDays = DaysStringToArray(recurrentBookingDto.getDaysOfWeek());
        Long newRecID = bookingDao.getMaxRecurrentId() + 1;
        Room room = roomDao.findById(recurrentBookingDto.getRoomId());
        recurrentBookingDto.setRecurrentId(newRecID);
        recurrentBookingDto.setRoom(room);
        recurrentBookingDto.setChild(childDao.findById(recurrentBookingDto.getKidId()));
        recurrentBookingDto.setUser(userDao.findById(recurrentBookingDto.getUserId()));

        List<BookingDto> newRecurrentBookingDto = new ArrayList<>();

        Calendar iterationDayStartTime = calendarStartTime;
        Calendar iterationDayEndTime = (Calendar) iterationDayStartTime.clone();
        iterationDayEndTime.set(Calendar.HOUR_OF_DAY, calendarEndTime.get(Calendar.HOUR_OF_DAY));
        iterationDayEndTime.set(Calendar.MINUTE, calendarEndTime.get(Calendar.MINUTE));
        calendarEndTime.add(Calendar.DAY_OF_MONTH, 1);
        while (iterationDayEndTime.before(calendarEndTime)) {
            if (recurrentBookingDto.getWeekDays().contains(iterationDayEndTime.get(Calendar.DAY_OF_WEEK)) ) {
                BookingDto newBbooking = new BookingDto(recurrentBookingDto);
                newBbooking.setDateStartTime(iterationDayStartTime.getTime());
                newBbooking.setDateEndTime(iterationDayEndTime.getTime());
                newBbooking.setBookingState(BookingState.BOOKED);
                newBbooking.setKidName(newBbooking.getChild().getFullName());
                newBbooking.setRoomName( newBbooking.getRoom().getAddress() );
                newBbooking.setIdChild(newBbooking.getChild().getId());
                newRecurrentBookingDto.add(newBbooking);
            }
            iterationDayStartTime.add(Calendar.DAY_OF_MONTH, 1);
            iterationDayEndTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        return newRecurrentBookingDto;
    }

    public ArrayList<Integer> DaysStringToArray(String days){
        ArrayList<Integer> weekDays = new ArrayList<>();
        String[] daysArray = days.split(" ");
        for(String day: daysArray){
            switch (day) {
                case "Sun":
                    weekDays.add(Calendar.SUNDAY);
                    break;
                case "Mon":
                    weekDays.add(Calendar.MONDAY);
                    break;
                case "Tue":
                    weekDays.add(Calendar.TUESDAY);
                    break;
                case "Wed":
                    weekDays.add(Calendar.WEDNESDAY);
                    break;
                case "Thu":
                    weekDays.add(Calendar.THURSDAY);
                    break;
                case "Fri":
                    weekDays.add(Calendar.FRIDAY);
                    break;
                case "Sat":
                    weekDays.add(Calendar.SATURDAY);
                    break;
            }
        }
        return weekDays;
    }

    public BookingDto getRecurrentBookingForEditingById(final long bookingId){
        final List<Booking> listOfRecurrentBooking = bookingDao.getRecurrentBookingsByRecurrentId(bookingId);
        Set <Integer> weekDays = new HashSet<>();
        Calendar calendar = Calendar.getInstance();
        for (Booking booking : listOfRecurrentBooking) {
            calendar.setTime(booking.getBookingStartTime());
            weekDays.add(calendar.get(Calendar.DAY_OF_WEEK));
        }
        return getBookingDto(listOfRecurrentBooking, weekDays);

    }

    public List<BookingDto> updateRecurrentBookings(BookingDto recurrentBookingDto) {
        Long recurrentId = recurrentBookingDto.getRecurrentId();
        List<Booking> recurrentBookingForDelete = bookingDao.getRecurrentBookingsByRecurrentId(recurrentId);
        for(Booking bdto:recurrentBookingForDelete) {
            bdto.setBookingState(BookingState.CANCELLED);
        }
        List<Booking> recurrentBookingForCreate = new ArrayList<>();
        List<BookingDto> listOfRecurrentBooking = recurrentDtoToList(recurrentBookingDto);

        if (listOfRecurrentBooking.isEmpty()) {
            return listOfRecurrentBooking;
        }
        for(BookingDto bdto:listOfRecurrentBooking){
//        listOfRecurrentBooking.forEach(bdto -> {
            Booking booking = bdto.getBookingObject();
            booking.setSum(0L);
            booking.setDuration(0L);
            bdto.setId(booking.getIdBook());
            recurrentBookingForCreate.add(booking);
        };
        updateRecurrentBookingsDeleteCreate (recurrentBookingForDelete, recurrentBookingForCreate);
        final ArrayList <BookingDto> recurrentBookings = new ArrayList<>();
        recurrentBookingForCreate.forEach((b)-> recurrentBookings.add(new BookingDto(b)));
        return recurrentBookings;
    }

    @Override
    @Transactional
    public List<Booking> updateRecurrentBookingsDeleteCreate(List<Booking> oldBookings, List<Booking> newBookings) {
        oldBookings.forEach(this::update);
        newBookings.forEach(this::create);
        return newBookings;

    }

}
