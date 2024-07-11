package saaspe.azure.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class CommonUtils {

	public static LocalDate getFirstDateOfQuarter() {
		return LocalDate.now().withMonth(LocalDate.now().get(IsoFields.QUARTER_OF_YEAR) * 3 - 2)
				.with(TemporalAdjusters.firstDayOfMonth());
	}

	public static LocalDate getLastDateOfQuarter() {
		return LocalDate.now().withMonth(LocalDate.now().get(IsoFields.QUARTER_OF_YEAR) * 3)
				.with(TemporalAdjusters.lastDayOfMonth());
	}

	public static LocalDate getCurrentMonthFirstDate() {
		return LocalDate.now().withDayOfMonth(1);
	}

	public static LocalDate getCurrentMonthLastDate() {
		return LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1);
	}

	public static LocalDate getFistDateOfYear() {
		return LocalDate.now().withDayOfYear(1);
	}

	public static LocalDate getLastDateOfYear() {
		return LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1);
	}
	
	public static Date dateStringtoDate(String stringDate) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date date = null;
		try {
			date = formatter.parse(stringDate);
		} catch (ParseException e) {
			throw new ParseException(e.getMessage(), 0);
		}
		return date;
	}

	public static LocalDate dateToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static Date simpleDateFormat(Date date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.parse(formatter.format(date));
	}

}
