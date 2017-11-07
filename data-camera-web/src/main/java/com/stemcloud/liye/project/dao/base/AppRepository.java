package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.AppInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_base_app_info
 * @author liye on 2017/11/6
 */
public interface AppRepository extends CrudRepository<AppInfo, Long> {
    /**
     * find by creator and isDeleted order by create time
     * @param creator: creator
     * @param isDeleted: 0 or 1
     * @return apps
     */
    List<AppInfo> findByCreatorAndIsDeletedOrderByCreateTime(String creator, int isDeleted);

    /**
     * find app by id
     * @param id id
     * @return app
     */
    AppInfo findById(long id);

    /**
     * update app
     * @param id
     * @param name
     * @param description
     * @return recorder count
     */
    @Query(value = "UPDATE AppInfo a SET a.name = ?2, a.description = ?3 WHERE a.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer updateApp(long id, String name, String description);

    /**
     * delete app
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE AppInfo a SET a.isDeleted = 1 WHERE a.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteApp(long id);
}
