package gov.nysenate.seta.service.period;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import gov.nysenate.common.DateUtils;
import gov.nysenate.common.SortOrder;
import gov.nysenate.seta.dao.period.HolidayDao;
import gov.nysenate.seta.model.cache.ContentCache;
import gov.nysenate.seta.model.payroll.Holiday;
import gov.nysenate.seta.service.base.BaseCachingService;
import gov.nysenate.seta.service.cache.EhCacheManageService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EssCachedHolidayService implements HolidayService
{
    private static final Logger logger = LoggerFactory.getLogger(EssCachedHolidayService.class);
    private static final String HOLIDAY_CACHE_KEY = "holiday";

    @Autowired private HolidayDao holidayDao;
    @Autowired private EventBus eventBus;
    @Autowired private EhCacheManageService cacheManageService;

    private Cache holidayCache;

    private static final class HolidayCacheTree
    {
        private TreeMap<LocalDate, Holiday> holidayTreeMap = new TreeMap<>();

        public HolidayCacheTree(List<Holiday> holidays) {
            holidays.forEach(h -> holidayTreeMap.put(h.getDate(), h));
        }

        public Optional<Holiday> getHoliday(LocalDate date) {
            return Optional.ofNullable(holidayTreeMap.get(date));
        }

        public List<Holiday> getHolidays(Range<LocalDate> dateRange) {
            return new ArrayList<>(holidayTreeMap.subMap(DateUtils.startOfDateRange(dateRange), true,
                                         DateUtils.endOfDateRange(dateRange), true).values());
        }
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
        this.holidayCache = this.cacheManageService.registerEternalCache(ContentCache.HOLIDAY.name());
        if (this.cacheManageService.isWarmOnStartup()) {
            cacheHolidays();
        }
    }

    @Override
    public Optional<Holiday> getHoliday(LocalDate date) {
        return getHolidayCacheTree(true).getHoliday(date);
    }

    @Override
    public List<Holiday> getHolidays(Range<LocalDate> dateRange, boolean includeQuestionable, SortOrder dateOrder) {
        List<Holiday> holidays = getHolidayCacheTree(true).getHolidays(dateRange);
        if (!includeQuestionable) {
            holidays = holidays.stream().filter(h -> !h.isQuestionable()).collect(Collectors.toList());
        }
        if (dateOrder.equals(SortOrder.DESC)) {
            Collections.reverse(holidays);
        }
        return holidays;
    }

    private HolidayCacheTree getHolidayCacheTree(boolean createIfEmpty) {
        holidayCache.acquireReadLockOnKey(HOLIDAY_CACHE_KEY);
        Element elem = holidayCache.get(HOLIDAY_CACHE_KEY);
        holidayCache.releaseReadLockOnKey(HOLIDAY_CACHE_KEY);
        if (elem == null) {
            if (!createIfEmpty) throw new IllegalStateException("Holidays are not cached yet!");
            cacheHolidays();
            holidayCache.acquireReadLockOnKey(HOLIDAY_CACHE_KEY);
            elem = holidayCache.get(HOLIDAY_CACHE_KEY);
            holidayCache.releaseReadLockOnKey(HOLIDAY_CACHE_KEY);
            if (elem == null) throw new IllegalStateException("Holidays are not caching properly.");
        }
        if (elem.getObjectValue() == null) throw new IllegalStateException("Holidays are not caching properly.");
        return (HolidayCacheTree) elem.getObjectValue();
    }

    @Scheduled(cron = "${cache.cron.holiday}") // Refresh the cache every 12 hours
    private void cacheHolidays() {
        logger.info("Caching holidays...");
        holidayCache.acquireWriteLockOnKey(HOLIDAY_CACHE_KEY);
        try {
            this.holidayCache.remove(HOLIDAY_CACHE_KEY);
            this.holidayCache.put(new Element(HOLIDAY_CACHE_KEY, new HolidayCacheTree(
                    holidayDao.getHolidays(Range.upTo(LocalDate.now().plusYears(2), BoundType.CLOSED), true, SortOrder.ASC))));
        }
        finally {
            holidayCache.releaseWriteLockOnKey(HOLIDAY_CACHE_KEY);
        }
        logger.info("Done caching holidays.");
    }
}