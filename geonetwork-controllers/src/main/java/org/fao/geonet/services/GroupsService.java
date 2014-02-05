package org.fao.geonet.services;

import org.fao.geonet.beans.GroupBean;
import org.fao.geonet.domain.Group;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.Updater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupsService {

    @Autowired
    GroupRepository groupRepository;

    @Transactional
    public void update(final GroupBean group) {
        groupRepository.update(Integer.valueOf(group.getId()), new Updater<Group>() {
            @Override
            public void apply(final Group entity) {
                entity.setEmail(group.getEmail())
                        .setName(group.getName())
                        .setDescription(group.getDescription());
            }
        });

        //TODO: Check, seem not getting transactional behaviour
        groupRepository.flush();
    }

}
