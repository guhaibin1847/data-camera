package com.stemcloud.liye.dc.controller;

import com.google.gson.Gson;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.common.ServerReturnTool;
import com.stemcloud.liye.dc.domain.config.SensorRegister;
import com.stemcloud.liye.dc.service.BaseInfoService;
import com.stemcloud.liye.dc.service.CommonService;
import com.stemcloud.liye.dc.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  增加(Create)、读取查询(Retrieve)、更新(Update)、删除(Delete)
 *  for app, experiment, track and sensor
 * @author liye on 2017/11/6
 */
@RestController
@RequestMapping("/crud")
public class CrudController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonService commonService;
    private final CrudService crudService;
    private final BaseInfoService baseInfoService;

    @Autowired
    public CrudController(CommonService commonService, CrudService crudService, BaseInfoService baseInfoService) {
        this.commonService = commonService;
        this.crudService = crudService;
        this.baseInfoService = baseInfoService;
    }

    /**
     * 新建场景
     * @param queryParams
     * @param request
     * @return
     */
    @PostMapping("/app/new")
    public Map newApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            AppInfo appInfo = new AppInfo();
            String user = commonService.getCurrentLoginUser(request);
            appInfo.setCreator(user);
            appInfo.setName(queryParams.get("app-name"));
            appInfo.setDescription(queryParams.get("app-desc"));
            AppInfo newApp = crudService.saveApp(appInfo);
            logger.info("User {} new app {}", user, newApp.getId());
            return ServerReturnTool.serverSuccess(newApp);
        } catch (Exception e){
            logger.error("[/app/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 编辑场景
     * @param queryParams
     * @param request
     * @return
     */
    @PostMapping("/app/update")
    public Map updateApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            AppInfo appInfo = crudService.findApp(Long.valueOf(queryParams.get("app-id")));
            String user = commonService.getCurrentLoginUser(request);
            if (!baseInfoService.isAppBelongUser(appInfo.getId(), user)){
                throw new Exception("App not belong user Exception");
            }
            appInfo.setCreator(user);
            appInfo.setName(queryParams.get("app-name"));
            appInfo.setDescription(queryParams.get("app-desc"));
            AppInfo newApp = crudService.saveApp(appInfo);
            logger.info("User {} update app {}", user, newApp.getId());
            return ServerReturnTool.serverSuccess(newApp);
        } catch (Exception e){
            logger.error("[/app/update]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除场景
     * @param queryParams
     * @param request
     * @return
     */
    @GetMapping("/app/delete")
    public Map deleteApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Long id = Long.parseLong(queryParams.get("app-id"));
            String user = commonService.getCurrentLoginUser(request);
            logger.info("User {} delete app {}", user, id);
            crudService.deleteApp(id);
            return ServerReturnTool.serverSuccess(id);
        } catch (Exception e){
            logger.error("[/app/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 新建传感器组
     * @param queryParams
     * @param sensors
     * @param request
     * @return
     */
    @PostMapping("/exp/new")
    public Map newExp(@RequestParam Map<String, String> queryParams, @RequestParam(value = "exp-select", required = false) List<String> sensors, HttpServletRequest request){
        try {
            ExperimentInfo expInfo = new ExperimentInfo();
            String user = commonService.getCurrentLoginUser(request);
            expInfo.setName(queryParams.get("exp-name"));
            expInfo.setDescription(queryParams.get("exp-desc"));
            expInfo.setApp(crudService.findApp(Long.valueOf(queryParams.get("app-id"))));
            ExperimentInfo newExpInfo = crudService.saveExp(expInfo);
            if (null != sensors && sensors.size() > 0){
                for (String s: sensors){
                    long sensorId = Long.parseLong(s);
                    crudService.newTrackAndBoundSensor(newExpInfo, crudService.findSensor(sensorId));
                }
            }
            logger.info("User {} new experiment {}", user, newExpInfo.getId());
            return ServerReturnTool.serverSuccess(newExpInfo);
        } catch (Exception e){
            logger.error("[/exp/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 编辑传感器组
     * @param queryParams
     * @param sensors
     * @param request
     * @return
     */
    @PostMapping("/exp/update")
    public Map updateExp(@RequestParam Map<String, String> queryParams, @RequestParam(value = "exp-select", required = false) List<String> sensors, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            ExperimentInfo expInfo = crudService.findExp(Long.valueOf(queryParams.get("exp-id")));
            expInfo.setName(queryParams.get("exp-name"));
            expInfo.setDescription(queryParams.get("exp-desc"));
            ExperimentInfo newExpInfo = crudService.saveExp(expInfo);
            // add sensor on experiment
            if (null != sensors && sensors.size() > 0){
                for (String s: sensors){
                    long sensorId = Long.parseLong(s);
                    crudService.newTrackAndBoundSensor(newExpInfo, crudService.findSensor(sensorId));
                }
            }
            logger.info("User {} update experiment {}", user, newExpInfo.getId());
            return ServerReturnTool.serverSuccess(newExpInfo);
        }catch (Exception e){
            logger.error("[/exp/update]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除传感器组
     * @param queryParams
     * @param request
     * @return
     */
    @GetMapping("/exp/delete")
    public Map deleteExp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Long id = Long.parseLong(queryParams.get("exp-id"));
            String user = commonService.getCurrentLoginUser(request);
            logger.info("User {} delete experiment {}", user, id);
            crudService.deleteExp(id);
            return ServerReturnTool.serverSuccess(id);
        } catch (Exception e){
            logger.error("[/exp/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除轨迹
     * @param ids
     * @param request
     * @return
     */
    @GetMapping("/track/delete")
    public Map deleteTrack(@RequestParam List<String> ids, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            logger.info("User {} delete track {}", user, new Gson().toJson(ids));
            for (String id : ids){
                crudService.deleteTrack(Long.parseLong(id));
            }
            return ServerReturnTool.serverSuccess(ids.size());
        } catch (Exception e){
            logger.error("[/track/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 绑定和解绑传感器
     * @param queryParams
     * @return
     */
    @PostMapping("/bound/toggle")
    public Map boundToggle(@RequestParam Map<String, String> queryParams){
        try {
            Map<String, String> result = new HashMap<String, String>();
            long trackId = Long.parseLong(queryParams.get("pk"));
            String dom = queryParams.get("name");
            if (queryParams.get("value").isEmpty()){
                // unbound
                TrackInfo track = crudService.findTrack(trackId);
                long sensorId = track.getSensor().getId();
                crudService.unboundSensor(sensorId, trackId);

                result.put("action", "unbound");
                result.put("sensor", String.valueOf(sensorId));
            } else {
                // bound
                long sensorId = Long.parseLong(queryParams.get("value"));
                crudService.boundSensor(sensorId, trackId);

                result.put("action", "bound");
                result.put("sensor", String.valueOf(sensorId));
            }
            result.put("dom", dom);
            return ServerReturnTool.serverSuccess(result);
        } catch (Exception e){
            logger.error("[/bound/toggle]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 新增设备
     * @param queryParams
     * @param request
     * @return
     */
    @PostMapping("/sensor/new")
    public Map newSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            SensorInfo sensor = new SensorInfo();
            String user = commonService.getCurrentLoginUser(request);

            // 查看注册表是否已注册该设备
            SensorRegister sensorRegister = crudService.findRegister(queryParams.get("sensor-code"));
            if (sensorRegister == null){
                return ServerReturnTool.serverFailure("传感器编号错误，系统未能识别该编号");
            } else if (sensorRegister.getIsRegistered() == 1){
                return ServerReturnTool.serverFailure("该编号已被其他用户绑定，无法新增");
            }
            sensor.setCode(queryParams.get("sensor-code"));
            sensor.setSensorConfig(sensorRegister.getSensorConfig());
            String img = queryParams.get("device-img");
            if (!img.trim().isEmpty()){
                sensor.setImg(img);
            }
            crudService.registerSensor(1, queryParams.get("sensor-code"));

            sensor.setCreator(user);
            sensor.setName(queryParams.get("sensor-name"));
            sensor.setDescription(queryParams.get("sensor-desc"));
            SensorInfo newSensor = crudService.saveSensor(sensor);
            logger.info("User {} new sensor {}", user, newSensor.getId());
            return ServerReturnTool.serverSuccess(newSensor);
        } catch (Exception e){
            logger.error("[/sensor/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 更新设备信息
     * @param queryParams
     * @param request
     * @return
     */
    @PostMapping("/sensor/update")
    public Map updateSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long id = Long.parseLong(queryParams.get("sensor-id"));
            SensorInfo sensor = crudService.findSensor(id);
            if (sensor == null || !sensor.getCreator().equals(user)){
                return ServerReturnTool.serverFailure("参数错误");
            }
            String img = queryParams.get("device-img");
            if (!img.trim().isEmpty()){
                sensor.setImg(img);
            }
            sensor.setCreator(user);
            sensor.setName(queryParams.get("sensor-name"));
            sensor.setDescription(queryParams.get("sensor-desc"));
            SensorInfo newSensor = crudService.saveSensor(sensor);
            logger.info("User {} update sensor {}", user, newSensor.getId());
            return ServerReturnTool.serverSuccess(newSensor);
        } catch (Exception e){
            logger.error("[/sensor/update]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除设备
     * @param queryParams
     * @param request
     * @return
     */
    @GetMapping("/sensor/delete")
    public Map deleteSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long sensorId = Long.parseLong(queryParams.get("sensor-id"));
            crudService.deleteSensor(sensorId, queryParams.get("sensor-code"));
            logger.info("User {} delete sensor {}", user, sensorId);
            return ServerReturnTool.serverSuccess(sensorId);
        } catch (Exception e){
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 修改数据片段名称
     * @param queryParams
     * @return
     */
    @PostMapping("/recorder/name")
    public Map modifySegmentName(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            long recorderId = Long.parseLong(queryParams.get("pk"));
            String name = queryParams.get("value");
            crudService.updateRecorderName(recorderId, name);
            logger.info("User {} update recorder {}'s name to {}", user, recorderId, name);
            return ServerReturnTool.serverSuccess(name);
        } catch (Exception e){
            logger.error("[/recorder/name]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 修改数据片段描述
     * @param queryParams
     * @return
     */
    @GetMapping("/recorder/desc")
    public Map modifySegmentDesc(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            long recorderId = Long.parseLong(queryParams.get("id"));
            String title = queryParams.get("title");
            String desc = queryParams.get("desc");
            crudService.updateRecorderDescription(recorderId, title, desc);
            logger.info("User {} update recorder {}'s name to {}", user, recorderId, desc);
            return ServerReturnTool.serverSuccess(desc);
        } catch (Exception e){
            logger.error("[/recorder/desc]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除数据片段
     * @param queryParams 参数
     * @param request http请求
     * @return
     */
    @GetMapping("/recorder/delete")
    public Map deleteRecorder(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            crudService.deleteAllRecorder(recorderId);
            logger.info("User {} delete recorder {}", user, recorderId);
            return ServerReturnTool.serverSuccess(Long.parseLong(queryParams.get("recorder-id")));
        } catch (Exception e){
            logger.error("[/recorder/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 发布内容
     * @param queryParams
     * @param request
     * @return
     */
    @PostMapping("/content/new")
    public Map publishContent(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            String name = queryParams.get("content-name");
            String desc = queryParams.get("content-desc");
            String category = queryParams.get("content-category-select");
            String tag = queryParams.get("tags");
            String img = queryParams.get("share-img");
            int isShared = Integer.parseInt(queryParams.get("content-private-select"));
            return ServerReturnTool.serverSuccess(crudService.saveContent(user, name, desc, category, tag, isShared, recorderId, img));
        } catch (Exception e){
            logger.error("[/content/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除内容
     * @param queryParams
     * @param request
     * @return
     */
    @GetMapping("/content/delete")
    public Map deleteContent(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Long id = Long.parseLong(queryParams.get("content-id"));
            String user = commonService.getCurrentLoginUser(request);
            logger.info("User {} delete content {}", user, id);
            crudService.deleteContent(id);
            return ServerReturnTool.serverSuccess(id);
        } catch (Exception e){
            logger.error("[/content/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}
