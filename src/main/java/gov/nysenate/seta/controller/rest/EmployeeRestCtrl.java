package gov.nysenate.seta.controller.rest;

import gov.nysenate.seta.client.response.base.BaseResponse;
import gov.nysenate.seta.client.response.base.ListViewResponse;
import gov.nysenate.seta.client.response.base.ViewObjectResponse;
import gov.nysenate.seta.client.view.DetailedEmployeeView;
import gov.nysenate.seta.client.view.EmployeeView;
import gov.nysenate.seta.dao.personnel.EmployeeDao;
import gov.nysenate.seta.model.personnel.Employee;
import gov.nysenate.seta.model.personnel.EmployeeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static gov.nysenate.seta.controller.rest.BaseRestCtrl.REST_PATH;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(REST_PATH + "/employees")
public class EmployeeRestCtrl extends BaseRestCtrl
{
    private static final Logger logger = LoggerFactory.getLogger(EmployeeRestCtrl.class);

    @Autowired protected EmployeeDao employeeDao;

    @RequestMapping(value = "")
    public BaseResponse getEmployeeById(@RequestParam(required = true) Integer empId[],
                                        @RequestParam(defaultValue = "false") boolean detail) throws EmployeeException {
        return getEmployeeResponse(
            Arrays.asList(empId).stream().map(employeeDao::getEmployeeById).collect(toList()), detail);
    }

    private BaseResponse getEmployeeResponse(List<Employee> employeeList, boolean detail) {
        if (employeeList.size() == 1) {
            return new ViewObjectResponse<>((detail) ? new DetailedEmployeeView(employeeList.get(0))
                                                     : new EmployeeView(employeeList.get(0)));
        }
        else {
            return ListViewResponse.of(
                employeeList.stream().map((detail) ? DetailedEmployeeView::new : EmployeeView::new).collect(toList()));
        }
    }
}