package ua.softserveinc.tc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.softserveinc.tc.constants.BookingConstants;
import ua.softserveinc.tc.constants.ModelConstants.DateConst;
import ua.softserveinc.tc.dao.BookingDao;
import ua.softserveinc.tc.dto.BookingDto;
import ua.softserveinc.tc.entity.*;
import ua.softserveinc.tc.service.BookingService;
import ua.softserveinc.tc.service.ChildService;
import ua.softserveinc.tc.service.RateService;
import ua.softserveinc.tc.util.ApplicationConfigurator;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static ua.softserveinc.tc.util.DateUtil.toDateAndTime;

@Service
public class BookingServiceImpl extends BaseServiceImpl<Booking> implements BookingService {

    @Autowired
    private BookingDao bookingDao;

    @Autowired
    private RateService rateService;

    @Autowired
    private ChildService childService;

    @Autowired
    private ApplicationConfigurator appConfigurator;

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate) {
        return getBookings(startDate, endDate, null, null);
    }

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, User user) {
        return getBookings(startDate, endDate, user, null);
    }

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, Room room) {
        return getBookings(startDate, endDate, null, room);
    }

    @Override
    public List<Booking> getBookings(Date startDate, Date endDate, User user, Room room) {
        EntityManager entityManager = bookingDao.getEntityManager();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Booking> criteria = builder.createQuery(Booking.class);
        Root<Booking> root = criteria.from(Booking.class);

        List<Predicate> restrictions = new ArrayList<>();
        restrictions.add(builder.between(root.get(
                BookingConstants.Entity.START_TIME), startDate, endDate));

        if (user != null)
            restrictions.add(builder.equal(root.get(BookingConstants.Entity.USER), user));
        if (room != null)
            restrictions.add(builder.equal(root.get(BookingConstants.Entity.ROOM), room));

        criteria.where(builder.and(restrictions.toArray(new Predicate[restrictions.size()])));
        criteria.orderBy(builder.asc(root.get(BookingConstants.Entity.START_TIME)));

        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public List<Booking> filterByState(List<Booking> bookings, BookingState bookingState) {
        return bookings.stream()
                .filter(booking ->
                        booking.getBookingState() == bookingState)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> filterByNotState(List<Booking> bookings, BookingState bookingState) {
        return bookings.stream()
                .filter(booking ->
                        booking.getBookingState() != bookingState)
                .collect(Collectors.toList());
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

        List<Rate> rates = booking.getIdRoom().getRates();
        Rate closestRate = rateService.calculateClosestRate(booking.getDuration(), rates);

        booking.setSum(closestRate.getPriceRate());
        bookingDao.update(booking);
    }

    @Override
    public Long getSumTotal(List<Booking> bookings) {
        return bookings.stream().mapToLong(Booking::getSum).sum();
    }

    @Override
    public Map<User, Long> generateAReport(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(Booking::getIdUser,
                        Collectors.summingLong(Booking::getSum)));
    }

    @Override
    public Map<Room, Long> generateStatistics(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(Booking::getIdRoom,
                        Collectors.summingLong(Booking::getSum)));
    }


    @Override
    public List<Booking> filterBySum(List<Booking> bookings, Long sum) {
        return  bookings.stream()
                .filter(booking -> booking.getSum() == 0L)
                .collect(Collectors.toList());
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
        resetSumAndDuration(booking);
        return booking;
    }


    @Override
    public Booking confirmBooking(BookingDto bookingDto) {
        Booking booking = findById(bookingDto.getId());
        booking.setBookingStartTime(replaceBookingTime(booking, bookingDto.getStartTime()));
        booking.setBookingStartTime(replaceBookingTime(booking, bookingDto.getEndTime()));
        resetSumAndDuration(booking);
        return booking;
    }

    private void resetSumAndDuration(Booking booking){
        booking.setDuration(0L);
        booking.setSum(0L);
    }
    @Override
    public Date replaceBookingTime(Booking booking, String time) {
        DateFormat dfDate = new SimpleDateFormat(DateConst.SHORT_DATE_FORMAT);
        String dateString = dfDate.format(booking.getBookingStartTime()) + " " + time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDateAndTime(dateString));
        return calendar.getTime();
    }

    @Override
    public List<BookingDto> persistBookingsFromDTOandSetID(List<BookingDto> listDTO) {

        listDTO.forEach(bookingDTO -> {
            Booking booking = bookingDTO.getBookingObject();
            booking.setBookingState(BookingState.BOOKED);
            bookingDao.create(booking);
            bookingDTO.setId(booking.getIdBook());
        });

        return listDTO;
    }



}


