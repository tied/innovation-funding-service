package org.innovateuk.ifs.project.monitor.repository;

import org.innovateuk.ifs.project.monitor.domain.ProjectMonitoringOfficer;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProjectMonitoringOfficerRepository extends PagingAndSortingRepository<ProjectMonitoringOfficer, Long> {

    List<ProjectMonitoringOfficer> findByUserId(long userId);

    boolean existsByProjectIdAndUserId(long projectId, long userId);
}