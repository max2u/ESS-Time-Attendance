package gov.nysenate.seta.service.transaction;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import gov.nysenate.seta.BaseTests;
import gov.nysenate.seta.dao.transaction.EmpTransDaoOption;
import gov.nysenate.seta.dao.transaction.EmpTransactionDao;
import gov.nysenate.seta.model.cache.CacheEvictIdEvent;
import gov.nysenate.seta.model.cache.ContentCache;
import gov.nysenate.seta.model.transaction.TransactionHistory;
import gov.nysenate.seta.model.transaction.TransactionHistoryUpdateEvent;
import gov.nysenate.seta.model.transaction.TransactionRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CachedTransactionServiceTests extends BaseTests {

    private static final Logger logger = LoggerFactory.getLogger(CachedTransactionServiceTests.class);

    @Autowired EmpTransactionDao transDao;
    @Autowired EssCachedEmpTransactionService transService;

    @Autowired EventBus eventBus;

    private void timedGet(int empId) {
        Stopwatch sw = Stopwatch.createStarted();
        TransactionHistory transHistory = transService.getTransHistory(empId);
        logger.info("got trans history for {}:  {}", empId, sw.stop().elapsed(TimeUnit.NANOSECONDS));
    }

    @Test
    public void cacheTest() {
        int empId = 45;
        for (int i = 0; i < 4; i++) {
            timedGet(empId);
        }
    }

    @Test
    public void invalidateTest() {
        int empId = 5803;
        timedGet(empId);
        timedGet(empId);
        logger.info("invalidating {}!", empId);
        eventBus.post(new CacheEvictIdEvent<>(ContentCache.TRANSACTION, empId));
        timedGet(empId);
    }

    @Test
    public void testTransactions() throws Exception {
        TransactionHistory transHistory = transDao.getTransHistory(1719, EmpTransDaoOption.NONE);
        logger.info("{}", transHistory.getEffectiveAccrualStatus(Range.upTo(LocalDate.now(), BoundType.CLOSED)));
    }

    @Test
    public void transHistoryUpdateEventTest() {
        ImmutableCollection<TransactionRecord> transactionRecords =
                transService.getTransHistory(439).getRecordsByCode().values();
        eventBus.post(new TransactionHistoryUpdateEvent(new ArrayList<>(transactionRecords), LocalDateTime.now()));
    }
}
