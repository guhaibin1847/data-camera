package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  service for base domain
 * @author liye on 2017/11/6
 */
@Service
public class BaseInfoService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AppRepository appRepository;
    private final SensorRepository sensorRepository;
    private final ExperimentRepository experimentRepository;
    private final TrackRepository trackRepository;

    @Autowired
    public BaseInfoService(AppRepository appRepository, SensorRepository sensorRepository, ExperimentRepository experimentRepository, TrackRepository trackRepository) {
        this.appRepository = appRepository;
        this.sensorRepository = sensorRepository;
        this.experimentRepository = experimentRepository;
        this.trackRepository = trackRepository;
    }

    /** APPS **/
    public Map<Long, AppInfo> getOnlineApps(String user){
        List<AppInfo> apps = appRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
        Map<Long, AppInfo> map = new HashMap<Long, AppInfo>(apps.size());
        for (AppInfo app: apps){
            map.put(app.getId(), app);
        }
        return map;
    }

    public Map<Long, ExperimentInfo> getOnlineExpOfApp(long id){
        AppInfo app = appRepository.findById(id);
        List<ExperimentInfo> experiments = experimentRepository.findByAppAndIsDeleted(app, 0);
        Map<Long, ExperimentInfo> map = new HashMap<Long, ExperimentInfo>(experiments.size());
        for (ExperimentInfo exp : experiments){
            map.put(exp.getId(), exp);
        }
        return map;
    }

    public Boolean isAppBelongUser(long id, String user) {
        AppInfo app = appRepository.findById(id);
        if (app == null){
            logger.warn("null app " + id);
            return false;
        }
        logger.info("compare user " + user + " with app creator " + app.getCreator());
        return app.getIsDeleted() == 0 && user.equals(app.getCreator());
    }

    public AppInfo getCurrentApp(long id){
        return appRepository.findById(id);
    }

    /** SENSORS **/
    public List<SensorInfo> getOnlineSensor(String user){
        return sensorRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
    }

    public List<ExperimentInfo> getOnlineExp(){
        return experimentRepository.findByIsDeletedOrderByCreateTime(0);
    }

    public List<TrackInfo> getOnlineTrack(){
        return trackRepository.findByIsDeletedOrderByCreateTime(0);
    }

    public List<SensorInfo> getAvailableSensorOfCurrentUser(String user){
        return sensorRepository.findByCreatorAndAppIdAndExpIdAndTrackId(user, 0, 0, 0);
    }

    public List<SensorInfo> getSensorsOfCurrentApp(long appId){
        return sensorRepository.findByAppId(appId);
    }
}
