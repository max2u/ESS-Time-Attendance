package gov.nysenate.seta.dao;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import gov.nysenate.common.DateUtils;
import gov.nysenate.common.OutputUtils;
import gov.nysenate.seta.BaseTests;
import gov.nysenate.seta.dao.personnel.SupervisorDao;
import gov.nysenate.seta.model.personnel.SupGrantType;
import gov.nysenate.seta.model.personnel.SupervisorEmpGroup;
import gov.nysenate.seta.model.personnel.SupervisorOverride;
import gov.nysenate.seta.model.transaction.TransactionInfo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SqlSupervisorDaoTests extends BaseTests
{
    private static final Logger logger = LoggerFactory.getLogger(SqlSupervisorDaoTests.class);

    @Autowired
    private SupervisorDao supervisorDao;

    @Test
    public void testGetSupEmpGroup_ReturnsEmpGroup() throws Exception {
        SupervisorEmpGroup group =
            supervisorDao.getSupervisorEmpGroup(1024, Range.closed(LocalDate.of(1970, 1, 1), LocalDate.of(2015, 8, 31)));
        logger.info(OutputUtils.toJson(group));
    }

    @Test
    public void issupTest() {
        supervisorDao.isSupervisor(11423, DateUtils.ALL_DATES);
        Stopwatch stopwatch = Stopwatch.createStarted();
        logger.info("{}", supervisorDao.isSupervisor(11423, DateUtils.ALL_DATES));
        logger.info("{}", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    @Test
    public void supOverrideTest() throws Exception {
//        supervisorDao.setSupervisorOverride(11423, 7048, Range.closed(LocalDate.of(2015, 7, 16), LocalDate.of(2015, 7, 29)));
        logger.info("{}", OutputUtils.toJson(supervisorDao.getSupervisorOverrides(9896, SupGrantType.GRANTER)));
    }

    @Test
    public void testSetSupervisorOverrides() throws Exception {
        supervisorDao.setSupervisorOverride(9896, 7048, true, null, null);
    }

    @Test
    public void getSupChangesTest() {
        LocalDateTime fromDate = LocalDate.of(2015, 9, 1).atStartOfDay();
        List<TransactionInfo> supChanges = supervisorDao.getSupTransChanges(fromDate);
        logger.info("{}", supChanges.stream().map(TransactionInfo::getEmployeeId).collect(Collectors.toSet()));
    }

    @Test
    public void getSupOvrChangesTest() {
        LocalDateTime fromDate = LocalDate.of(2015, 9, 1).atStartOfDay();
        List<SupervisorOverride> ovrChanges = supervisorDao.getSupOverrideChanges(fromDate);
        logger.info("{}", ovrChanges.size());
    }

    @Test
    public void getLatestSupdateTest() {
        LocalDateTime latestUpdate = supervisorDao.getLastSupUpdateDate();
        logger.info("{}", latestUpdate);
    }
}
