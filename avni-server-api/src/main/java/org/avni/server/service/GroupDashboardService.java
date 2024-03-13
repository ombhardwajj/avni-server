package org.avni.server.service;

import org.avni.server.common.EntityHelper;
import org.avni.server.dao.DashboardRepository;
import org.avni.server.dao.GroupDashboardRepository;
import org.avni.server.dao.GroupRepository;
import org.avni.server.domain.Dashboard;
import org.avni.server.domain.Group;
import org.avni.server.domain.GroupDashboard;
import org.avni.server.domain.ValidationException;
import org.avni.server.framework.security.UserContextHolder;
import org.avni.server.web.contract.GroupDashboardBundleContract;
import org.avni.server.web.request.GroupDashboardContract;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupDashboardService implements NonScopeAwareService {
    private final GroupDashboardRepository groupDashboardRepository;
    private final DashboardRepository dashboardRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public GroupDashboardService(GroupDashboardRepository groupDashboardRepository, DashboardRepository dashboardRepository, GroupRepository groupRepository) {
        this.groupDashboardRepository = groupDashboardRepository;
        this.dashboardRepository = dashboardRepository;
        this.groupRepository = groupRepository;
    }

    public List<GroupDashboard> save(List<GroupDashboardContract> request) throws ValidationException {
        List<GroupDashboard> groupDashboards = new ArrayList<>();
        for (GroupDashboardContract contract : request) {
            GroupDashboard groupDashboard = EntityHelper.newOrExistingEntity(groupDashboardRepository, contract.getUuid(), contract.getId(), new GroupDashboard());
            Group group = groupRepository.findOne(contract.getGroupId());
            Dashboard dashboard = dashboardRepository.findOne(contract.getDashboardId());
            if (dashboard == null || group == null) {
                throw new ValidationException(String.format("Invalid dashboard id %d or group id %d", contract.getDashboardId(), contract.getGroupId()));
            }
            groupDashboard.setDashboard(dashboard);
            groupDashboard.setGroup(group);
            groupDashboard.setOrganisationId(UserContextHolder.getUserContext().getOrganisationId());
            groupDashboards.add(groupDashboard);
        }
        return groupDashboards;
    }

    public void saveFromBundle(List<GroupDashboardBundleContract> request) {
        List<GroupDashboard> groupDashboards = new ArrayList<>();
        for (GroupDashboardBundleContract contract : request) {
            GroupDashboard groupDashboard = EntityHelper.newOrExistingEntity(groupDashboardRepository, contract.getUuid(), null, new GroupDashboard());
            Group group = groupRepository.findByUuid(contract.getGroupUUID());
            Dashboard dashboard = dashboardRepository.findByUuid(contract.getDashboardUUID());
            groupDashboard.setDashboard(dashboard);
            groupDashboard.setGroup(group);
            groupDashboard.setOrganisationId(UserContextHolder.getUserContext().getOrganisationId());
            groupDashboards.add(groupDashboard);
        }
        groupDashboardRepository.saveAll(groupDashboards);
    }

    private GroupDashboard buildAndSave(GroupDashboardContract contract, GroupDashboard groupDashboard) {
        groupDashboard.setDashboard(dashboardRepository.findOne(contract.getDashboardId()));
        groupDashboard.setGroup(groupRepository.findOne(contract.getGroupId()));
        groupDashboard.setPrimaryDashboard(contract.isPrimaryDashboard());
        groupDashboard.setSecondaryDashboard(contract.isSecondaryDashboard());
        groupDashboard = groupDashboardRepository.save(groupDashboard);

        List<GroupDashboard> otherGroupDashboards = groupDashboardRepository.findByGroup_IdAndIdNotAndIsVoidedFalse(contract.getGroupId(), groupDashboard.getId());
        if (contract.isPrimaryDashboard()) {
            for (GroupDashboard nonPrimaryDashboard : otherGroupDashboards) {
                nonPrimaryDashboard.setPrimaryDashboard(false);
            }
        }
        if (contract.isSecondaryDashboard()) {
            for (GroupDashboard nonPrimaryDashboard : otherGroupDashboards) {
                nonPrimaryDashboard.setSecondaryDashboard(false);
            }
        }
        groupDashboardRepository.saveAll(otherGroupDashboards);
        return groupDashboard;
    }

    public GroupDashboard edit(GroupDashboardContract updates, Long id) {
        return buildAndSave(updates, groupDashboardRepository.findOne(id));
    }

    public void delete(GroupDashboard groupDashboard) {
        groupDashboard.setVoided(true);
        groupDashboardRepository.save(groupDashboard);
    }

    @Override
    public boolean isNonScopeEntityChanged(DateTime lastModifiedDateTime) {
        return groupDashboardRepository.existsByLastModifiedDateTimeGreaterThan(lastModifiedDateTime);
    }
}
